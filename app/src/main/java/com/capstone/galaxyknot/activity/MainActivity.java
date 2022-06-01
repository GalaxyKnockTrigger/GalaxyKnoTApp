package com.capstone.galaxyknot.activity;

import static com.capstone.galaxyknot.Constants.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import com.capstone.galaxyknot.AppManager;
import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.StateManager;
import com.capstone.galaxyknot.databinding.MainActivityBinding;

import java.util.Map;

public class MainActivity extends FragmentActivity {

    private MainActivityBinding binding;
    private boolean permissionToUseAccepted = false;
    private AppManager appManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, permissions, RCODE);

        appManager = AppManager.getInstance(this.getApplicationContext());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setIsClassifier(StateManager.isNowClassifierState);
        StateManager.setPropertyChangedCallback(StateManager.NOW_STATE,
                new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        changeState();
                    }
                });
        StateManager.setObserver(StateManager.SHOW_TOAST, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    StateManager.doShowToast.setValue(false);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String label = StateManager.label;
                            Toast.makeText(
                                    MainActivity.this.getApplicationContext(),
                                    label + AppManager.getInstance().getCommand(label),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            }
        }, this);
        binding.setActivity(this);
        binding.getRoot().requestFocus();

        Log.i("MAIN_ACTIVITY", "onCreate Finished");
    }

    @Override
    protected void onStart() {
        super.onStart();
        appManager.startAudioThread();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent ev) {
        Log.i("GENERICMOTION", "NOW: "+ev.getAction());
        Log.i("GENERICMOTION", "WANT: "+MotionEvent.ACTION_SCROLL);
        if(ev.getAction() == MotionEvent.ACTION_SCROLL && ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER) ){

            float delta = - ev.getAxisValue(MotionEventCompat.AXIS_SCROLL)*
                    ViewConfigurationCompat.getScaledVerticalScrollFactor(
                            ViewConfiguration.get(getApplicationContext()), getApplicationContext()
                    );

            Log.i("ROTARY_VALUE", "" + delta);

            if(delta > 1.0){
                toCollector();
            }
            else if(delta < -1.0){
                toClassifier();
            }
            else
                return false;
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RCODE) {
            permissionToUseAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
        if(!permissionToUseAccepted){
//            if(this.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                }
//            }
//            else{
            finish();
//            }
        }
    }

    public void onToClassifierButtonClick(View v){
        toClassifier();
    }

    public void onToCollectorButtonClick(View v){
        toCollector();
    }

    private void toClassifier(){
        Log.i("ONCLICK", "TO_CLASSIFIER_BUTTON");

        if(StateManager.isCollectorStart == null || StateManager.isCollectorStart.getValue() == null){
            Log.i("ONCLICK", "TO_CLASSIFIER_FAILED");
            return;
        }

        if(StateManager.isCollectorStart.getValue()){
            Toast.makeText(this, "Need To Stop Collecting", Toast.LENGTH_SHORT).show();
        }
        else{
            StateManager.isNowClassifierState.set(true);
        }
    }

    private void toCollector(){
        Log.i("ONCLICK", "TO_COLLECTOR_BUTTON_" + StateManager.isNowClassifierState.get());
        if(StateManager.isClassifierStart == null || StateManager.isClassifierStart.getValue() == null){
            Log.i("ONCLICK", "TO_COLLECTOR_BUTTON_");
            return;
        }
        if(StateManager.isClassifierStart.getValue()){
            Toast.makeText(this, "Need To Stop", Toast.LENGTH_SHORT).show();
        }
        else{
            StateManager.isNowClassifierState.set(false);
        }
    }

    private void changeState(){
        if(StateManager.isNowClassifierState.get()){
            binding.mainNowStatePlaceholder.setContentId(ConstraintLayout.LayoutParams.UNSET);
        }
        else{
            binding.mainNowStatePlaceholder.setContentId(binding.mainNowStateImage.getId());
        }
    }

}
