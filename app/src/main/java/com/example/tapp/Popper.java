package com.example.tapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class Popper extends AppCompatActivity implements Balloon.BalloonListener {

    private int numTrials = 3;
    private int numBalloons = 10;
    private long reactionTimes[][]; // times in nanoseconds
    private long startTime;
    private int trialsComplete;
    private int balloonCount;
    private int[] mBalloonColors = new int[3];
    private ViewGroup mContentView;
    private int mScreenWidth, mScreenHeight;
    final private String POPPER_TEST_TYPE = "Pop";
    final private String POPPER_METRIC = "Response time for 10 bubbles";

    Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popper);
        trialsComplete = 0;
        balloonCount = 0;
        reactionTimes = new long[numTrials][numBalloons];
        buttonStart = (Button) findViewById(R.id.popper_start);
        buttonStart.setText(String.format(getString(R.string.popper_start), trialsComplete + 1));
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

    public void setStart(View v) {
        if (trialsComplete < numTrials) {
            balloonCount = 0;
            buttonStart.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(), "Trial %d started!", trialsComplete + 1), Toast.LENGTH_SHORT).show();
            launchBalloon();
        } else if (trialsComplete == numTrials) {
            // Send data to sheets
            Intent sheets = new Intent(this, Sheets.class);
            ArrayList<String> row = new ArrayList<>();
            row.add(POPPER_TEST_TYPE); // Test type
            Time now = new Time();
            now.setToNow();
            row.add(now.toString());
            row.add(POPPER_METRIC);
            row.add(Arrays.toString(reactionTimes[0]));
            row.add(Arrays.toString(reactionTimes[1]));
            row.add(Arrays.toString(reactionTimes[2]));

            sheets.putStringArrayListExtra(Sheets.EXTRA_SHEETS, row);
            startActivity(sheets);

            // Compute averages and print
            double[] averages = new double[numTrials];
            for (int i = 0; i < numTrials; i++) {
                double sum = 0;
                for(int j = 0; j < numBalloons; j++) {
                    sum += reactionTimes[i][j];
                }
                averages[i] = sum / numBalloons;
            }
            String resString = "";
            for (int i = 0; i < numTrials; i++) {
                resString += String.format(Locale.US, "Trial %d average: %.2f sec\n", i+1,
                        (averages[i] / 1000000000));
            }
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
        reactionTimes[trialsComplete][balloonCount] = elapsedTime;
        balloonCount++;
        if (balloonCount >= numBalloons) {
            trialsComplete++;
            buttonStart.setVisibility(View.VISIBLE);
            if (trialsComplete == numTrials) {
                buttonStart.setText(getString(R.string.popper_view));
            } else {
                buttonStart.setText(String.format(getString(R.string.popper_start), trialsComplete + 1));
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
