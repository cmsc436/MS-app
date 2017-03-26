package com.example.tapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class Curling extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private final float[] mRotationMatrix = new float[16];
    private final float orientationThreshold = 0.7f;
    private long lCurlTimes[]; // times in nanoseconds
    private long rCurlTimes[]; // times in nanoseconds
    private int trialsComplete = 0;
    public static final int numTrials = 6; // 3 trials per arm
    private boolean isFaceUp;
    private int curlCount;
    public static final int curlGoal = 10;
    private long startTime;
    TextView curlText;
    Button curlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curling);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // initialize the rotation matrix to identity
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;

        lCurlTimes = new long[3];
        rCurlTimes = new long[3];
        curlText = (TextView) findViewById(R.id.curl_text);
        curlButton = (Button) findViewById(R.id.curling_start_button);
        // TODO set initial button text
        // TODO set initial textView text
    }

    private void startSensor() {
        // poll every second
        mSensorManager.registerListener(this, mSensor, 1000000);
    }

    private void stopSensor() {
        mSensorManager.unregisterListener(this);
    }

    private boolean deviceIsFacingUp() {
        return mRotationMatrix[10] > orientationThreshold;
    }

    private boolean deviceIsFacingDown() {
        return mRotationMatrix[10] < -orientationThreshold;
    }

    public void curlButtonPress(View v) {
        if (trialsComplete < numTrials) {
            curlButton.setVisibility(View.GONE);
            isFaceUp = false;
            curlCount = 0;
            this.startSensor();
        } else {
            // TODO write data to Google Sheets and display results
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            if (isFaceUp && deviceIsFacingDown()) {
                isFaceUp = false;
                curlCount++;
                curlText.setText(String.format(Locale.getDefault(), "%d", curlCount));
            } else if (!isFaceUp && deviceIsFacingUp()) {
                isFaceUp = true;
                if (curlCount == 0) {
                    startTime = System.nanoTime();
                }
            }
            if (curlCount >= curlGoal) {
                this.stopSensor();
                long endTime = System.nanoTime();
                if (trialsComplete % 2 == 0) {
                    lCurlTimes[trialsComplete / 2] = endTime - startTime;
                } else {
                    rCurlTimes[trialsComplete / 2] = endTime - startTime;
                }
                trialsComplete++;
                curlCount = 0;
                curlButton.setVisibility(View.VISIBLE);
                // TODO set new button text
                // TODO set instructions for next trials in textView
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
