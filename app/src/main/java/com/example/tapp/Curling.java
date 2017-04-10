package com.example.tapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Curling extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private final float[] mRotationMatrix = new float[16];
    private final float orientationThreshold = 0.7f;
    private long lCurlTimes[]; // times in nanoseconds
    private long rCurlTimes[]; // times in nanoseconds
    private double lAvg;
    private double rAvg;
    private int trialsComplete = 0;
    public static final int numTrials = 6; // 3 trials per arm
    private boolean isFaceUp;
    private int curlCount;
    public static final int curlGoal = 10;
    private long startTime;
    TextView curlText;
    TextView curlInst;
    Button curlButton;
    private boolean sensorIsRegistered = false;
    boolean done = false;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curling);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // initialize the rotation matrix to identity
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;

        lCurlTimes = new long[3];
        rCurlTimes = new long[3];
        curlInst = (TextView) findViewById(R.id.instructions);
        curlText = (TextView) findViewById(R.id.curl_text);
        curlButton = (Button) findViewById(R.id.curling_start_button);
        this.setButtonTrialText();
        // TODO set initial textView text
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorIsRegistered) {
            this.startSensor();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorIsRegistered) {
            this.stopSensor();
        }
    }

    private void setButtonTrialText() {
        if (trialsComplete < numTrials) {
            curlButton.setText(String.format(getString(R.string.trial_start), trialsComplete % 2 == 0 ? "left" : "right", (trialsComplete / 2) + 1));
        } else {
            curlButton.setText(getString(R.string.curl_res));
        }
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
            curlButton.setVisibility(View.INVISIBLE);

            isFaceUp = false;
            curlCount = 0;
            curlText.setText(String.format(Locale.getDefault(), "%d", curlCount));
            curlInst.setVisibility(View.VISIBLE);
            this.startSensor();
            sensorIsRegistered = true;
        } else if (done) {
            finish();
        } else {
            long[] lScores = {this.lCurlTimes[0], this.lCurlTimes[1], this.lCurlTimes[2]};
            long[] rScores = {this.rCurlTimes[0], this.rCurlTimes[1], this.rCurlTimes[2]};

            sendToSheets(lScores, Sheets.UpdateType.LH_CURL.ordinal());
            sendToSheets(rScores, Sheets.UpdateType.RH_CURL.ordinal());

            lAvg = 0;
            rAvg = 0;
            for (int i = 0; i < numTrials/2; i++) {
                lAvg += lCurlTimes[i]/1000000000.0;
                rAvg += rCurlTimes[i]/1000000000.0;
            }
            lAvg /= (numTrials/2);
            rAvg /= (numTrials/2);

            curlInst.setText(getString(R.string.curl_avg));
            curlInst.setVisibility(View.VISIBLE);
            curlText.setText(String.format(Locale.getDefault(),"Left: %.2f seconds\nRight: %.2f seconds",lAvg,rAvg));
            curlText.setVisibility(View.VISIBLE);

            done = true;
            curlButton.setText(getString(R.string.curl_end));
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

                vibrator.vibrate(200);
            } else if (!isFaceUp && deviceIsFacingUp()) {
                isFaceUp = true;
                if (curlCount == 0) {
                    startTime = System.nanoTime();
                }
            }
            if (curlCount >= curlGoal) {
                this.stopSensor();
                sensorIsRegistered = false;
                long elapsedTime = System.nanoTime() - startTime;
                if (trialsComplete % 2 == 0) {
                    lCurlTimes[trialsComplete / 2] = elapsedTime;
                } else {
                    rCurlTimes[trialsComplete / 2] = elapsedTime;
                }
                trialsComplete++;
                curlCount = 0;
                curlButton.setVisibility(View.VISIBLE);
                this.setButtonTrialText();
                curlInst.setVisibility(View.INVISIBLE);
                curlText.setText(String.format(Locale.getDefault(), "Elapsed time: %.2f seconds", elapsedTime / 1000000000.0));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendToSheets(long[] scores, int sheet) {
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
            row.add(Long.toString(scores[i]));

        sheets.putStringArrayListExtra(Sheets.EXTRA_SHEETS, row);
        sheets.putExtra(Sheets.EXTRA_TYPE, sheet);
        startActivity(sheets);
    }
}
