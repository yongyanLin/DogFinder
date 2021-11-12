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

import com.example.dogfinder.Adapter.CommentListAdapter;
import com.example.dogfinder.Adapter.ListAdapter;
import com.example.dogfinder.Entity.Comment;
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

public class CommentListActivity extends BaseActivity {
    Button back;
    RecyclerView sendRecyclerView,receiveRecyclerView;
    List<String> sendDogList,receiveDogList;
    List<Comment> sendCommentList,receiveCommentList;
    DatabaseReference dogReference,commentReference;
    CommentListAdapter sendAdapter,receiveAdapter;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ProfileActivity.class);
                finish();
            }
        });
        auth = FirebaseAuth.getInstance();
        dogReference = FirebaseDatabase.getInstance().getReference("Dog");
        commentReference = FirebaseDatabase.getInstance().getReference("Comment");
        sendRecyclerView = findViewById(R.id.send_recycle_view);
        receiveRecyclerView = findViewById(R.id.receive_recycle_view);
        sendRecyclerView.setHasFixedSize(true);
        sendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        receiveRecyclerView.setHasFixedSize(true);
        receiveRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sendDogList = new ArrayList<>();
        receiveDogList = new ArrayList<>();
        sendCommentList = new ArrayList<>();
        receiveCommentList = new ArrayList<>();
        sendAdapter = new CommentListAdapter(getApplicationContext(),sendCommentList);
        receiveAdapter = new CommentListAdapter(getApplicationContext(),receiveCommentList);
        sendRecyclerView.setAdapter(sendAdapter);
        receiveRecyclerView.setAdapter(receiveAdapter);
        sendAdapter.SetOnItemClickListener(new CommentListAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Comment comment = sendCommentList.get(position);
                AlertDialog builder = createDialog(comment);
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                String dogId = sendCommentList.get(position).getPostId();
                dogReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                            Dog dog = dataSnapshot.getValue(Dog.class);
                            if(dog.getId().equals(dogId)){
                                Intent intent = new Intent(getApplicationContext(),DogDetailActivity.class);
                                intent.putExtra("dog",dog);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        }});
        receiveAdapter.SetOnItemClickListener(new CommentListAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(int position) {
                Comment comment = receiveCommentList.get(position);
                AlertDialog builder = createDialog(comment);
                builder.show();
            }

            @Override
            public void onContentClick(int position) {
                String dogId = receiveDogList.get(position);
                dogReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                            Dog dog = dataSnapshot.getValue(Dog.class);
                            if(dog.getId().equals(dogId)){
                                Intent intent = new Intent(getApplicationContext(),DogDetailActivity.class);
                                intent.putExtra("dog",dog);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                    if(dog.getUserId().equals(auth.getCurrentUser().getUid())){
                        receiveDogList.add(dog.getId());
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
                    if(comment.getUserId().equals(auth.getCurrentUser().getUid())){
                        sendCommentList.add(comment);
                    }
                    if(receiveDogList.contains(comment.getPostId())){
                        receiveCommentList.add(comment);
                    }
                }
                sendAdapter.notifyDataSetChanged();
                receiveAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public AlertDialog createDialog(Comment comment){
        AlertDialog builder = new AlertDialog.Builder(CommentListActivity.this).setTitle("Delete")
                .setMessage("Do you want to delete this post?").setIcon(R.mipmap.delete)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        commentReference.child(comment.getId()).removeValue();
                        dialog.dismiss();
                        navigate(CommentListActivity.class);
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