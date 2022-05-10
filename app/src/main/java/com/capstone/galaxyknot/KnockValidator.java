package com.capstone.galaxyknot;

import static com.capstone.galaxyknot.Constants.TH_SOUND_PEAK;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class KnockValidator {
    private List<Short> audioData;
    private List<float[]> accData;
    private List<float[]> gyroData;

    private List<Short> outputAudioData;
    private List<float[]> outputAccData;
    private List<float[]> outputGyroData;
    private int soundStart = -1;

    public KnockValidator(List<Short> audioData, List<float[]> accData, List<float[]> gyroData){
        this.audioData = new LinkedList<>(audioData);
        this.accData = new LinkedList<>(accData);
        this.gyroData = new LinkedList<>(gyroData);

        audioData.clear();
        accData.clear();
        gyroData.clear();

        long start = System.currentTimeMillis();
        peakDetection();
        Log.i("PEAK_DETECTION_TIME", "" + (System.currentTimeMillis() - start ));



        start = System.currentTimeMillis();
        responsePruning();
        Log.i("RESPONSE_PRUNING_TIME", "" + (System.currentTimeMillis() - start));

    }

    public List<Short> getAudioData(){
        return outputAudioData;
    }
    public List<float[]> getAccData(){
        return outputAccData;
    }
    public List<float[]> getGyroData(){
        return getGyroData();
    }

    public String getAudioAsCSV(){
        StringBuilder builder = new StringBuilder();
        for(short val : outputAudioData){
            builder.append(val).append("\n");
        }

        String ret = builder.toString();

        Log.i("AUDIO_RAW_VAL", "" + outputAudioData.size());

        outputAudioData.clear();
        audioData.clear();
        return ret;
    }

    public String getAccAsCSV (){
        StringBuilder ret = new StringBuilder("#x, y, z\n");
//            long start = timestamps.peek();
        for(float[] values : outputAccData){
//                ret.append(timestamps.poll() - start);
            assert values != null;
            ret.append(values[0]).append(',').append(values[1]).append(',').append(values[2]).append("\n");
        }
        Log.i("ACC_RAW_VAL", "" + outputAccData.size());
        outputAccData.clear();
        accData.clear();
        return ret.toString();
    }
    public String getGyroAsCSV (){
        StringBuilder ret = new StringBuilder("#x, y, z\n");
//            long start = timestamps.peek();
        for(float[] values : outputGyroData){
//                ret.append(timestamps.poll() - start);
            assert values != null;
            ret.append(values[0]).append(',').append(values[1]).append(',').append(values[2]).append("\n");
        }
        Log.i("GYRO_RAW_VAL", ret.toString());
        outputGyroData.clear();
        gyroData.clear();
        return ret.toString();
    }
    private void peakDetection(){
        int firstPeakIdx = 0;
        int firstPeakValue = 0;
        int dataStartPoint = 0;

        for(int i = 0; i < audioData.size(); i++){
            if(audioData.get(i) > TH_SOUND_PEAK){
//                outputAudioData = audioData.subList(i, i + 4096);
                dataStartPoint = i;
                break;
            }
//            if(audioData.get(i) > firstPeakValue){
//                firstPeakIdx = i;
//                firstPeakValue = audioData.get(i);
//            }
        }
//
//        int secondPeakIdx = firstPeakIdx;
//        int secondPeakValue = 0;
//
//        int zeroDetectionCount = 0;
//
//
//        for(int i = secondPeakIdx; i > 2; i--){
//            if(audioData.get(secondPeakIdx) < secondPeakValue){
//                secondPeakIdx = i;
//                secondPeakValue = audioData.get(secondPeakIdx);
//            }
//
//            float val = audioData.get(i) + audioData.get(i-1) + audioData.get(i-2) + audioData.get(i-3);
//            val /= 4;
//            if(Math.abs(val) < 2){
//                zeroDetectionCount++;
//                if(zeroDetectionCount > 100){
//                    dataStartPoint = i - 3;
//                    break;
//                }
//            }
//        }
        outputAudioData = audioData.subList(dataStartPoint, dataStartPoint + 4096);
        soundStart = dataStartPoint;
    }

    private void responsePruning(){
        final int peakStartIdx = soundStart / 480; // peak * 100 / (48000)

        final int searchEndIdx = Math.min(75 + peakStartIdx, accData.size());

        int searchStartIdx = 0;

        int idx = searchStartIdx;
        float maxVal = accData.get(idx)[2];
        int maxIdx = idx;
        for(; idx < searchEndIdx - 8; idx++){
            if(Math.abs(accData.get(idx)[2]) > maxVal){
                maxVal = Math.abs(accData.get(idx)[2]);
                maxIdx = idx;
            }
        }

        outputAccData = accData.subList(maxIdx, maxIdx + 8);
        outputGyroData = gyroData.subList(maxIdx, maxIdx + 8);
    }
}
