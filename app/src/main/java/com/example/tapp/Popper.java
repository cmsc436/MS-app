package com.example.tapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

import edu.umd.cmsc436.sheets.Sheets;

public class Popper extends AppCompatActivity implements Balloon.BalloonListener, Sheets.Host {

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

    private Sheets sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popper);
        trialsComplete = 0;
        balloonCount = 0;
        lReactionTimes = new long[numTrials][numBalloons];
        rReactionTimes = new long[numTrials][numBalloons];
        buttonStart = (Button) findViewById(R.id.popper_start);
        buttonStart.setText(String.format(getString(R.string.start_trial), hand, trialsComplete + 1));
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
        sheet = new Sheets(this, getString(R.string.app_name), getString(R.string.class_sheet),
                getString(R.string.private_sheet));
    }

    private void sendToSheets(double avg, Sheets.TestType type) {
        // Send to central sheet
        sheet.writeData(type, getString(R.string.userID), (float)avg);
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

            sendToSheets(lAverage / 1000000000, Sheets.TestType.LH_POP);
            sendToSheets(rAverage / 1000000000, Sheets.TestType.RH_POP);

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
                buttonStart.setText(getString(R.string.results_view));
            } else {
                hand = (hand.equals("left"))? "right" : "left";
                buttonStart.setText(String.format(getString(R.string.start_trial), hand, (trialsComplete/2) + 1));
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

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return Info.LIB_ACCOUNT_NAME_REQUEST_CODE;
            case REQUEST_AUTHORIZATION:
                return Info.LIB_AUTHORIZATION_REQUEST_CODE;
            case REQUEST_PERMISSIONS:
                return Info.LIB_PERMISSION_REQUEST_CODE;
            case REQUEST_PLAY_SERVICES:
                return Info.LIB_PLAY_SERVICES_REQUEST_CODE;
            default:
                return -1;
        }
    }

    @Override
    public void notifyFinished(Exception e) {
        if (e != null) {
            throw new RuntimeException(e);
        }
        Log.i(getClass().getSimpleName(), "Done");
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        this.sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.sheet.onActivityResult(requestCode, resultCode, data);
    }
}
