package com.capstone.galaxyknot.listener;

import static com.capstone.galaxyknot.Constants.RECEIVING_TIME;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class GyroListener extends IMUListener {
    public GyroListener() {
        super();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] values = { sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2] };
        dataQueue.add(values);
//        if(dataQueue.size() > 100){
//            dataQueue.remove(0);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int getDataSize(){
        return dataQueue.size();
    }
    public LinkedList<Float> getData(){

        StringBuilder d = new StringBuilder();

        LinkedList<Float> ret = new LinkedList<>();
        for(int j = 0; j < 3; j++) {
            for (int i = maxIdx; i < maxIdx + 8; i++) {
                ret.add(dataQueue.get(i)[j]);
                d.append(ret.getLast()).append(',');
            }
        }
        Log.i("GYRO_VAL", d.toString());

        dataQueue.clear();
        return ret;
    }
    public String getDataAsCSV (){
        StringBuilder ret = new StringBuilder("#x, y, z\n");
//            long start = timestamps.peek();
        int i = 0;
        while(i < RECEIVING_TIME * 0.1){
//                ret.append(timestamps.poll() - start);

            float[] values = dataQueue.get(i);
            assert values != null;
            ret.append(values[0]).append(',').append(values[1]).append(',').append(values[2]).append("\n");
            i++;
        }

        Log.i("GYRO_RAW_VAL_SIZE", ""+dataQueue.size());
        Log.i("GYRO_RAW_VAL", ret.toString());
        dataQueue.clear();
        return ret.toString();
    }
}
