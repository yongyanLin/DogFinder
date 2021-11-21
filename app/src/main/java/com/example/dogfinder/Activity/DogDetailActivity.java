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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.dogfinder.Adapter.CommentAdapter;
import com.example.dogfinder.Entity.Favorites;
import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DogDetailActivity extends BaseActivity {
    BottomNavigationView navigationView;
    ToggleButton heart;
    Button back;
    ImageView imageView;
    TextView breed_title,condition,behavior,color,size,description,time,location;
    Dog dog;
    String currentTime;
    FirebaseAuth auth;
    DatabaseReference favoritesReference,commentReference;
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List<Comment> list;
    double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);
        auth = FirebaseAuth.getInstance();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentTime = dateFormat.format(date);
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_CODE);

            return;
        }
        Location clocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //get current location
        longitude = clocation.getLongitude();
        latitude =  clocation.getLatitude();
        favoritesReference = FirebaseDatabase.getInstance().getReference("Favorites");
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
        location = findViewById(R.id.location);
        time = findViewById(R.id.time);
        imageView = findViewById(R.id.dog_img);
        breed_title = findViewById(R.id.breed_title);
        condition = findViewById(R.id.condition);
        behavior = findViewById(R.id.behavior);
        color = findViewById(R.id.color);
        size = findViewById(R.id.size);
        description = findViewById(R.id.description);
        setInformation();
        heart = findViewById(R.id.lost_add_like);
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = auth.getCurrentUser().getUid();
                String dogId =  dog.getId();
                String id = userId+" "+dogId;
                if(!heart.isChecked()){
                    favoritesReference.child(id).removeValue();

                }else{
                    Favorites favorites = new Favorites(userId,dog,currentTime);
                    favorites.setId(id);
                    favoritesReference.child(id).setValue(favorites);
                }
            }
        });
        commentReference = FirebaseDatabase.getInstance().getReference("Comment");
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        commentAdapter = new CommentAdapter(getApplicationContext(),list);
        recyclerView.setAdapter(commentAdapter);
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
                        navigate(FavoritesActivity.class);
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
            double lat = Double.parseDouble(dog.getLocation().split(" ")[0]);
            double lon = Double.parseDouble(dog.getLocation().split(" ")[1]);
            location.setText(DataUtil.distance(latitude,longitude,lat,lon)+" miles");
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        favoritesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Favorites favorites = dataSnapshot.getValue(Favorites.class);
                    String id = auth.getCurrentUser().getUid()+" "+dog.getId();
                    if(favorites.getId().equals(id)){
                        heart.setChecked(true);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    if(comment.getPost().getId().equals(dog.getId()) && comment.getParentId().equals("0")){
                        list.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
                commentAdapter.SetOnItemClickListener(new CommentAdapter.OnItemClickListener() {
                    @Override
                    public void onContentClick(int position) {
                        Intent intent = new Intent(DogDetailActivity.this,CommentActivity.class);
                        intent.putExtra("dog",dog);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onReplyClick(Comment childComment, Comment parentComment) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}