package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity {

    LinearLayout nav_account,nav_post,nav_like,nav_comment,nav_logout;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    CircleImageView profile_photo;
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
        nav_post = findViewById(R.id.nav_post);
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
        nav_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(CollectionListActivity.class);
                finish();
            }
        });
        nav_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(PostListActivity.class);
                finish();
            }
        });
        nav_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(CommentListActivity.class);
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
                        Intent intent1 = new Intent(getApplicationContext(),SquareActivity.class);
                        intent1.putExtra("type","stray");
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.lost_btn:
                        Intent intent2 = new Intent(getApplicationContext(),SquareActivity.class);
                        intent2.putExtra("type","lost");
                        startActivity(intent2);
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