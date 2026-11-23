package com.example.interviewsimulator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class QualificationActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_choice);
        ((TextView) findViewById(R.id.tvTitle)).setText("Select your qualification");
        String[] items = {"SSC", "HSC", "Diploma", "Bachelor's", "Master's"};
        ListView lv = findViewById(R.id.lv);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        lv.setOnItemClickListener((p, v, i, id) -> startActivity(new Intent(this, SectorActivity.class)
            .putExtra("email", getIntent().getStringExtra("email")).putExtra("qualification", items[i])));
    }
}
