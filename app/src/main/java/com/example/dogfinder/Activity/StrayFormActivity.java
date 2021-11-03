package com.example.dogfinder.Activity;


import static com.example.dogfinder.Activity.IndexActivity.LOCATION_PERM_CODE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.example.dogfinder.Adapter.BehaviorAdapter;
import com.example.dogfinder.Adapter.BodyAdapter;
import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.Entity.StrayDog;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StrayFormActivity extends BaseActivity {
    FirebaseAuth auth;
    Spinner spinnerBody, spinnerBehavior, spinnerColor;
    BodyAdapter bodyAdapter;
    BehaviorAdapter behaviorAdapter;

    Button back_btn, publish_btn;
    String condition, behavior, color, breed, location,cLocation,userID;
    List<Body> bodyList;
    List<Behavior> behaviorList;
    List<Integer> colorList;
    String[] colorArray;
    boolean[] selectedColor;
    EditText breed_filed,description_view;
    ImageView imageView;
    TextView location_btn,color_view;
    Uri image;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stray_form);


        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        location = null;
        back_btn = findViewById(R.id.back_index);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(IndexActivity.class);
            }
        });
        //breed
        breed_filed = findViewById(R.id.breed);
        breed = breed_filed.getText().toString().trim();
        //Body
        spinnerBody = findViewById(R.id.body_spinner);
        bodyAdapter = new BodyAdapter(StrayFormActivity.this, DataUtil.getBodyList());
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
        behaviorAdapter = new BehaviorAdapter(StrayFormActivity.this, DataUtil.getBehaviorList());
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
        //Color
        colorArray = DataUtil.getColorArray();
        color_view = findViewById(R.id.color);
        colorList = new ArrayList<>();
        selectedColor = new boolean[colorArray.length];
        color_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(StrayFormActivity.this);
                builder.setTitle("Select color");
                builder.setCancelable(false);
                String colorString = color_view.getText().toString().trim();
                // set selected items
                for(int i = 0;i<selectedColor.length;i++){
                    if(colorString.contains(colorArray[i])){
                        selectedColor[i] = true;
                        colorList.add(i);
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
                            colorList.remove(which);
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
        setCurrentLocation();
        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToMap();
            }
        });
        resetForm();
        publish_btn = findViewById(R.id.publish);
        progressDialog =  new ProgressDialog(StrayFormActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference("strayDog");
        databaseReference = FirebaseDatabase.getInstance().getReference("strayDog");
        progressDialog = new ProgressDialog(StrayFormActivity.this);
        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToDatabase();
            }
        });


    }
    public String getExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    public void uploadToDatabase(){
        if(image != null){
            progressDialog.setTitle("Uploading....");
            progressDialog.show();
            StorageReference storageReference1 = storageReference.child(System.currentTimeMillis()+"."+getExtension(image));
            storageReference1.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    showToast("Uploading successfully!");
                    String breed1 = breed_filed.getText().toString().trim();
                    String condition1 = spinnerBody.getSelectedItem().toString();
                    String behavior1 = spinnerBehavior.getSelectedItem().toString();
                    String color1 = color_view.getText().toString().trim();
                    String description1 = description_view.getText().toString().trim();
                    StrayDog strayDog = new StrayDog(userID,breed1,condition1,behavior1,color1,taskSnapshot.getUploadSessionUri().toString(),
                            location,description1);
                    String uploadId = databaseReference.push().getKey();
                    databaseReference.child(uploadId).setValue(strayDog).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            navigate(IndexActivity.class);
                        }
                    });

                }
            });

        }else{
            showToast("please select the picture.");
        }

    }


    //save image to gallery
    public void saveToGallery(Bitmap bitmap) {
        OutputStream os;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Img_" + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                os = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                Objects.requireNonNull(os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void setCurrentLocation() {
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);;
        List<Address> addresses = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_CODE);

            return;
        }
        Location clocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (clocation == null) {
            return;
        }
        double longitude = clocation.getLongitude();
        double latitude =  clocation.getLatitude();
        Geocoder geocoder = new Geocoder(StrayFormActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                if (address.getPostalCode() == null) {
                    cLocation = "(" + address.getLatitude() + "," + address.getLongitude() + ")";
                } else {
                    cLocation = address.getPostalCode();
                }
                location = address.getLatitude()+" "+address.getLongitude();
                location_btn.setText(cLocation);
            } else {
            }
        } catch (IOException e) {
            System.out.println(e);
        }
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
        data.putString("breed",breed);
        data.putString("condition",condition);
        data.putString("behavior",behavior);
        String description = description_view.getText().toString().trim();
        data.putString("description",description);
        String color1 = color_view.getText().toString().trim();
        data.putString("color",color1);
        data.putString("image",image.toString());
        Intent intent = new Intent(StrayFormActivity.this, StrayMapActivity.class);
        intent.putExtras(data);
        startActivity(intent);
    }
    public void resetForm(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if (bundle.get("cameraImage") != null) {
                image = (Uri) bundle.get("cameraImage");
                imageView.setImageURI(image);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                saveToGallery(bitmap);
            }
            if (bundle.get("galleryImage") != null) {
                image = (Uri) bundle.get("galleryImage");
                imageView.setImageURI(image);
            }
            if(bundle.get("breed") != null){
                breed_filed.setText(bundle.getString("breed"));
            }
            if(bundle.get("condition") != null){
                spinnerBody.setSelection(getSelection(spinnerBody,bundle.getString("condition")));
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
            if(bundle.get("image") != null){
                image = Uri.parse(bundle.getString("image"));
                imageView.setImageURI(Uri.parse(bundle.getString("image")));
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