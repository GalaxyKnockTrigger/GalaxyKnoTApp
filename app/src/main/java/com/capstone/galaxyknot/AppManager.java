package com.capstone.galaxyknot;

import static com.capstone.galaxyknot.Constants.PORT;
import static com.capstone.galaxyknot.Constants.URL;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.capstone.galaxyknot.listener.AudioListener;

import org.conscrypt.Conscrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Security;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppManager {

    private AudioListener audioListener;
    private OkHttpClient okHttpClient;

    private LifecycleOwner classifierOwner, collectorOwner;

    private boolean isClassifierStartChanged = false;
    private boolean isCollectorStartChanged = false;
    private final Thread postValues = new Thread(()->{
        try {
            RequestBody requestBody = RequestBody.create(
                    compress(audioListener.getData()),
                    MediaType.parse("text/plain")
            );
            Request request = new Request.Builder()
                    .addHeader("label", "Test") // TODO: 테스트 끝나면 삭제
                    .addHeader("type","collector")
                    .post(requestBody)
                    .url(URL + ":" + PORT)
                    .build();

            final long cstart = System.currentTimeMillis();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("NETWORK_TEST", "Network Failed\n");

                    e.printStackTrace();
                    Log.i("NETWORK_TEST", "CNT: "+okHttpClient.connectionPool().connectionCount());
                    okHttpClient.connectionPool().evictAll();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.i("NETWORK_TEST", "File Transfer SUCCESS");
//                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    response.body().close();
                    Log.i("Latency_info", "communication latency: " + (System.currentTimeMillis()-cstart));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            StateManager.isRecordEnd.postValue(false);
        }
    });

    private AppManager(Context context){
        audioListener = new AudioListener(context);
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        okHttpClient = TrustOkHttpClientUtil.getUnsafeOkHttpClient().build();
    }

    private static AppManager instance = null;

    public static AppManager getInstance(Context context){
        instance = new AppManager(context);
        return instance;
    }

    public static AppManager getInstance() throws NullPointerException{
        if(instance == null){
            throw new NullPointerException();
        }
        return instance;
    }

    public void startAudioThread(){
        Thread audioTh = new Thread(() -> {
            while(true){
                synchronized (audioListener) {
                    try {
                        Log.i("AUDIO_TH", "Thread IN");
                        audioListener.wait();
                        Log.i("AUDIO_TH", "Thread Wait End");
                        if (isClassifierStartChanged && StateManager.isNowClassifierState.get()) {
                            isClassifierStartChanged = false;
                            Log.i("AUDIO_TH", "Classifier Start Changed");
                            audioListener.onRecord(StateManager.isClassifierStart.getValue());
                            Log.i("AUDIO_TH", "Change to ");

                        } else if (isCollectorStartChanged && !StateManager.isNowClassifierState.get()) {
                            isCollectorStartChanged = false;
                            Log.i("AUDIO_TH", "Collector Start Changed");
                            audioListener.onRecord(StateManager.isCollectorStart.getValue());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        audioTh.start();
    }

    public void setClassifierOwner(LifecycleOwner classifierOwner) {
        this.classifierOwner = classifierOwner;
        if(!StateManager.isClassifierStart.hasObservers()){
            StateManager.setObserve(StateManager.CLASSIFIER_START, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    Log.i("AUDIO_INFO", "CLASSIFIER_START_CHANGED");
                    isClassifierStartChanged = true;
                    synchronized (audioListener){
                        audioListener.notifyAll();
                    }
                }
            }, classifierOwner);
            StateManager.setObserve(StateManager.RECORD_END, new Observer<Boolean>() {
                private boolean isStart = false;
                @Override
                public void onChanged(Boolean val) {
                    // record end가 true일 때
                    if(val && StateManager.isNowClassifierState.get() && !isStart){
                        isStart = true;
                        Log.i("AUDIO_INFO", "RECORD is ENDED + CLASSIFIER");
                        try {

                            Log.i("NETWORK", "MAKE REQUEST BODY");
                            RequestBody requestBody = RequestBody.create(
                                    compress(audioListener.getData()),
                                    MediaType.parse("text/plain")
                            );
                            Log.i("NETWORK", "MAKE REQUEST");

                            Request request = new Request.Builder()
                                    .addHeader("label", "Test") // TODO: 테스트 끝나면 삭제
                                    .addHeader("type","collector")
                                    .post(requestBody)
                                    .url(URL + ":" + PORT)
                                    .build();

                            Log.i("NETWORK", "SEND_DATA");
                            final long cstart = System.currentTimeMillis();
                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.i("NETWORK_TEST", "Network Failed\n");

                                    e.printStackTrace();
                                    Log.i("NETWORK_TEST", "CNT: "+okHttpClient.connectionPool().connectionCount());
                                    okHttpClient.connectionPool().evictAll();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    Log.i("NETWORK_TEST", "File Transfer SUCCESS");
//                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                    response.body().close();
                                    Log.i("Latency_info", "communication latency: " + (System.currentTimeMillis()-cstart));
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally{
                            StateManager.isRecordEnd.postValue(false);
                            isStart = false;
                        }
                    }
                }
            }, classifierOwner);
        }
    }

    public void setCollectorOwner(LifecycleOwner collectorOwner) {
        this.collectorOwner = collectorOwner;
        if(!StateManager.isCollectorStart.hasObservers()) {
            StateManager.setObserve(StateManager.COLLECTOR_START, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    Log.i("AUDIO_INFO", "CLASSIFIER_START_CHANGED");
                    isCollectorStartChanged = true;
                    synchronized (this) {
                        notifyAll();
                    }
                }
            }, collectorOwner);
            StateManager.setObserve(StateManager.RECORD_END, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean val) {
                    // record end가 true일 때
                    if(val && !StateManager.isNowClassifierState.get()){
                        Log.i("AUDIO_INFO", "RECORD is ENDED + COLLECTOR");
                    }
                }
            }, collectorOwner);
        }


    }

    private byte[] compress(List<Short> sh) throws IOException {
        if (sh == null || sh.size() == 0) {
            return null;
        }

        long start = System.currentTimeMillis();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);

        byte[] bytes = new byte[sh.size() * 2];

        for(int i = 0; i < sh.size(); i++){
            bytes[2*i] = (byte)(sh.get(i) >> 8);
            bytes[2*i + 1] = (byte)(sh.get(i) & 0x00FF);
        }

        gzip.write(bytes);
        gzip.flush();
        gzip.close();
        Log.i("COMPRESS_INFO_BEFORE size", "" + (2*sh.size()));
        Log.i("COMPRESS_INFO_AFTER size", "" + out.size());

        long end = System.currentTimeMillis();

        Log.i("Latency_info", "compress latency: " + (end-start));
        Log.i("Latency_info", "compress ratio: " + (out.toByteArray().length / (sh.size()*2)));

        return out.toByteArray();
    }

}