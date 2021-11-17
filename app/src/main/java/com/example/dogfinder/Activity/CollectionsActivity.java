package com.example.dogfinder.Activity;

import static com.example.dogfinder.Activity.IndexActivity.LOCATION_PERM_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionsActivity extends BaseActivity {
    BottomNavigationView navigationView;
    RecyclerView recyclerView;
    SearchView searchView;
    DatabaseReference dogReference,collectionReference;
    DogAdapter dogAdapter;
    List<Dog> dogList;
    List<String> dogId;
    FirebaseAuth auth;
    double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);
        auth = FirebaseAuth.getInstance();
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
        //set bottom navigation
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.likes_btn);
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
        dogId = new ArrayList<>();
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogReference = FirebaseDatabase.getInstance().getReference("Dog");
        dogList = new ArrayList<>();
        dogAdapter = new DogAdapter(getApplicationContext(),dogList,latitude,longitude);
        recyclerView.setAdapter(dogAdapter);
        searchView = findViewById(R.id.search);
        getData();
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
    public void getData(){
        collectionReference  = FirebaseDatabase.getInstance().getReference("Collection");
        collectionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Collection collection = snapshot1.getValue(Collection.class);
                    String userId = collection.getUserId();
                    if(userId.equals(auth.getCurrentUser().getUid())){
                        dogId.add(collection.getPostId());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dogReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Dog dog = dataSnapshot.getValue(Dog.class);
                    if(dogId.contains(dog.getId())){
                        dogList.add(dog);
                    }
                }
                Collections.sort(dogList);
                dogAdapter = new DogAdapter(getApplicationContext(),dogList,latitude,longitude);
                recyclerView.setAdapter(dogAdapter);
                dogAdapter.notifyDataSetChanged();
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
                        //dogAdapter.notifyDataSetChanged();

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

                    @Override
                    public void onShareClick(int position) {
                        Dog dog = dogList.get(position);
                        String imageUri = dog.getImageUrl();
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    Bitmap bitmap = Picasso.with(getApplicationContext()).load(imageUri).get();
                                    String fileUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, dog.getBreed(),dog.getId());
                                    Uri contentUri = Uri.parse(fileUrl);
                                    if (contentUri != null) {
                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                        shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }

                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}