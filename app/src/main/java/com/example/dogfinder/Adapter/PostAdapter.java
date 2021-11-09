package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.CustomViewHolder>{
    Context context;
    List<Dog> list;
    private PostAdapter.OnItemClickListener mlistener;
    FirebaseAuth auth;

    public void SetOnItemClickListener(PostAdapter.OnItemClickListener listener){
        mlistener = listener;
    }
    public PostAdapter(Context context,List<Dog> list){
        this.context = context;
        this.list = list;
    }
    public static class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView breed,time;
        Button delete_btn;
        LinearLayout content;
        public CustomViewHolder(@NonNull View itemView, PostAdapter.OnItemClickListener listener){
            super(itemView);
            content = itemView.findViewById(R.id.content);
            breed = itemView.findViewById(R.id.breed);
            time = itemView.findViewById(R.id.time);
            delete_btn = itemView.findViewById(R.id.delete);
            content.setOnClickListener(new View.OnClickListener() {
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
            delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onButtonClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public PostAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item,parent,false);

        return new PostAdapter.CustomViewHolder(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.CustomViewHolder holder, int position) {
        Dog dog = list.get(position);
        holder.breed.setText(dog.getBreed());
        holder.time.setText(dog.getTime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public interface OnItemClickListener {
        void onButtonClick(int position);

        void onContentClick(int position);
    }
}
