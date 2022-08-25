package com.capstone.galaxyknot.listener;

import android.hardware.SensorEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class IMUListener implements SensorEventListener, IGetDataAsCSV, IGetDataSize {
    protected final LinkedList<float[]> dataQueue;

    protected static Integer maxIdx;

    public IMUListener() {
        dataQueue = new LinkedList<>();
        maxIdx = -1;
    }
    public abstract int getDataSize();

    public abstract String getDataAsCSV ();
}