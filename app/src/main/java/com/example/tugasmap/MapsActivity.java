package com.example.tugasmap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationListener;

import java.text.DateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int PERMISSION_FINE_LOCATION = 69;
    private GoogleMap mMap;
//    private ActivityMapsBinding binding;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // UI Widgets
    private Boolean mRequestingLocationUpdates;
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mLastUpdateTimeTextView;
    private TextView mStatus;

    // Labels
    protected String mLatitudeLabel = "Lat: ";
    protected String mLongitudeLabel = "Long: ";
    protected String mLastUpdateTimeLabel = "Last update: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mRequestingLocationUpdates = false;

        // Locate UI widgets
        mStartUpdatesButton = findViewById(R.id.start_update_button);
        mStopUpdatesButton = findViewById(R.id.stop_update_button);
        mLatitudeTextView = findViewById(R.id.latitude_text);
        mLongitudeTextView = findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = findViewById(R.id.last_update_time_text);
        mStatus = findViewById(R.id.status);

//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        locationRequest = LocationRequest.create().setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save location
                updateUI(locationResult.getLastLocation());
            }
        };

        mStartUpdatesButton.setOnClickListener(startUpdate);
        mStopUpdatesButton.setOnClickListener(stopUpdate);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }
                else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private View.OnClickListener startUpdate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                setButtonsEnabledState();
                startLocationUpdates();
            }
        }
    };

    private View.OnClickListener stopUpdate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mRequestingLocationUpdates) {
                mRequestingLocationUpdates = false;
                setButtonsEnabledState();
                stopLocationUpdates();
            }
        }
    };

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

            // Permission has been granted, do the update
            // Error still occurred even after adding ACCESS_FINE_LOCATION permissions in AndroidManifest.xml (line 11)
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(@NonNull Location location) {
                    // Update here
                    updateUI(location);
                }
            });
        }
        else {
            // Permission not granted yet
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateUI(@NonNull Location location) {
        if(location == null) {
            Toast.makeText(this, "Cannot get location at the moment", Toast.LENGTH_SHORT).show();
        }
        else {
            LatLng myLoc =new LatLng(location.getLatitude(), location.getLongitude());

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(myLoc).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 15));
            setButtonsEnabledState();
            updateLocationUI(location);
        }
    }

    private void updateLocationUI(Location location) {
        mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel, location.getLatitude()));
        mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel, location.getLongitude()));
        mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel, DateFormat.getTimeInstance().format(new Date())));
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
            mStatus.setText("Tracking");

        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
            mStatus.setText("NOT Tracking");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateUI(location);
    }
}