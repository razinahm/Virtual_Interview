package com.example.interviewsimulator;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_signup);
        DBHelper db = new DBHelper(this);
        EditText name = findViewById(R.id.etName), email = findViewById(R.id.etEmail), pass = findViewById(R.id.etPass);
        findViewById(R.id.btnSignup).setOnClickListener(v -> {
            String n = name.getText().toString().trim(), e = email.getText().toString().trim(), p = pass.getText().toString();
            if (n.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches() || p.length() < 6) {
                Toast.makeText(this, "Enter name, valid email, password (6+ chars)", Toast.LENGTH_SHORT).show(); return;
            }
            if (db.register(n, e, p)) { Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show(); finish(); }
            else Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
        });
    }
}
