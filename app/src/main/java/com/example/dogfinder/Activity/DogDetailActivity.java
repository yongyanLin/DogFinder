package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.dogfinder.Adapter.CommentAdapter;
import com.example.dogfinder.Entity.Collection;
import com.example.dogfinder.Entity.Comment;
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
import java.util.List;

public class DogDetailActivity extends BaseActivity {
    BottomNavigationView navigationView;
    ToggleButton heart;
    Button back;
    ImageView imageView;
    TextView breed_title,condition,behavior,color,size,description,time;
    Dog dog;
    FirebaseAuth auth;
    DatabaseReference collectionReference,commentReference;
    RecyclerView recyclerView;
    CommentAdapter commentAdapter;
    List<Comment> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);
        auth = FirebaseAuth.getInstance();
        collectionReference = FirebaseDatabase.getInstance().getReference("Collection");
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
        heart = findViewById(R.id.lost_add_like);
        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = auth.getCurrentUser().getUid();
                String dogId =  dog.getId();
                String id = userId+" "+dogId;
                if(!heart.isChecked()){
                    collectionReference.child(id).removeValue();

                }else{
                    Collection collection = new Collection(userId,dogId);
                    collection.setId(id);
                    collectionReference.child(id).setValue(collection);
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
    @Override
    protected void onStart(){
        super.onStart();
        collectionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Collection collection = dataSnapshot.getValue(Collection.class);
                    String id = auth.getCurrentUser().getUid()+" "+dog.getId();
                    if(collection.getId().equals(id)){
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
                    if(comment.getPostId().equals(dog.getId())){
                        list.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}