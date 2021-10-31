package com.example.dogfinder.Activity;


import static com.example.dogfinder.Activity.IndexActivity.LOCATION_PERM_CODE;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.dogfinder.Adapter.BehaviorAdapter;
import com.example.dogfinder.Adapter.BodyAdapter;
import com.example.dogfinder.Adapter.ColorAdapter;
import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class PostFormActivity extends BaseActivity {
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    Spinner spinnerBody, spinnerBehavior, spinnerColor;
    BodyAdapter bodyAdapter;
    BehaviorAdapter behaviorAdapter;
    ColorAdapter colorAdapter;
    Button back_btn, publish_btn;
    String condition, behavior, color, breed, location, description;
    List<Body> bodyList;
    List<Behavior> behaviorList;
    List<String> colorList;
    LinearLayout otherColor;
    EditText mixColor,description_view;
    ImageView imageView;
    TextView location_btn;
    Uri image;
    String cLocation;
    LocationManager mLocationManager;


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
        behaviorAdapter = new BehaviorAdapter(PostFormActivity.this, DataUtil.getBehaviorList());
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
                color = (String) parent.getItemAtPosition(position);
                if (color.equals("Color")) {
                    color = "Unknown";
                }
                if (color.equals("Other")) {
                    otherColor.setVisibility(View.VISIBLE);
                } else {
                    otherColor.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                color = "Unknown";
            }
        });
        if (otherColor.getVisibility() == View.VISIBLE) {
            String colorString = mixColor.getText().toString().trim();
            if (!colorString.equals("") || colorString.contains(";")) {
                String colors[] = colorString.split(";");
                for (int i = 0; i < colors.length; i++) {
                    color += colors[i] + " ";
                }
            } else if (colorString.equals("")) {
                color = "Unknown";
            } else {
                showToast("Please input colors as required format.");
            }
        }

        //set the location

        location_btn = findViewById(R.id.location);
        //getLastKnownLocation();
        setCurrentLocation();
        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostFormActivity.this, MapActivity.class);
                intent.putExtra("image", image);
                startActivity(intent);
            }
        });

        //get location and image
        //set image
        imageView = findViewById(R.id.dog_photo);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.get("cameraImage") != null) {
                image = (Uri) extras.get("cameraImage");
                imageView.setImageURI(image);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                saveToGallery(bitmap);
            }
            if (extras.get("galleryImage") != null) {
                image = (Uri) extras.get("galleryImage");
                imageView.setImageURI(image);
            }
            if (extras.get("image") != null) {
                String imageString = (String) extras.get("image");
                imageView.setImageURI(Uri.parse(imageString));
                String locationString = (String) extras.get("location");
                if (locationString != null) {
                    location_btn.setText(locationString);
                }
                location = (String) extras.get("latLocation");
            }
        }
        //set Description
        description_view = findViewById(R.id.description);
        description = description_view.getText().toString().trim();
        //upload to firebase
        publish_btn = findViewById(R.id.publish);
        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
            //showToast("Here");
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            showToast("null");
            return;
        }
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Geocoder geocoder = new Geocoder(PostFormActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                if (address.getPostalCode() == null) {
                    cLocation = "(" + address.getLatitude() + "," + address.getLongitude() + ")";
                } else {
                    cLocation = address.getPostalCode();
                }
                location_btn.setText(cLocation);
            } else {
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}