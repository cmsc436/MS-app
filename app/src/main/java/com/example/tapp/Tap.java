package com.example.tapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.util.Locale;

import edu.umd.cmsc436.sheets.Sheets;

public class Tap extends AppCompatActivity implements Sheets.Host {

    int lCount = 0;
    int rCount = 0;
    private int lTotal = 0;
    private int rTotal = 0;
    private CountDownTimer timer;
    private CountDownTimer waitingTimer;
    private boolean countdownReadyToStart = true;
    private int timersComplete = 0;
    private String hand = "left";
    private int trial = 1;
    private int numTrials = 6;
    int[] lScores = new int[numTrials/2];
    int[] rScores = new int[numTrials/2];

    private Sheets sheet;

    private void handleTimerComplete() {
        final TextView timerLabel = (TextView) findViewById(R.id.timerView);
        final TextView countText = (TextView) findViewById(R.id.countView);
        final TextView tapRegion = (TextView) findViewById(R.id.tapRegion);
        final Button goToMain = (Button) findViewById(R.id.buttonReturn);
        this.timersComplete++;

        switch (this.timersComplete) {
            case 1:
                // Clear screen and set screen to waiting period.
                trial++;
                tapRegion.setVisibility(View.INVISIBLE);
                timerLabel.setText("Please repeat " + hand + " hand for trial " + trial + ".");
                countText.setText("");
                tapRegion.setVisibility(View.VISIBLE);
                this.waitingTimer.start();
                if (hand == "left") {
                    lScores[trial-1] = lCount;
                } else {
                    rScores[trial-1] = rCount;
                }
                lTotal += lCount;
                lCount = 0;
                rTotal += rCount;
                rCount = 0;
                break;
            case 2:
                // End the waiting period and allow the next tap to start the timer.
                tapRegion.setVisibility(View.INVISIBLE);
                timerLabel.setText("Tap the screen with your " + hand + " hand.\nYou have 10 seconds" +
                        " to tap as quickly as possible.\n");
                this.countdownReadyToStart = true;
                if (trial == 3 && hand == "left") {
                    trial = 1;
                } else if (trial == 3 && hand == "right") {
                    timersComplete = 4;
                } else {
                    timersComplete = 0;
                }
                tapRegion.setVisibility(View.VISIBLE);
                break;
            case 3:
                // Clear screen and set screen to waiting period.
                tapRegion.setVisibility(View.INVISIBLE);
                timerLabel.setText("Please switch to your right hand.");
                countText.setText("");
                hand = "right";
                trial = 1;
                this.timersComplete = 1;
                tapRegion.setVisibility(View.VISIBLE);
                this.waitingTimer.start();
                lTotal += lCount;
                lCount = 0;
                rTotal += rCount;
                rCount = 0;
                break;
            case 5:
                // Inform user that the tapping is finished.
                rScores[trial-1] = rCount;
                rTotal += rCount;
                rCount = 0;
                tapRegion.setVisibility(View.INVISIBLE);
                timerLabel.setText("And you're done!");
                countText.setVisibility(View.VISIBLE);
                countText.setText(String.format(Locale.US, "Left taps: %d\nRight taps: %d", this.lTotal/3, this.rTotal/3));

                sendToSheets(lScores, Sheets.TestType.LH_TAP);
                sendToSheets(rScores, Sheets.TestType.RH_TAP);

                goToMain.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap);

        final TextView timerLabel = (TextView) findViewById(R.id.timerView);
        timerLabel.setText("Tap the screen with your left hand.\nYou have 10 seconds to tap as quickly as possible.");

        // Configure a 10 second timer to allow the user tap.
        this.timer = new CountDownTimer(10000, 1000) {
            private int secondsLeft = 10;
            public void onTick(long msTilFinish) {
                this.secondsLeft--;
                timerLabel.setText("");
            }
            public void onFinish() {
                this.secondsLeft = 10;
                handleTimerComplete();
            }
        };

        // Configure a three second waiting period to prevent users from tapping between phases.
        this.waitingTimer = new CountDownTimer(3000, 3000) {
            public void onTick(long msTilFinish) {
            }
            public void onFinish() {
                handleTimerComplete();
            }
        };

        sheet = new Sheets(this, this, getString(R.string.app_name),
                getString(R.string.class_sheet), getString(R.string.private_sheet));
    }

    private void sendToSheets(int[] scores, Sheets.TestType type) {
        // Compute the average across all trials
        float avg = 0;
        float fScores[] = new float[scores.length];
        for (int i = 0; i < numTrials / 2; i++) {
            avg += scores[i];
            fScores[i] = scores[i];
        }
        avg /= numTrials / 2;
        // Send data to the central sheet
        sheet.writeData(type, getString(R.string.userID), avg);
        // Send data to private sheet (per trial)
        sheet.writeTrials(type, getString(R.string.userID), fScores);
    }

    public void count(View v) {
        if (this.countdownReadyToStart) {
            this.countdownReadyToStart = false;
            this.timer.start();
        }
        if (hand == "left" && this.timersComplete % 2 == 0) {
            this.lCount++;
            //setContentView(v);
            TextView countText = (TextView) findViewById(R.id.countView);
            countText.setText(String.format(Locale.US, "%d", this.lCount));
        } else if (hand == "right" && this.timersComplete % 2 == 0) {
            this.rCount++;
            //setContentView(v);
            TextView countText = (TextView) findViewById(R.id.countView);
            countText.setText(String.format(Locale.US, "%d", this.rCount));
        }
    }

    public void onClick(View v) {
        finish();
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
