package com.capstone.galaxyknot;

import static com.capstone.galaxyknot.Constants.PORT;
import static com.capstone.galaxyknot.Constants.TRAINING_SET_COUNT;
import static com.capstone.galaxyknot.Constants.TYPE_ACCEL;
import static com.capstone.galaxyknot.Constants.TYPE_GYRO;
import static com.capstone.galaxyknot.Constants.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.capstone.galaxyknot.fragment.ClassifierFragment;
import com.capstone.galaxyknot.listener.AccListener;
import com.capstone.galaxyknot.listener.AudioListener;
import com.capstone.galaxyknot.listener.GyroListener;
import com.capstone.galaxyknot.listener.IMUListener;
import com.capstone.galaxyknot.listener.IMUListenerFactory;

import org.conscrypt.Conscrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AppManager {

    private final AudioListener audioListener;
    private final AccListener accListener;
    private final GyroListener gyroListener;

    private SensorManager sensorManager;
    private Sensor accSensor, gyroSensor;

    private OkHttpClient okHttpClient;

    private LifecycleOwner classifierOwner, collectorOwner;

//    private boolean isClassifierStartChanged = false;
//    private boolean isCollectorStartChanged = false;

//    private Map<String, String> cmdAndLabel;
    private SharedPreferences sharedPref;
//    private final Thread postValues = new Thread(() -> {
//        try {
//            RequestBody requestBody = RequestBody.create(
//                    compress(audioListener.getData()),
//                    MediaType.parse("text/plain")
//            );
//            Request request = new Request.Builder()
//                    .addHeader("label", "Test") // TODO: 테스트 끝나면 삭제
//                    .addHeader("type", "collector")
//                    .post(requestBody)
//                    .url(URL + ":" + PORT)
//                    .build();
//
//            final long cstart = System.currentTimeMillis();
//            okHttpClient.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                    Log.i("NETWORK_TEST", "Network Failed\n");
//
//                    e.printStackTrace();
//                    Log.i("NETWORK_TEST", "CNT: " + okHttpClient.connectionPool().connectionCount());
//                    okHttpClient.connectionPool().evictAll();
//                }
//
//                @Override
//                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                    Log.i("NETWORK_TEST", "File Transfer SUCCESS");
////                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
//                    response.body().close();
//                    Log.i("Latency_info", "communication latency: " + (System.currentTimeMillis() - cstart));
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            StateManager.isRecordEnd.postValue(false);
//        }
//    });

    private Map<String, Thread> sensingThList;

    private AppManager(Context context) {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        okHttpClient = TrustOkHttpClientUtil.getUnsafeOkHttpClient().build();

        sharedPref = context.getSharedPreferences("com.capstone.galaxyknot", Context.MODE_PRIVATE);
//        sharedPref.edit().clear().apply();

        if(!sharedPref.contains("userid")){
            Log.i("User_ID", "get User Id Start");
            getUserId();
        }

        audioListener = new AudioListener(context);

        accListener = (AccListener) IMUListenerFactory.getListener(TYPE_ACCEL);
        gyroListener = (GyroListener) IMUListenerFactory.getListener(TYPE_GYRO);

        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(TYPE_ACCEL);
        gyroSensor = sensorManager.getDefaultSensor(TYPE_GYRO);

        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        okHttpClient = TrustOkHttpClientUtil.getUnsafeOkHttpClient().build();

        sharedPref = context.getSharedPreferences("com.capstone.galaxyknot", Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();

        sensingThList = new HashMap<>();

//
//        cmdAndLabel = (Map<String, String>) sharedPref.getAll();
//
//        for(String key : cmdAndLabel.keySet()){
//            Log.i("APP_MANAGER", "KEY_MAP\t" + key + "\t" + cmdAndLabel.get(key));
//        }
    }
//    public String getCommand(String cmd){
//        return cmdAndLabel.get(cmd);
//    }
//    public void addCommand(String label, String cmd){
//
//        if(cmdAndLabel.containsKey(cmd)){
//            if(cmdAndLabel.get(cmd).equals(label)){
//                return;
//            }
//        }
//
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(cmd, label);
//        editor.apply();
//
//        cmdAndLabel.put(cmd, label);
//    }

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

    public void startThreads(){
        if(sensingThList.containsKey("AccelTh")){
            if(!sensingThList.get("AccelTh").isAlive())
                sensingThList.get("AccelTh").start();
        }
        else{
            startIMUThread(accListener, accSensor);
        }
        if(sensingThList.containsKey("GyroTh")){
            if(!sensingThList.get("GyroTh").isAlive())
                sensingThList.get("GyroTh").start();
        }
        else{
            startIMUThread(gyroListener, gyroSensor);
        }
        if(sensingThList.containsKey("AudioTh")){
            if(!sensingThList.get("AudioTh").isAlive())
                sensingThList.get("AudioTh").start();
        }
        else{
            startAudioThread();
        }
    }

    public void stopThreads(){
        for(Thread th : sensingThList.values()){
            th.interrupt();
        }
    }

    private void startIMUThread(IMUListener listener, Sensor sensor){
        Thread imuTh = new Thread(() -> {
            while(true){
                synchronized (listener) {
                    try {
                        Log.i("IMU_TH", "Thread IN");
                        listener.wait();
                        Log.i("IMU_TH", "Thread Wait End");
                        if (StateManager.isNowClassifierState.get()) {

                            Log.i("IMU_TH", "Classifier Start Changed to " + (StateManager.isClassifierStart.getValue() ? "True" : "False"));

                            if(StateManager.isClassifierStart.getValue()){
                                sensorManager.registerListener(listener, sensor, sensor.getMinDelay());
                            }
                            else{
                                sensorManager.unregisterListener(listener);
                            }

                        } else if (!StateManager.isNowClassifierState.get()) {

                            Log.i("IMU_TH", "Collector Start Changed to " + (StateManager.isCollectorStart.getValue() ? "True" : "False"));

                            if(StateManager.isCollectorStart.getValue()){
                                sensorManager.registerListener(listener, sensor, sensor.getMinDelay());
                            }
                            else{
                                sensorManager.unregisterListener(listener);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if(listener instanceof AccListener){
            imuTh.setName("AccelTh");
        }
        else{
            imuTh.setName("GyroTh");
        }

        sensingThList.put(imuTh.getName(), imuTh);

        imuTh.start();
    }

    private void startAudioThread(){
        Thread audioTh = new Thread(() -> {
            while(true){
                synchronized (audioListener) {
                    try {
                        Log.i("AUDIO_TH", "Thread IN");
                        audioListener.wait();
                        Log.i("AUDIO_TH", "Thread Wait End");
                        if (StateManager.isNowClassifierState.get()) {
                            Log.i("AUDIO_TH", "Classifier Start Changed to " + (StateManager.isClassifierStart.getValue() ? "True" : "False"));
                            audioListener.onRecord(StateManager.isClassifierStart.getValue());

                        } else if (!StateManager.isNowClassifierState.get()) {
                            Log.i("AUDIO_TH", "Collector Start Changed to " + (StateManager.isCollectorStart.getValue() ? "True" : "False"));
                            audioListener.onRecord(StateManager.isCollectorStart.getValue());

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        audioTh.setName("AudioTh");

        sensingThList.put(audioTh.getName(), audioTh);

        audioTh.start();
    }

    public void setClassifierOwner(LifecycleOwner classifierOwner, ClassifierFragment frag) {
        this.classifierOwner = classifierOwner;
        if(!StateManager.isClassifierStart.hasObservers()){
            StateManager.setObserver(StateManager.CLASSIFIER_START, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    Log.i("AUDIO_INFO", "CLASSIFIER_START_CHANGED");
//                    isClassifierStartChanged = true;
                    synchronized (audioListener){
                        audioListener.notifyAll();
                    }
                    synchronized (accListener){
                        accListener.notifyAll();
                    }
                    synchronized (gyroListener){
                        gyroListener.notifyAll();
                    }
                }
            }, classifierOwner);
            StateManager.setObserver(StateManager.RECORD_END,
                (Boolean val) -> {
                    // record end가 true일 때
                    if(val && StateManager.isNowClassifierState.get()){
                        Log.i("AUDIO_INFO", "RECORD is ENDED + CLASSIFIER");
                        try {
                            final long cstart = System.currentTimeMillis();

                            byte[] data = compress(
                                    convertList2CSV(audioListener.getData())
                                    + convertList2CSV(accListener.getData())
                                    + convertList2CSV(gyroListener.getData())
                            );

                            Log.i("NETWORK", "MAKE REQUEST BODY");
                            RequestBody requestBody = RequestBody.create(
                                    data,
                                    MediaType.parse("text/plain")
                            );
                            Log.i("NETWORK", "MAKE REQUEST");

                            Request request = new Request.Builder()
                                    .addHeader("type","classifier")
                                    .addHeader("user-id", ""+sharedPref.getLong("userid", 0))
                                    .post(requestBody)
                                    .url(URL + ":" + PORT)
                                    .build();

                            long mid = System.currentTimeMillis();
                            Log.i("NETWORK", "SEND_DATA");
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

                                    String label = response.body().string();

//                                    Log.i("Classifier_response_body", cmd + "\t" + cmdAndLabel.get(cmd));
                                    StateManager.label = label;
                                    StateManager.doShowToast.postValue(true);

                                    Objects.requireNonNull(response.body()).close();
                                    long end = System.currentTimeMillis();
                                    Log.i("Latency_info", "total communication latency: " + (end -cstart));
                                    Log.i("Latency_info", "only communication latency: " + (end -mid));
                                    Log.i("Latency_info", "communication prepare latency: " + (mid - cstart));

                                    StateManager.isClassifierStart.postValue(false);

                                    // TODO: 네트워크 복구되면 주석 해제
                                    frag.onClassifierButtonClick();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally{
                            StateManager.isRecordEnd.postValue(false);

                            // TODO: 네트워크 복구되면 주석 처리.
//                            frag.onClassifierButtonClick();
                        }
                    }
                }
            , classifierOwner);
        }
    }

    public void setCollectorOwner(LifecycleOwner collectorOwner) {
        this.collectorOwner = collectorOwner;
        if(!StateManager.isCollectorStart.hasObservers()) {
            StateManager.setObserver(StateManager.COLLECTOR_START, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    Log.i("AUDIO_INFO", "COLLECTOR_START_CHANGED");
//                    isCollectorStartChanged = true;
                    synchronized (accListener){
                        accListener.notifyAll();
                    }
                    synchronized (gyroListener){
                        gyroListener.notifyAll();
                    }
                    synchronized (audioListener){
                        audioListener.notifyAll();
                    }

                }
            }, collectorOwner);
            StateManager.setObserver(StateManager.RECORD_END, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean val) {
                    // record end가 true일 때
                    if(val && !StateManager.isNowClassifierState.get()) {
                        Log.i("AUDIO_INFO", "RECORD is ENDED + COLLECTOR");
                        try {
                            final long cstart = System.currentTimeMillis();
                            byte[] data = compress(
                                    convertList2CSV(audioListener.getData())
                                            + convertList2CSV(accListener.getData())
                                            + convertList2CSV(gyroListener.getData())
                            );
                            Log.i("NETWORK", "MAKE REQUEST BODY");
                            RequestBody requestBody = RequestBody.create(
                                    data,
                                    MediaType.parse("text/plain")
                            );
                            Log.i("NETWORK", "MAKE REQUEST");

                            String cmd = StateManager.trainingCmd.get();
                            String label = StateManager.trainingLabel.get();
                            Log.i("NETWORK_CMD", cmd);
                            Log.i("NETWORK_LABEL", label);

                            Request request = new Request.Builder()
                                    .addHeader("type", "collector")
                                    .addHeader("label", label)
                                    .addHeader("cmd", cmd)
                                    .addHeader("user-id", ""+sharedPref.getLong("userid", 0))
                                    .post(requestBody)
                                    .url(URL + ":" + PORT)
                                    .build();

                            long mid = System.currentTimeMillis();
                            Log.i("NETWORK", "SEND_DATA");
                            okHttpClient.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.i("NETWORK_TEST", "Network Failed\n");

                                    e.printStackTrace();
                                    Log.i("NETWORK_TEST", "CNT: " + okHttpClient.connectionPool().connectionCount());
                                    okHttpClient.connectionPool().evictAll();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    Log.i("NETWORK_TEST", "File Transfer SUCCESS");
//                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                    response.body().close();
                                    long end = System.currentTimeMillis();
                                    Log.i("Latency_info", "total communication latency: " + (end - cstart));
                                    Log.i("Latency_info", "only communication latency: " + (end - mid));
                                    Log.i("Latency_info", "communication prepare latency: " + (mid - cstart));
                                    if(StateManager.trainingCount.getValue() != null){
                                        int newVal = StateManager.trainingCount.getValue() + 1;
                                        if(newVal > TRAINING_SET_COUNT){
                                            newVal = 1;
//                                            addCommand(StateManager.trainingLabel.get(), StateManager.trainingCmd.get());
                                            StateManager.isCollectorStart.postValue(false);

                                            RequestBody requestBody = RequestBody.create(
                                                    compress( "0"),
                                                    MediaType.parse("text/plain")
                                            );
                                            Request request = new Request.Builder()
                                                    .addHeader("type", "collectingEnd")
                                                    .addHeader("user-id", ""+sharedPref.getLong("userid", 0))
                                                    .post(requestBody)
                                                    .url(URL + ":" + PORT)
                                                    .build();
                                            okHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                    Log.i("COLLECTOR", "Training Complete");
                                                }
                                            });
                                        }
                                        StateManager.trainingCount.postValue(newVal);
                                        Log.i("COLLECTOR_CNT", "" + newVal);
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            StateManager.isRecordEnd.postValue(false);
                        }
                    }
                }
            }, collectorOwner);
        }
    }

    private String convertList2CSV(List<?> list){
        StringBuilder stringBuilder = new StringBuilder();

        for(Object o : list){
            stringBuilder.append(o.toString()).append(',');
        }
        return stringBuilder.toString();
    }

    private byte[] compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }

        long start = System.currentTimeMillis();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        gzip.write(bytes);
        gzip.flush();
        gzip.close();
        Log.i("COMPRESS_INFO_BEFORE size", "" + (2 * str.length()));
        Log.i("COMPRESS_INFO_AFTER size", "" + out.size());

        long end = System.currentTimeMillis();

        Log.i("Latency_info", "compress latency: " + (end - start));
        Log.i("Latency_info", "compress ratio: " + (out.toByteArray().length / (str.length() * 2)));

        return out.toByteArray();
    }
}
