package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;


import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.PopUpUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class IndexActivity extends BaseActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    LinearLayout strayPostBtn,straySquareBtn,lostPostBtn,lostSquareBtn,adoptBtn;

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
                showPopFormBottom();
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
    public void showPopFormBottom() {
        PopUpUtil popup = new PopUpUtil(this, onClickListener);

        //showAtLocation(View parent, int gravity, int x, int y)
        popup.showAtLocation(findViewById(R.id.drawerLayout), Gravity.BOTTOM|Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera_btn:
                    navigate(PostFormActivity.class);
                    break;
                case R.id.gallery_btn:
                    showToast("Gallery");
                    break;
            }
        }
    };

}