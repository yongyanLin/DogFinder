package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogfinder.Entity.Comment;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.CustomViewHolder> implements Filterable {
    Context context;
    List<Dog> dogFullList;
    List<Dog> dogList;
    List<Dog> filteredList = new ArrayList<>();
    double latitude,longitude;
    private OnItemClickListener mlistener;
    DatabaseReference commentReference,collectionReference;
    FirebaseAuth auth;


    public void SetOnItemClickListener(OnItemClickListener listener){
        mlistener = listener;
    }
    public DogAdapter(Context context, List<Dog> list,double latitude,double longitude) {
        this.context = context;
        this.dogFullList = list;
        this.dogList = new ArrayList<>(dogFullList);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public Filter getFilter() {
        return dogFilter;
    }

    private final Filter dogFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList = new ArrayList<>();
            //String itemList[] = new String[7];

            String pattern = constraint.toString().toLowerCase();
            if(pattern.contains("search")){
                if(pattern.split(" ").length>1){
                    pattern = pattern.split(" ")[1];
                }else{
                    filteredList = dogFullList;
                }
                for(Dog dog:dogFullList){
                    if(dog.getType().toLowerCase().contains(pattern) && !filteredList.contains(dog)){
                        filteredList.add(dog);
                    }else if(dog.getBreed().toLowerCase().contains(pattern) && !filteredList.contains(dog)){
                        filteredList.add(dog);
                    }else if(dog.getBehavior().toLowerCase().contains(pattern) && !filteredList.contains(dog)){
                        filteredList.add(dog);
                    }else if(dog.getColor().toLowerCase().contains(pattern) && !filteredList.contains(dog)){
                        filteredList.add(dog);
                    }else if(dog.getCondition().toLowerCase().contains(pattern) && !filteredList.contains(dog)) {
                        filteredList.add(dog);
                    }
                }
            }else if(pattern.contains("filter")){
                String patternList[] = new String[8];
                if(pattern.split(" ").length>1){
                    patternList = pattern.split(",");
                }
                for(Dog dog:dogFullList) {
                    boolean isRight = true;
                    if(!patternList[1].equals("distance")){
                        double distance = DataUtil.distance(latitude,longitude,Double.parseDouble(dog.getLocation().split(" ")[0]),Double.parseDouble(dog.getLocation().split(" ")[1]));
                        if(patternList[1].equals("10 miles") && distance > 10){
                            isRight = false;
                        }
                        if(patternList[1].equals("20 miles") && distance > 20){
                            isRight = false;
                        }
                        if(patternList[1].equals("50 miles") && distance > 50){
                            isRight = false;
                        }
                        if(patternList[1].equals("100 miles") && distance > 100){
                            isRight = false;
                        }
                        if(patternList[1].equals("200 miles") && distance > 200){
                            isRight = false;
                        }
                    }
                    if(!patternList[2].equals("post time")){
                        Date postTime = null;
                        Date now = Calendar.getInstance().getTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            postTime = dateFormat.parse(dog.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Calendar startCalendar = new GregorianCalendar();
                        startCalendar.setTime(postTime);
                        Calendar endCalendar = new GregorianCalendar();
                        endCalendar.setTime(now);
                        int diffMonth = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                        int diffDay = endCalendar.get(Calendar.DAY_OF_MONTH) - startCalendar.get(Calendar.DAY_OF_MONTH);
                        if(patternList[2].equals("within a week") && diffDay >=7){
                            isRight = false;
                        }
                        if(patternList[2].equals("within a month") && diffMonth >1){
                            isRight = false;
                        }
                        if(patternList[2].equals("within three months") && diffMonth >3){
                            isRight = false;
                        }
                        if(patternList[2].equals("within six months") && diffMonth >6){
                            isRight = false;
                        }
                    }
                    if(!patternList[3].equals("null")){
                        if(!patternList[3].contains(dog.getBreed().toLowerCase())){
                            isRight = false;
                            if(dog.getBreed().equals("No detection")){
                                isRight = true;
                            }
                        }
                    }
                    if(!patternList[4].equals("size")){
                        if(!dog.getSize().toLowerCase().equals(patternList[4])){
                            isRight = false;
                        }
                    }
                    if(!patternList[5].equals("behavior")){
                        if(!dog.getBehavior().toLowerCase().equals(patternList[5])){
                            isRight = false;
                        }
                    }
                    if(!patternList[6].equals("condition")){
                        if(!dog.getCondition().toLowerCase().equals(patternList[6])){
                            isRight = false;
                        }
                    }
                    if(!patternList[7].equals("null")){
                        if(!patternList[7].contains(dog.getColor().toLowerCase())){
                            isRight = false;
                        }
                    }
                    if(isRight == true){
                        filteredList.add(dog);
                    }
                }
            }
        FilterResults results = new FilterResults();
        results.values = filteredList;
        results.count = filteredList.size();
        return results;}


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            dogList.clear();
            if(results.values != null){
                dogList.addAll((List)results.values);
            }else{
                dogList.addAll(dogFullList);
            }
            notifyDataSetChanged();
        }
    };
    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ToggleButton heart;
        TextView name,distance,link,show_comment;
        Button comment_btn,share_btn;
        public CustomViewHolder(@NonNull View itemView,OnItemClickListener listener){
            super(itemView);

            imageView = itemView.findViewById(R.id.dog_image);
            heart = itemView.findViewById(R.id.add_like);
            name = itemView.findViewById(R.id.breed);
            distance = itemView.findViewById(R.id.distance);
            link = itemView.findViewById(R.id.profile_link);
            share_btn = itemView.findViewById(R.id.shareBtn);
            show_comment = itemView.findViewById(R.id.show_comment);
            comment_btn = itemView.findViewById(R.id.add_comment);
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        position = dogFullList.indexOf(dogList.get(position));
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
                        position = dogFullList.indexOf(dogList.get(position));
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
                        position = dogFullList.indexOf(dogList.get(position));
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
                        position = dogFullList.indexOf(dogList.get(position));
                        if(position != RecyclerView.NO_POSITION){
                            listener.onShowCommentClick(position);
                        }
                    }
                }
            });
            share_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){

                        int position = getAdapterPosition();
                        position = dogFullList.indexOf(dogList.get(position));
                        if(position != RecyclerView.NO_POSITION){
                            listener.onShareClick(position);
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
        Dog dog = dogList.get(position);
        String imageUri = dog.getImageUrl();
        Picasso.with(holder.imageView.getContext()).load(imageUri).into(holder.imageView);
        holder.name.setText(dog.getBreed());
        //get the post location
        double lat2 = Double.parseDouble(dog.getLocation().split(" ")[0]);
        double lon2 = Double.parseDouble(dog.getLocation().split(" ")[1]);
        double distance = DataUtil.distance(latitude,longitude,lat2,lon2);
        holder.distance.setText(distance+" miles");
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
        return dogList.size();
    }

    public interface OnItemClickListener {
        void onLinkClick(int position);

        void onCollectionClick(int position,boolean isChecked);

        void onCommentClick(int position);

        void onShowCommentClick(int position);

        void onShareClick(int position);
    }

}
