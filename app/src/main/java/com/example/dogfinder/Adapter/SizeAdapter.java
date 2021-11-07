package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.Entity.Size;
import com.example.dogfinder.R;

import java.util.List;


public class SizeAdapter extends BaseAdapter {
    private Context context;
    private List<Size> list;
    public SizeAdapter(Context context,List<Size> list){
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
        View view = LayoutInflater.from(context).inflate(R.layout.size_item,parent,false);
        TextView name = view.findViewById(R.id.size_name);
        ImageView image = view.findViewById(R.id.size_image);
        name.setText(list.get(position).getName());
        image.setVisibility(View.INVISIBLE);
        return view;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.size_item,parent,false);
        TextView name = view.findViewById(R.id.size_name);
        ImageView image = view.findViewById(R.id.size_image);
        if(position == 0){
            image.setVisibility(View.INVISIBLE);
        }
        name.setText(list.get(position).getName());
        image.setImageResource(list.get(position).getImage());
        return view;
    }
}
