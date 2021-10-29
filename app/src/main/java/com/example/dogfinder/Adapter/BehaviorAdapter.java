package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.R;

import java.util.List;

public class BehaviorAdapter extends BaseAdapter {
    private Context context;
    private List<Behavior> list;
    public BehaviorAdapter(Context context,List<Behavior> list){
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
        View view = LayoutInflater.from(context).inflate(R.layout.text_item,parent,false);
        TextView name = view.findViewById(R.id.behavior_name);
        TextView description = view.findViewById(R.id.behavior_description);
        name.setText(list.get(position).getName());
        description.setVisibility(View.INVISIBLE);
        return view;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.text_item,parent,false);
        TextView name = view.findViewById(R.id.behavior_name);
        TextView description = view.findViewById(R.id.behavior_description);
        name.setText(list.get(position).getName());
        description.setText(list.get(position).getDescription());
        return view;
    }
}
