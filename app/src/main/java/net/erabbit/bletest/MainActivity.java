package net.erabbit.bletest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.erabbit.ble.BleDevice;
import net.erabbit.ble.BleDevicesManager;
import net.erabbit.ble.BleSearchReceiver;
import net.erabbit.ble.utils.LogUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;


public class MainActivity extends Activity {

    ListView listView;
    ProgressBar progress;
    CheckBox checkbox;

    LocalBroadcastManager lbm;

    String TAG = this.getClass().getSimpleName();
    BleDevicesManager bleDevicesManager;
    BleDevice bleDevice;
    MainAdapter myAdapter;
    ArrayList<Device> list;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        progress = (ProgressBar) findViewById(R.id.progress);
        checkbox = (CheckBox) findViewById(R.id.checkbox);
        bleDevicesManager = BleDevicesManager.getInstance(this);
        lbm = LocalBroadcastManager.getInstance(this);
        new MySearchReceiver().registerReceiver(lbm);

        //bleDevicesManager.addDeviceFilter(readJSON());
        list = new ArrayList<>();
        myAdapter = new MainAdapter(list, this);
        listView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = list.get(position);
                String deviceID = device.addr;
                if ("CC2650 SensorTag".equals(device.name)) {
                    Intent intent = new Intent(context, CharacteristicActivity.class);
                    intent.putExtra("deviceID", deviceID);
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "本测试工程只能连接CC2650 SensorTag", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onSearch(View view) {
        Log.i(TAG, "===onSearch");
        if (checkbox.isChecked()) {
            try {
                bleDevicesManager.addSearchFilter(JsonUtil.readJSON(context));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //bleDevicesManager.setSearchTimeout(10*1000);
        bleDevicesManager.startSearch(this);
    }


    class MySearchReceiver extends BleSearchReceiver {
        @Override
        public void onFoundDevice(String deviceID, int rssi, Map<Integer, byte[]> data, String deviceType) {
            super.onFoundDevice(deviceID, rssi, data, deviceType);
            LogUtil.i(TAG, "onFoundDevice");
            Device device = new Device();
            device.addr = deviceID;
            device.name = deviceType;
            device.rssi = rssi;
            list.add(device);

            Collections.sort(list, new Comparator<Device>() {
                @Override
                public int compare(Device o1, Device o2) {
                    if (o1.rssi > o2.rssi) {
                        return -1;
                    } else if (o1.rssi < o2.rssi) {
                        return 1;
                    }
                    return 0;
                }
            });

            myAdapter.notifyDataSetChanged();
        }

        @Override
        public void onAdvertisementUpdated(String deviceID, Map<Integer, byte[]> data) {
            super.onAdvertisementUpdated(deviceID,data);
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
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}