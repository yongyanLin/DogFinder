package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.lostPopUpUtil;
import com.example.dogfinder.Utils.strayPopUpUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends BaseActivity {

    LinearLayout nav_account,nav_stray,nav_lost,nav_like,nav_comment,nav_logout;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    ImageView profile_photo;
    TextView email_field,account_field;
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        account_field = findViewById(R.id.account);
        email_field = findViewById(R.id.email);
        profile_photo = findViewById(R.id.profile_photo);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("users").document(auth.getCurrentUser().getUid());

        nav_account = findViewById(R.id.nav_account);
        nav_stray = findViewById(R.id.nav_stray);
        nav_lost = findViewById(R.id.nav_lost);
        nav_like = findViewById(R.id.nav_like);
        nav_comment = findViewById(R.id.nav_comment);
        nav_logout = findViewById(R.id.logout);

        nav_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(AccountActivity.class);
                finish();
            }
        });

        nav_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                navigate(MainActivity.class);
                finish();
            }
        });
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.profile_btn);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_btn:
                        navigate(IndexActivity.class);
                        finish();
                        break;
                    case R.id.likes_btn:
                        navigate(CollectionsActivity.class);
                        finish();
                        break;
                    case R.id.stray_btn:
                        navigate(StraySquareActivity.class);
                        finish();
                        break;
                    case R.id.lost_btn:
                        navigate(LostSquareActivity.class);
                        finish();
                        break;
                    case R.id.profile_btn:
                        navigate(ProfileActivity.class);
                        finish();
                        break;
                }
                return false;
            }
        });

    }
    @Override
    protected void onStart(){
        super.onStart();
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    String username = task.getResult().getString("username");
                    String email = task.getResult().getString("email");
                    String imageUri = task.getResult().getString("image");
                    account_field.setText(username);
                    email_field.setText(email);
                    if(imageUri == null){
                        profile_photo.setImageResource(R.mipmap.profile_light);
                    }else{
                        Picasso.with(getApplicationContext()).load(imageUri).into(profile_photo);
                    }
                }else {
                    showToast("No profile exists.");
                    auth.signOut();
                    navigate(MainActivity.class);
                }
            }
        });
    }
}