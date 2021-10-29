package com.example.dogfinder.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.example.dogfinder.Adapter.BehaviorAdapter;
import com.example.dogfinder.Adapter.BodyAdapter;
import com.example.dogfinder.Adapter.ColorAdapter;
import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;

import java.util.List;

public class PostFormActivity extends BaseActivity {
    Spinner spinnerBody,spinnerBehavior,spinnerColor;
    BodyAdapter bodyAdapter;
    BehaviorAdapter behaviorAdapter;
    ColorAdapter colorAdapter;
    Button back_btn,publish_btn;
    String condition,behavior,color,breed,location,description;
    List<Body> bodyList;
    List<Behavior> behaviorList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_form);
        back_btn = findViewById(R.id.back_index);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(IndexActivity.class);
            }
        });
        spinnerBody = findViewById(R.id.body_spinner);
        bodyAdapter = new BodyAdapter(PostFormActivity.this, DataUtil.getBodyList());
        spinnerBody.setAdapter(bodyAdapter);
        bodyList = DataUtil.getBodyList();
        spinnerBody.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                condition = (String)parent.getItemAtPosition(position);
                if(condition.equals("Condition")){
                    condition = "Unknown";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                condition = "Unknown";
            }
        });

        spinnerBehavior = findViewById(R.id.behavior_spinner);
        behaviorAdapter = new BehaviorAdapter(PostFormActivity.this, DataUtil.getBehaviorList());
        spinnerBehavior.setAdapter(behaviorAdapter);
        behaviorList = DataUtil.getBehaviorList();
        spinnerBehavior.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                behavior = (String)parent.getItemAtPosition(position);
                if(behavior.equals("Behavior")){
                    behavior = "Unknown";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                behavior = "Unknown";
            }
        });
        spinnerColor = findViewById(R.id.color_spinner);
        colorAdapter = new ColorAdapter(PostFormActivity.this, DataUtil.getColorList());
        spinnerBehavior.setAdapter(behaviorAdapter);
        behaviorList = DataUtil.getBehaviorList();
        spinnerBehavior.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                behavior = (String)parent.getItemAtPosition(position);
                if(behavior.equals("Behavior")){
                    behavior = "Unknown";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                behavior = "Unknown";
            }
        });
    }
}