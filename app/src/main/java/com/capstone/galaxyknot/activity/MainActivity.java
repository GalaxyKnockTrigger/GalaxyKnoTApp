package com.capstone.galaxyknot.activity;

import static com.capstone.galaxyknot.Constants.*;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.capstone.galaxyknot.R;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {

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

}
