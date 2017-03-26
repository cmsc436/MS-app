package com.example.tapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    public void navRecord(View v) {
        Intent rec = new Intent(this, Tap.class);
        startActivity(rec);
    }

    public void navSpiral(View v) {
        Intent spiral = new Intent(this, Spiral.class);
        startActivity(spiral);
    }

    public void navLevel(View v) {
        Intent level = new Intent(this, Level.class);
        startActivity(level);
    }

    public void navPopper(View v) {
        Intent popper = new Intent(this, Popper.class);
        startActivity(popper);
    }

    public void navCurl(View v) {
        Intent curl = new Intent(this, Curling.class);
        startActivity(curl);
    }
}
