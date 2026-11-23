package com.example.interviewsimulator;

import java.util.*;

public class QuestionBank {
    public static class Q {
        public final String text; public final String[] keywords;
        Q(String t, String... k) { text = t; keywords = k; }
    }
    public static final String[] TOPICS = {"Java Developer", "Android Developer", "HR / General"};

    /** Offline fallback. Replace/extend with AI-generated questions (see AiQuestionService note below). */
    public static List<Q> get(String topic) {
        List<Q> l = new ArrayList<>();
        switch (topic) {
            case "Java Developer":
                l.add(new Q("What is the difference between an interface and an abstract class?", "interface", "abstract", "multiple", "implement"));
                l.add(new Q("Explain how garbage collection works in Java.", "heap", "unreachable", "memory", "gc"));
                l.add(new Q("What is the difference between ArrayList and LinkedList?", "array", "linked", "index", "insertion"));
                break;
            case "Android Developer":
                l.add(new Q("Explain the Android Activity lifecycle.", "oncreate", "onstart", "onresume", "onpause", "ondestroy"));
                l.add(new Q("What is the difference between a Service and a Thread?", "background", "service", "thread", "ui"));
                l.add(new Q("What is SQLite and how is it used in Android?", "database", "sqlite", "local", "query"));
                break;
            default:
                l.add(new Q("Tell me about yourself.", "experience", "skills", "education", "project"));
                l.add(new Q("What are your strengths and weaknesses?", "strength", "weakness", "improve", "learn"));
                l.add(new Q("Why should we hire you?", "skill", "team", "value", "company"));
        }
        return l;
    }

    /** Simple scoring: answer is "correct" if it hits at least half of the expected keywords. */
    public static boolean isCorrect(Q q, String answer) {
        String a = answer.toLowerCase(); int hit = 0;
        for (String k : q.keywords) if (a.contains(k)) hit++;
        return hit >= Math.max(1, q.keywords.length / 2);
    }

    /*
     * AI + bdjobs integration (do this on YOUR backend, not inside the app):
     *  1. Your server fetches job titles/skills for a topic (bdjobs has no public documented API --
     *     check with bdjobs for partner access, otherwise use a licensed dataset).
     *  2. Server sends them to an LLM API (e.g. Claude) with the prompt:
     *     "Generate 5 interview questions + expected keywords for <job title>, return JSON."
     *  3. App calls your server with HttpURLConnection/Retrofit, parses JSON into List<Q>.
     *  Never put the LLM API key in the APK -- it can be extracted.
     */
}
