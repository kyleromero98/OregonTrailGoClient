package com.oregontrail.kromero.oregontrailgo;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        TextView tv = (TextView) findViewById(R.id.join);
        Typeface tf = Typeface.createFromAsset(tv.getContext().getAssets(), "fonts/font.ttf");
        tv.setTypeface(tf);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.join) {
            Intent intent = new Intent(MainMenu.this, SetupGame.class);
            startActivity(intent);
        }
    }
}

