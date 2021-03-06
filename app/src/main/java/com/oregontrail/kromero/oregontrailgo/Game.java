package com.oregontrail.kromero.oregontrailgo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Game extends AppCompatActivity {

    // stores the client and server updater information
    public LocationService locationService;

    private Player client;
    private ServerUpdater updater;
    private CustomDialog dialog;
    private ResponseDialog responseDialog;
    private GPSTracker gps;

    private BroadcastReceiver locationUpdateReceiver;
    private BroadcastReceiver responseReceiver;

    private String serverResponse;

    final Handler handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        dialog = null;
        serverResponse = "";

        gps = new GPSTracker(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            client = new Player(extras.getString("id"), this);
            //The key argument here must match that used in the other activity
            client.setLon(gps.getLongitude());
            client.setLat(gps.getLatitude());
        }

        TextView tv = (TextView) findViewById(R.id.user);
        Typeface tf = Typeface.createFromAsset(tv.getContext().getAssets(), "fonts/font.ttf");
        tv.setTypeface(tf);
        tv.setText(client.getId());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        final Intent locationService = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(locationService);
        this.getApplication().bindService(locationService, serviceConnection, Context.BIND_AUTO_CREATE);

        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location newLocation = intent.getParcelableExtra("location");
                client.setLat(gps.getLatitude());
                client.setLon(gps.getLongitude());
                Log.i("LOCATION", "Updated client-side--Lat:" + client.getLat() + "---Lon:" + client.getLon());
            }
        };

        responseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                serverResponse = intent.getExtras().getString("response");

                android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("responseDialog");
                if (prev == null) {
                    openResponseDialog();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationUpdateReceiver,
                new IntentFilter("LocationUpdated"));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                responseReceiver,
                new IntentFilter("ResponseUpdated"));

        updater = new ServerUpdater(this, client, gps);
        updater.start();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();

            if (name.endsWith("LocationService")) {
                locationService = ((LocationService.LocationServiceBinder) service).getService();
                locationService.startUpdatingLocation();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("LocationService")) {
                locationService.stopUpdatingLocation();
                locationService = null;
            }
        }
    };

    public String getPromptMessage () {
        // construct the message that we want to prompt client with
        String eventMessage = "";

        // the event happened to us so add that to the message
        if (client.getEventRecipient().equals(client.getId())) {
            eventMessage += "You have ";
            eventMessage += client.getEventLine();
            eventMessage += client.getEventPrompt();
        } else {
            // event happened to someone else so do that
            eventMessage += client.getEventRecipient();
            eventMessage += " has ";
            eventMessage += client.getEventLine();
            eventMessage += client.getEventPrompt();
        }
        return  eventMessage;
    }

    public void handleEvent () {
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        android.support.v4.app.Fragment response = getSupportFragmentManager().findFragmentByTag("responseDialog");
        if (prev == null && response == null) {
            openDialog();
        }
    }

    public void openDialog() {
        dialog = new CustomDialog(this, client);
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    public void openResponseDialog() {
        responseDialog = new ResponseDialog(this, client, serverResponse);
        responseDialog.show(getSupportFragmentManager(), "responseDialog");
    }

    public void renderUI () {
        TextView food = (TextView) findViewById(R.id.food);
        TextView water = (TextView) findViewById(R.id.water);
        TextView supplies = (TextView) findViewById(R.id.supplies);
        TextView bullets = (TextView) findViewById(R.id.bullets);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);


        food.setText(Integer.toString(client.getFood()));
        water.setText(Integer.toString(client.getWater()));
        supplies.setText(Integer.toString(client.getSupplies()));
        bullets.setText(Integer.toString(client.getBullets()));

        progressBar.setProgress(Math.round(client.getPercentComplete()));

        Log.i("PROGRESS", Integer.toString(client.getPercentComplete()));

        if (client.isAlive() == false && client.getEventId() == -1) {
            Intent intent = new Intent(this, LoseScreen.class);
            startActivity(intent);
        }

        if (client.getPercentComplete() >= 100.0 && client.getEventId() == -1) {
            Intent intent = new Intent(this, WinScreen.class);
            startActivity(intent);
        }
    }
}
