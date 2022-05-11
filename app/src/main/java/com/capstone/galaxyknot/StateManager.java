package com.capstone.galaxyknot;

import androidx.databinding.ObservableBoolean;

public class StateManager {
    public static ObservableBoolean isNowClassifierState = new ObservableBoolean(true);
    public static ObservableBoolean isStateTransaction   = new ObservableBoolean(false);
    public static ObservableBoolean isClassifierStart    = new ObservableBoolean(false);
    public static ObservableBoolean isCollectorStart     = new ObservableBoolean(false);

}
