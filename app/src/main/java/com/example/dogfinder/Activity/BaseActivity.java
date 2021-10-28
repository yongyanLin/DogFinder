package com.example.dogfinder.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {
    public Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
    }
    public void showToast(String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
    public void navigate(Class cls){
        startActivity(new Intent(context,cls));
    }
}
