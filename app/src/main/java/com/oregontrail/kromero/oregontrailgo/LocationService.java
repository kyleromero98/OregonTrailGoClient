package com.oregontrail.kromero.oregontrailgo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class LocationService extends Service implements LocationListener, GpsStatus.Listener{

    public static double lat;
    public static double lon;

    private final LocationServiceBinder binder = new LocationServiceBinder();

    public LocationService() {
        lat = 0;
        lon = 0;
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    public void startUpdatingLocation() {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(false);

        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        Integer gpsFreqInMillis = 1000;
        Integer gpsFreqInDistance = 1;  // in meters

        locationManager.addGpsStatusListener(this);

        locationManager.requestLocationUpdates(gpsFreqInMillis, gpsFreqInDistance, criteria, this, null);
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
        Log.i("LAT", Double.toString(lat));
        Log.i("LON", Double.toString(lon));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public static double getLat () {
        return lat;
    }

    public static double getLon () {
        return lon;
    }
}
