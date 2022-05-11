package com.capstone.galaxyknot.activity;

import static com.capstone.galaxyknot.Constants.*;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.capstone.galaxyknot.R;
import com.capstone.galaxyknot.StateManager;
import com.capstone.galaxyknot.databinding.MainActivityBinding;
import com.capstone.galaxyknot.KnockValidator;
import com.capstone.galaxyknot.TrustOkHttpClientUtil;
import com.capstone.galaxyknot.listener.AudioListener;

import org.conscrypt.Conscrypt;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.zip.GZIPOutputStream;

import okhttp3.OkHttpClient;

public class MainActivity extends FragmentActivity {

    private MainActivityBinding binding;
    private AudioListener audioListener;
    private OkHttpClient okHttpClient;

    private boolean permissionToUseAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, permissions, RCODE);

        audioListener = new AudioListener(getApplicationContext());

        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        okHttpClient = TrustOkHttpClientUtil.getUnsafeOkHttpClient().build();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setIsClassifier(StateManager.isNowClassifierState);
        binding.setActivity(this);

//        changeState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case RCODE:
                permissionToUseAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                break;
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

    private byte[] compress(String str) throws IOException {

        if (str == null || str.length() == 0) {
            return null;
        }

        long start = System.currentTimeMillis();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
        gzip.flush();
        gzip.close();
//        Log.i("COMPRESS_INFO_BEFORE", str);
//        Log.i("COMPRESS_INFO_AFTER", out.toString());


        long end = System.currentTimeMillis();

//        Log.i("Latency_info", "compress latency: " + (end-start));
//        Log.i("Latency_info", "compress ratio: " + (out.toByteArray().length / str.getBytes().length));

        return out.toByteArray();
    }

    public void onToClassifierButtonClick(View v){
        Log.i("ONCLICK", "TO_CLASSIFIER_BUTTON");

        if(StateManager.isCollectorStart.get()){
            Toast.makeText(this, "Need To Stop Collecting", Toast.LENGTH_SHORT).show();
        }
        else{
            StateManager.isNowClassifierState.set(true);
        }
        changeState();
    }
    public void onToCollectorButtonClick(View v){
        Log.i("ONCLICK", "TO_COLLECTOR_BUTTON_" + StateManager.isNowClassifierState.get());

        if(StateManager.isClassifierStart.get()){
            Toast.makeText(this, "Need To Stop", Toast.LENGTH_SHORT).show();
        }
        else{
            StateManager.isNowClassifierState.set(false);
        }
        changeState();
    }

    private void changeState(){
        if(StateManager.isNowClassifierState.get()){
            binding.mainNowStatePlaceholder.setContentId(binding.mainNowStatePlaceholder.getId());
        }
        else{
            binding.mainNowStatePlaceholder.setContentId(binding.mainNowStateImage.getId());
        }
    }

}
