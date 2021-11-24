package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dogfinder.Adapter.BehaviorAdapter;
import com.example.dogfinder.Adapter.BodyAdapter;
import com.example.dogfinder.Adapter.SizeAdapter;
import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.Entity.Favorites;
import com.example.dogfinder.Entity.Size;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFormActivity extends BaseActivity {
    FirebaseAuth auth;
    Dog dog;
    Spinner spinnerBody, spinnerBehavior,spinnerSize;
    BodyAdapter bodyAdapter;
    BehaviorAdapter behaviorAdapter;
    SizeAdapter sizeAdapter;
    Button back_btn, publish_btn;
    String condition, behavior, color,size, breed, location,cLocation,userID,time;
    List<Body> bodyList;
    List<Behavior> behaviorList;
    List<Integer> colorList;
    List<Size> sizeList;
    String[] colorArray;
    boolean[] selectedColor;
    EditText breed_field,description_view;
    ImageView imageView;
    TextView location_btn,color_view;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    List<String> tokensList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_form);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = dateFormat.format(currentTime);
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        location = null;
        tokensList = new ArrayList<>();
        back_btn = findViewById(R.id.back_index);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(IndexActivity.class);
                finish();
            }
        });
        //breed
        breed_field = findViewById(R.id.breed);
        breed = breed_field.getText().toString().trim();
        //Body
        spinnerBody = findViewById(R.id.body_spinner);
        bodyAdapter = new BodyAdapter(EditFormActivity.this, DataUtil.getBodyList());
        spinnerBody.setAdapter(bodyAdapter);
        bodyList = DataUtil.getBodyList();
        spinnerBody.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                condition = (String) parent.getItemAtPosition(position);
                if (condition.equals("Condition")) {
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
        behaviorAdapter = new BehaviorAdapter(EditFormActivity.this, DataUtil.getBehaviorList());
        spinnerBehavior.setAdapter(behaviorAdapter);
        behaviorList = DataUtil.getBehaviorList();
        spinnerBehavior.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                behavior = (String) parent.getItemAtPosition(position);
                if (behavior.equals("Behavior")) {
                    behavior = "Unknown";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                behavior = "Unknown";
            }
        });
        //Size
        spinnerSize = findViewById(R.id.size_spinner);
        sizeAdapter = new SizeAdapter(EditFormActivity.this, DataUtil.getSizeList());
        spinnerSize.setAdapter(sizeAdapter);
        sizeList = DataUtil.getSizeList();
        spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                size = (String) parent.getItemAtPosition(position);
                if (size.equals("Size")) {
                    size = "Unknown";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                behavior = "Unknown";
            }
        });
        //Color
        colorArray = DataUtil.getColorArray();
        color_view = findViewById(R.id.color);
        colorList = new ArrayList<>();
        selectedColor = new boolean[colorArray.length];
        color_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(EditFormActivity.this);
                builder.setTitle("Select color");
                builder.setCancelable(false);
                String colorString = color_view.getText().toString().trim();
                // set selected items
                for(int i = 0;i<selectedColor.length;i++){
                    if(colorString.contains(colorArray[i])){
                        selectedColor[i] = true;
                        //colorList.add(i);
                    }else{
                        selectedColor[i] = false;
                    }
                }
                builder.setMultiChoiceItems(colorArray, selectedColor, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            colorList.add(which);
                            Collections.sort(colorList);
                        }else{
                            colorList.remove(Integer.valueOf(which));
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int i = 0;i<colorList.size();i++){
                            stringBuilder.append(colorArray[colorList.get(i)]);
                            if(i != colorList.size()-1){
                                stringBuilder.append(",");
                            }
                        }
                        color = stringBuilder.toString();
                        color_view.setText(stringBuilder);
                    }

                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i =0;i<selectedColor.length;i++){
                            selectedColor[i] = false;
                            colorList.clear();
                            color_view.setText("");
                        }
                    }
                });
                builder.show();
            }
        });
        //get location and image
        //set image
        imageView = findViewById(R.id.dog_photo);
        //set Description
        description_view = findViewById(R.id.description);
        //upload to firebase
        //set the location
        location_btn = findViewById(R.id.location);
        //getLastKnownLocation();
        setInformation();
        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToMap();
            }
        });
        resetForm();
        publish_btn = findViewById(R.id.publish);
        progressDialog =  new ProgressDialog(EditFormActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference("Dog");
        databaseReference = FirebaseDatabase.getInstance().getReference("Dog");
        progressDialog = new ProgressDialog(EditFormActivity.this);
        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatabase();
            }
        });

    }

    private void updateDatabase() {
        progressDialog.setTitle("Uploading....");
        progressDialog.show();

        DatabaseReference updateReference = databaseReference.child(dog.getId());
        Map<String, Object> updates = new HashMap<String,Object>();
        updates.put("id",dog.getId());
        updates.put("behavior",behavior);
        updates.put("breed",breed_field.getText().toString());
        updates.put("color",color_view.getText().toString());
        updates.put("condition",condition);
        updates.put("description",description_view.getText().toString());
        updates.put("imageUrl",dog.getImageUrl());
        if(location != null){
            updates.put("location",location);
        }else{
            updates.put("location",dog.getLocation());
        }
        updates.put("size",size);
        updates.put("time",time);
        updates.put("type",dog.getType());
        updates.put("userId",dog.getUserId());
        updateReference.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                showToast("Uploading successfully!");
                navigate(PostListActivity.class);
                finish();
            }
        });
    }

    //set the chosen option for spinner
    private int getSelection(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return -1;
    }

    public void sendToMap(){
        Bundle data = new Bundle();
        String breed1 = breed_field.getText().toString().trim();
        data.putString("breed",breed1);
        data.putSerializable("dog1",dog);
        data.putString("condition",condition);
        data.putString("behavior",behavior);
        data.putString("size",size);
        String description = description_view.getText().toString().trim();
        data.putString("description",description);
        String color1 = color_view.getText().toString().trim();
        data.putString("color",color1);
        data.putString("location",dog.getLocation());
        Intent intent = new Intent(EditFormActivity.this, EditMapActivity.class);
        intent.putExtras(data);
        startActivity(intent);
        finish();
    }
    public void setInformation(){
        Intent data = getIntent();
        if(data.getSerializableExtra("dog") != null){
            dog =(Dog) data.getSerializableExtra("dog");
            Picasso.with(getApplicationContext()).load(dog.getImageUrl()).into(imageView);
            breed_field.setText(dog.getBreed());
            spinnerBody.setSelection(getSelection(spinnerBody,dog.getCondition()));
            spinnerSize.setSelection(getSelection(spinnerSize,dog.getSize()));
            spinnerBehavior.setSelection(getSelection(spinnerBehavior,dog.getBehavior()));
            color_view.setText(dog.getColor());
            description_view.setText(dog.getDescription());
            double lat = Double.parseDouble(dog.getLocation().split(" ")[0]);
            double lon = Double.parseDouble(dog.getLocation().split(" ")[1]);
            location_btn.setText("("+lat+","+lon+")");
        }
    }
    public void resetForm(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.get("breed") != null){
                String breed_string = bundle.getString("breed");
                if(breed_string.contains(":")){
                    breed_string = breed_string.split(":")[0];
                }else{
                    breed_string = bundle.getString("breed");
                }
                breed_field.setText(breed_string);
            }
            if(bundle.get("condition") != null){
                spinnerBody.setSelection(getSelection(spinnerBody,bundle.getString("condition")));
            }
            if(bundle.get("size") != null){
                spinnerSize.setSelection(getSelection(spinnerSize,bundle.getString("size")));
            }
            if(bundle.get("behavior") != null){
                spinnerBehavior.setSelection(getSelection(spinnerBehavior,bundle.getString("behavior")));
            }
            if(bundle.get("color") != null){
                color_view.setText(bundle.getString("color"));
            }
            if(bundle.get("description") != null){
                description_view.setText(bundle.getString("description"));

            }
            if(bundle.getSerializable("dog1") != null){
                dog = (Dog) bundle.getSerializable("dog1");
                Picasso.with(getApplicationContext()).load(dog.getImageUrl()).into(imageView);
            }
            if(bundle.get("location") != null){
                location_btn.setText(bundle.getString("location"));
                location_btn.setClickable(false);
            }
            if(bundle.get("latLocation") != null) {
                location = bundle.getString("latLocation");
            }
        }
    }
}