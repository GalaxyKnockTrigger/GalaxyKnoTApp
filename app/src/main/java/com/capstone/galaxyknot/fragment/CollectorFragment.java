package com.capstone.galaxyknot.fragment;

import static com.capstone.galaxyknot.Constants.TRAINING_SET_COUNT;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.capstone.galaxyknot.AppManager;
import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.StateManager;
import com.capstone.galaxyknot.databinding.CollectorFragmentBinding;

public class CollectorFragment extends Fragment implements View.OnClickListener {

    private CollectorFragmentBinding binding;
    private ArrayAdapter<CharSequence> adapter;
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
            boolean resetFlag = false;
            @Override
            public void onChanged(Integer integer) {
                binding.collectorCounter.setText(String.valueOf(integer));
                if(integer == TRAINING_SET_COUNT){
                    Log.i("COLLECTOR", "RESET_FLAG_ON");
                    resetFlag = true;
                }
                else if(resetFlag && integer == 1){
                    resetFlag = false;
                    Log.i("COLLECTOR", "RESET_FLAG_OFF & CHANGE STATE");
                    onCollectorButtonClick(binding.collectorStartBtn);
                }
            }
        }, this.getViewLifecycleOwner());

        StateManager.setPropertyChangedCallback(StateManager.TRAINING_LABEL, new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(propertyId == R.id.collector_edit_label){
                    Log.i("OBSERVER", "EDIT_LABEL_CHANGED");
                }
            }
        });
        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.state_array, android.R.layout.simple_spinner_item);

// Apply the adapter to the spinner
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

    @Override
    public void onClick(View v) {
        String title = "";
        String message = "Select Command";
        StateOnClickListener soc = null;

        if(v.getId() == binding.collectorSpin1.getId()){
            title = "Switch 1";
            soc = new StateOnClickListener(0);
        }
        else if(v.getId() == binding.collectorSpin2.getId()){
            title = "Switch 2";
            soc = new StateOnClickListener(1);
        }
        else if(v.getId() == binding.collectorSpin3.getId()){
            title = "Finder";
            soc = new StateOnClickListener(2);
        }
        AlertDialog.Builder dialog;

        dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
//                .setMessage(message)
                .setItems(getResources().getStringArray(R.array.state_array), soc)
                .setNegativeButton("Cancel", (dialog1, which) -> {dialog1.dismiss();});

        getActivity().runOnUiThread(()->{
            dialog.create().show();
        });
    }

    private class StateOnClickListener implements DialogInterface.OnClickListener{
        private int id;
        public StateOnClickListener(int objID){
            id = objID;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            char c = (char)(which + '1');
            char[] cmd = StateManager.trainingCmd.get().toCharArray();
            if(cmd.length != 3){
                cmd = new char[]{'4', '4', '4'};
            }
            cmd[id] = c;

            StateManager.trainingCmd.set(String.copyValueOf(cmd));
            if(id == 0){
                binding.collectorSpin1.setText(adapter.getItem(which));
            }
            else if(id == 1){
                binding.collectorSpin2.setText(adapter.getItem(which));
            }
            else if(id == 2){
                binding.collectorSpin3.setText(adapter.getItem(which));
            }
        }
    }
}