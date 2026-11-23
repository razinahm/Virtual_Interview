package com.example.interviewsimulator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SectorActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_choice);
        ((TextView) findViewById(R.id.tvTitle)).setText("Select job sector");
        String[] items = {"IT / Software", "Banking & Finance", "Marketing & Sales", "Teaching",
            "Engineering", "Healthcare", "HR / Admin", "Garments / Merchandising"};
        ListView lv = findViewById(R.id.lv);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        lv.setOnItemClickListener((p, v, i, id) -> {
            String[] labels = {"10 questions (short)", "15 questions (medium)", "20 questions (full)"};
            int[] counts = {10, 15, 20};
            new AlertDialog.Builder(this).setTitle("How many questions?")
                .setItems(labels, (d, which) -> startActivity(new Intent(this, InterviewActivity.class)
                    .putExtra("email", getIntent().getStringExtra("email"))
                    .putExtra("qualification", getIntent().getStringExtra("qualification"))
                    .putExtra("sector", items[i])
                    .putExtra("count", counts[which])))
                .show();
        });
    }
}
