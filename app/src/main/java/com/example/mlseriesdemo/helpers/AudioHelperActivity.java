package com.example.mlseriesdemo.helpers;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mlseriesdemo.R;

public class AudioHelperActivity extends AppCompatActivity {

    protected TextView outputTextView;
    protected Button breathingRecordingButton;
    protected Button coughRecordingButton;
    protected Button speechRecordingButton;
    protected Button stopRecordingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_helper);

        outputTextView = findViewById(R.id.audio_output_textview);
        breathingRecordingButton = findViewById(R.id.audio_breathing_recording);
        coughRecordingButton = findViewById(R.id.audio_cough_recording);
        speechRecordingButton = findViewById(R.id.audio_speech_recording);
        stopRecordingButton = findViewById(R.id.audio_stop_recording);

        stopRecordingButton.setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
        }
    }

    public void onStartBreathingRecording(View view){
        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
    }

    public void onStartCoughRecording(View view){
        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
    }

    public void onStartSpeechRecording(View view){
        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);
    }

    public void onStopRecording(View view){
        breathingRecordingButton.setEnabled(true);
        coughRecordingButton.setEnabled(true);
        speechRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);
    }
}