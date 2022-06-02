package com.capstone.galaxyknot;

import android.Manifest;
import android.hardware.Sensor;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Constants {
    public static final String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };
    public static final int RCODE = 10000;
    public static final int RECEIVING_TIME = 80;
    public static final String URL = "https://nas.splo2t.com";
    public static final int PORT = 9999;
    public static final int AUDIO_SAMP_RATE = 48000;
    public static final short TH_SOUND_PEAK = 15000;
    public static final int REQUEST_ENABLE_BT = 1000;

    public static final int TRAINING_SET_COUNT = 20;
}
