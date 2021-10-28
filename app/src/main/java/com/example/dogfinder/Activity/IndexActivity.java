package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;


import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class IndexActivity extends BaseActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        auth = FirebaseAuth.getInstance();

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

}