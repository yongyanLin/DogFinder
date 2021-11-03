package com.example.dogfinder.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyActivity extends BaseActivity {
    TextView email,back;
    Button verify_btn;
    String emaiL_string;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.user_email);
        back = findViewById(R.id.back_link);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                navigate(RegisterActivity.class);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            emaiL_string = extras.getString("email");
            email.setText(emaiL_string);
        }
        FirebaseUser user = auth.getCurrentUser();
        verify_btn = findViewById(R.id.verify);
        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showToast("Verification email has been sent.");
                        Intent intent = new Intent(VerifyActivity.this,MainActivity.class);
                        intent.putExtra("email",emaiL_string);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}