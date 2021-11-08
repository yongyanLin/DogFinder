package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.LostDog;
import com.example.dogfinder.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LostDogAdapter extends RecyclerView.Adapter<LostDogAdapter.LostCustomViewHolder>{
    Context context;
    List<LostDog> list;
    public LostDogAdapter(Context context, List<LostDog> list) {
        this.context = context;
        this.list = list;
    }

    public static class LostCustomViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ToggleButton heart;
        TextView name,distance,link,show_comment;
        Button share_btn,comment_btn;
        public LostCustomViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.lostDogProfile_image);
            heart = itemView.findViewById(R.id.lost_add_like);
            name = itemView.findViewById(R.id.lost_breed);
            distance = itemView.findViewById(R.id.lost_distance);
            link = itemView.findViewById(R.id.lost_profile_link);
            show_comment = itemView.findViewById(R.id.lost_comment_btn);
            share_btn = itemView.findViewById(R.id.lost_share_btn);
            comment_btn = itemView.findViewById(R.id.lost_add_comment);

        }
    }

    @NonNull
    @Override
    public LostDogAdapter.LostCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lostdog_item,parent,false);
        return new LostDogAdapter.LostCustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LostDogAdapter.LostCustomViewHolder holder, int position) {
        LostDog lostDog = list.get(position);
        String imageUri = lostDog.getImageUrl();
        Picasso.with(holder.imageView.getContext()).load(imageUri).into(holder.imageView);
        holder.name.setText(lostDog.getBreed());
        holder.distance.setText("location");
        holder.link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
