package com.oregontrail.kromero.oregontrailgo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//private final String regAddress = "http://10.0.2.2:8080/register";
//private final String startAddress = "http://10.0.2.2:8080/start";

public class SetupGame extends Activity {
    Globals addr = Globals.getInstance();
    //addr.setData("");
    //cade's computer
    private final String regAddress = addr.getData() + "register";
    private final String startAddress = addr.getData() + "start";

    private String clientId;
    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setupgame);

        TextView tv = (TextView) findViewById(R.id.name);
        TextView tv1 = (TextView) findViewById(R.id.connect);
        TextView tv2 = (TextView) findViewById(R.id.nameInput);
        TextView tv3 = (TextView) findViewById(R.id.start);
        Typeface tf = Typeface.createFromAsset(tv.getContext().getAssets(), "fonts/font.ttf");
        tv.setTypeface(tf);
        tv1.setTypeface(tf);
        tv2.setTypeface(tf);
        tv3.setTypeface(tf);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        Button button = (Button) findViewById(R.id.start);
        button.setEnabled(false);
    }

    @SuppressLint("StaticFieldLeak")
    public void onClick(View view) throws JSONException {
        if (view.getId() == R.id.connect) {
            EditText nameInput = (EditText) findViewById(R.id.nameInput);
            clientId = nameInput.getText().toString();

            if (clientId.equals("")) {
                Log.i("ERROR", "Bad client name");
            } else {
                gps = new GPSTracker(this);

                final String request = getPlayerDataAsJson();

                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... voids) {
                        return registerClient(request);
                    }

                    @Override
                    protected void onPostExecute(String s) {

                    }
                }.execute();
            }
        } else if (view.getId() == R.id.start) {
            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... voids) {
                    return startGame();
                }

                @Override
                protected void onPostExecute(String s) {

                }
            }.execute();
        }
    }

    private String getPlayerDataAsJson () {
        try {
            JSONObject request = new JSONObject();
            request.put("id", clientId);

            JSONObject location = new JSONObject();
            location.put("lat", gps.getLatitude());
            location.put("lon", gps.getLongitude());

            request.put("location", location);
            Log.d("CADE", request.toString());
            return request.toString();
        } catch (Exception e) {
            Log.d("KMR", "Can't format your JSON");
        }
        return null;
    }

    public String registerClient(String request) {
        try {
            String ra = addr.getData() + "register";//"http://149.142.227.146:8080/register";
            Log.d("CADE", ra);
            URL url = new URL(ra);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(request);

            os.flush();
            os.close();

            String status = String.valueOf(conn.getResponseCode());
            Log.i("STATUS", status);
            String data = getStringFromInputStream(conn.getInputStream());
            Log.i("DATA" , data);

            if (conn.getResponseCode() == 200) {
                Log.i("RESPONSE", "Successfully registered client");
                Button button = (Button) findViewById(R.id.start);
                button.setEnabled(true);
            } else {
                Log.i("RESPONSE", "Unsuccessfully registered client");
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String startGame() {

        try {
            URL url = new URL(startAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String status = String.valueOf(conn.getResponseCode());
            Log.i("STATUS", status);
            String data = getStringFromInputStream(conn.getInputStream());
            Log.i("DATA" , data);

            if (conn.getResponseCode() == 200) {
                Log.i("RESPONSE", "Successfully started game");
                Intent intent = new Intent(SetupGame.this, Game.class);
                intent.putExtra("id", clientId);
                startActivity(intent);
            } else {
                Log.i("RESPONSE", "Unsuccessfully started game");
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
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

}
