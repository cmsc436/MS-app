package com.example.tapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class Level extends AppCompatActivity {
        Accelerometer accelerometer;

        float x_degrees, y_degrees, valuesAccel_X, valuesAccel_Y;
        double norm_of_degrees;
        SensorManager sensorManager;
        Sensor sensorLinAccel;
        Sensor sensorGravity;
        Sensor sensorAccel;
        MyTask myTask;
        Timer timer;

        Button button_Start;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_level);

            setViews();
            setSensors();

            button_Start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    set_Start();
                }
            });
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
        }

        private void set_Start() {
            if(timer!=null){timer.cancel();}
            timer = new Timer();
            myTask = new MyTask();
            timer.schedule(myTask, 0, 10);
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

        class MyTask extends TimerTask{

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
    }