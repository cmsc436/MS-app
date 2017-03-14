package com.example.tapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Popper extends AppCompatActivity implements Balloon.BalloonListener {

    private int numTrials = 6;
    private int numBalloons = 10;
    private long lReactionTimes[][]; // times in nanoseconds
    private long rReactionTimes[][]; // times in nanoseconds
    private long startTime;
    private int trialsComplete;
    private int balloonCount;
    private int[] mBalloonColors = new int[3];
    private ViewGroup mContentView;
    private int mScreenWidth, mScreenHeight;
    private String hand = "left";
    Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popper);
        trialsComplete = 0;
        balloonCount = 0;
        lReactionTimes = new long[numTrials][numBalloons];
        rReactionTimes = new long[numTrials][numBalloons];
        buttonStart = (Button) findViewById(R.id.popper_start);
        buttonStart.setText(String.format(getString(R.string.popper_start), hand, trialsComplete + 1));
        mBalloonColors[0] = Color.argb(255, 255, 0, 0);
        mBalloonColors[1] = Color.argb(255, 0, 255, 0);
        mBalloonColors[2] = Color.argb(255, 0, 0, 255);
        mContentView = (ViewGroup) findViewById(R.id.balloon_view);

        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // TODO: account for device rotation
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();
                }
            });
        }
    }

    private void sendToSheets(double avg, int sheet) {
        // Send data to sheets
        Intent sheets = new Intent(this, Sheets.class);
        ArrayList<String> row = new ArrayList<>();
        row.add(Integer.toString(Sheets.teamID));

        SimpleDateFormat format;
        Calendar c = Calendar.getInstance();
        format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        row.add(format.format(c.getTime()));

        row.add("n/a");
        row.add(Integer.toString(numBalloons * numTrials));
        row.add(Double.toString(avg));

        sheets.putStringArrayListExtra(Sheets.EXTRA_SHEETS, row);
        sheets.putExtra(Sheets.EXTRA_TYPE, sheet);
        startActivity(sheets);
    }

    public void setStart(View v) {
        if (trialsComplete < numTrials) {
            balloonCount = 0;
            buttonStart.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(), "Trial %s %d started!", hand, (trialsComplete/2) + 1), Toast.LENGTH_SHORT).show();
            launchBalloon();
        } else if (trialsComplete == numTrials) {
            // Compute averages and print
            double lAverage = 0;
            double rAverage = 0;
            for (int i = 0; i < numTrials; i++) {
                for(int j = 0; j < numBalloons; j++) {
                    lAverage += lReactionTimes[i][j];
                    rAverage += rReactionTimes[i][j];
                }
            }
            lAverage /= (numTrials * numBalloons);
            rAverage /= (numTrials * numBalloons);

            sendToSheets(lAverage, Sheets.UpdateType.LH_POP.ordinal());
            sendToSheets(rAverage, Sheets.UpdateType.RH_POP.ordinal());

            // Print averages for user
            String resString = "";
            resString += String.format(Locale.US, "Left hand average: %.2f sec\n", lAverage / 1000000000);
            resString += String.format(Locale.US, "Right hand average: %.2f sec\n", rAverage / 1000000000);
            TextView results = (android.widget.TextView) findViewById(R.id.popResults);
            results.setText(resString);
            buttonStart.setText(getString(R.string.popper_end));
            trialsComplete++;
        } else {
            finish();
        }
    }

    private void launchBalloon() {
        Balloon balloon = new Balloon(this, mBalloonColors[0], 150, 1);
        Random random = new Random(new Date().getTime());
        balloon.setX(random.nextInt(mScreenWidth - 200));
        balloon.setY(random.nextInt(mScreenHeight - 350));
        mContentView.addView(balloon);
        startTime = System.nanoTime();
    }

    @Override
    public void popBalloon(Balloon balloon, boolean touched) {
        mContentView.removeView(balloon);
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        switch (hand) {
            case "right":
                rReactionTimes[trialsComplete][balloonCount] = elapsedTime;
                break;
            case "left":
                lReactionTimes[trialsComplete][balloonCount] = elapsedTime;
                break;
        }

        balloonCount++;
        if (balloonCount >= numBalloons) {
            trialsComplete++;
            buttonStart.setVisibility(View.VISIBLE);
            if (trialsComplete == numTrials) {
                buttonStart.setText(getString(R.string.popper_view));
            } else {
                hand = (hand.equals("left"))? "right" : "left";
                buttonStart.setText(String.format(getString(R.string.popper_start), hand, (trialsComplete/2) + 1));
            }
        } else {
            Random random = new Random(new Date().getTime());
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    launchBalloon();
                }
            }, random.nextInt(1000));
        }
    }
}
