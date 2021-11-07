package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.dogfinder.Adapter.StrayDogAdapter;
import com.example.dogfinder.Entity.StrayDog;
import com.example.dogfinder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StraySquareActivity extends BaseActivity {
    BottomNavigationView navigationView;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    StrayDogAdapter strayDogAdapter;
    List<StrayDog> strayDogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stray_square);
        //set bottom navigation
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.stray_btn);
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
                    case R.id.lost_btn:
                        navigate(LostSquareActivity.class);
                        finish();
                    case R.id.profile_btn:
                        navigate(ProfileActivity.class);
                        finish();
                }
                return false;
            }
        });
        recyclerView = findViewById(R.id.stray_recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference("strayDog");
        strayDogList = new ArrayList<>();
        strayDogAdapter = new StrayDogAdapter(getApplicationContext(),strayDogList);
        recyclerView.setAdapter(strayDogAdapter);
        }
    @Override
    protected void onStart(){
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    StrayDog strayDog = dataSnapshot.getValue(StrayDog.class);
                    strayDogList.add(strayDog);
                }
                strayDogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
