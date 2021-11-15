package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dogfinder.Adapter.CommentAdapter;
import com.example.dogfinder.Adapter.DogAdapter;
import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends BaseActivity {
    private static final String CHANNEL_ID = "commentNotification";
    CircleImageView circleImageView;
    Button post_btn,back_btn;
    EditText comment;
    RecyclerView recyclerView;
    Dog dog;
    String time,userId,content;
    CommentAdapter commentAdapter;
    DocumentReference documentReference;
    DatabaseReference databaseReference,dogReference;
    FirebaseAuth auth;
    List<Comment> list;
    NotificationManagerCompat notificationManagerCompat;
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
        notificationManagerCompat = NotificationManagerCompat.from(this);
        auth = FirebaseAuth.getInstance();

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
                        String imageUrl = snapshot.getString("image");
                        Picasso.with(getApplicationContext()).load(imageUrl).into(circleImageView);
                    }
                }
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        commentAdapter = new CommentAdapter(getApplicationContext(),list);
        recyclerView.setAdapter(commentAdapter);
        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                time = dateFormat.format(currentTime);
                String content = comment.getText().toString().trim();
                String id = databaseReference.push().getKey();
                Comment comment = new Comment(id,auth.getCurrentUser().getUid(),dog.getId(),
                        content,time);
                databaseReference.child(id).setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if(auth.getCurrentUser().getUid().equals(userId)){
                            sendChannel();
                        }
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
                    if(comment.getPostId().equals(dog.getId())){
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
    public void sendChannel(){
        content = comment.getText().toString().trim();
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.mipmap.comment)
                .setContentTitle("Receiving new comment!")
                .setContentText(content)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAllowSystemGeneratedContextualActions(true)
                .build();
        notificationManagerCompat.notify(1,notification);
    }

}