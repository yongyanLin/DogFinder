package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.User;
import com.example.dogfinder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.CustomViewHolder> {
    Context context;
    List<Comment> list;
    private OnItemClickListener mListener;

    public void SetOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public ReplyAdapter(Context context,List<Comment> list){
        this.context = context;
        this.list = list;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView username,comment,date;
        LinearLayout linearLayout;
        public CustomViewHolder(@NonNull View itemView, OnItemClickListener listener){
            super(itemView);
            linearLayout = itemView.findViewById(R.id.comment_content);
            imageView = itemView.findViewById(R.id.user_img);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            date = itemView.findViewById(R.id.time);
            linearLayout.setOnClickListener(new View.OnClickListener() {
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
        }

    }

    @NonNull
    @Override
    public ReplyAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reply_item,parent,false);
        return new ReplyAdapter.CustomViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.CustomViewHolder holder, int position) {
        Comment comment = list.get(position);
        User user = comment.getUser();
        String imageUrl = user.getImage();
        String username = user.getUsername();
        holder.username.setText(username);
        if(imageUrl == null){
            holder.imageView.setImageResource(R.mipmap.profile);
        }else {
            Picasso.with(holder.imageView.getContext()).load(imageUrl).into(holder.imageView);
        }

        holder.comment.setText(comment.getContent());
        holder.date.setText(comment.getTime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onContentClick(int position);
    }
}
