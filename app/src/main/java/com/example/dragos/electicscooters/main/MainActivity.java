package com.example.dragos.electicscooters.main;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.dragos.electicscooters.QRCodeActivity;
import com.example.dragos.electicscooters.R;
import com.example.dragos.electicscooters.main.scooterdetails.ScooterDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ScooterDetails.ScooterDetailsListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;
    Marker userMarker;
    ArrayList<Pair<Double, Double>> scooterCoordinates = new ArrayList<>();
    Button scanBtn;

    //DATABASE REFFERENCE


    /**
     * handles permission response
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                currentLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }

    /**
     * checks/asks for location permission
     */
    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            currentLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    /**
     * moves maps camera to current user position
     */
    void updateMapView(double lat, double lng){
        LatLng updatedPos = new LatLng(lat,lng);
        userMarker.setPosition(updatedPos);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(updatedPos,16.0f));
    }


    /**
     * until server data is available, populate with dummy data
     */
    void populateScooterCoords(){
        scooterCoordinates.add(new Pair<Double, Double>(46.7704, 23.5934));
        scooterCoordinates.add(new Pair<Double, Double>(46.7709,23.5958));
        scooterCoordinates.add(new Pair<Double, Double>(46.7732, 23.5901));
        scooterCoordinates.add(new Pair<Double, Double>(46.835,23.62));
        scooterCoordinates.add(new Pair<Double, Double>(46.7532, 23.56));
        scooterCoordinates.add(new Pair<Double, Double>(46.79, 23.58));
    }


    /**
     * initializes scooter markers on map
     */
    void initScooterMarkers(){

        populateScooterCoords();
        //TODO: fetch coordinates from server

        for(Pair<Double, Double> scooter:scooterCoordinates){
            //TODO: clear map
            double latitude=scooter.first, longitude=scooter.second;
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }


    /**
     * connect to google maps, initialize location manager and listener
     */
    void setUpMap(){
        //get map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation.setLatitude(location.getLatitude());
                currentLocation.setLongitude(location.getLongitude());
                updateMapView(currentLocation.getLatitude(), currentLocation.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        checkLocationPermission();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(getIntent().getStringExtra("qrResult") != null){
//            showQrDialog(getIntent().getStringExtra("qrResult"));
//        }

        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        setUpMap();

        scanBtn=findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScanPressed();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.toggle);

        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
////                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        toggle.setDrawerIndicatorEnabled(false);
//
////        drawer.addDrawerListener(toggle);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.START);
            }
        });
//        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    /**
     * Function to show qr Dialog
     */
    private void showQrDialog(String rawResult) {
        final String result = rawResult;
        Log.d("QRCodeScanner", rawResult);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ride Options");
        builder.setPositiveButton("Start right away!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Implement start right away
            }
        });
        builder.setNeutralButton("Choose one of our options!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Implement choose one of our options
            }
        });
        builder.setMessage("Scooter number: " + rawResult);
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    /**
     * closes menu drawer
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * inflates right-side menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * set listeners and init the map after it's loaded
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a userMarker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        if(currentLocation==null){
//            currentLocation=new Location(LocationManager.GPS_PROVIDER);
//            currentLocation.setLongitude(46.7712);
//            currentLocation.setLatitude(23.6236);
//            updateMapView(23.6236, 46.7712);
//        }
//        userMarker =mMap.addMarker(new MarkerOptions().position(new LatLng(23.6236, 46.7712)));
//        mMap.setOnMarkerClickListener(MainActivity.this);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(23.6236, 46.7712)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
//        initScooterMarkers();

//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    /**
     * open up scooter details modal
     */
    private void showScooterDetails() {
//        FragmentManager fm = getSupportFragmentManager();
//        ScooterDetails scooterDetails=ScooterDetails.newInstance("sth");
//
//        scooterDetails.show(fm,"tag");
        Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.scooter_details_window);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity=Gravity.TOP;
        params.y=100;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }


    /**
     * handle click on scooter marker
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        //TODO: center map on marker
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        //ignore user marker
        if(marker.getId().equals("m0")){
            return false;
        }
        showScooterDetails();
        return true;
    }

    @Override
    public void onCallPressed() {

    }

    @Override
    public void onScanPressed() {

//        databaseReference = FirebaseDatabase.getInstance().getReference();
//        /**
//         * WRITING TO FIREBASE
//         */
//        System.out.println("========================================");
//        com.example.dragos.electicscooters.main.domain.Location loc = new com.example.dragos.electicscooters.main.domain.Location(1251, 412.61241, 511.12312);
//        databaseReference.child("locations").child(loc.getId().toString()).setValue(loc);


        //TODO: scan QR code
        Intent intent=new Intent(getApplicationContext(), QRCodeActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1){
//            Toast.makeText(getApplicationContext(), data.getStringExtra("result"), Toast.LENGTH_LONG).show();
//            Intent intent=new Intent(getApplicationContext(), RideOptionsActivity.class);
//            startActivity(intent);
            showQrDialog(data.getStringExtra("qrResult"));
        }
    }
}
