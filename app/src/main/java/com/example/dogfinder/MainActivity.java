package com.example.dogfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dogfinder.Activity.BaseActivity;
import com.example.dogfinder.Activity.IndexActivity;
import com.example.dogfinder.Activity.RegisterActivity;
import com.example.dogfinder.Activity.ResetPasswordActivity;
import com.example.dogfinder.Utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity {
    Button login_btn;
    TextView register_link,reset_link;
    EditText email,password;
    FirebaseAuth auth;
    DocumentReference documentReference;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login_btn = findViewById(R.id.login);
        register_link = findViewById(R.id.register_link);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(auth.getCurrentUser() != null && user.isEmailVerified()){
            navigate(IndexActivity.class);
        }

        //from Verification page
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String userEmail = extras.getString("email");
            email.setText(userEmail);
        }

        register_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(RegisterActivity.class);
            }
        });
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_string = email.getText().toString().trim();
                String password_string = password.getText().toString().trim();
                if (TextUtil.isEmpty(email_string)){
                    email.setError("Email can't be empty!");
                }
                if(TextUtil.isEmpty(password_string)){
                    password.setError("Password can't be empty!");
                }
                auth.signInWithEmailAndPassword(email_string,password_string).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            documentReference = firebaseFirestore.collection("users").document(auth.getCurrentUser().getUid());
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.getResult().exists()) {
                                        String password1 = task.getResult().getString("password");
                                        if (password1 != password_string) {
                                            firebaseFirestore.runTransaction(new Transaction.Function<Object>() {
                                                @Nullable
                                                @Override
                                                public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                                    DocumentSnapshot snapshot = transaction.get(documentReference);
                                                    transaction.update(documentReference, "password", password_string);
                                                    return null;
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            if(firebaseUser.isEmailVerified()){
                                navigate(IndexActivity.class);
                            }else{
                                firebaseUser.sendEmailVerification();
                                showToast("Please verify your email.");
                                auth.signOut();
                            }
                            //go to index
                        }else{
                            showToast("Information is incorrect!");
                            email.setText("");
                            password.setText("");
                            navigate(MainActivity.class);
                        }
                    }
                });
            }
        });
        reset_link = findViewById(R.id.reset_link);
        reset_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ResetPasswordActivity.class);
                if(TextUtil.isEmpty(email.getText().toString().trim())){
                    navigate(ResetPasswordActivity.class);
                }else{
                    intent.putExtra("email",email.getText().toString().trim());
                    startActivity(intent);
                }
            }
        });
    }
}