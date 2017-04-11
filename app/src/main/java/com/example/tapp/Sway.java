package com.example.tapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class Sway extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private int trial = 1;
    private int numTrials = 3;
    private int curIdx = 0;

    static final float NS2S = 1.0f / 1000000000.0f;
    float[] last_values = null;
    float[] velocity = null;
    float[] position = null;
    long last_timestamp = 0;

    private Vibrator vibrator;
    private CountDownTimer timer;

    // measure X, Y, Z at each second (an array of 10 [X,Y,Z] arrays)
    // where final results are stored for trials
    private float mLastX, mLastY, mLastZ;
    float[][] t1 = new float[10][3];
    float[][] t2 = new float[10][3];
    float[][] t3 = new float[10][3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // loop 10 second timer for 3 sway trials
        this.timer = new CountDownTimer(10000, 1000) {

            public void onTick(long msTilFinish) {
                curIdx++;
            }

            public void onFinish() {
                vibrator.vibrate(200);

                if(trial == 1) {
                    TextView tv = (TextView) findViewById(R.id.textView1);
                    //X,Y,Z position captured for each trial at each second in t1, t2, t3
                    //TODO: Use these points to make a drawing and save to external storage
                    //TODO; write average sway for each trial to Sheets

                    //sample averaging of positions (to validate data, may not be useful metric)
                    float x = 0;
                    float y = 0;
                    float z = 0;

                    for(int i = 0; i < 10; i++) {
                        x += t1[i][0];
                        y += t1[i][1];
                        z += t1[i][2];
                    }
                    tv.setText("\n Trial 1 Done - Averages: " + (x/10) + " " + y/10 + " " + z/10);

                } else if (trial == 2) {
                    TextView tv = (TextView) findViewById(R.id.textView2);
                    //X,Y,Z position captured for each trial at each second in t1, t2, t3
                    //TODO: Use these points to make a drawing and save to external storage
                    //TODO; write average sway for each trial to Sheets

                    //sample averaging of positions (to validate data, may not be useful metric)
                    float x = 0;
                    float y = 0;
                    float z = 0;

                    for(int i = 0; i < 10; i++) {
                        x += t2[i][0];
                        y += t2[i][1];
                        z += t2[i][2];
                    }
                    tv.setText("\n Trial 2 Done - Averages: " + (x/10) + " " + y/10 + " " + z/10);

                } else if (trial == 3) {
                    TextView tv = (TextView) findViewById(R.id.textView3);
                    //X,Y,Z position captured for each trial at each second in t1, t2, t3
                    //TODO: Use these points to make a drawing and save to external storage
                    //TODO; write average sway for each trial to Sheets

                    //sample averaging of positions (to validate data, may not be useful metric)
                    float x = 0;
                    float y = 0;
                    float z = 0;

                    for(int i = 0; i < 10; i++) {
                        x += t3[i][0];
                        y += t3[i][1];
                        z += t3[i][2];
                    }
                    tv.setText("\n Trial 3 Done - Averages: " + (x/10) + " " + y/10 + " " + z/10);

                }

                trial++;
                curIdx = 0;

                if(trial < 4) {
                    timer.cancel();
                    timer.start();
                } else {
                    vibrator.vibrate(500);
                }
            }
        };
    }

    public void onClick(View v) {
        timer.start();
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // http://stackoverflow.com/questions/11068671/read-x-y-z-coordinates-of-android-phone-using-accelerometer
    public void onSensorChanged(SensorEvent event) {
        if(last_values != null){
            float dt = (event.timestamp - last_timestamp) * NS2S;

            for(int index = 0; index < 3;++index){
                velocity[index] += (event.values[index] + last_values[index])/2 * dt;
                position[index] += velocity[index] * dt;
            }
        }
        else{
            last_values = new float[3];
            velocity = new float[3];
            position = new float[3];
            velocity[0] = velocity[1] = velocity[2] = 0f;
            position[0] = position[1] = position[2] = 0f;
        }
        System.arraycopy(event.values, 0, last_values, 0, 3);
        last_timestamp = event.timestamp;

        if(trial == 1) {
            t1[curIdx] = position;
        } else if (trial == 2) {
            t2[curIdx] = position;
        } else if (trial == 3) {
            t3[curIdx] = position;
        }

    }

}
