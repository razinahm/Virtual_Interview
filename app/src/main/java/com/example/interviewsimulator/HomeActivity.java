package com.example.interviewsimulator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    private String email; private ListView lv;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);
        email = getIntent().getStringExtra("email");
        lv = findViewById(R.id.lvHistory);
        ((TextView) findViewById(R.id.tvWelcome)).setText("Welcome, " + email);
        findViewById(R.id.btnNew).setOnClickListener(v ->
            startActivity(new Intent(this, QualificationActivity.class).putExtra("email", email)));
    }

    @Override protected void onResume() {
        super.onResume();
        ArrayList<String> rows = new ArrayList<>();
        Cursor c = new DBHelper(this).getReadableDatabase().rawQuery(
            "SELECT topic, correct, total, date FROM results WHERE email=? ORDER BY id DESC LIMIT 10", new String[]{email});
        while (c.moveToNext()) rows.add(c.getString(0) + "\n" + c.getInt(1) + "/" + c.getInt(2) + " correct  •  " + c.getString(3));
        c.close();
        if (rows.isEmpty()) rows.add("No interviews yet. Tap Start New Interview!");
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows));
    }
}
