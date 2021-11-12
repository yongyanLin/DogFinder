package com.example.dogfinder.Activity;

import static com.example.dogfinder.Activity.IndexActivity.LOCATION_PERM_CODE;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;

import com.example.dogfinder.Adapter.DogAdapter;
import com.example.dogfinder.Entity.Collection;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SquareActivity extends BaseActivity {
    BottomNavigationView navigationView;
    RecyclerView recyclerView;
    DatabaseReference dogReference,collectionReference;
    DogAdapter dogAdapter;
    List<Dog> dogList;
    String type;
    FirebaseAuth auth;
    Button filter_btn;
    SearchView searchView;
    double latitude,longitude;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_square);
        //get current location
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_CODE);

            return;
        }
        Location clocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        longitude = clocation.getLongitude();
        latitude =  clocation.getLatitude();
        //get the type of this square
        Intent intent = getIntent();
        auth = FirebaseAuth.getInstance();
        if(intent != null){
            type = intent.getStringExtra("type");
        }
        filter_btn = findViewById(R.id.filter_btn);
        searchView = findViewById(R.id.search);

        //set bottom navigation
        navigationView = findViewById(R.id.bottom_navigation);
        if(type.equals("stray")){
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
        collectionReference  = FirebaseDatabase.getInstance().getReference("Collection");
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogReference = FirebaseDatabase.getInstance().getReference("Dog");
        dogList = new ArrayList<>();
        dogAdapter = new DogAdapter(getApplicationContext(),dogList,latitude,longitude);
        recyclerView.setAdapter(dogAdapter);
        dogAdapter.SetOnItemClickListener(new DogAdapter.OnItemClickListener() {
            @Override
            public void onLinkClick(int position) {
                Intent intent = new Intent(getApplicationContext(),DogDetailActivity.class);
                Dog dog = dogList.get(position);
                intent.putExtra("dog",dog);
                startActivity(intent);
                finish();
            }
            @Override
            public void onCollectionClick(int position,boolean isChecked) {
                String userId = auth.getCurrentUser().getUid();
                String dogId = dogList.get(position).getId();
                String id = userId+" "+dogId;
                if(!isChecked){
                    collectionReference.child(id).removeValue();

                }else{
                    Collection collection = new Collection(userId,dogId);
                    collection.setId(id);
                    collectionReference.child(id).setValue(collection);
                }
            }


            @Override
            public void onCommentClick(int position) {
                Intent intent = new Intent(getApplicationContext(),CommentActivity.class);
                Dog dog = dogList.get(position);
                intent.putExtra("dog",dog);
                startActivity(intent);
                finish();
            }

            @Override
            public void onShowCommentClick(int position) {
                Intent intent = new Intent(getApplicationContext(),CommentActivity.class);
                Dog dog = dogList.get(position);
                intent.putExtra("dog",dog);
                startActivity(intent);
                finish();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                dogAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                dogAdapter.getFilter().filter(newText);
                return false;
            }
        });
        }
    @Override
    protected void onStart(){
        super.onStart();

        dogReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Dog dog = dataSnapshot.getValue(Dog.class);
                    if(dog.getType().equals(type)){
                        dogList.add(dog);
                    }
                }
                dogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
