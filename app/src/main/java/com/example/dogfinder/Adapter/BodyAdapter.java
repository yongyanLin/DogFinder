package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.R;

import java.util.List;

public class BodyAdapter extends BaseAdapter {
    private Context context;
    private List<Body> list;
    public BodyAdapter(Context context,List<Body> list){
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.body_item,parent,false);
        TextView name = view.findViewById(R.id.body_name);
        TextView description = view.findViewById(R.id.body_description);
        ImageView image = view.findViewById(R.id.body_image);
        name.setText(list.get(position).getName());
        description.setVisibility(View.INVISIBLE);
        image.setVisibility(View.INVISIBLE);
        return view;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.body_item,parent,false);
        TextView name = view.findViewById(R.id.body_name);
        TextView description = view.findViewById(R.id.body_description);
        ImageView image = view.findViewById(R.id.body_image);
        if(position == 0){
            image.setVisibility(View.INVISIBLE);
        }
        name.setText(list.get(position).getName());
        description.setText(list.get(position).getDescription());
        image.setImageResource(list.get(position).getImage());
        return view;
    }
}
