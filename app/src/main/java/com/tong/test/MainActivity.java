package com.tong.test;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataResolver;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private HealthDataStore mStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a Health Data Store
        mStore = new HealthDataStore(this, mConnectionListener);
        mStore.connectService();
    }

    private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() {
        @Override
        public void onConnected() {
            Toast.makeText(MainActivity.this, "Connected to Samsung Health", Toast.LENGTH_SHORT).show();
            requestPermissions();
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Toast.makeText(MainActivity.this, "Connection to Samsung Health failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected() {
            Toast.makeText(MainActivity.this, "Disconnected from Samsung Health", Toast.LENGTH_SHORT).show();
        }
    };

    private void requestPermissions() {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        Set<HealthPermissionManager.PermissionKey> pmsKeySet = new HashSet<>();
        pmsKeySet.add(new HealthPermissionManager.PermissionKey(HealthConstants.HeartRate.HEART_RATE, HealthPermissionManager.PermissionType.READ));

        try {
            pmsManager.requestPermissions(pmsKeySet, MainActivity.this).setResultListener(mPermissionListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> mPermissionListener = result -> {
        if (result.getResultMap().containsValue(Boolean.FALSE)) {
            Toast.makeText(MainActivity.this, "Permissions denied", Toast.LENGTH_SHORT).show();
        } else {
            readData();
        }
    };

    private void readData() {
        readHeartRate();
    }

    private void readHeartRate() {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEART_RATE)
                .build();

        try {
            resolver.read(request).setResultListener(result -> {
                for (HealthData data : result) {
                    int heartRate = data.getInt(HealthConstants.HeartRate.HEART_RATE);
                    // Save or display heart rate data
                    Toast.makeText(MainActivity.this, "Heart Rate: " + heartRate, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStore.disconnectService();
    }
}
