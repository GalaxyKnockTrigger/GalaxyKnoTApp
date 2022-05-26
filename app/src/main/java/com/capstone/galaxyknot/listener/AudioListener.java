package com.capstone.galaxyknot.listener;

import static com.capstone.galaxyknot.Constants.AUDIO_SAMP_RATE;
import static com.capstone.galaxyknot.Constants.RECEIVING_TIME;
import static com.capstone.galaxyknot.Constants.TH_SOUND_PEAK;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.capstone.galaxyknot.StateManager;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.LinkedList;

public class AudioListener implements IGetDataSize {
    private final AudioManager audioManager;
    private AudioRecord recorder;
    private final int bufferShortSize = 4096;
    private short[] bufferRecord;
    private int bufferRecordSize;
    private final ShortBuffer shortBuffer = ShortBuffer.allocate(4096 * 4);
    private final Context context;

    public AudioListener(Context context) {
        this.context = context;
        audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        recorder = null;
//
        Log.i("AUDIO_INFO", "Audio Listener Initialized");
//        setting();
    }

    private final Thread recordStart = new Thread(){
        @Override
        public synchronized void start() {
            startRecording();
        }
    };

    private final Thread recordStop = new Thread(){
        @Override
        public synchronized void start() {
            stopRecording();
        }
    };
    public void onRecord(boolean start) {
        if (start) {
            Log.i("AUDIO_INFO", "On Record Start");
            if(!recordStart.isAlive()){
                recordStart.start();
            }
        } else {
            Log.i("AUDIO_INFO", "On Record Stop");
            if(!recordStop.isAlive())
                recordStop.start();
        }
    }

    private void startRecording() {
        setting();
        Log.i("AUDIO_INFO", "RECEIVE START");

        boolean recordStart = false;

        long start = 0;
        while(StateManager.isClassifierStart.getValue()){
            if(StateManager.isRecordEnd.getValue() && !recordStart){
                continue;
            }
            synchronized (shortBuffer) {
                int size = recorder.read(bufferRecord, 0, bufferRecordSize);
                if (!recordStart) {
                    for(int i = 0; i < bufferRecord.length; i++){
                        short val = bufferRecord[i];
                        if (val > TH_SOUND_PEAK) {
                            Log.i("AUDIO_INFO_VAL", "" + val);
                            recordStart = true;
                            shortBuffer.put(bufferRecord, i, size - i);
                            start = System.currentTimeMillis();
                            break;
                        }
                    }
                } else {
                    shortBuffer.put(bufferRecord, 0, size);
                    if (shortBuffer.position() + bufferRecordSize > bufferShortSize) {
                        StateManager.isRecordEnd.postValue(true);
                        recordStart = false;
                        Log.i("Latency_info", "Collecting Latency: " + (System.currentTimeMillis() - start));
                    }
                }
            }
        }
    }

    private void stopRecording() {
        shortBuffer.position(0);

        if(recorder != null){
            recorder.stop();
            recorder.release();
            recorder = null;
        }

    }

    private void setting(){
        Log.i("AUDIO_INFO", "Setting Buffer Property");
        bufferRecordSize = AudioRecord.getMinBufferSize(AUDIO_SAMP_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        bufferRecord = new short[bufferRecordSize];

        Log.i("AUDIO_INFO", "Check Self Permission");

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                AUDIO_SAMP_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferRecordSize);

        Log.i("AUDIO_INFO", "SampleRate: " + audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
        Log.i("AUDIO_INFO", "Buffer Size: " + audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
        Log.i("AUDIO_INFO", "Buffer Record Size: " + bufferRecordSize);

        recorder.startRecording();
        shortBuffer.rewind();
        shortBuffer.position(0);
    }

    public LinkedList<Short> getData(){
        long start = System.currentTimeMillis();
        Short[] arr = new Short[4096];

        int i = 0;
        StringBuilder sb = new StringBuilder().append('\n');
        synchronized (shortBuffer){
            for(short v : shortBuffer.array()){
                if(i >= 4096)
                    break;
                arr[i] = v;
                sb.append(arr[i]).append('\n');
                i++;
            }
            shortBuffer.rewind();
            shortBuffer.position(0);
        }

        Log.i("AUDIO_VAL", sb.toString());
        Log.i("Latency_info", "Getting Data Latency: " + (System.currentTimeMillis() - start));
        return new LinkedList<>(Arrays.asList(arr));
    }

    @Override
    public int getDataSize(){
        return bufferRecord.length;
    }

}