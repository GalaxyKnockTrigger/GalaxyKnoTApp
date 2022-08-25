package com.capstone.galaxyknot.listener;

import static com.capstone.galaxyknot.Constants.RECEIVING_TIME;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.fft.FloatFFT_1D;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public
class AccListener extends IMUListener {
    public AccListener() {
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
        float[] data = dataQueue.stream().sorted(new Comparator<float[]>() {
            @Override
            public int compare(float[] o1, float[] o2) {
                float f1 = 0, f2 = 0;

                for(int i = 0; i < 3; i++){
                    f1 += o1[i] * o1[i];
                    f2 += o2[i] * o2[i];
                }

                return Float.compare(f2, f1);
            }
        }).iterator().next();

        Log.i("AccListener GetData", ": " + data[0] + ", " + data[1] + ", " + data[2]);

        int maxIdx = dataQueue.indexOf(data);
        LinkedList<Float> ret = new LinkedList<>();
        StringBuilder d = new StringBuilder().append(dataQueue.size()).append(',');

        for(int j = 0; j < 3; j++) {
            for (int i = dataQueue.size()/2; i < dataQueue.size(); i++) {
                d.append(dataQueue.get(i)[j]).append(',');
            }
            d.append('\n');
        }

        for(int j = 0; j < 3; j++) {
            for (int i = maxIdx; i < maxIdx + 8; i++) {
                ret.add(dataQueue.get(i)[j]);
//                d.append(ret.getLast()).append(',');
            }
        }
        Log.i("ACCEL_VAL", d.toString());

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
        Log.i("ACC_RAW_VAL_SIZE", "" + dataQueue.size());
        Log.i("ACC_RAW_VAL", ret.toString());
        dataQueue.clear();
        return ret.toString();
    }

    public void doFFT(){
        final int SAMPLE_RATE = 100;

        final int ZERO_PADDING_SIZE = 200;

        final int INPUT_SAMPLE_SIZE = 16;


        int blockSize = INPUT_SAMPLE_SIZE + ZERO_PADDING_SIZE;

        double[][] toTransform = new double[3][2*blockSize];   // Real + Imagine
        double[][] mag = new double[3][blockSize];             // toTransform_size / 2

        int i = 0;
        for(Object arr : dataQueue.toArray()){
            for(int j = 0; j < 3; j++){
                toTransform[j][2*i] = ((float[])arr)[j];
                toTransform[j][2*i + 1] = 0;
            }
            i+=1;

            if(i > INPUT_SAMPLE_SIZE)
                break;
        }

        DoubleFFT_1D fft = new DoubleFFT_1D(blockSize);
        for(i = 0; i<3; i++)
            fft.complexForward(toTransform[i]);

        for(int j = 0; j < 3; j++){
            for(i = 0; i < blockSize / 2; i++){
                mag[j][i] = Math.sqrt(Math.pow(toTransform[j][2*i],2) + Math.pow(toTransform[j][2*i + 1], 2));
            }
        }

        StringBuilder[] fftResult = new StringBuilder[3];

        for(i = 0; i < 3; i++){
            fftResult[i] = new StringBuilder();
            for(double val : mag[i]){
                fftResult[i].append(val).append(',');
            }
        }
        Log.i("ACC_FFT_VAL_X", fftResult[0].toString());
        Log.i("ACC_FFT_VAL_Y", fftResult[1].toString());
        Log.i("ACC_FFT_VAL_Z", fftResult[2].toString());
//
//        for(i = 0; i<)

    }
}