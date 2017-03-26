package com.example.tapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Tap extends AppCompatActivity {

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
                rTotal += rCount;
                rCount = 0;
                tapRegion.setVisibility(View.INVISIBLE);
                timerLabel.setText("And you're done!");
                countText.setVisibility(View.VISIBLE);
                countText.setText(String.format(Locale.US, "Left taps: %d\nRight taps: %d", this.lTotal/3, this.rTotal/3));

                // TODO: add PER-TRIAL scores
                int[] lScores = {this.lTotal/3, this.lTotal/3, this.lTotal/3};
                int[] rScores = {this.rTotal/3, this.rTotal/3, this.rTotal/3};

                sendToSheets(lScores, Sheets.UpdateType.LH_TAP.ordinal());
                sendToSheets(rScores, Sheets.UpdateType.RH_TAP.ordinal());

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
    }

    private void sendToSheets(int[] scores, int sheet) {
        // Send data to sheets
        Intent sheets = new Intent(this, Sheets.class);
        ArrayList<String> row = new ArrayList<>();
        row.add(Integer.toString(Sheets.teamID));

        SimpleDateFormat format;
        Calendar c = Calendar.getInstance();
        format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        row.add(format.format(c.getTime()));

        row.add("n/a");

        for (int i = 0; i < numTrials / 2; i++)
            row.add(Integer.toString(scores[i]));

        sheets.putStringArrayListExtra(Sheets.EXTRA_SHEETS, row);
        sheets.putExtra(Sheets.EXTRA_TYPE, sheet);
        startActivity(sheets);
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
}
