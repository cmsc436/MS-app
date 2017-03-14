package com.example.tapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Spiral extends AppCompatActivity {
    // TODO: add PER-TRIAL scores and durations
    private int numTrials = 6;
    private String hand = "left";

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

    public void saveSpiral(View v) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            this.savePictureToGallery();
        }
    }

    private void sendToSheets(int[] scores, int[] durations, int sheet) {
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
            row.add(Integer.toString(durations[i]));

        for (int i = 0; i < numTrials / 2; i++)
            row.add(Integer.toString(scores[i]));

        sheets.putStringArrayListExtra(Sheets.EXTRA_SHEETS, row);
        sheets.putExtra(Sheets.EXTRA_TYPE, sheet);
        startActivity(sheets);
    }

    private void savePictureToGallery() {
        View drawing = (View) findViewById(R.id.draw_view);
        drawing.setDrawingCacheEnabled(true);
        Bitmap bitmap = drawing.getDrawingCache();

        Bitmap combined = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);

        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), combined, "Spiral", "Image of spiral");

        Context context = getApplicationContext();
        CharSequence text = "Saved image to " + savedImageURL;
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
        finish();
    }

}
