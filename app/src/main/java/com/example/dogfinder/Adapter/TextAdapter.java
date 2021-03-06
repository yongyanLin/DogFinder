package com.example.dogfinder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dogfinder.Entity.Size;
import com.example.dogfinder.R;

import java.util.List;

public class TextAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;
    public TextAdapter(Context context,List<String> list){
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.filter_item,parent,false);
        TextView name = view.findViewById(R.id.name);
        name.setText(list.get(position));
        return view;
    }
}
