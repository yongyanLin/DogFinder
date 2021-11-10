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

import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CustomViewHolder>{
    Context context;
    List<Comment> commentList;
    private CommentListAdapter.OnItemClickListener mlistener;

    public void SetOnItemClickListener(CommentListAdapter.OnItemClickListener listener){
        mlistener = listener;
    }
    public CommentListAdapter(Context context,List<Comment> commentList){
        this.context = context;
        this.commentList = commentList;
    }
    public static class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView breed,time,comment;
        Button delete_btn;
        LinearLayout content;
        public CustomViewHolder(@NonNull View itemView, CommentListAdapter.OnItemClickListener listener){
            super(itemView);
            content = itemView.findViewById(R.id.content);
            breed = itemView.findViewById(R.id.breed);
            time = itemView.findViewById(R.id.time);
            comment = itemView.findViewById(R.id.comment);
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
    public CommentListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_list_item,parent,false);

        return new CommentListAdapter.CustomViewHolder(view,mlistener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentListAdapter.CustomViewHolder holder, int position) {
        holder.comment.setText(commentList.get(position).getContent());
        holder.time.setText(commentList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
    public interface OnItemClickListener {
        void onButtonClick(int position);

        void onContentClick(int position);
    }
}
