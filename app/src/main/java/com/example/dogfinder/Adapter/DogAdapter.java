package com.example.dogfinder.Adapter;

import android.content.Context;
import android.os.CpuUsageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.Collection;
import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.CustomViewHolder> {
    Context context;
    List<Dog> list;
    private OnItemClickListener mlistener;
    DatabaseReference commentReference,collectionReference;
    FirebaseAuth auth;
    Collection collection;

    public void SetOnItemClickListener(OnItemClickListener listener){
        mlistener = listener;
    }
    public DogAdapter(Context context, List<Dog> list) {
        this.context = context;
        this.list = list;

    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ToggleButton heart;
        TextView name,distance,link,show_comment;
        Button comment_btn;
        public CustomViewHolder(@NonNull View itemView,OnItemClickListener listener){
            super(itemView);
            imageView = itemView.findViewById(R.id.dog_image);
            heart = itemView.findViewById(R.id.add_like);
            name = itemView.findViewById(R.id.breed);
            distance = itemView.findViewById(R.id.distance);
            link = itemView.findViewById(R.id.profile_link);
            show_comment = itemView.findViewById(R.id.show_comment);
            comment_btn = itemView.findViewById(R.id.add_comment);


            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onLinkClick(position);
                        }
                    }
                }
            });

            heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onCollectionClick(position,heart.isChecked());
                        }
                    }
                }
            });
            comment_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onCommentClick(position);
                        }
                    }
                }
            });
            show_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onShowCommentClick(position);
                        }
                    }
                }
            });
        }

    }

    @NonNull
    @Override
    public DogAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dog_item,parent,false);

        return new CustomViewHolder(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull DogAdapter.CustomViewHolder holder, int position) {
        Dog dog = list.get(position);
        String imageUri = dog.getImageUrl();
        Picasso.with(holder.imageView.getContext()).load(imageUri).into(holder.imageView);
        holder.name.setText(dog.getBreed());
        holder.distance.setText("location");
        auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid()+" "+dog.getId();
        collectionReference = FirebaseDatabase.getInstance().getReference("Collection").child(id);
        collectionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    holder.heart.setChecked(false);
                }else{
                    holder.heart.setChecked(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        commentReference = FirebaseDatabase.getInstance().getReference("Comment");
        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    if(comment.getPostId().equals(dog.getId())){
                        count += 1;
                    }
                }
                holder.show_comment.setText("View "+count+" comments");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onLinkClick(int position);

        void onCollectionClick(int position,boolean isChecked);

        void onCommentClick(int position);

        void onShowCommentClick(int position);
    }
}
