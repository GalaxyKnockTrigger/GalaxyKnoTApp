package com.capstone.galaxyknot;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class StateManager {
    public static final int NOW_STATE           = 0;
    public static final int CLASSIFIER_START    = 1;
    public static final int COLLECTOR_START     = 2;
    public static final int STATE_TRANSACTION   = 3;
    public static final int RECORD_END          = 4;
    public static final int TRAINING_COUNT      = 5;
    public static final int TRAINING_LABEL      = 6;
    public static final int TRAINING_CMD        = 7;

    public static final int SHOW_TOAST          = 8;

    public static ObservableBoolean isNowClassifierState = new ObservableBoolean(true);
    public static ObservableField<String> trainingLabel = new ObservableField<>("");
    public static ObservableField<String>  trainingCmd  = new ObservableField<>("");

    public static MutableLiveData<Boolean> isClassifierStart    = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> isCollectorStart     = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> isStateTransaction   = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> doShowToast         = new MutableLiveData<>(false);

    public static MutableLiveData<Boolean> isRecordEnd = new MutableLiveData<>(false);
    public static MutableLiveData<Integer> trainingCount = new MutableLiveData<>(1);

    public static String label;

    public static void setPropertyChangedCallback(int id, Observable.OnPropertyChangedCallback callback){
        if(id == NOW_STATE){
            isNowClassifierState.addOnPropertyChangedCallback(callback);
        }
        else if(id == TRAINING_LABEL){
            trainingLabel.addOnPropertyChangedCallback(callback);
        }
        else if(id == TRAINING_CMD){
            trainingCmd.addOnPropertyChangedCallback(callback);
        }
    }

    public static void setObserver(int id, Observer<?> observer, LifecycleOwner owner){
        if(id == CLASSIFIER_START){
            if(!isClassifierStart.hasActiveObservers())
                isClassifierStart.observe(owner, (Observer<Boolean>)observer);
        }
        else if(id == COLLECTOR_START){
            if(!isCollectorStart.hasActiveObservers())
                isCollectorStart.observe(owner, (Observer<Boolean>)observer);
        }
        else if(id == RECORD_END){
            isRecordEnd.observe(owner, (Observer<Boolean>)observer);
        }
        else if(id == TRAINING_COUNT){
            trainingCount.observe(owner, (Observer<Integer>)observer);
        }
        else if(id == SHOW_TOAST){
            doShowToast.observe(owner, (Observer<Boolean>)observer);
        }
    }
}
