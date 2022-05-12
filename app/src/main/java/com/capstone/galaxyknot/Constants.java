package com.capstone.galaxyknot;

import android.Manifest;
import android.hardware.Sensor;

public class Constants {
    public static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    public static final int RCODE = 10000;
    public static final int RECEIVING_TIME = 80;
    public static final String URL = "https://nas.splo2t.com";
    public static final int PORT = 9999;
    public static final int AUDIO_SAMP_RATE = 48000;
    public static final short TH_SOUND_PEAK = 10000;
}
