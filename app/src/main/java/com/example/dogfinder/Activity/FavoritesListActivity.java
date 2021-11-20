package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dogfinder.Adapter.ListAdapter;
import com.example.dogfinder.Entity.Favorites;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritesListActivity extends BaseActivity {
    Button back;
    RecyclerView collectionRecyclerView;
    List<Dog> collectionList;
    DatabaseReference dogReference,collectionReference;
    ListAdapter collectionAdapter;
    FirebaseAuth auth;
    List<String> dogId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);
        auth = FirebaseAuth.getInstance();
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ProfileActivity.class);
                finish();
            }
        });
        dogReference = FirebaseDatabase.getInstance().getReference("Dog");
        collectionReference = FirebaseDatabase.getInstance().getReference("Collection");
        collectionRecyclerView = findViewById(R.id.collectionList_view);
        collectionRecyclerView.setHasFixedSize(true);
        collectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectionList = new ArrayList<>();
        dogId = new ArrayList<>();

        collectionAdapter = new ListAdapter(getApplicationContext(),collectionList);
        collectionRecyclerView.setAdapter(collectionAdapter);
        collectionAdapter.SetOnItemClickListener(new ListAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Dog dog = collectionList.get(position);
                String id = auth.getUid()+" "+dog.getId();
                AlertDialog builder = new AlertDialog.Builder(FavoritesListActivity.this).setTitle("Delete")
                        .setMessage("Do you want to delete this post?").setIcon(R.mipmap.delete)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                collectionReference.child(id).removeValue();
                                dialog.dismiss();
                                navigate(FavoritesListActivity.class);
                                finish();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                Dog dog = collectionList.get(position);
                Intent intent = new Intent(getApplicationContext(),DogDetailActivity.class);
                intent.putExtra("dog",dog);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        collectionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Favorites favorites = snapshot1.getValue(Favorites.class);
                    String userId = favorites.getUserId();
                    if(userId.equals(auth.getCurrentUser().getUid())){
                        dogId.add(favorites.getPostId());
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
                        collectionList.add(dog);
                    }
                }
                collectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}