package com.example.tapp;

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
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

public class Popper extends AppCompatActivity implements Balloon.BalloonListener {

    private int numTrials = 3;
    private int numBalloons = 10;
    private long reactionTimes[][]; // times in nanoseconds
    private long startTime;
    private int trialsComplete;
    private boolean finished;
    private int balloonCount;
    private int[] mBalloonColors = new int[3];
    private ViewGroup mContentView;
    private int mScreenWidth, mScreenHeight;

    Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popper);
        finished = true;
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
        if (finished && trialsComplete < numTrials) {
            finished = false;
            balloonCount = 0;
            launchBalloon();
        } else if (trialsComplete == numTrials){
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Trial not complete!", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchBalloon() {
        Balloon balloon = new Balloon(this, mBalloonColors[0], 150, 1);
        Random random = new Random(new Date().getTime());
        balloon.setX(random.nextInt(mScreenWidth - 200));
        balloon.setY(random.nextInt(mScreenHeight - 200));
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
            finished = true;
            trialsComplete++;
            if (trialsComplete == numTrials) {
                buttonStart.setText(getString(R.string.popper_end));
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
