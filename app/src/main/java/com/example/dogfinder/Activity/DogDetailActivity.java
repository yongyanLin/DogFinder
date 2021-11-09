package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class DogDetailActivity extends BaseActivity {
    BottomNavigationView navigationView;
    ToggleButton heart;
    Button back;
    ImageView imageView;
    TextView breed_title,condition,behavior,color,size,description,time;
    Dog dog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);
        heart = findViewById(R.id.lost_add_like);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SquareActivity.class);
                intent.putExtra("type",dog.getType());
                startActivity(intent);
                finish();
            }
        });
        time = findViewById(R.id.time);
        imageView = findViewById(R.id.dog_img);
        breed_title = findViewById(R.id.breed_title);
        condition = findViewById(R.id.condition);
        behavior = findViewById(R.id.behavior);
        color = findViewById(R.id.color);
        size = findViewById(R.id.size);
        description = findViewById(R.id.description);
        setInformation();
        navigationView = findViewById(R.id.bottom_navigation);
        if(dog.getType().equals("stray")){
            navigationView.setSelectedItemId(R.id.stray_btn);
        }else{
            navigationView.setSelectedItemId(R.id.lost_btn);
        }
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
    public void setInformation(){
        Intent data = getIntent();
        if(data != null){
            dog =(Dog) data.getSerializableExtra("dog");
            Picasso.with(getApplicationContext()).load(dog.getImageUrl()).into(imageView);
            breed_title.setText(dog.getBreed());
            condition.setText(dog.getCondition());
            behavior.setText(dog.getBehavior());
            color.setText(dog.getColor());
            description.setText(dog.getDescription());
            size.setText(dog.getSize());
            time.setText(dog.getTime());
        }
    }
}