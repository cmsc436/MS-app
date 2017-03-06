package com.example.tapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Popper extends AppCompatActivity {

    private double[][] reactionTimes;
    private int numTrials = 3;
    private int trialsComplete;
    private int balloonCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popper);

        trialsComplete = 0;
        balloonCount = 0;
    }

    public void setStart(View v) {
    }
}
