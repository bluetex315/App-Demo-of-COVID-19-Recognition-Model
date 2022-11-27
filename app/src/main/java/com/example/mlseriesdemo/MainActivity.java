package com.example.mlseriesdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import com.example.mlseriesdemo.audio.AudioClassificationActivity;
import com.example.mlseriesdemo.helpers.AudioHelperActivity;
import com.example.mlseriesdemo.helpers.ImageHelperActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGotoImageActivity(View view){
        // start image helper activity
        Intent intent = new Intent(this, ImageHelperActivity.class);
        startActivity(intent);

    }
    public void onGotoAudioClassification(View view){
        // start audio helper activity
        Intent intent = new Intent(this, AudioClassificationActivity.class);
        startActivity(intent);

    }
}