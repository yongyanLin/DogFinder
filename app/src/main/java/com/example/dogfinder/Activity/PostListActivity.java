package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.example.dogfinder.Adapter.ListAdapter;
import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.postPopUpUtil;
import com.example.dogfinder.Utils.profilePopUpUtil;
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
    DatabaseReference databaseReference,commentReference;
    ListAdapter strayAdapter,lostAdapter;
    FirebaseAuth auth;
    Dog dog;
    postPopUpUtil popup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        dog = null;
        auth = FirebaseAuth.getInstance();
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ProfileActivity.class);
                finish();
            }
        });
        commentReference = FirebaseDatabase.getInstance().getReference("Comment");
        databaseReference = FirebaseDatabase.getInstance().getReference("Dog");
        strayRecyclerView = findViewById(R.id.stray_recycle_view);
        lostRecyclerView = findViewById(R.id.lost_recycle_view);
        strayRecyclerView.setHasFixedSize(true);
        strayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lostRecyclerView.setHasFixedSize(true);
        lostRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        strayList = new ArrayList<>();
        lostList = new ArrayList<>();
        strayAdapter = new ListAdapter(getApplicationContext(),strayList);
        lostAdapter = new ListAdapter(getApplicationContext(),lostList);
        strayRecyclerView.setAdapter(strayAdapter);
        lostRecyclerView.setAdapter(lostAdapter);
        strayAdapter.SetOnItemClickListener(new ListAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Dog dog = strayList.get(position);
                AlertDialog builder = createDialog(dog);
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                dog = strayList.get(position);
                popPostFormBottom();
            }
        });
        lostAdapter.SetOnItemClickListener(new ListAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Dog dog = lostList.get(position);
                AlertDialog builder = createDialog(dog);
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                dog = lostList.get(position);
                popPostFormBottom();
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
                    if(dog.getType().equals("stray") && dog.getUserId().equals(auth.getCurrentUser().getUid())){
                        strayList.add(dog);
                    }else if(dog.getType().equals("lost") && dog.getUserId().equals(auth.getCurrentUser().getUid())){
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
    public void popPostFormBottom() {
        popup = new postPopUpUtil(this, onClickListener);
        popup.showAtLocation(findViewById(R.id.postList), Gravity.BOTTOM|Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.edit_btn:
                    Intent intent1 = new Intent(getApplicationContext(),EditFormActivity.class);
                    intent1.putExtra("dog",dog);
                    startActivity(intent1);
                    finish();
                    popup.dismiss();
                    break;
                case R.id.browse_btn:
                    Intent intent2 = new Intent(getApplicationContext(),DogDetailActivity.class);
                    intent2.putExtra("dog",dog);
                    startActivity(intent2);
                    finish();
                    popup.dismiss();
                    break;
            }
        }
    };
    public AlertDialog createDialog(Dog dog){
        AlertDialog builder = new AlertDialog.Builder(PostListActivity.this).setTitle("Delete")
                .setMessage("Do you want to delete this post?").setIcon(R.mipmap.delete)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dogId = dog.getId();
                        databaseReference.child(dogId).removeValue();
                        dialog.dismiss();
                        //delete related comments
                        commentReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot snapshot1:snapshot.getChildren()){
                                    Comment comment = snapshot1.getValue(Comment.class);
                                    if(comment.getPost().getId().equals(dogId)){
                                        commentReference.child(comment.getId()).removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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