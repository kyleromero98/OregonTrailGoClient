package com.oregontrail.kromero.oregontrailgo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
Add a progress bar. android provides the stuff. resl/layout/game.xml
add a dialogue class to show the result of an event.
add checking to the event. you will get a 404 if, say, you don't have the resources to service an event and respond anyway
 */

@SuppressLint("ValidFragment")
public class CustomDialog extends AppCompatDialogFragment {
    Globals addr = Globals.getInstance();
    private String positiveResponseAddress = addr.getData() + "respond";
    private String negativeResponseAddress = addr.getData() + "respond";

    private Game game;
    private Player client;
    private TextView messageText;
    private ImageView messageIcon;

    @SuppressLint("ValidFragment")
    public CustomDialog(Game game, Player player) {
        super();
        this.game = game;
        this.client = player;
        positiveResponseAddress = positiveResponseAddress + "/" + client.getId() + "/true";
        negativeResponseAddress = negativeResponseAddress + "/" + client.getId() + "/false";
        Log.d("tag", "positiveResponseAddress " + positiveResponseAddress);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        messageText = (TextView) view.findViewById(R.id.message);
        messageText.setText(game.getPromptMessage());

        messageIcon = (ImageView) view.findViewById(R.id.message_icon);

        switch (client.getEventId()) {
            case 0:
                messageIcon.setImageResource(R.drawable.dysentery);
                break;
            case 1:
                messageIcon.setImageResource(R.drawable.bandit);
                break;
            case 2:
                messageIcon.setImageResource(R.drawable.hunting);
                break;
            case 3:
                messageIcon.setImageResource(R.drawable.bad_water);
                break;
            case 4:
                messageIcon.setImageResource(R.drawable.broken_wheel);
                break;
            case 5:
                messageIcon.setImageResource(R.drawable.starve);
                break;
            case 6:
                messageIcon.setImageResource(R.drawable.town);
                break;
            case 7:
                messageIcon.setImageResource(R.drawable.dysentery);
                break;
        }

        if (client.eventHasChoice()) {
            builder.setView(view)
                    .setTitle(null)
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AsyncTask<Void, Void, String>() {

                                @Override
                                protected String doInBackground(Void... voids) {

                                    try {
                                        URL url = new URL(negativeResponseAddress);
                                        HttpURLConnection conn = null;
                                        conn = (HttpURLConnection) url.openConnection();
                                        conn.setRequestMethod("GET");

                                        if (conn.getResponseCode() == 200) {
                                            String data = getStringFromInputStream(conn.getInputStream());
                                            if (data != null || (data.length() >= 2)) {
                                                try {
                                                    JSONObject jsonObj = new JSONObject(data);
                                                    data = jsonObj.getString("message");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    System.out.println("The player got an f'ed up JSON string");
                                                }
                                                Log.i("STRING LENGTH", Integer.toString(data.length()));
                                                if (data != null || (data.length() >= 2)) {
                                                    Log.i("DATA", data);

                                                    Intent intent = new Intent("ResponseUpdated");
                                                    intent.putExtra("response", data);

                                                    LocalBroadcastManager.getInstance(game.getApplication()).sendBroadcast(intent);

                                                    Log.i("RESPONSE", "Successfully said no to event");
                                                } else {
                                                    client.clearEvent();
                                                }
                                            } else {
                                                client.clearEvent();
                                            }
                                        } else {
                                            client.clearEvent();
                                        }
                                        conn.disconnect();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println(e.getMessage());
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(String s) {

                                }
                            }.execute();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, String>() {

                                    @Override
                                    protected String doInBackground(Void... voids) {

                                        try {
                                            URL url = new URL(positiveResponseAddress);
                                            HttpURLConnection conn = null;
                                            conn = (HttpURLConnection) url.openConnection();
                                            conn.setRequestMethod("GET");

                                            if (conn.getResponseCode() == 200) {
                                                String data = getStringFromInputStream(conn.getInputStream());
                                                try {
                                                    JSONObject jsonObj = new JSONObject(data);
                                                    data = jsonObj.getString("message");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    System.out.println("The player got an f'ed up JSON string");
                                                }
                                                if (data.length() >= 2) {
                                                    Log.i("DATA", data);

                                                    Log.i("RESPONSE", "Successfully said yes to event");
                                                    Intent intent = new Intent("ResponseUpdated");
                                                    intent.putExtra("response", data);

                                                    LocalBroadcastManager.getInstance(game.getApplication()).sendBroadcast(intent);
                                                } else {
                                                    client.clearEvent();
                                                }
                                            } else {
                                                client.clearEvent();
                                            }
                                            conn.disconnect();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            System.out.println(e.getMessage());
                                        }
                                        return null;
                                    }
                                }.execute();
                        }
                    });
        } else {
            builder.setView(view)
                    .setTitle(null)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { // write this method.
                            //model it after the top two. should send false to allow the game to progress
                            // no timeout yet
                            new AsyncTask<Void, Void, String>() {

                                @Override
                                protected String doInBackground(Void... voids) {

                                    try {
                                        URL url = new URL(positiveResponseAddress);
                                        HttpURLConnection conn = null;
                                        conn = (HttpURLConnection) url.openConnection();
                                        conn.setRequestMethod("GET");

                                        if (conn.getResponseCode() == 200) {
                                            String data = getStringFromInputStream(conn.getInputStream());
                                            if (data != null || (data.length() >= 2)) {
                                                try {
                                                    JSONObject jsonObj = new JSONObject(data);
                                                    data = jsonObj.getString("message");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    System.out.println("The player got an f'ed up JSON string");
                                                }
                                                if (data != null || data.length() >= 2) {
                                                    Log.i("DATA", data);

                                                    Log.i("RESPONSE", "Successfully said no to event with no choice");

                                                    Intent intent = new Intent("ResponseUpdated");
                                                    intent.putExtra("response", data);

                                                    LocalBroadcastManager.getInstance(game.getApplication()).sendBroadcast(intent);
                                                } else {
                                                    client.clearEvent();
                                                }
                                            } else {
                                                client.clearEvent();
                                            }
                                        } else {
                                            client.clearEvent();
                                        }
                                        conn.disconnect();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println(e.getMessage());
                                    }
                                    return null;
                                }
                            }.execute();
                        }
                    });
        }
        return builder.create();
    }

    private String getStringFromInputStream(InputStream is) {
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
