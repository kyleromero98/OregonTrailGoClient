package com.oregontrail.kromero.oregontrailgo;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Greg on 5/20/2018.
 */

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    //Minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2;

    //Minimmum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1;

    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        Log.d("dave", "gps created");
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            //Log.d("dave", "Literally not in an if statement");

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.d("dave", "no network provider");
            } else {
                //Log.d("dave", "yes network provider");
                this.canGetLocation = true;
                // First get location from Network Provider

                if (isNetworkEnabled) {
                    //Log.d("dave", "isNetworkEnabled");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    //Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }

                    Log.d("dave", " Lat: " + latitude + ", Long: " + longitude);
                } else {
                    Log.d("dave", "Didn't have permission");
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    //if (location == null) {//
                        //Log.d("dave", "GPS works");
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                //Log.d("dave", "Location is not null");
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    //}//
                } else {
                    Log.d("dave", "GPS not enabled");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;

    }

    public double getLatitude() {

        if (location != null) {
            Log.i("LOCATION", "Accuracy = " + location.getAccuracy());
            if (location.getAccuracy() >= 10.0) {
                latitude = location.getLatitude();
            }
        }
        return latitude;
    }

    public double getLongitude() {

        if (location != null) {
            if (location.getAccuracy() >= 10.0) {
                longitude = location.getLongitude();
            }
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    //Show an alert asking to turn on GPS
    public void showSettingsAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Go to settings?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}



