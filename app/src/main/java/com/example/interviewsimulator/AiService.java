package com.example.interviewsimulator;

import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class AiService {
    // TESTING ONLY: a key inside the APK can be extracted. For release, call your own server instead.
    static final String API_KEY = "PASTE_YOUR_ANTHROPIC_KEY_HERE";

    public interface Callback { void done(List<QuestionBank.Q> list, String error); }
    public interface GradeCallback { void done(int score, String feedback, String error); }

    private static String call(String prompt, int maxTokens) throws Exception {
        JSONObject body = new JSONObject().put("model", "claude-sonnet-5-5").put("max_tokens", maxTokens)
            .put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)));
        HttpURLConnection c = (HttpURLConnection) new URL("https://api.anthropic.com/v1/messages").openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(15000); c.setReadTimeout(120000);
        c.setRequestProperty("x-api-key", API_KEY);
        c.setRequestProperty("anthropic-version", "2023-06-01");
        c.setRequestProperty("content-type", "application/json");
        c.setDoOutput(true);
        c.getOutputStream().write(body.toString().getBytes("UTF-8"));
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(code < 400 ? c.getInputStream() : c.getErrorStream()));
        StringBuilder sb = new StringBuilder(); String line;
        while ((line = r.readLine()) != null) sb.append(line);
        if (code >= 400) throw new Exception("API error " + code);
        return new JSONObject(sb.toString()).getJSONArray("content").getJSONObject(0).getString("text")
            .replaceAll("```json|```", "").trim();
    }

    /** Generates 'count' questions: easy, medium and complex, on different topics for the qualification. */
    public static void generate(String qual, String sector, int count, Callback cb) {
        Handler h = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                int basic = count / 4, adv = count / 3, mid = count - basic - adv;
                String prompt = "You are a senior job interviewer in Bangladesh. Candidate qualification: " + qual +
                    ". Job sector: " + sector + ". Write exactly " + count + " interview questions: " +
                    basic + " basic (introduction, motivation, general), " + mid + " intermediate (sector knowledge), " +
                    adv + " advanced (complex scenarios, case studies, problem solving). " +
                    "Cover many different topics of the sector and never repeat a topic. " +
                    "Match difficulty to the qualification: SSC/HSC = practical and general, Diploma = hands-on technical, " +
                    "Bachelor's/Master's = deeper theory and real-world decisions. " +
                    "Every question must need a detailed spoken answer of 3-5 sentences, never yes/no. " +
                    "Reply ONLY with a JSON array of strings, no markdown: [\"question 1\", \"question 2\"]";
                JSONArray arr = new JSONArray(call(prompt, 3500));
                List<QuestionBank.Q> out = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) out.add(new QuestionBank.Q(arr.getString(i)));
                h.post(() -> cb.done(out, null));
            } catch (Exception e) { h.post(() -> cb.done(null, e.getMessage())); }
        }).start();
    }

    /** AI grades one answer from 0 to 10. */
    public static void grade(String qual, String sector, String question, String answer, GradeCallback cb) {
        Handler h = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                String prompt = "You are a fair job interviewer. Sector: " + sector + ". Candidate qualification: " + qual +
                    ".\nQuestion: " + question + "\nCandidate's spoken answer (speech-to-text, ignore small recognition errors): " + answer +
                    "\nGrade the answer from 0 to 10 for correctness, depth and clarity. Very short or off-topic answers get low scores. " +
                    "Reply ONLY with JSON: {\"score\":7,\"feedback\":\"one short sentence\"}";
                String t = call(prompt, 200);
                JSONObject o = new JSONObject(t.substring(t.indexOf('{'), t.lastIndexOf('}') + 1));
                int s = o.getInt("score"); String f = o.optString("feedback", "");
                h.post(() -> cb.done(s, f, null));
            } catch (Exception e) { h.post(() -> cb.done(0, "", e.getMessage())); }
        }).start();
    }
}
