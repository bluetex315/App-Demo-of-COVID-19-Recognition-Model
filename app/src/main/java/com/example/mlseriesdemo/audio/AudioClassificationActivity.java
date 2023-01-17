package com.example.mlseriesdemo.audio;

import static android.content.ContentValues.TAG;
import static android.util.Log.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

//import androidx.core.app.ActivityCompat;
import com.example.mlseriesdemo.R;
import com.example.mlseriesdemo.jlibrosa.src.main.java.com.jlibrosa.audio.JLibrosa;

import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.pytorch.LiteModuleLoader;


public class AudioClassificationActivity extends Activity {

    // UI elements
    private static final int REQUEST_RECORD_AUDIO = 13;
    protected TextView outputTextView;
    protected Button breathingRecordingButton;
    protected Button coughRecordingButton;
    protected Button speechRecordingButton;
    protected Button stopRecordingButton;

    // Working variables.

//    window_length = 25  # In milliseconds
//    window_step = 10    # In milliseconds
//    n_fft = 512
//
//    partials_n_frames = 300
//    audio_norm_target_dBFS = -30

    private Module module_b;
    private Module module_c;
    private Module module_s;
    private Module module_ffn;

    private static final int SAMPLE_RATE = 16000;
    private static final int AUDIO_NORM_TARGET_dBFS = -30;
    private static final int WINDOW_LENGTH = 25;
    private static final int WINDOW_STEP = 10;
    private static final int N_FFT = 512;
    private static final int nMFCC = 512;
    private static final int n_mels = 512;
    private static final int PARTIAL_N_FRAMES = 300;

    private static final double INT16_MAX = Math.pow(2, 15) - 1;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int RECORDING_LENGTH_B = 0;
    private int RECORDING_LENGTH_C = 0;
    private int RECORDING_LENGTH_S = 0;

    AudioRecord audioRecord;
    ArrayList<Short> recordingBufferBreathing = new ArrayList<>();
    ArrayList<Short> recordingBufferCough = new ArrayList<>();
    ArrayList<Short> recordingBufferSpeech = new ArrayList<>();

    boolean shouldContinue = true;
    private Thread breathingRecordingThread;
    private Thread coughRecordingThread;
    private Thread speechRecordingThread;
    boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();

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

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
    }

    private String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            Log.d(TAG, "name of file"+file.getAbsolutePath());
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, assetName + ": " + e.getLocalizedMessage());
        }
        return null;
    }

//    private void requestMicrophonePermission() {
//        requestPermissions(
//                new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == REQUEST_RECORD_AUDIO
//                && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        }
//    }

    public void startBreathingRecord(View view) {
        if (breathingRecordingThread != null) {
            return;
        }

        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);

//        breathingRecord();
        shouldContinue = true;
        breathingRecordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d("thread","successfully started thread");
                                breathingRecord();
                            }
                        });

        breathingRecordingThread.start();
    }

    @SuppressLint("MissingPermission")
    public void breathingRecord() {
        Log.d("message", "119 can we make here?");

        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        short[] audioBuffer = new short[bufferSize];

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("error", "Audio Record can't initialize!");
            return;
        }

        audioRecord.startRecording();
        Log.d("display", "successfully started recording");
        while(shouldContinue && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            int numberRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            for (int i = 0; i < audioBuffer.length; i ++){
                recordingBufferBreathing.add(audioBuffer[i]);
            }
            Log.d("line142","can we make here?"+numberRead);
        }
        RECORDING_LENGTH_B = recordingBufferBreathing.size();
        Log.d("line146", "can we make here?"+RECORDING_LENGTH_B);

    }

    public void onStartCoughRecording(View view) {
        if (coughRecordingThread != null) {
            return;
        }

        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);

//        breathingRecord();
        shouldContinue = true;
        coughRecordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d("thread","successfully started thread");
                                coughRecord();
                            }
                        });

        coughRecordingThread.start();
    }

    @SuppressLint("MissingPermission")
    public void coughRecord() {
        Log.d("message", "184 can we make here?");

        int bufferSize = audioRecord.getMinBufferSize(
                SAMPLE_RATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        short[] audioBuffer = new short[bufferSize];

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("error", "Audio Record can't initialize!");
            return;
        }

        audioRecord.startRecording();
        Log.d("display", "successfully started recording");
        while(shouldContinue && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            int numberRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            for (int i = 0; i < audioBuffer.length; i ++){
                recordingBufferCough.add(audioBuffer[i]);
            }
            Log.d("line213","can we make here?"+numberRead);
        }
        RECORDING_LENGTH_C = recordingBufferCough.size();
        Log.d("line216", "can we make here?"+RECORDING_LENGTH_C);

    }
    public void onStartSpeechRecording(View view) {

        if (speechRecordingThread != null) {
            return;
        }

        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(true);

//        breathingRecord();
        shouldContinue = true;
        speechRecordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d("thread","successfully started thread");
                                speechRecord();
                            }
                        });

        speechRecordingThread.start();
    }

    @SuppressLint("MissingPermission")
    public void speechRecord() {
        Log.d("message", "184 can we make here?");

        int bufferSize = audioRecord.getMinBufferSize(
                SAMPLE_RATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        short[] audioBuffer = new short[bufferSize];

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("error", "Audio Record can't initialize!");
            return;
        }

        audioRecord.startRecording();
        Log.d("display", "successfully started recording");
        while(shouldContinue && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
            int numberRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            for (int i = 0; i < audioBuffer.length; i ++){
                recordingBufferSpeech.add(audioBuffer[i]);
            }
            Log.d("line213","can we make here?"+numberRead);
        }
        RECORDING_LENGTH_S = recordingBufferSpeech.size();
        Log.d("line331", "can we make here?"+RECORDING_LENGTH_S);
    }

    public void onStopRecording(View view) {

        breathingRecordingButton.setEnabled(true);
        coughRecordingButton.setEnabled(true);
        speechRecordingButton.setEnabled(true);
        stopRecordingButton.setEnabled(false);

        shouldContinue = false;

        audioRecord.stop();
        audioRecord.release();

        d("display", "successfully stopped recording");
    }

    public synchronized void onStartRecognition(View view) {
        if (recognitionThread != null) {
            return;
        }

        breathingRecordingButton.setEnabled(false);
        coughRecordingButton.setEnabled(false);
        speechRecordingButton.setEnabled(false);
        stopRecordingButton.setEnabled(false);

        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    private void recognize(){
        int HOP_LENGTH = SAMPLE_RATE * WINDOW_STEP / 1000;
        Log.d("","start recognition");

        float[] breathingProcessedBuffer = process_wav(RECORDING_LENGTH_B, recordingBufferBreathing);
        float[] coughProcessedBuffer = process_wav(RECORDING_LENGTH_C, recordingBufferCough);
        float[] speechProcessedBuffer = process_wav(RECORDING_LENGTH_S, recordingBufferSpeech);

//         Log.d("","can we make here 275?"+RECORDING_LENGTH_B+" "+RECORDING_LENGTH_C+" "+RECORDING_LENGTH_S);
        JLibrosa JLib = new JLibrosa();

        Complex[][] breathing_stft = JLib.generateSTFTFeatures(breathingProcessedBuffer, SAMPLE_RATE, nMFCC, N_FFT, n_mels, HOP_LENGTH);
        Complex[][] cough_stft = JLib.generateSTFTFeatures(coughProcessedBuffer, SAMPLE_RATE, nMFCC, N_FFT, n_mels, HOP_LENGTH);
        Complex[][] speech_stft = JLib.generateSTFTFeatures(speechProcessedBuffer, SAMPLE_RATE, nMFCC, N_FFT, n_mels, HOP_LENGTH);

//         Log.d("","can we make here 298?"+breathing_stft[0][0]+" "+breathing_stft[0][0]);

        if (module_b == null) {
            module_b = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "breathing_optimized.ptl"));
//             Log.d("","can we make here 392?");
        }

        if (module_c == null) {
            module_c = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "cough_optimized.ptl"));
//             Log.d("","can we make here 397?");
        }

        if (module_s == null) {
            module_s = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "speech_optimized.ptl"));
//             Log.d("","can we make here 402?");
        }

        if (module_ffn == null) {
            module_ffn = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "ffn_optimized.ptl"));
//             Log.d("","can we make here 407?");
        }

//        int total_length = breathing_stft.length*breathing_stft[0].length;
        int total_length = 300 * 257;
        FloatBuffer inTensorBuffer_b = Tensor.allocateFloatBuffer(total_length);
        FloatBuffer inTensorBuffer_c = Tensor.allocateFloatBuffer(total_length);
        FloatBuffer inTensorBuffer_s = Tensor.allocateFloatBuffer(total_length);
        Log.d("","can we make here 409?");

//        double[][] breathing_input = new double[breathing_stft.length][breathing_stft[0].length];
        double[][] breathing_input = new double[257][300];
//        double[][] breathing_input = new double[breathing_stft.length][breathing_stft[0].length];
        double[][] cough_input = new double[257][300];
//        double[][] breathing_input = new double[breathing_stft.length][breathing_stft[0].length];
        double[][] speech_input = new double[257][300];
//         Log.d("","can we make here 417?");

        for (int i = 0; i < breathing_input.length; i ++){
            for (int j = 0; j < breathing_input[0].length; j ++){
                breathing_input[i][j] = 0;
                inTensorBuffer_b.put((float)breathing_input[i][j]);
            }
        }

        for (int i = 0; i < cough_input.length; i ++){
            for (int j = 0; j < cough_input[0].length; j ++){
                cough_input[i][j] = 0;
                inTensorBuffer_c.put((float)cough_input[i][j]);
            }
        }

        for (int i = 0; i < speech_input.length; i ++){
            for (int j = 0; j < speech_input[0].length; j ++){
                speech_input[i][j] = 0;
                inTensorBuffer_s.put((float)speech_input[i][j]);
            }
        }
//        int current_length = breathing_input[0].length;
//
//        while (current_length < PARTIAL_N_FRAMES){
//            current_length *= 2;
//            double[][] extend_breathing_input = new double[][]
//        }
//        def generate_test_sequence(feature, partial_n_frames, shift=None):
//            while feature.shape[0] <= partial_n_frames:
//              feature = np.repeat(feature, 2, axis=0)
//            if shift is None:
//              shift = partial_n_frames // 2
//            test_sequence = []
//              start = 0
//            while start + partial_n_frames <= feature.shape[0]:
    //            test_sequence.append(feature[start: start + partial_n_frames])
    //            start += shift
//            test_sequence = np.stack(test_sequence, axis=0)
//            return test_sequence
//

//         Log.d("","can we make here 459?");
        Tensor inTensor_b = Tensor.fromBlob(inTensorBuffer_b , new long[]{1, breathing_input[0].length, breathing_input.length});
        Tensor inTensor_c = Tensor.fromBlob(inTensorBuffer_c , new long[]{1, cough_input[0].length, cough_input.length});
        Tensor inTensor_s = Tensor.fromBlob(inTensorBuffer_s , new long[]{1, speech_input[0].length, speech_input.length});

//         for (long shape : inTensor_b.shape())
//             Log.d("shape is", String.valueOf(shape));

        final Tensor result_b = module_b.forward(IValue.from(inTensor_b)).toTensor();
        final Tensor result_c = module_c.forward(IValue.from(inTensor_c)).toTensor();
        final Tensor result_s = module_s.forward(IValue.from(inTensor_s)).toTensor();

//         Log.d("", "can we make here 471");
//         for (long shape : result_b.shape())
//             Log.d("can we make here 473 shape is", String.valueOf(shape));
//         for (long shape : result_c.shape())
//             Log.d("can we make here 475 shape is", String.valueOf(shape));
//         for (long shape : result_s.shape())
//             Log.d("can we make here 477 shape is", String.valueOf(shape));

        float[] result_b_ffn = result_b.getDataAsFloatArray();
        float[] result_c_ffn = result_c.getDataAsFloatArray();
        float[] result_s_ffn = result_s.getDataAsFloatArray();

        int ffn_length = 512 * 3;
        FloatBuffer inTensorBuffer_ffn = Tensor.allocateFloatBuffer(ffn_length);

        for (int i = 0; i < result_b_ffn.length; i ++){
            inTensorBuffer_ffn.put((float)result_b_ffn[i]);
        }
        for (int i = 0; i < result_c_ffn.length; i ++){
            inTensorBuffer_ffn.put((float)result_c_ffn[i]);
        }
        for (int i = 0; i < result_s_ffn.length; i ++){
            inTensorBuffer_ffn.put((float)result_s_ffn[i]);
        }
        Tensor inTensor_ffn = Tensor.fromBlob(inTensorBuffer_ffn , new long[]{1, 1, ffn_length});
        Log.d("","can we make here 496?");
        final float[] result_ffn = module_ffn.forward(IValue.from(inTensor_ffn)).toTensor().getDataAsFloatArray();
        float covid_prob = result_ffn[0];
        float covid_free_prob = result_ffn[1];
        if (covid_prob > covid_free_prob){
            Log.d("","covid voices detected");
        }
        else{
            Log.d("","No covid voices detected");
        }

    }

    private float[] process_wav(int recordingLength, ArrayList<Short> recordingBuffer){
        double count = 0;
        double rms;
        double wave_dBFS;
        double dBFS_change;

        double[] doubleInputBuffer = new double[recordingLength];
        float[] ResultBuffer = new float[recordingLength];

        for (int i = 0; i < recordingLength; i ++){
            doubleInputBuffer[i] = Math.pow(recordingBuffer.get(i) * INT16_MAX, 2);
            count += doubleInputBuffer[i];

            doubleInputBuffer[i] = recordingBuffer.get(i) / 32767.0;
        }

        rms = Math.pow(count / recordingLength, 2);
        wave_dBFS = 20 * Math.log10(rms / INT16_MAX);
        dBFS_change = AUDIO_NORM_TARGET_dBFS - wave_dBFS;

        for (int i = 0; i < recordingLength; i ++){
            double res = recordingBuffer.get(i) * (Math.pow(10, dBFS_change / 20));
            ResultBuffer[i] = (float) res;
        }
        return ResultBuffer;
    }
}
