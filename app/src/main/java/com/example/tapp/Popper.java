package com.example.tapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Popper extends AppCompatActivity {

    private int numTrials = 3;
    private int numBalloons = 10;
    private double reactionTimes[][];
    private int trialsComplete;
    private boolean finished;
    private int balloonCount;

    Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popper);
        finished = true;
        trialsComplete = 0;
        balloonCount = 0;
        reactionTimes = new double[numTrials][numBalloons];
        buttonStart = (Button) findViewById(R.id.popper_start);
        buttonStart.setText(String.format(getString(R.string.popper_start), trialsComplete + 1));
    }

    private void runTrial(View v) {
        while (balloonCount < numBalloons) {
            // Spawn balloon at random interval between 0-1 second
            // Record the time it took the user to tap that balloon
            System.out.println("Balloon " + balloonCount + " in trial number " + (trialsComplete + 1));
            balloonCount++;
        }
        finished = true;
        trialsComplete++;
        if (trialsComplete == numTrials) {
            buttonStart.setText(getString(R.string.popper_end));
        } else {
            buttonStart.setText(String.format(getString(R.string.popper_start), trialsComplete + 1));
        }
    }

    public void setStart(View v) {
        if (finished && trialsComplete < numTrials) {
            finished = false;
            balloonCount = 0;
            runTrial(v);
        } else if (trialsComplete == numTrials){
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Trial not complete!", Toast.LENGTH_SHORT).show();
        }
    }
}
