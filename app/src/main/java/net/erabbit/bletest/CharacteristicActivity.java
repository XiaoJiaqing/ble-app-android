package net.erabbit.bletest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.erabbit.ble.BleDevice;
import net.erabbit.ble.BleDevicesManager;
import net.erabbit.ble.DeviceStateReceiver;
import net.erabbit.ble.entity.Characteristic;
import net.erabbit.ble.entity.DeviceObject;
import net.erabbit.ble.entity.Service;
import net.erabbit.ble.utils.LogUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CharacteristicActivity extends AppCompatActivity {

    Toolbar toolbar;
    ListView listView;
    ProgressBar progress;
    TextView deviceName;
    CharacteristicAdapter myAdapter;
    ArrayList<Characteristic> list = new ArrayList<>();
    Context context = this;
    String TAG = getClass().getSimpleName();
    BleDevice bleDevice;
    BleDevicesManager bleDevicesManager;

    HashMap<String, byte[]> dataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristic);
        progress = (ProgressBar) findViewById(R.id.progress);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        deviceName = (TextView) findViewById(R.id.deviceName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.listView);
        bleDevicesManager = BleDevicesManager.getInstance(context);

        myAdapter = new CharacteristicAdapter(list, context);
        listView.setAdapter(myAdapter);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        new MyDeviceStateReceiver().registerReceiver(lbm);

        String deviceID = getIntent().getStringExtra("deviceID");

        bleDevice = bleDevicesManager.findDevice(deviceID);
        if (bleDevice == null) {
            bleDevice = bleDevicesManager.createDevice(deviceID, CharacteristicActivity.this, BleDevice.class, JsonUtil.readJSON(context));
        }

        if (bleDevice != null) {
            bleDevice.connect();
            deviceName.setText("当前设备：" + bleDevice.getDeviceName() + "(" + bleDevice.getDeviceKey() + ")");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (bleDevice != null)
                    bleDevice.disconnect();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (bleDevice != null)
            bleDevice.disconnect();
        super.onBackPressed();
    }

    public void onDisconnect(View view) {
        if (bleDevice != null) {
            if (bleDevice.getConnected()) {
                bleDevice.disconnect();
                ((TextView) view).setText("连接");
            } else {
                bleDevice.connect();
                ((TextView) view).setText("断开连接");
                progress.setVisibility(View.VISIBLE);
            }
        }
    }

    class MyDeviceStateReceiver extends DeviceStateReceiver {

        @Override
        public void onDeviceConnected(String deviceID) {
            super.onDeviceConnected(deviceID);
            LogUtil.i(TAG, "onDeviceConnected");
            BleDevicesManager.getInstance(context).setCurDevice(bleDevice);
            Toast.makeText(context, "连接到设备：" + bleDevice.getDeviceName(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDeviceDisconnected(String deviceID) {
            super.onDeviceDisconnected(deviceID);
            LogUtil.i(TAG, "onDeviceDisconnected");
            Toast.makeText(context, "已断开连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDeviceError(String deviceID, int errId, String error) {
            super.onDeviceError(deviceID, errId, error);
            LogUtil.i(TAG, "onDeviceError");
            progress.setVisibility(View.GONE);
        }

        @Override
        public void onDeviceMismatch(String deviceID) {
            super.onDeviceMismatch(deviceID);
            LogUtil.i(TAG, "onDeviceMismatch");

        }

        @Override
        public void onDeviceReady(String deviceID) {
            super.onDeviceReady(deviceID);
            LogUtil.i(TAG, "onDeviceReady");
            progress.setVisibility(View.GONE);
            ArrayList<Characteristic> characteristics = new ArrayList<>();
            DeviceObject deviceObject = bleDevice.getDeviceObject();
            if (deviceObject != null) {
                for (int i = 0; i < deviceObject.services.size(); i++) {
                    Service service = deviceObject.services.get(i);
                    characteristics.addAll(service.characteristics);
                }
            }

            list.clear();
            list.addAll(characteristics);
            myAdapter.notifyDataSetChanged();

            // bleDevice.sendData("send", new byte[]{1});
            // bleDevice.startReceiveData("bleDevice");
        }

        @Override
        public void onDeviceReceivedData(String deviceID, String name, byte[] data) {
            super.onDeviceReceivedData(deviceID, name, data);
            LogUtil.i(TAG, "onDeviceReceivedData");
            if (deviceID.equals(bleDevice.getDeviceKey())) {
                dataMap.put(name, data);
            }
            myAdapter.setDataMap(dataMap);
            myAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDeviceRSSIUpdated(String deviceID, int rssi) {
            super.onDeviceRSSIUpdated(deviceID, rssi);
            LogUtil.i(TAG, "onDeviceRSSIUpdated");

        }

        @Override
        public void onDeviceValueChanged(String deviceID, int key, Serializable value) {
            super.onDeviceValueChanged(deviceID, key, value);
            LogUtil.i(TAG, "onDeviceValueChanged");

        }

    }

}
