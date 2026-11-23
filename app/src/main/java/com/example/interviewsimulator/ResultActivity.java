package com.example.interviewsimulator;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_result);
        int c = getIntent().getIntExtra("correct", 0), t = getIntent().getIntExtra("total", 1);
        int h = getIntent().getIntExtra("happy", 0), n = getIntent().getIntExtra("neutral", 0), s = getIntent().getIntExtra("nervous", 0);
        int sum = Math.max(1, h + n + s);
        ((TextView) findViewById(R.id.tvScore)).setText("Correct answers: " + c + " / " + t);
        ((TextView) findViewById(R.id.tvReaction)).setText(
                "Confident: " + h * 100 / sum + "%\nCalm: " + n * 100 / sum + "%\nTense / Nervous: " + s * 100 / sum + "%");
        String m = s * 100 / sum > 40 ? "You looked tense. Practice slow breathing and keep eye contact."
                : c >= t / 2 ? "Good job! Keep practicing." : "Review the topic and try again.";
        ((TextView) findViewById(R.id.tvAdvice)).setText(m);
    }
}
