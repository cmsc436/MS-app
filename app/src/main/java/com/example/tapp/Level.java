package com.example.tapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.umd.cmsc436.sheets.Sheets;

public class Level extends AppCompatActivity implements Sheets.Host {
        Accelerometer accelerometer;

        float x_degrees, y_degrees, valuesAccel_X, valuesAccel_Y;
        double norm_of_degrees;
        SensorManager sensorManager;
        Sensor sensorLinAccel;
        Sensor sensorGravity;
        Sensor sensorAccel;
        MyTask myTask;
        Timer timer;
        CountDownTimer recordTimer;

        int trialsComplete;
        String hand = "left";
        static int numTrials = 6;
        int[] lScores = new int[numTrials/2];
        int[] rScores = new int[numTrials/2];
        Button button_Start;

        int lScore = 0;
        int rScore = 0;

        private Sheets sheet;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_level);

            setViews();
            setSensors();

            // Configure a 10 second timer to allow the user tap.
            recordTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long msTilFinish) { }
                public void onFinish() {
                    handleTimerComplete();
                }
            };

            trialsComplete = 0;

            sheet = new Sheets(this, getString(R.string.app_name), getString(R.string.class_sheet),
                    getString(R.string.private_sheet));
        }

        private void sendToSheets(int[] scores, Sheets.TestType type) {
            // Compute average across the trials
            float avg = 0;
            for (int i = 0; i < numTrials / 2; i++)
                avg += scores[i];
            avg /= numTrials / 2;
            // Send data to central sheet
            sheet.writeData(type, getString(R.string.userID), avg);
        }

        private void handleTimerComplete() {
            trialsComplete++;
            if (trialsComplete < numTrials) {
                int temp;
                switch (hand) {
                    case "right":
                        temp = this.scoreCache();
                        rScores[(trialsComplete/2)-1] = temp;
                        rScore = rScore + temp;

                        hand = "left";
                        break;
                    case "left":
                        temp = this.scoreCache();
                        lScores[(trialsComplete/2)] = temp;
                        lScore = lScore + temp;

                        hand = "right";
                        break;
                    default:
                        hand = "left";
                }
                button_Start.setText(String.format(getString(R.string.trial_start), hand, (trialsComplete/2) + 1));
                Toast.makeText(getApplicationContext(), "Trial complete!", Toast.LENGTH_LONG).show();
                timer.cancel();
                this.saveCanvasToGallery("Level Test", String.format(Locale.US,
                        "%s hand: trial %d", hand, trialsComplete));
                accelerometer.clear();
            } else {
                int temp = this.scoreCache();
                rScores[(trialsComplete/2)-1] = temp;
                rScore = rScore + temp;
                this.saveCanvasToGallery("Level Test", String.format(Locale.US,
                        "%s hand: trial %d", hand, trialsComplete));

                lScore = lScore / (numTrials/2);
                rScore = rScore / (numTrials/2);
                Toast.makeText(getApplicationContext(), "All trials complete!",
                        Toast.LENGTH_LONG).show();
                button_Start.setVisibility(View.INVISIBLE);
                timer.cancel();
                accelerometer.clear();

                // display score until exit
                View accel = findViewById(R.id.accelerometer);
                accel.setVisibility(View.INVISIBLE);

                sendToSheets(lScores, Sheets.TestType.LH_LEVEL);
                sendToSheets(rScores, Sheets.TestType.RH_LEVEL);

                Button returnButton = (Button) findViewById(R.id.buttonReturn);
                TextView scoreDisplay = (TextView) findViewById(R.id.score_display);
                scoreDisplay.setVisibility(View.VISIBLE);
                scoreDisplay.setText("Left score: " + lScore + "\nRight score: " + rScore);
                returnButton.setVisibility(View.VISIBLE);
            }
        }

        private int scoreCache() {
            double totalDist = 0;
            double pixCount = 0;
            int redThreshold = 200;

            View drawing = findViewById(R.id.accelerometer);
            drawing.setDrawingCacheEnabled(true);
            Bitmap bitmap = drawing.getDrawingCache();

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();

            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    if(Color.red(bitmap.getPixel(i, j)) > redThreshold) {
                        double dist = Math.sqrt(((height/2)-j)*((height/2)-j) + ((width/2)-i)*((width/2)-i));
                        totalDist += dist;
                        pixCount++;
                    }
                }
            }

            int score = 0;
            if (pixCount != 0) {
                score = (int) Math.round(totalDist / pixCount);
            }

            return score;
        }

        private void saveCanvasToGallery(String title, String description) {
            View drawing = findViewById(R.id.accelerometer);
            drawing.setDrawingCacheEnabled(true);
            Bitmap bitmap = drawing.getDrawingCache();

            Bitmap combined = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Canvas canvas = new Canvas(combined);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, 0, 0, null);

            String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), combined, title, description);

            Context context = getApplicationContext();
            CharSequence text = "Saved image to " + savedImageURL;
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }

        private void setSensors() {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorLinAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

            sensorManager.registerListener(listener, sensorAccel,SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(listener, sensorLinAccel,SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(listener, sensorGravity,SensorManager.SENSOR_DELAY_FASTEST);
        }

        private void setViews() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            button_Start = (Button) findViewById(R.id.button_Start);
            accelerometer = (Accelerometer) findViewById(R.id.accelerometer);

            button_Start.setText(String.format(getString(R.string.trial_start), hand, (trialsComplete/2) + 1));
        }

        public void set_Start(View v) {
            if(timer!=null){timer.cancel();}
            timer = new Timer();
            myTask = new MyTask();
            timer.schedule(myTask, 0, 10);
            recordTimer.start();
        }

        private void setDegrees() {
            norm_of_degrees = Math.sqrt(Math.pow(valuesAccel[0], 2) +
                    Math.pow(valuesAccel[1], 2) +
                    Math.pow(valuesAccel[2], 2));

            // Normalize the accelerometer vector
            valuesAccel_X = (float) (valuesAccel[0] / norm_of_degrees);
            valuesAccel_Y = (float) (valuesAccel[1] / norm_of_degrees);

            x_degrees = (float)(90 - Math.toDegrees(Math.acos(valuesAccel_X)));
            y_degrees = (float)(90 - Math.toDegrees(Math.acos(valuesAccel_Y)));
        }

        float[] valuesAccel = new float[3];
        float[] valuesAccelMotion = new float[3];
        float[] valuesAccelGravity = new float[3];
        float[] valuesLinAccel = new float[3];
        float[] valuesGravity = new float[3];

        SensorEventListener listener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        for (int i = 0; i < 3; i++) {
                            valuesAccel[i] = event.values[i];
                            valuesAccelGravity[i] = (float) (0.1 * event.values[i] + 0.9 * valuesAccelGravity[i]);
                            valuesAccelMotion[i] = event.values[i]
                                    - valuesAccelGravity[i];
                        }
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        System.arraycopy(event.values, 0, valuesLinAccel, 0, 3);
                        break;
                    case Sensor.TYPE_GRAVITY:
                        System.arraycopy(event.values, 0, valuesGravity, 0, 3);
                        break;
                }
            }
        };

        private class MyTask extends TimerTask{

            @Override
            public void run() {

                setDegrees();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accelerometer.setXY(valuesAccel[0], valuesAccel[1]);
                        accelerometer.onXY_Update(accelerometer.getXdimen(), accelerometer.getYdimen());
                    }
                });
            }
        }

        public void returnToMain(View v) {
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