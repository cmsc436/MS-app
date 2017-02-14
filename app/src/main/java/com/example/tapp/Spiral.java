package com.example.tapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Spiral extends AppCompatActivity {

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

    private void savePictureToGallery() {
        View drawing = (View) findViewById(R.id.draw_view);
        drawing.setDrawingCacheEnabled(true);
        Bitmap bitmap = drawing.getDrawingCache();
        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Spiral", "Image of spiral");

        Context context = getApplicationContext();
        CharSequence text = "Saved image to " + savedImageURL;
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
        finish();
    }
}
