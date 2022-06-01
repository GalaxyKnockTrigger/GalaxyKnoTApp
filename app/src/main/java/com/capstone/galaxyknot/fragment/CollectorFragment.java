package com.capstone.galaxyknot.fragment;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.capstone.galaxyknot.AppManager;
import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.StateManager;
import com.capstone.galaxyknot.databinding.CollectorFragmentBinding;

public class CollectorFragment extends Fragment {

    private CollectorFragmentBinding binding;

    public CollectorFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_collector, container, false);
        binding.setFrag(this);
        binding.setState(new StateManager());

        AppManager.getInstance().setCollectorOwner(this.getViewLifecycleOwner());

        StateManager.setObserver(StateManager.TRAINING_COUNT, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                binding.collectorCounter.setText(String.valueOf(integer));
                if(integer == 1){
                    onCollectorButtonClick(binding.collectorStartBtn);
                }
            }
        }, this.getViewLifecycleOwner());

        StateManager.setPropertyChangedCallback(StateManager.TRAINING_LABEL, new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        AppManager.getInstance().setCollectorOwner(this.getViewLifecycleOwner());
    }

    public void onCollectorButtonClick(View v){
        boolean val = !v.isSelected();
        v.setSelected(val);
        StateManager.isCollectorStart.setValue(val);
        Log.i("AUDIO_INFO", "Button " + (val ? "Start" : "Stop"));
    }
}