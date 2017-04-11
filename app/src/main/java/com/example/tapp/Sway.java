package com.example.tapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

import edu.umd.cmsc436.sheets.Sheets;

public class Sway extends AppCompatActivity implements SensorEventListener, Sheets.Host {
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
    float[][] t1 = new float[11][3];
    float[][] t2 = new float[11][3];
    float[][] t3 = new float[11][3];

    Bitmap mBitmap1;
    Canvas mCanvas1;
    Bitmap mBitmap2;
    Canvas mCanvas2;
    Bitmap mBitmap3;
    Canvas mCanvas3;

    private Path mPath;
    private Paint mPaint;

    private float x_pos, y_pos;

    int width = 400;
    float x_mid = width/2;
    int height = 400;
    float y_mid = height/2;
    float scaleFac = 40;

    private Sheets sheet;
    float distance;
    float[] scores = new float[3];
    double average = 0;

    boolean done = false;

    float[] hardcodeX = {20,0,-50,-30,-50,10,60,30,40,50};
    float[] hardcodeY = {20,60,60,0,-20,-30,10,-40,10,20};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sway);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        initializePaint();

        sheet = new Sheets(this, this, getString(R.string.app_name),
                getString(R.string.class_sheet), getString(R.string.private_sheet));

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
                    //make a drawing and save to external storage
                    mBitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mCanvas1 = new Canvas(mBitmap1);

                    mPath.moveTo(x_mid,y_mid);
                    for (int i = 1; i < 11; i++) {
                        x_pos = t1[i][0]*scaleFac;
                        y_pos = t1[i][1]*scaleFac;
                        mPath.lineTo(x_mid+x_pos,y_mid+y_pos);
                    }

                    mCanvas1.drawPath(mPath, mPaint);

                    savePictureToGallery(mBitmap1, 1);

                    //calculate average distance from center
                    float x = 0;
                    float y = 0;
                    float z = 0;
                    float distance = 0;

                    for(int i = 0; i < 10; i++) {
                        // distance formula
                        distance += Math.sqrt(Math.pow(t1[i][0],2)+Math.pow(t1[i][1],2));
                        // averages
                        x += t1[i][0];
                        y += t1[i][1];
                        z += t1[i][2];
                    }

                    scores[0] = distance/10;
                    tv.setText("\nTrial 1 Done\nScore: " + scores[0]);



                } else if (trial == 2) {
                    TextView tv = (TextView) findViewById(R.id.textView2);
                    //X,Y,Z position captured for each trial at each second in t1, t2, t3
                    //make a drawing and save to external storage
                    initializePaint();

                    mBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mCanvas2 = new Canvas(mBitmap2);

                    for (int i = 0; i < 10; i++) {
                        x_pos = t2[i][0]*scaleFac;
                        y_pos = t2[i][1]*scaleFac;
                        mPath.lineTo(x_pos,y_pos);
                    }

                    mCanvas2.drawPath(mPath, mPaint);

                    savePictureToGallery(mBitmap2, 2);

                    //calculate average distance from center
                    float x = 0;
                    float y = 0;
                    float z = 0;
                    float distance = 0;

                    for(int i = 0; i < 10; i++) {
                        // distance formula
                        distance += Math.sqrt(Math.pow(t2[i][0],2)+Math.pow(t2[i][1],2));
                        // averages
                        x += t2[i][0];
                        y += t2[i][1];
                        z += t2[i][2];
                    }

                    scores[1] = distance/10;
                    tv.setText("\nTrial 2 Done\nScore: " + scores[1]);

                } else if (trial == 3) {
                    TextView tv = (TextView) findViewById(R.id.textView3);
                    //X,Y,Z position captured for each trial at each second in t1, t2, t3
                    //make a drawing and save to external storage
                    initializePaint();

                    mBitmap3 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mCanvas3 = new Canvas(mBitmap3);

                    for (int i = 0; i < 10; i++) {
                        x_pos = t3[i][0]*scaleFac;
                        y_pos = t3[i][1]*scaleFac;
                        mPath.lineTo(x_pos,y_pos);
                    }

                    mCanvas3.drawPath(mPath, mPaint);

                    savePictureToGallery(mBitmap3, 3);

                    //calculate average distance from center
                    float x = 0;
                    float y = 0;
                    float z = 0;
                    float distance = 0;

                    for(int i = 0; i < 10; i++) {
                        // distance formula
                        distance += Math.sqrt(Math.pow(t3[i][0],2)+Math.pow(t3[i][1],2));
                        // averages
                        x += t3[i][0];
                        y += t3[i][1];
                        z += t3[i][2];
                    }

                    scores[2] = distance/10;

                    tv.setText("Trial 3 Done\nScore: " + scores[2]);

                }

                trial++;
                curIdx = 0;

                if(trial < 4) {
                    timer.cancel();
                    timer.start();
                } else {
                    // calculate average score and send to sheets

                    for (int i = 0; i < 3; i++) {
                        average += scores[i];
                    }
                    average /= 3;

                    sendToSheets(average, scores, Sheets.TestType.HEAD_SWAY);

                    vibrator.vibrate(500);

                    done = true;

                    Button but = (Button) findViewById(R.id.startTrials);
                    but.setText("Return");
                    but.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    public void onClick(View v) {
        if (!done) {
            findViewById(R.id.startTrials).setVisibility(View.INVISIBLE);
            timer.start();
        } else {
            finish();
        }
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
        if(trial == 1) {
            System.arraycopy(event.values, 0, t1[curIdx], 0, 3);
        } else if (trial == 2) {
            System.arraycopy(event.values, 0, t2[curIdx], 0, 3);
        } else if (trial == 3) {
            System.arraycopy(event.values, 0, t3[curIdx], 0, 3);
        }

    }

    public void initializePaint() {
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2f);
    }

    private void savePictureToGallery(Bitmap toSave, int trial) {
        String descript = String.format(Locale.getDefault(), "Trial %d", trial);
        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), toSave, "Sway", descript);

        Context context = getApplicationContext();
        CharSequence text = "Saved image to " + savedImageURL;
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }

    private void sendToSheets(double avg, float[] trialScores, Sheets.TestType type) {
        // Send to central sheet
        sheet.writeData(type, getString(R.string.userID), (float)avg);
        // Send to private sheet
        sheet.writeTrials(type, getString(R.string.userID), trialScores);
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
