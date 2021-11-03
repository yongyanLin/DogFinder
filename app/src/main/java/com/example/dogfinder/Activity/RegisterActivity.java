package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;

import com.example.dogfinder.Entity.User;
import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {
    Button register;
    EditText username,email,password,confirm_password;
    ImageView back;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        register = findViewById(R.id.register);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(MainActivity.class);
            }
        });
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
       // if(auth.getCurrentUser() != null){
            //go to index directly
       //     finish();
      //  }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().trim();
                String email_string = email.getText().toString().trim();
                String password_string = password.getText().toString().trim();
                String password_string_confirm = confirm_password.getText().toString().trim();
                if(TextUtil.isEmpty(user)){
                    username.setError("Username can't be empty!");
                    return;
                }
                if(TextUtil.isEmpty(password_string)){
                    password.setError("Password can't be empty!");
                    return;
                }
                if(password_string.length() < 6){
                    password.setError("Password should at least have 6 characteristics!");
                    return;
                }
                if(!password_string.equals(password_string_confirm)){
                    confirm_password.setError("Please confirm your password again!");
                    return;
                }
                if(!TextUtil.isEmail(email_string)){
                    email.setError("Please input correct email address!");
                    return;
                }
                auth.createUserWithEmailAndPassword(email_string,password_string).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            userID = auth.getCurrentUser().getUid();
                            DocumentReference reference = firebaseFirestore.collection("users").document(userID);
                            User user1 = new User(user,email_string,password_string,null);

                            reference.set(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    showToast("Register successfully,please now verify your email.");
                                }
                            });
                            Intent intent = new Intent(RegisterActivity.this,VerifyActivity.class);
                            intent.putExtra("email",email_string);
                            startActivity(intent);
                        }else{
                            showToast("Information is incorrect!");
                            navigate(RegisterActivity.class);
                        }
                    }
                });
            }
        });

    }

}