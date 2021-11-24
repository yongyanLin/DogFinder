package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.example.dogfinder.Adapter.CommentAdapter;
import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.Entity.NotificationSender;
import com.example.dogfinder.Entity.TokenData;
import com.example.dogfinder.Entity.User;
import com.example.dogfinder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends BaseActivity {
    CircleImageView circleImageView;
    Button post_btn,back_btn;
    EditText comment;
    RecyclerView recyclerView;
    Dog dog;
    String time,userId;
    CommentAdapter commentAdapter;
    DocumentReference documentReference;
    DatabaseReference databaseReference,dogReference;
    FirebaseAuth auth;
    List<Comment> list;
    String replyID,receiverId;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        getContent();
        databaseReference = FirebaseDatabase.getInstance().getReference("Comment");
        dogReference = FirebaseDatabase.getInstance().getReference("Dog");
        //get the publisher of this post
        dogReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Dog dog1 = dataSnapshot.getValue(Dog.class);
                    if(dog1.getId().equals(dog.getId())){
                        userId = dog1.getUserId();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        auth = FirebaseAuth.getInstance();
        updateToken();
        circleImageView = findViewById(R.id.image_icon);
        comment = findViewById(R.id.comment_field);
        post_btn = findViewById(R.id.post);
        back_btn = findViewById(R.id.back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SquareActivity.class);
                intent.putExtra("type",dog.getType());
                startActivity(intent);
                finish();
            }
        });
        documentReference = FirebaseFirestore.getInstance().collection("users").document(auth.getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot != null){
                        currentUser = snapshot.toObject(User.class);
                        String imageUrl = snapshot.getString("image");
                        if(imageUrl == null) {
                            circleImageView.setImageResource(R.mipmap.profile);
                        }else{
                            Picasso.with(getApplicationContext()).load(imageUrl).into(circleImageView);
                        }
                    }
                }
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        commentAdapter = new CommentAdapter(getApplicationContext(),list);
        commentAdapter.SetOnItemClickListener(new CommentAdapter.OnItemClickListener() {
            @Override
            public void onContentClick(int position) {
                comment.setText("");
                Comment comment1 = list.get(position);
                User user = comment1.getUser();
                replyID = comment1.getId();
                String username = user.getUsername();
                comment.setText("Reply "+username+":");
            }

            @Override
            public void onReplyClick(Comment childComment,Comment parentComment) {
                //get the reply below the comment
                comment.setText("");
                User user = childComment.getUser();
                String username = user.getUsername();
                comment.setText("Reply "+username+":");
                replyID = parentComment.getId();
            }

        });
        recyclerView.setAdapter(commentAdapter);
        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                time = dateFormat.format(currentTime);
                String content = comment.getText().toString().trim();
                String id = databaseReference.push().getKey();
                Comment comment1 = new Comment(id,currentUser,auth.getUid(),dog,
                        content,time);
                if(!content.contains("Reply")){
                    FirebaseFirestore.getInstance().collection("DeviceTokens").document(dog.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String id = documentSnapshot.getString("userId");
                            String token = documentSnapshot.getString("token");
                            if(!id.equals(auth.getCurrentUser().getUid())){
                                NotificationSender sender = new NotificationSender(token,content,getApplicationContext(),CommentActivity.this);
                                sender.sendCommentNotification();
                            }
                        }
                    });
                    comment1.setParentId("0");
                }else{
                    comment1.setParentId(replyID);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1:snapshot.getChildren()){
                                Comment parentComment = snapshot1.getValue(Comment.class);
                                if(parentComment.getId().equals(replyID)){
                                    receiverId = parentComment.getUserId();
                                    break;
                                }
                            }
                            FirebaseFirestore.getInstance().collection("DeviceTokens").document(receiverId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String token = documentSnapshot.getString("token");
                                    NotificationSender sender = new NotificationSender(token,content.split(":")[1],getApplicationContext(),CommentActivity.this);
                                    sender.sendCommentNotification();
                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                databaseReference.child(id).setValue(comment1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Intent intent = new Intent(CommentActivity.this,CommentActivity.class);
                        intent.putExtra("dog",dog);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
    public void getContent(){
        Intent intent = getIntent();
        if(intent != null){
            dog = (Dog) intent.getSerializableExtra("dog");
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    if(comment.getPost().getId().equals(dog.getId()) && comment.getParentId().equals("0")){
                        list.add(comment);
                    }
                }
                Collections.sort(list);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void updateToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                TokenData tokenData = new TokenData(auth.getUid(),s);
                FirebaseFirestore.getInstance().collection("DeviceTokens").document(auth.getUid()).set(tokenData);
            }
        });

    }


}