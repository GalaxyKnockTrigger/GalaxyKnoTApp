package com.capstone.galaxyknot.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.capstone.galaxyknot.AppManager;
import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.StateManager;
import com.capstone.galaxyknot.databinding.ClassifierFragmentBinding;

public class ClassifierFragment extends Fragment {
    private ClassifierFragmentBinding binding;

    public ClassifierFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_classifier, container, false);
        binding.setFrag(this);
        binding.setIsClassifier(StateManager.isNowClassifierState);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        AppManager.getInstance().setClassifierOwner(this.getViewLifecycleOwner());
    }

    public void onClassifierButtonClick(View v){
        boolean val = !v.isSelected();
        v.setSelected(val);
        StateManager.isClassifierStart.setValue(val);
        Log.i("AUDIO_INFO", "Button " + (val ? "Start" : "Stop"));
    }
}
