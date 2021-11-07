package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.Size;
import com.example.dogfinder.Entity.StrayDog;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.TextUtil;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class StrayDogAdapter extends RecyclerView.Adapter<StrayDogAdapter.CustomViewHolder> {
    Context context;
    List<StrayDog> list;
    private int currentPosition = -1;
    public StrayDogAdapter(Context context, List<StrayDog> list) {
        this.context = context;
        this.list = list;
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ToggleButton heart;
        TextView name,distance,link,show_comment;
        Button share_btn,comment_btn;
        public CustomViewHolder(@NonNull View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.strayDogProfile_image);
            heart = itemView.findViewById(R.id.stray_add_like);
            name = itemView.findViewById(R.id.stray_breed);
            distance = itemView.findViewById(R.id.stray_distance);
            link = itemView.findViewById(R.id.stray_profile_link);
            show_comment = itemView.findViewById(R.id.stray_comment_btn);
            share_btn = itemView.findViewById(R.id.stray_share_btn);
            comment_btn = itemView.findViewById(R.id.stray_add_comment);

        }
    }

    @NonNull
    @Override
    public StrayDogAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.straydog_item,parent,false);

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StrayDogAdapter.CustomViewHolder holder, int position) {
        StrayDog strayDog = list.get(position);
        String imageUri = strayDog.getImageUrl();
        Picasso.with(holder.imageView.getContext()).load(imageUri).into(holder.imageView);
        holder.name.setText(strayDog.getBreed());
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
