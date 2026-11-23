package com.example.interviewsimulator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        DBHelper db = new DBHelper(this);
        EditText email = findViewById(R.id.etEmail), pass = findViewById(R.id.etPass);
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            if (db.login(e, pass.getText().toString())) {
                startActivity(new Intent(this, HomeActivity.class).putExtra("email", e));
                finish();
            } else Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.tvSignup).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}
