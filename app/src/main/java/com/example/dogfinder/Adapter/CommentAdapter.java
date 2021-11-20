package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.CommentNotificationService;
import com.example.dogfinder.Utils.TextUtil;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CustomViewHolder> {
    Context context;
    List<Comment> list;
    DocumentReference documentReference;
    DatabaseReference commentReference;
    private OnItemClickListener mListener;

    public CommentAdapter(Context context,List<Comment> list){
        this.context = context;
        this.list = list;
    }
    public void SetOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
    public class CustomViewHolder extends RecyclerView.ViewHolder{
        RecyclerView recyclerView;
        CircleImageView imageView;
        LinearLayout content_field;
        TextView username,comment,date,reply_btn;
        public CustomViewHolder(@NonNull View itemView, OnItemClickListener listener){
            super(itemView);
            recyclerView = itemView.findViewById(R.id.child_recyclerview);
            content_field = itemView.findViewById(R.id.comment_content);
            reply_btn = itemView.findViewById(R.id.view_reply);
            imageView = itemView.findViewById(R.id.user_img);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            date = itemView.findViewById(R.id.time);
            content_field.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onContentClick(position);
                        }
                    }
                }
            });
            reply_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

    }
    @NonNull
    @Override
    public CommentAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item,parent,false);
        return new CommentAdapter.CustomViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CustomViewHolder holder, int position) {
        Comment comment = list.get(position);
        List<Comment> commentList = new ArrayList<>();
        holder.reply_btn.setVisibility(View.INVISIBLE);
        commentReference = FirebaseDatabase.getInstance().getReference("Comment");
        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasChild = false;
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment1 = dataSnapshot.getValue(Comment.class);
                    if(comment1.getParentId().equals(comment.getId())){
                        commentList.add(comment1);
                        hasChild = true;
                    }
                }
                if(hasChild){
                    holder.reply_btn.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        documentReference = FirebaseFirestore.getInstance().collection("users").document(comment.getUserId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot != null){
                        String imageUrl = snapshot.getString("image");
                        String username = snapshot.getString("username");
                        holder.username.setText(username);
                        if(imageUrl == null){
                            holder.imageView.setImageResource(R.mipmap.profile_light);
                        }else {
                            Picasso.with(holder.imageView.getContext()).load(imageUrl).into(holder.imageView);
                        }
                    }
                }
            }
        });

        holder.comment.setText(comment.getContent());
        holder.date.setText(comment.getTime());
        holder.reply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.reply_btn.getText().equals("View reply")){
                    holder.recyclerView.setVisibility(View.VISIBLE);
                    ReplyAdapter replyAdapter = new ReplyAdapter(context,commentList);
                    holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                    holder.recyclerView.setHasFixedSize(true);
                    holder.recyclerView.setAdapter(replyAdapter);
                    replyAdapter.notifyDataSetChanged();
                    holder.reply_btn.setText("Collapse reply");
                    replyAdapter.SetOnItemClickListener(new ReplyAdapter.OnItemClickListener() {
                        @Override
                        public void onContentClick(int position) {
                            //pass to CommentActivity
                            Comment childComment = commentList.get(position);
                            mListener.onReplyClick(childComment,comment);
                        }
                    });
                }else{
                    holder.recyclerView.setVisibility(View.GONE);
                    holder.reply_btn.setText("View reply");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onContentClick(int position);
        void onReplyClick(Comment childComment,Comment parentComment);
    }
}
