package erabbit.erabbit.bletest;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.erabbit.BleDevice;
import net.erabbit.BleDevicesManager;
import net.erabbit.BleSearchReceiver;
import net.erabbit.DeviceStateReceiver;
import net.erabbit.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends Activity {

    ListView listView;
    ProgressBar progress;
    LocalBroadcastManager lbm;

    String TAG = this.getClass().getSimpleName();
    BleDevicesManager bleDevicesManager;
    BleDevice bleDevice;
    ListAdapter myAdapter;
    ArrayList<BleDevice> list;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_test);
        listView = (ListView) findViewById(R.id.listView);
        progress = (ProgressBar) findViewById(R.id.progress);
        bleDevicesManager = BleDevicesManager.getInstance(this);
        lbm = LocalBroadcastManager.getInstance(this);
        new MySearchReceiver().registerReceiver(lbm);
        new MyDeviceStateReceiver().registerReceiver(lbm);

        //bleDevicesManager.addDeviceFilter(readJSON());


        list = new ArrayList<>();
        myAdapter = new ListAdapter(list, this);
        listView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BleDevice device = list.get(position);
                if (device != null)
                    device.connect();
            }
        });

    }

    public void onSearch(View view) {
        Log.i(TAG, "===onSearch");
        bleDevicesManager.startSearch(this);
    }

    public void onDisconnect(View view) {
        if (bleDevice != null) {
            bleDevice.disconnect();
        }
    }

    class MyDeviceStateReceiver extends DeviceStateReceiver {

        @Override
        public void onDeviceConnected(String deviceID) {
            super.onDeviceConnected(deviceID);
            LogUtil.i(TAG, "onDeviceConnected");
            bleDevice = bleDevicesManager.findDevice(deviceID);
            //startActivity();
            Toast.makeText(context, "连接到设备：" + bleDevice.getDeviceName(), Toast.LENGTH_SHORT).show();
            bleDevice.sendData("send", "1".getBytes());
            bleDevice.startReceiveData("bleDevice");

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

        }

        @Override
        public void onDeviceReceivedData(String deviceID, String name, byte[] data) {
            super.onDeviceReceivedData(deviceID, name, data);
            LogUtil.i(TAG, "onDeviceReceivedData");

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


    class MySearchReceiver extends BleSearchReceiver {
        @Override
        public void onFoundDevice(String deviceID, int rssi, byte[] data, String deviceType) {
            super.onFoundDevice(deviceID, rssi, data, deviceType);
            LogUtil.i(TAG, "onFoundDevice");
            BleDevice bleDevice = bleDevicesManager.findDevice(deviceID);
            if (bleDevice == null) {
                bleDevice = bleDevicesManager.createDevice(deviceID, MainActivity.this, BleDevice.class, readJSON());
            }
            list.add(bleDevice);
        }

        @Override
        public void onAdvertisementUpdated() {
            super.onAdvertisementUpdated();
            LogUtil.i(TAG, "onAdvertisementUpdated");

        }

        @Override
        public void onRSSIUpdated(String deviceID, int rssi) {
            super.onRSSIUpdated(deviceID, rssi);
            LogUtil.i(TAG, "onRSSIUpdated");
        }

        @Override
        public void onSearchStarted() {
            super.onSearchStarted();
            list.clear();
            LogUtil.i(TAG, "onSearchStarted");
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSearchTimeOut() {
            super.onSearchTimeOut();
            LogUtil.i(TAG, "onSearchTimeOut");
            progress.setVisibility(View.GONE);
            myAdapter.notifyDataSetChanged();

        }

    }


    private JSONObject readJSON() {
        JSONObject testjson = null;
        try {
            InputStreamReader isr = new InputStreamReader(getAssets().open("testjson.json"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            br.close();
            isr.close();
            testjson = new JSONObject(builder.toString());//builder读取了JSON中的数据。
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testjson;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // lbm.unregisterReceiver();
        // lbm.unregisterReceiver();
    }
}