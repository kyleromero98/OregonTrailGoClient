package com.oregontrail.kromero.oregontrailgo;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerUpdater extends Thread {
    Globals addr = Globals.getInstance();
    private final String updateAddress = addr.getData() + "checkin";
    //private final String updateAddress = "http://149.142.227.140:8080/client/";

    private Game game;
    private Player client;
    private GPSTracker tracker;

    public ServerUpdater (Game game, Player client, GPSTracker tracker) {
        this.client = client;
        this.tracker = tracker;
        this.game = game;
    }

    public String getServerUpdate() {
        try {
            URL url = new URL(updateAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            String update = getPlayerDataAsJson();

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(update);

            os.flush();
            os.close();

            String status = String.valueOf(conn.getResponseCode());
            Log.i("STATUS", status);
            String data = getStringFromInputStream(conn.getInputStream());
            Log.i("DATA" , data);

            if (conn.getResponseCode() == 200) {
                Log.i("RESPONSE", "Successfully checked in client");
                client.updatePlayerData(data);
            } else {
                Log.i("RESPONSE", "Unsuccessfully checked in client");
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    private String getPlayerDataAsJson () {
        try {
            JSONObject request = new JSONObject();
            request.put("id", client.getId());

            JSONObject location = new JSONObject();
            location.put("lat", LocationService.getLat());
            location.put("lon", LocationService.getLon());

            request.put("location", location);

            return request.toString();
        } catch (Exception e) {
            Log.d("KMR", "Can't format your JSON");
        }
        return null;
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }



    @Override
    public void run() {
        while (true) {
            if (client.getEventId() == -1) {
                getServerUpdate();
            } else {
                game.handleEvent();
            }
            game.renderUI();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                break;
            }
            if (!client.isAlive()) {
                break;
            }
        }

        // client is dead
        while (true) {
            getServerUpdate();
            game.renderUI();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
