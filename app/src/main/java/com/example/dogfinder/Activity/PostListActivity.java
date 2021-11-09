package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dogfinder.Adapter.DogAdapter;
import com.example.dogfinder.Adapter.PostAdapter;
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

public class PostListActivity extends BaseActivity {
    Button back;
    RecyclerView strayRecyclerView,lostRecyclerView;
    List<Dog> strayList,lostList;
    DatabaseReference databaseReference;
    PostAdapter strayAdapter,lostAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ProfileActivity.class);
                finish();
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Dog");
        strayRecyclerView = findViewById(R.id.stray_recycle_view);
        lostRecyclerView = findViewById(R.id.lost_recycle_view);
        strayRecyclerView.setHasFixedSize(true);
        strayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lostRecyclerView.setHasFixedSize(true);
        lostRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        strayList = new ArrayList<>();
        lostList = new ArrayList<>();
        strayAdapter = new PostAdapter(getApplicationContext(),strayList);
        lostAdapter = new PostAdapter(getApplicationContext(),lostList);
        strayRecyclerView.setAdapter(strayAdapter);
        lostRecyclerView.setAdapter(lostAdapter);
        strayAdapter.SetOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Dog dog = strayList.get(position);
                AlertDialog builder = createDialog(dog);
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                Dog dog = strayList.get(position);
                Intent intent = new Intent(getApplicationContext(),DogDetailActivity.class);
                intent.putExtra("dog",dog);
                startActivity(intent);
                finish();
            }
        });
        lostAdapter.SetOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Dog dog = lostList.get(position);
                AlertDialog builder = createDialog(dog);
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                Dog dog = lostList.get(position);
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
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Dog dog = dataSnapshot.getValue(Dog.class);
                    if(dog.getType().equals("stray")){
                        strayList.add(dog);
                    }else{
                        lostList.add(dog);
                    }
                }
                lostAdapter.notifyDataSetChanged();
                strayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public AlertDialog createDialog(Dog dog){
        AlertDialog builder = new AlertDialog.Builder(PostListActivity.this).setTitle("Delete")
                .setMessage("Do you want to delete this post?").setIcon(R.mipmap.delete)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.child(dog.getId()).removeValue();
                        dialog.dismiss();
                        navigate(PostListActivity.class);
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        return builder;
    }
}