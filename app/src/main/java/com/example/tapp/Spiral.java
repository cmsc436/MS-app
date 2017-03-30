package com.example.tapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sheets436.Sheets;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.atan2;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class Spiral extends AppCompatActivity {
    private int numTrials = 6;
    private String hand = "left";

    private int trial = 1;
    private int[] lScores = new int[numTrials/2];
    private int[] rScores = new int[numTrials/2];
    private long[] lTimes = new long[numTrials/2];
    private long[] rTimes = new long[numTrials/2];
    private long startTime;
    private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    this.savePictureToGallery();
                break;
            default:
                break;
        }
    }

    public void begin (View v) {
        View save = findViewById(R.id.button3);
        save.setVisibility(View.VISIBLE);
        View draw = findViewById(R.id.draw_view);
        draw.setVisibility(View.VISIBLE);
        View text = findViewById(R.id.textView);
        text.setVisibility(View.VISIBLE);
        View start = findViewById(R.id.start_but);
        start.setVisibility(View.INVISIBLE);
        startTime = System.nanoTime();
    }

    public void next (View v) {
        View save = findViewById(R.id.button3);
        save.setVisibility(View.VISIBLE);
        View next = findViewById(R.id.next_but);
        next.setVisibility(View.INVISIBLE);
        View draw = findViewById(R.id.draw_view);
        draw.setVisibility(View.VISIBLE);
        startTime = System.nanoTime();
    }

    public void saveSpiral(View v) {
        endTime = System.nanoTime();
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            this.savePictureToGallery();
            View save = findViewById(R.id.button3);
            save.setVisibility(View.INVISIBLE);
            View next = findViewById(R.id.next_but);
            next.setVisibility(View.VISIBLE);
            View draw = findViewById(R.id.draw_view);
            draw.setVisibility(View.INVISIBLE);
        }
    }

    private void sendToSheets(int[] scores, long[] durations, int sheet) {
        // Send data to sheets
        Intent sheets = new Intent(this, Sheets.class);

        float avg = 0;
        for (int i = 0; i < numTrials / 2; i++)
            avg += scores[i];
        avg /= numTrials / 2;

        sheets.putExtra(Sheets.EXTRA_VALUE, avg);
        sheets.putExtra(Sheets.EXTRA_USER, getString(R.string.userID));
        sheets.putExtra(Sheets.EXTRA_TYPE, sheet);

        startActivity(sheets);
    }

    private void savePictureToGallery() {
        View drawing = (View) findViewById(R.id.draw_view);
        drawing.setDrawingCacheEnabled(true);
        Bitmap user_drawn = drawing.getDrawingCache();

        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.cropped_spiral);
        Bitmap bmp2 = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas3 = new Canvas(bmp2);
        Matrix m = new Matrix();
        m.setScale((float) bmp1.getWidth() / user_drawn.getWidth(), (float) bmp1.getHeight() / user_drawn.getHeight());
        canvas3.drawBitmap(user_drawn, m, new Paint());

        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas2 = new Canvas(bmOverlay);
        canvas2.drawBitmap(bmp1, new Matrix(), null);
        canvas2.drawBitmap(bmp2, new Matrix(), null);

        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), bmOverlay, "Spiral", "Image of spiral");

        Context context = getApplicationContext();
        CharSequence text = "Saved image to " + savedImageURL;
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
        drawing.setDrawingCacheEnabled(false);
        displayScore();
    }

    private void displayScore() {

        int score = score_spiral();
        if (hand == "left") {
            lScores[trial-1] = score;
            lTimes[trial-1] = (endTime-startTime)/(1000000);
        } else {
            rScores[trial-1] = score;
            rTimes[trial-1] = (endTime-startTime)/(1000000);
        }
        TextView text = (TextView) findViewById(R.id.textView);
        text.setText("Score: " + score);

        DrawView drawing = (DrawView) findViewById(R.id.draw_view);
        drawing.clear();
        if (trial < 3) {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            trial++;
                        }
                    },
                    2000
            );
        } else if (hand == "left") {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            hand = "right";
                            trial = 1;
                        }
                    },
                    2000
            );
        } else {
            Context context = getApplicationContext();
            CharSequence done = "All trials complete!";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, done, duration).show();
            sendToSheets(lScores, lTimes, Sheets.UpdateType.LH_SPIRAL.ordinal());
            sendToSheets(rScores, rTimes, Sheets.UpdateType.RH_SPIRAL.ordinal());
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },
                    2000
            );
        }
    }


    /* Calculates score based on variance from line r = theta. */
    private int score_spiral() {

        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.cropped_spiral);

        View drawing = (View) findViewById(R.id.draw_view);
        drawing.setDrawingCacheEnabled(true);
        Bitmap user_drawn = drawing.getDrawingCache();

        double score = 0;

        double mid_x = user_drawn.getWidth()/2;
        double mid_y = user_drawn.getHeight()/2;
        double dx = 0;
        double dy = 0;
        double r = 0;
        double theta = 0;
        int index = 0;
        // large data structures necessary to avoid overflow
        double theta_vec[] = new double[60000];
        double r_vec[] = new double[60000];

        for (int i = 0; i < user_drawn.getWidth(); i+=2) {
            for (int j = 0; j < user_drawn.getHeight(); j+=2) {
                // if this pixel is part of the spiral
                if (user_drawn.getPixel(i,j) != 0 && user_drawn.getPixel(i,j) != Color.WHITE && user_drawn.getPixel(i,j) != Color.BLACK) {
                    // get x and y values relative to center of spiral
                    // 24.25 is a scaling factor found experimentally
                    dx = (i-mid_x)/24.25;
                    dy = (mid_y-j)/24.25;
                    // convert to polar coordinates
                    r = sqrt((dx*dx) + (dy*dy));
                    theta = -atan2(dy,dx)+Math.PI/2;

                    // adjust theta beyond 4 quadrants
                    if (theta < 0) {
                        theta = theta + 2 * Math.PI;
                    }
                    if (r > 2*Math.PI) {
                        theta = theta + 2 * Math.PI;
                    }
                    if (r > 4*Math.PI) {
                        theta = theta + 2 * Math.PI;
                    }
                    // store theta and r values
                    r_vec[index] = r;
                    theta_vec[index] = theta;
                    index++;


                }
            }
        }

        double variance = 0;

        // calculate variance as square of difference between r and theta normalized by r
        for (int i = 0; i < index; i++) {
            if (r_vec[i] != 0) {
                variance += (r_vec[i] - theta_vec[i]) * (r_vec[i] - theta_vec[i]) / r_vec[i];
            }
        }
        // score is average variance among points observed with scaling factor and scheme determined
        // by much experimentation to roughly normalize out of 100
        if (index != 0) {
            score = 100*(1.5-(variance / index))*0.7;
        } else {
            score = 0;
        }

        drawing.setDrawingCacheEnabled(false);

        return (int) round(score);
    }

}
