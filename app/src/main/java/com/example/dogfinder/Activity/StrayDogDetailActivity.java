package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.dogfinder.Entity.StrayDog;
import com.example.dogfinder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class StrayDogDetailActivity extends BaseActivity {
    BottomNavigationView navigationView;
    ToggleButton heart;
    Button back;
    ImageView imageView;
    TextView breed_title,condition,behavior,color,size,description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stray_dog_detail);
        heart = findViewById(R.id.stray_add_like);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(StraySquareActivity.class);
                finish();
            }
        });
        imageView = findViewById(R.id.dog_img);
        breed_title = findViewById(R.id.breed_title);
        condition = findViewById(R.id.condition);
        behavior = findViewById(R.id.behavior);
        color = findViewById(R.id.color);
        size = findViewById(R.id.size);
        description = findViewById(R.id.description);
        setInformation();
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.lost_btn);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_btn:
                        navigate(IndexActivity.class);
                        finish();
                        break;
                    case R.id.likes_btn:
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
    public void setInformation(){
        Intent data = getIntent();
        if(data != null){
            StrayDog strayDog =(StrayDog) data.getSerializableExtra("dog");
            Picasso.with(getApplicationContext()).load(strayDog.getImageUrl()).into(imageView);
            breed_title.setText(strayDog.getBreed());
            condition.setText(strayDog.getCondition());
            behavior.setText(strayDog.getBehavior());
            color.setText(strayDog.getColor());
            description.setText(strayDog.getDescription());
            size.setText(strayDog.getSize());
        }
    }
}