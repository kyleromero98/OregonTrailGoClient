package com.oregontrail.kromero.oregontrailgo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class ResponseDialog extends AppCompatDialogFragment {

    private Game game;
    private Player client;
    private TextView messageText;

    private String response;

    @SuppressLint("ValidFragment")
    public ResponseDialog(Game game, Player player, String serverResponse) {
        super();
        this.game = game;
        this.client = player;
        response = serverResponse;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.response_layout, null);

        messageText = (TextView) view.findViewById(R.id.text);
        messageText.setText(response);

        builder.setView(view)
                .setTitle(null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client.clearEvent();
                    }
                });
        return builder.create();
    }
}
