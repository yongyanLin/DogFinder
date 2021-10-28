package com.example.dogfinder.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dogfinder.R;

public class VerifyActivity extends BaseActivity {
    TextView email,back;
    Button verify_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        email = findViewById(R.id.user_email);
        back = findViewById(R.id.back_link);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(RegisterActivity.class);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String emaiL_string = extras.getString("email");
            email.setText(emaiL_string);
        }
        verify_btn = findViewById(R.id.verify);
        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}