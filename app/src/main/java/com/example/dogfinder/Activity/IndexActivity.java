package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;


import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.lostPopUpUtil;
import com.example.dogfinder.Utils.strayPopUpUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IndexActivity extends BaseActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    LinearLayout strayPostBtn,straySquareBtn,lostPostBtn,lostSquareBtn,adoptBtn;
    String photoPath;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int WRITE_PERM_CODE = 1;
    public static final int GALLERY_STRAY_REQUEST_CODE = 105;
    public static final int GALLERY_LOST_REQUEST_CODE = 106;
    public static final int LOCATION_PERM_CODE = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        auth = FirebaseAuth.getInstance();

        strayPostBtn = findViewById(R.id.strayPostbtn);
        straySquareBtn = findViewById(R.id.straySquarebtn);
        lostPostBtn = findViewById(R.id.lostPostbtn);
        lostSquareBtn = findViewById(R.id.lostSquarebtn);
        adoptBtn = findViewById(R.id.adoptSquarebtn);
        strayPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popStrayFormBottom();
            }
        });
        lostPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popLostFormBottom();
            }
        });
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openNavigation,R.string.closeNavigation);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        navigate(IndexActivity.class);
                        break;
                    case R.id.nav_profile:
                        navigate(ProfileActivity.class);
                        break;
                    case R.id.logout:
                        auth.signOut();
                        navigate(MainActivity.class);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }
    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }
    public void popStrayFormBottom() {
        strayPopUpUtil popup = new strayPopUpUtil(this, onClickListener);
        //showAtLocation(View parent, int gravity, int x, int y)
        popup.showAtLocation(findViewById(R.id.drawerLayout), Gravity.BOTTOM|Gravity.CENTER, 0, 0);
    }
    public void popLostFormBottom() {
        lostPopUpUtil popup = new lostPopUpUtil(this, onClickListener);
        //showAtLocation(View parent, int gravity, int x, int y)
        popup.showAtLocation(findViewById(R.id.drawerLayout), Gravity.BOTTOM|Gravity.CENTER, 0, 0);
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera_btn:
                    askCameraPermission();
                    break;
                case R.id.gallery_btn:
                    askStrayGalleryPermission();
                    break;
                case R.id.lost_gallery_btn:
                    askLostGalleryPermission();
            }
        }
    };

    private void askStrayGalleryPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERM_CODE);
        }else{
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent,GALLERY_STRAY_REQUEST_CODE);
        }
    }
    private void askLostGalleryPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERM_CODE);
        }else{
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent,GALLERY_LOST_REQUEST_CODE);
        }
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERM_CODE);
            }
            getCameraIntent();
        }else{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERM_CODE);
            }
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[]permissions,@NonNull int[] grantResults){
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCameraIntent();
            }else{
                showToast("Camera permission is required.");
            }
        }
        if(requestCode == LOCATION_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else {
                showToast("Please allow us to tag the location.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(photoPath);
                Intent intent = new Intent(IndexActivity.this, StrayFormActivity.class);
                //send image to form
                intent.putExtra("cameraImage",Uri.fromFile(f));
                //save image into gallery
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERM_CODE);
                }
                //go to form
                startActivity(intent);
            }
        }
        if(requestCode == GALLERY_STRAY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                Intent intent = new Intent(IndexActivity.this, StrayFormActivity.class);
                intent.putExtra("galleryImage",contentUri);
                startActivity(intent);
            }
        }
        if(requestCode == GALLERY_LOST_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                Intent intent = new Intent(IndexActivity.this, LostFormActivity.class);
                intent.putExtra("galleryImage",contentUri);
                startActivity(intent);
            }
        }
    }
    private File createImageUri() throws IOException{
        String time = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String fileName = time+"_pic";
        File storeDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storeDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName,".jpg",storeDir);
        photoPath = image.getAbsolutePath();
        return image;
    }
    private void getCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageUri();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }

    }
}