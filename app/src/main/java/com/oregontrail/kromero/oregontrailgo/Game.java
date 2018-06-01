package com.oregontrail.kromero.oregontrailgo;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class Game extends AppCompatActivity {

    private Player client;
    private ServerUpdater updater;
    private CustomDialog dialog;

    public LocationService locationService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        dialog = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            client = new Player(extras.getString("id"));
            //The key argument here must match that used in the other activity
        }

        TextView tv = (TextView) findViewById(R.id.user);
        Typeface tf = Typeface.createFromAsset(tv.getContext().getAssets(), "fonts/font.ttf");
        tv.setTypeface(tf);
        tv.setText(client.getId());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        final Intent serviceStart = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(serviceStart);
        this.getApplication().bindService(serviceStart,serviceConnection, Context.BIND_AUTO_CREATE);

        updater = new ServerUpdater(this, client);
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
        if (prev == null) {
            openDialog();
        }
    }

    public void openDialog() {
        dialog = new CustomDialog(this, client);
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    public void renderUI () {
        TextView food = (TextView) findViewById(R.id.food);
        TextView water = (TextView) findViewById(R.id.water);
        TextView supplies = (TextView) findViewById(R.id.supplies);
        TextView bullets = (TextView) findViewById(R.id.bullets);

        food.setText(Integer.toString(client.getFood()));
        water.setText(Integer.toString(client.getWater()));
        supplies.setText(Integer.toString(client.getSupplies()));
        bullets.setText(Integer.toString(client.getBullets()));
    }
}
