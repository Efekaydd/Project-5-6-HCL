package com.example.braceletapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity implements BottomNavigationView
        .OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    BottomNavigationView bottomNavigationView;
    private List<String> capturedAddresses;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private boolean scanningEnd;
    String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
    int requestCodePermission;

    private static final UUID NOTIFY_SERVICE = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID NOTIFY_CHAR = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    BluetoothGattCharacteristic characteristicNotify;

    FirstFragment firstFragment = new FirstFragment();
    SecondFragment secondFragment = new SecondFragment();
    ThirdFragment thirdFragment = new ThirdFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomnav);

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.data);

        try {
            commitToFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String temp = "";
        try {
            temp = readFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        firstFragment.Startup(temp);

        capturedAddresses = new ArrayList<>();
        Timer timer2 = new Timer();

        timer2.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                firstFragment.update(scanningEnd);
                secondFragment.update(scanningEnd);
            }
        },0,500);

        if (!hasPermissions(MainActivity.this, permissions)) {
            requestPermissions(permissions, requestCodePermission);
        } else {
            scanLeDevice();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.data){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, firstFragment)
                    .commit();
            return true;
        }else if (item.getItemId() == R.id.connect){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, secondFragment)
                    .commit();
            return true;
        }else if (item.getItemId() == R.id.settings){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, thirdFragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == requestCodePermission && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanLeDevice();
        }
    }

    private void scanLeDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, requestCodePermission);
            return;
        }
        if (!scanningEnd) {
            mBluetoothLeScanner.startScan(leScanCallback);
        } else {
            mBluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (!scanningEnd) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, requestCodePermission);
                    return;
                }
                Log.i(TAG, "onScanResult: " + result.getDevice().getAddress() + ": " + result.getDevice().getName());

                if (!capturedAddresses.contains(result.getDevice().getAddress() + ": " + result.getDevice().getName())) {
                    capturedAddresses.add(result.getDevice().getAddress() + ": " + result.getDevice().getName());
                    secondFragment.scanLog(capturedAddresses);
                }

                if (result.getDevice().getAddress().equals("30:30:F9:18:1B:95")) {
                    scanningEnd = true;
                    secondFragment.setData(result.getDevice().getName() + ": " + result.getDevice().getAddress());

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(permissions, requestCodePermission);
                        return;
                    }
                    mBluetoothLeScanner.stopScan(leScanCallback);
                    result.getDevice().connectGatt(MainActivity.this, true, mGattCallback);
                }
            }
        }

        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(permissions, requestCodePermission);
                        return;
                    }
                    gatt.discoverServices();
                    mBluetoothGatt = gatt;

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    scanningEnd = false;
                    disconnectBLE();
                    gatt.close();
                    mBluetoothGatt = null;
                    scanLeDevice();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                characteristicNotify = gatt.getService(NOTIFY_SERVICE).getCharacteristic(NOTIFY_CHAR);

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, requestCodePermission);
                    return;
                }

                gatt.setCharacteristicNotification(characteristicNotify, true);
            }

            @Override
            public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);
                if(characteristic.equals(characteristicNotify)){
                    try {
                        commitToFile(value[0], value[1], value[2], value[3]);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("'Date: 'dd-MM-yyyy HH:mm:ss");
                    String currentDateAndTime = sdf.format(new Date());
                    final String entryString = currentDateAndTime + ", Heart rate: " + value[0] + ", O2: " + value[1] + ", Motion: " + value[2] + ", Temperature: " + value[3] + "\n";

                    firstFragment.setData(value[0], value[1], value[2], value[3], entryString);
                }
            }
        };

        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private void disconnectBLE(){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, requestCodePermission);
            return;
        }
        mBluetoothGatt.disconnect();
    }

    private boolean hasPermissions(Context context, String[] permissions){
        for (String permission:permissions){
            if(ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void commitToFile(int hR, int o2, int mT, int tP) throws IOException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("'Date: 'dd-MM-yyyy HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        final String entryString = currentDateAndTime + ", Heart rate: " + hR + ", O2: " + o2 + ", Motion: " + mT + ", Temperature: " + tP + "\n";
        FileOutputStream fOut = openFileOutput("SensorData1.txt", Context.MODE_APPEND);
        OutputStreamWriter outputWriter = new OutputStreamWriter(fOut);
        outputWriter.append(entryString);
        outputWriter.flush();
        outputWriter.close();
    }

    private void commitToFile() throws IOException {
        FileOutputStream fOut = openFileOutput("SensorData1.txt", Context.MODE_APPEND);
        OutputStreamWriter outputWriter = new OutputStreamWriter(fOut);
        outputWriter.append("----");
        outputWriter.flush();
        outputWriter.close();
    }

    public String readFromFile() throws IOException {
        String out = "";
        InputStream inputStream = openFileInput("SensorData1.txt");
        if(inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String temp = "";
            StringBuilder stringBuilder = new StringBuilder();
            while((temp = bufferedReader.readLine()) != null){
                stringBuilder.append(temp);
                stringBuilder.append("\n");
            }
            inputStream.close();
            out = stringBuilder.toString();
        }
        return out;
    }
}