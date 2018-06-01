package com.oregontrail.kromero.oregontrailgo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocationService extends Service implements LocationListener, GpsStatus.Listener{


    private final LocationServiceBinder binder = new LocationServiceBinder();
    boolean isUpdatingLocation;

    // just a placeholder constructor
    public LocationService() {

    }

    @Override
    public void onCreate() {
        isUpdatingLocation = false;
    }

    @SuppressLint("MissingPermission")
    public void startUpdatingLocation() {
        if (this.isUpdatingLocation) {
            isUpdatingLocation = false;

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            try {
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
            } catch (IllegalArgumentException e) {
                Log.e("LOCATION", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (SecurityException e) {
                Log.e("LOCATION", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (RuntimeException e) {
                Log.e("LOCATION", e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public void stopUpdatingLocation(){
        if(this.isUpdatingLocation == true){
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.removeUpdates(this);
            isUpdatingLocation = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("LOCATION", "onRebind ");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("LOCATION", "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d("LOCATION", "onDestroy ");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("LOCATION", "onTaskRemoved ");
        this.stopUpdatingLocation();

        stopSelf();
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(false);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(true);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if (status == LocationProvider.OUT_OF_SERVICE) {
                notifyLocationProviderStatusUpdated(false);
            } else {
                notifyLocationProviderStatusUpdated(true);
            }
        }
    }

    private void notifyLocationProviderStatusUpdated(boolean isLocationProviderAvailable) {
        //Broadcast location provider status change here
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLocationChanged(Location location) {

        // this doesn't work for now
        /*
        if(location.getAccuracy() <= 0){
            Log.d("LOCATION", "Latitidue and longitude values are invalid.");
            return;
        }

        //setAccuracy(newLocation.getAccuracy());
        float horizontalAccuracy = location.getAccuracy();
        if(horizontalAccuracy > 10){ //10meter filter
            Log.d("LOCATION", "Accuracy is too low.");
            return;
        }*/

        Intent intent = new Intent("LocationUpdated");
        intent.putExtra("location", location);

        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(intent);
    }
}
