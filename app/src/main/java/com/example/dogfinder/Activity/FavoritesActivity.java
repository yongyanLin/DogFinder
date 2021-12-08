package com.example.dogfinder.Activity;

import static com.example.dogfinder.Activity.IndexActivity.LOCATION_PERM_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dogfinder.Adapter.BehaviorAdapter;
import com.example.dogfinder.Adapter.BodyAdapter;
import com.example.dogfinder.Adapter.DogAdapter;
import com.example.dogfinder.Adapter.SizeAdapter;
import com.example.dogfinder.Adapter.TextAdapter;
import com.example.dogfinder.Entity.Favorites;
import com.example.dogfinder.Entity.Dog;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.DataUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FavoritesActivity extends BaseActivity {
    BottomNavigationView navigationView;
    RecyclerView recyclerView;
    SearchView searchView;
    DatabaseReference dogReference,favoriteReference;
    DogAdapter dogAdapter;
    List<Dog> dogList;
    FirebaseAuth auth;
    Button filter_btn,cancel_btn,clear_btn,done_btn;
    BodyAdapter bodyAdapter;
    SizeAdapter sizeAdapter;
    BehaviorAdapter behaviorAdapter;
    TextAdapter timeAdapter,locationAdapter;
    Spinner spinnerBody, spinnerBehavior,spinnerSize,spinnerLocation,spinnerTime;
    List<Integer> colorList,breedList;
    String[] colorArray,breedsArray;
    boolean[] selectedColor,selectedBreed;
    TextView breed_filed,color_field;
    String breed,body, behavior, color, size,location,time,currentTime;
    double latitude,longitude;
    private static final int GPS_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        auth = FirebaseAuth.getInstance();
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentTime = dateFormat.format(date);
        bodyAdapter = new BodyAdapter(FavoritesActivity.this, DataUtil.getBodyList());
        sizeAdapter = new SizeAdapter(FavoritesActivity.this,DataUtil.getSizeList());
        behaviorAdapter = new BehaviorAdapter(FavoritesActivity.this,DataUtil.getBehaviorList());
        timeAdapter = new TextAdapter(FavoritesActivity.this,DataUtil.getTimeOption());
        locationAdapter = new TextAdapter(FavoritesActivity.this, DataUtil.getLocationOption());
        LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_CODE);
            return;
        }
        Location clocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (clocation == null) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, GPS_REQUEST_CODE);
        }else{
            longitude = clocation.getLongitude();
            latitude = clocation.getLatitude();
        }
        //set bottom navigation
        navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setSelectedItemId(R.id.likes_btn);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_btn:
                        navigate(IndexActivity.class);
                        finish();
                        break;
                    case R.id.likes_btn:
                        navigate(FavoritesActivity.class);
                        finish();
                        break;
                    case R.id.stray_btn:
                        Intent intent1 = new Intent(getApplicationContext(),SquareActivity.class);
                        intent1.putExtra("type","stray");
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.lost_btn:
                        Intent intent2 = new Intent(getApplicationContext(),SquareActivity.class);
                        intent2.putExtra("type","lost");
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.profile_btn:
                        navigate(ProfileActivity.class);
                        finish();
                        break;
                }
                return false;
            }
        });
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogReference = FirebaseDatabase.getInstance().getReference("Dog");
        favoriteReference = FirebaseDatabase.getInstance().getReference("Favorites");
        dogList = new ArrayList<>();
        getData();
        searchView = findViewById(R.id.search);
        filter_btn = findViewById(R.id.filter_btn);
        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                dogAdapter.getFilter().filter("search "+ query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                dogAdapter.getFilter().filter("search "+newText);
                return false;
            }
        });
    }
    public void getData(){
        favoriteReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Favorites favorites = snapshot1.getValue(Favorites.class);
                    String userId = favorites.getUserId();
                    if(userId.equals(auth.getCurrentUser().getUid())){
                        dogList.add(favorites.getDog());
                    }
                }
                Collections.sort(dogList);
                dogAdapter = new DogAdapter(getApplicationContext(),dogList,latitude,longitude);
                recyclerView.setAdapter(dogAdapter);
                dogAdapter.notifyDataSetChanged();
                dogAdapter.SetOnItemClickListener(new DogAdapter.OnItemClickListener() {
                    @Override
                    public void onLinkClick(int position) {
                        Intent intent = new Intent(getApplicationContext(),DogDetailActivity.class);
                        Dog dog = dogList.get(position);
                        intent.putExtra("dog",dog);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCollectionClick(int position,boolean isChecked) {
                        String userId = auth.getCurrentUser().getUid();
                        String dogId = dogList.get(position).getId();
                        String id = userId+" "+dogId;
                        if(!isChecked){
                            favoriteReference.child(id).removeValue();
                            navigate(FavoritesActivity.class);
                            finish();
                        }else{
                            Favorites favorites = new Favorites(userId,dogList.get(position),currentTime);
                            favorites.setId(id);
                            favoriteReference.child(id).setValue(favorites);
                        }

                    }
                    @Override
                    public void onCommentClick(int position) {
                        Intent intent = new Intent(getApplicationContext(),CommentActivity.class);
                        Dog dog = dogList.get(position);
                        intent.putExtra("dog",dog);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onShowCommentClick(int position) {
                        Intent intent = new Intent(getApplicationContext(),CommentActivity.class);
                        Dog dog = dogList.get(position);
                        intent.putExtra("dog",dog);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onShareClick(int position) {
                        Dog dog = dogList.get(position);
                        String imageUri = dog.getImageUrl();
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    Bitmap bitmap = Picasso.with(getApplicationContext()).load(imageUri).get();
                                    String fileUrl = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, dog.getBreed(),dog.getId());
                                    Uri contentUri = Uri.parse(fileUrl);
                                    if (contentUri != null) {
                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                        shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void showFilterDialog(){
        ViewGroup viewGroup = findViewById(android.R.id.content);
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoritesActivity.this);
        builder.setCancelable(false);
        View view1 = LayoutInflater.from(FavoritesActivity.this).inflate(R.layout.filter_dialog,viewGroup,false);
        builder.setView(view1);
        spinnerBody = view1.findViewById(R.id.body_spinner);
        spinnerBehavior = view1.findViewById(R.id.location_spinner);
        spinnerTime = view1.findViewById(R.id.time_spinner);
        spinnerSize = view1.findViewById(R.id.size_spinner);
        spinnerBehavior = view1.findViewById(R.id.behavior_spinner);
        spinnerLocation = view1.findViewById(R.id.location_spinner);
        spinnerBody.setAdapter(bodyAdapter);
        spinnerBody.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                body = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                body = "body";
            }
        });
        spinnerBehavior.setAdapter(behaviorAdapter);
        spinnerBehavior.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                behavior = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                behavior = "behavior";
            }
        });
        spinnerSize.setAdapter(sizeAdapter);
        spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                size = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                size = "size";
            }
        });
        spinnerLocation.setAdapter(locationAdapter);
        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                location = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                location = "location";
            }
        });
        spinnerTime.setAdapter(timeAdapter);
        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                time = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                time = "time";
            }
        });
        breed_filed = view1.findViewById(R.id.breed);

        breedsArray = getResources().getStringArray(R.array.breeds);
        breedList = new ArrayList<>();
        selectedBreed = new boolean[breedsArray.length];
        breed_filed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderBreed = new AlertDialog.Builder(FavoritesActivity.this);
                builderBreed.setTitle("Select breed of your dog.");
                builderBreed.setCancelable(false);
                String breedString = breed_filed.getText().toString().trim();
                // set selected items
                for(int i = 0;i< selectedBreed.length;i++){
                    if(breedString.contains(breedsArray[i])){
                        selectedBreed[i] = true;
                        //breedList.add(i);
                    }else{
                        selectedBreed[i] = false;
                    }
                }
                builderBreed.setMultiChoiceItems(breedsArray, selectedBreed, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            breedList.add(which);
                            Collections.sort(breedList);
                        }else if(breedList.contains(which)){
                            breedList.remove(Integer.valueOf(which));
                        }
                    }
                });
                builderBreed.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int i = 0;i<breedList.size();i++){
                            stringBuilder.append(breedsArray[breedList.get(i)]);
                            if(i != breedList.size()-1){
                                stringBuilder.append("/");
                            }
                        }
                        breed = stringBuilder.toString();
                        breed_filed.setText(stringBuilder);
                    }

                });
                builderBreed.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        breed = "breed";
                        dialog.dismiss();
                    }
                });
                builderBreed.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i =0;i<selectedBreed.length;i++){
                            selectedBreed[i] = false;
                            breedList.clear();
                            breed = "breed";
                            breed_filed.setText("");
                        }
                    }
                });
                builderBreed.show();
            }

        });
        colorArray = DataUtil.getColorArray();
        color_field = view1.findViewById(R.id.color);
        colorList = new ArrayList<>();
        selectedColor = new boolean[colorArray.length];
        color_field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderColor = new AlertDialog.Builder(FavoritesActivity.this);
                builderColor.setTitle("Select color");
                builderColor.setCancelable(false);
                String colorString = color_field.getText().toString().trim();
                // set selected items
                for(int i = 0;i<selectedColor.length;i++){
                    if(colorString.contains(colorArray[i])){
                        selectedColor[i] = true;
                        //colorList.add(i);
                    }else{
                        selectedColor[i] = false;
                    }
                }
                builderColor.setMultiChoiceItems(colorArray, selectedColor, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            colorList.add(which);
                            Collections.sort(colorList);
                        }else if(colorList.contains(which)){
                            colorList.remove(Integer.valueOf(which));
                        }
                    }
                });
                builderColor.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int i = 0;i<colorList.size();i++){
                            stringBuilder.append(colorArray[colorList.get(i)]);
                            if(i != colorList.size()-1){
                                stringBuilder.append("/");
                            }
                        }
                        color = stringBuilder.toString();
                        color_field.setText(stringBuilder);
                    }

                });
                builderColor.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        color = "color";
                        dialog.dismiss();
                    }
                });
                builderColor.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i =0;i<selectedColor.length;i++){
                            selectedColor[i] = false;
                            colorList.clear();
                            color = "color";
                            color_field.setText("");
                        }
                    }
                });
                builderColor.show();
            }
        });
        cancel_btn = view1.findViewById(R.id.cancel_btn);
        clear_btn = view1.findViewById(R.id.clear_btn);
        done_btn = view1.findViewById(R.id.done_btn);
        AlertDialog alertDialog = builder.create();
        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = "filter"+","+location+","+time+","+breed+","+size+","+behavior+","+body+","+color;
                dogAdapter.getFilter().filter(query);
                alertDialog.dismiss();
            }
        });
        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i =0;i<selectedColor.length;i++){
                    selectedColor[i] = false;
                    colorList.clear();
                    color_field.setText("");
                }
                for(int i =0;i<selectedBreed.length;i++){
                    selectedBreed[i] = false;
                    breedList.clear();
                    breed_filed.setText("");
                }
                spinnerLocation.setSelection(0);
                spinnerBehavior.setSelection(0);
                spinnerBody.setSelection(0);
                spinnerSize.setSelection(0);
                spinnerTime.setSelection(0);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_CODE);
                    return;
                }
                Location clocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                longitude = clocation.getLongitude();
                latitude = clocation.getLatitude();
                showToast(longitude+"");
            }
        }
    }
}