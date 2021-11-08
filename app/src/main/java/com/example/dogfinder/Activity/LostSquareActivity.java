package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.dogfinder.Adapter.LostDogAdapter;
import com.example.dogfinder.Adapter.StrayDogAdapter;
import com.example.dogfinder.Entity.LostDog;
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

public class LostSquareActivity extends BaseActivity {
    BottomNavigationView navigationView;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    LostDogAdapter lostDogAdapter;
    List<LostDog> lostDogList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_square);
        //set bottom navigation
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
        recyclerView = findViewById(R.id.lost_recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference = FirebaseDatabase.getInstance().getReference("lostDog");
        lostDogList = new ArrayList<>();
        lostDogAdapter = new LostDogAdapter(getApplicationContext(),lostDogList);
        recyclerView.setAdapter(lostDogAdapter);
    }
    @Override
    protected void onStart(){
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    LostDog lostDog = dataSnapshot.getValue(LostDog.class);
                    lostDogList.add(lostDog);
                }
                lostDogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}