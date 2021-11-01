package com.example.dogfinder.Activity;

import static com.example.dogfinder.Activity.IndexActivity.LOCATION_PERM_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.dogfinder.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;
    Button confirm_btn;
    public static final int zoom = 16;

    String location,latLocation;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        location = null;
        latLocation = null;
        geocoder = new Geocoder(this);
        searchView = findViewById(R.id.search);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchLocation = searchView.getQuery().toString().trim();
                List<Address> addressList = null;
                if (searchLocation != null || !searchLocation.equals("")) {
                    try {
                        addressList = geocoder.getFromLocationName(searchLocation, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(addressList != null && addressList.size()>0){
                        Address address = addressList.get(0);
                        if(address.getPostalCode() == null){
                            location = "(" + address.getLatitude() + "," + address.getLongitude() + ")";
                        }else{
                            location = address.getPostalCode();
                        }
                        latLocation = address.getLatitude()+" "+address.getLongitude();
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        map.clear();
                        map.addMarker(new MarkerOptions().position(latLng).title(location).draggable(true));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                    }else{
                        showToast("No location found.");
                    }
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
        confirm_btn = findViewById(R.id.confirm);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBackData();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERM_CODE);
            return;
        }
        map.setMyLocationEnabled(true);
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setCurrentLocation();
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                LatLng latLng = marker.getPosition();

                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(addresses.size() > 0){
                        Address address = addresses.get(0);
                        if(address.getPostalCode() == null){
                            location = "(" + address.getLatitude() + "," + address.getLongitude() + ")";
                        }else{
                            location = address.getPostalCode();
                        }
                        latLocation = address.getLatitude()+" "+address.getLongitude();
                        showToast(latLocation);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng).title(location).draggable(true));
            }
        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(addresses.size() > 0){
                        Address address = addresses.get(0);
                        if(address.getPostalCode() == null){
                            location = "(" + address.getLatitude() + "," + address.getLongitude() + ")";
                        }else{
                            location = address.getPostalCode();
                        }

                        latLocation = address.getLatitude()+" "+address.getLongitude();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showToast(latLocation);
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng).title(location).draggable(true));
            }
        });
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
        Location cLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (cLocation == null) {
            showToast("null");
            return;
        }
        double longitude = cLocation.getLongitude();
        double latitude = cLocation.getLatitude();
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                if (address.getPostalCode() == null) {
                    location = "(" + address.getLatitude() + "," + address.getLongitude() + ")";
                } else {
                    location = address.getPostalCode();
                }
                latLocation = address.getLatitude()+" "+address.getLongitude();
                LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(location).title(location).draggable(true));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            } else {
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public void sendBackData(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            Bundle bundle1 = new Bundle();
            Intent intent = new Intent(MapActivity.this,PostFormActivity.class);
            bundle1.putString("breed",bundle.getString("breed"));
            bundle1.putString("condition",bundle.getString("condition"));
            bundle1.putString("behavior",bundle.getString("behavior"));
            bundle1.putString("color",bundle.getString("color"));
            bundle1.putString("description",bundle.getString("description"));
            bundle1.putString("image",bundle.getString("image"));
            bundle1.putString("location",location);
            bundle1.putString("latLocation",latLocation);
            intent.putExtras(bundle1);
            startActivity(intent);
        }else{

        }
    }


}