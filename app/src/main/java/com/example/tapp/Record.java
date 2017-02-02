package com.example.tapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class Record extends AppCompatActivity {

    int lCount = 0;
    int rCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
    }

    public void count(View v) {
        this.lCount++;
        setContentView(v);
        TextView countText = (TextView) findViewById(R.id.countView);
        countText.setText(String.format(Locale.US, "%d", this.lCount));
    }
}
