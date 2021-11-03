package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends BaseActivity {
    TextView back;
    EditText email_field;
    Button reset_btn;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        back = findViewById(R.id.back_link);
        email_field = findViewById(R.id.user_email);
        reset_btn = findViewById(R.id.reset);
        auth = FirebaseAuth.getInstance();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(MainActivity.class);
            }
        });
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            email_field.setText(bundle.getString("email"));
        }
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = email_field.getText().toString().trim();
        if(TextUtil.isEmpty(email)){
            email_field.setError("Email is required.");
        }
        if(!TextUtil.isEmail(email)){
            email_field.setError("Please input the correct email address.");
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("Please check the email to reset your password.");
                    navigate(MainActivity.class);
                }else{
                    showToast("Try again!");
                }
            }
        });
    }
}