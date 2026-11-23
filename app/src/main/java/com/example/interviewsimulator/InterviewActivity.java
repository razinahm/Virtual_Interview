package com.example.interviewsimulator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;
import java.util.*;
import java.util.concurrent.*;

public class InterviewActivity extends AppCompatActivity {
    private PreviewView preview; private TextView tvQ, tvEmotion, tvAnswer, tvStatus; private Button btnMic, btnNext;
    private TextToSpeech tts; private FaceDetector detector; private ExecutorService exec; private SpeechRecognizer sr;
    private List<QuestionBank.Q> questions = new ArrayList<>();
    private int index = 0, correct = 0, graded = 0, happy = 0, neutral = 0, nervous = 0;
    private String email, topic, qual, sector, finalText = "";
    private boolean listening = false, finishRequested = false, finished = false;
    private Intent recIntent;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_interview);
        email = getIntent().getStringExtra("email");
        qual = getIntent().getStringExtra("qualification");
        sector = getIntent().getStringExtra("sector");
        topic = sector + " (" + qual + ")";
        preview = findViewById(R.id.preview); tvQ = findViewById(R.id.tvQuestion); tvEmotion = findViewById(R.id.tvEmotion);
        tvAnswer = findViewById(R.id.tvAnswer); tvStatus = findViewById(R.id.tvStatus);
        btnMic = findViewById(R.id.btnMic); btnNext = findViewById(R.id.btnNext);
        btnNext.setEnabled(false); btnMic.setEnabled(false);

        tts = new TextToSpeech(this, s -> { if (s == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US); });
        detector = FaceDetection.getClient(new FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build());
        exec = Executors.newSingleThreadExecutor();
        setupSpeech();
        btnMic.setOnClickListener(v -> toggleMic());
        btnNext.setOnClickListener(v -> next());

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (ContextCompat.checkSelfPermission(this, perms[0]) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, perms[1]) == PackageManager.PERMISSION_GRANTED) init();
        else ActivityCompat.requestPermissions(this, perms, 1);
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] r) {
        super.onRequestPermissionsResult(rc, p, r);
        boolean ok = r.length == 2 && r[0] == PackageManager.PERMISSION_GRANTED && r[1] == PackageManager.PERMISSION_GRANTED;
        if (ok) init(); else { Toast.makeText(this, "Camera and microphone permissions are required", Toast.LENGTH_LONG).show(); finish(); }
    }

    private void init() {
        startCamera();
        int count = getIntent().getIntExtra("count", 15);
        AiService.generate(qual, sector, count, (list, err) -> {
            if (list == null || list.isEmpty()) {
                Toast.makeText(this, "AI unavailable (" + err + "). Using offline questions.", Toast.LENGTH_LONG).show();
                list = QuestionBank.get("HR / General");
            }
            questions = list;
            findViewById(R.id.startBox).setVisibility(android.view.View.GONE);
            btnNext.setEnabled(true); btnMic.setEnabled(true);
            ask();
        });
    }

    private void ask() {
        String q = questions.get(index).text;
        tvQ.setText((index + 1) + "/" + questions.size() + "  " + q);
        finalText = "";
        tvAnswer.setText("Tap the mic and answer in detail (3-5 sentences)");
        tts.speak(q, TextToSpeech.QUEUE_FLUSH, null, "q" + index);
        btnNext.setText(index == questions.size() - 1 ? "Finish" : "Next");
    }

    private void next() {
        listening = false; sr.cancel(); btnMic.setText("🎤 Speak");
        String a = finalText.trim(); String q = questions.get(index).text;
        final int words = a.isEmpty() ? 0 : a.split("\\s+").length;
        if (words < 3) onGraded(0);
        else AiService.grade(qual, sector, q, a, (score, fb, err) -> onGraded(err == null ? score : (words >= 15 ? 5 : 0)));
        index++;
        if (index < questions.size()) ask();
        else {
            finishRequested = true; btnNext.setEnabled(false); btnMic.setEnabled(false);
            findViewById(R.id.startBox).setVisibility(android.view.View.VISIBLE);
            tvStatus.setText("AI is evaluating your answers...");
            tryFinish();
        }
    }

    private void onGraded(int score) { graded++; if (score >= 6) correct++; tryFinish(); }
    private void tryFinish() { if (finishRequested && !finished && graded == questions.size()) finishInterview(); }

    private void setupSpeech() {
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle p) { }
            public void onBeginningOfSpeech() { }
            public void onRmsChanged(float v) { }
            public void onBufferReceived(byte[] b) { }
            public void onEvent(int t, Bundle p) { }
            public void onEndOfSpeech() { }
            public void onError(int e) {
                if (listening && (e == 6 || e == 7 || e == 8)) restart();
                else { listening = false; btnMic.setText("🎤 Speak"); }
            }
            public void onResults(Bundle r) {
                String t = best(r);
                if (!t.isEmpty()) finalText = (finalText + " " + t).trim();
                tvAnswer.setText(finalText);
                if (listening) restart();
            }
            public void onPartialResults(Bundle r) {
                String t = best(r);
                if (!t.isEmpty()) tvAnswer.setText((finalText + " " + t).trim());
            }
            private String best(Bundle r) {
                ArrayList<String> l = r.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                return (l == null || l.isEmpty()) ? "" : l.get(0);
            }
            private void restart() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> { if (listening) sr.startListening(recIntent); }, 300);
            }
        });
        recIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        recIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000);
    }

    private void toggleMic() {
        if (listening) { listening = false; sr.stopListening(); btnMic.setText("🎤 Speak"); return; }
        tts.stop(); listening = true; btnMic.setText("⏹ Stop");
        sr.startListening(recIntent);
    }

    private void startCamera() {
        ListenableFutureHelper.run(this, provider -> {
            Preview p = new Preview.Builder().build(); p.setSurfaceProvider(preview.getSurfaceProvider());
            ImageAnalysis ia = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
            ia.setAnalyzer(exec, this::analyze);
            provider.unbindAll();
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, p, ia);
        });
    }

    @androidx.annotation.OptIn(markerClass = ExperimentalGetImage.class)
    private void analyze(ImageProxy proxy) {
        if (proxy.getImage() == null || questions.isEmpty() || finishRequested) { proxy.close(); return; }
        InputImage img = InputImage.fromMediaImage(proxy.getImage(), proxy.getImageInfo().getRotationDegrees());
        detector.process(img).addOnSuccessListener(faces -> {
            if (faces.isEmpty()) return;
            Face f = faces.get(0);
            float smile = f.getSmilingProbability() == null ? 0 : f.getSmilingProbability();
            float eye = f.getLeftEyeOpenProbability() == null ? 1 : f.getLeftEyeOpenProbability();
            boolean away = Math.abs(f.getHeadEulerAngleY()) > 25 || Math.abs(f.getHeadEulerAngleX()) > 20;
            if (smile > 0.6f) { happy++; tvEmotion.setText("Confident 😊"); }
            else if (smile < 0.1f && (eye < 0.4f || away)) { nervous++; tvEmotion.setText("Tense / Nervous 😟"); }
            else { neutral++; tvEmotion.setText("Calm 😐"); }
        }).addOnCompleteListener(t -> proxy.close());
    }

    private void finishInterview() {
        finished = true;
        new DBHelper(this).saveResult(email, topic, correct, questions.size(), happy, neutral, nervous);
        startActivity(new Intent(this, ResultActivity.class)
            .putExtra("correct", correct).putExtra("total", questions.size())
            .putExtra("happy", happy).putExtra("neutral", neutral).putExtra("nervous", nervous));
        finish();
    }

    @Override protected void onDestroy() { super.onDestroy(); tts.shutdown(); exec.shutdown(); detector.close(); sr.destroy(); }
}
