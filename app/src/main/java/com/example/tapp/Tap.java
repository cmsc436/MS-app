package com.example.tapp;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.util.Locale;

public class Tap extends AppCompatActivity {

    int lCount = 0;
    int rCount = 0;
    private CountDownTimer timer;
    private CountDownTimer waitingTimer;
    private boolean countdownReadyToStart = true;
    private int timersComplete = 0;

    private void handleTimerComplete() {
        final TextView timerLabel = (TextView) findViewById(R.id.timerView);
        final TextView countText = (TextView) findViewById(R.id.countView);
        final Button goToMain = (Button) findViewById(R.id.buttonReturn);
        this.timersComplete++;

        switch (this.timersComplete) {
            case 1:
                // Clear screen and set screen to waiting period.
                timerLabel.setText("Please switch to your right hand.");
                countText.setText("");
                this.waitingTimer.start();
                break;
            case 2:
                // End the waiting period and allow the next tap to start the timer.
                timerLabel.setText("Tap the screen with your right hand.\nYou have 10 seconds to tap as quickly as possible.");
                this.countdownReadyToStart = true;
                break;
            case 3:
                // Inform user that the tapping is finished.
                timerLabel.setText("And you're done!");
                countText.setText(String.format(Locale.US, "Left taps: %d\nRight taps: %d", this.lCount, this.rCount));
                goToMain.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        final TextView timerLabel = (TextView) findViewById(R.id.timerView);
        timerLabel.setText("Tap the screen with your left hand.\nYou have 10 seconds to tap as quickly as possible.");

        // Configure a 10 second timer to allow the user tap.
        this.timer = new CountDownTimer(10000, 1000) {
            private int secondsLeft = 10;
            public void onTick(long msTilFinish) {
                timerLabel.setText("Seconds left: " + String.valueOf(--this.secondsLeft));
            }
            public void onFinish() {
                this.secondsLeft = 10;
                handleTimerComplete();
            }
        };

        // Configure a two second waiting period to prevent users from tapping between phases.
        this.waitingTimer = new CountDownTimer(2000, 2000) {
            public void onTick(long msTilFinish) {
            }
            public void onFinish() {
                handleTimerComplete();
            }
        };
    }

    public void count(View v) {
        if (this.countdownReadyToStart) {
            this.countdownReadyToStart = false;
            this.timer.start();
        }
        if (this.timersComplete == 0) {
            this.lCount++;
            setContentView(v);
            TextView countText = (TextView) findViewById(R.id.countView);
            countText.setText(String.format(Locale.US, "%d", this.lCount));
        } else if (this.timersComplete == 2) {
            this.rCount++;
            setContentView(v);
            TextView countText = (TextView) findViewById(R.id.countView);
            countText.setText(String.format(Locale.US, "%d", this.rCount));
        }
    }

    public void onClick(View v) {
        finish();
    }
}
