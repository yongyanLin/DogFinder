package com.example.dogfinder.Activity;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dogfinder.Adapter.BehaviorAdapter;
import com.example.dogfinder.Adapter.BodyAdapter;
import com.example.dogfinder.Adapter.ColorAdapter;
import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class PostFormActivity extends BaseActivity {
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    Spinner spinnerBody,spinnerBehavior,spinnerColor;
    BodyAdapter bodyAdapter;
    BehaviorAdapter behaviorAdapter;
    ColorAdapter colorAdapter;
    Button back_btn,publish_btn;
    String condition,behavior,color,breed,location,description;
    List<Body> bodyList;
    List<Behavior> behaviorList;
    List<String> colorList;
    LinearLayout otherColor;
    EditText mixColor;
    ImageView imageView;
    TextView location_btn;
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
        //Body
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
        //Behavior
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
        //Color
        mixColor = findViewById(R.id.colorMix);
        otherColor = findViewById(R.id.otherColor);
        otherColor.setVisibility(View.INVISIBLE);
        spinnerColor = findViewById(R.id.color_spinner);
        colorAdapter = new ColorAdapter(PostFormActivity.this, DataUtil.getColorList());
        spinnerColor.setAdapter(colorAdapter);
        colorList = DataUtil.getColorList();
        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                color = (String)parent.getItemAtPosition(position);
                if(color.equals("Color")){
                    color = "Unknown";
                }
                if(color.equals("Other")){
                    otherColor.setVisibility(View.VISIBLE);
                }else {
                    otherColor.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                color = "Unknown";
            }
        });
        if(otherColor.getVisibility() == View.VISIBLE){
            String colorString = mixColor.getText().toString().trim();
            if(!colorString.equals("") || colorString.contains(";")){
                String colors[] = colorString.split(";");
                for(int i = 0;i<colors.length;i++){
                    color += colors[i]+" ";
                }
            }else if(colorString.equals("")){
                color = "Unknown";
            }else{
                showToast("Please input colors as required format.");
            }
        }
        //set image
        imageView = findViewById(R.id.dog_photo);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.get("cameraImage") != null){
                Uri image = (Uri) extras.get("cameraImage");
                imageView.setImageURI(image);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                saveToGallery(bitmap);
            }
            if(extras.get("galleryImage") != null){
                Uri image = (Uri) extras.get("galleryImage");
                imageView.setImageURI(image);
            }
        }
        //set the location
        location_btn = findViewById(R.id.location);
        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(MapActivity.class);
            }
        });


    }
    //save image to gallery
    public void saveToGallery(Bitmap bitmap) {
        OutputStream os;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Img_"+".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
            try {
                os = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,os);
                Objects.requireNonNull(os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}