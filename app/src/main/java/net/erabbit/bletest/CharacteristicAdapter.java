package net.erabbit.bletest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.erabbit.ble.BleDevice;
import net.erabbit.ble.BleDevicesManager;
import net.erabbit.ble.entity.Characteristic;
import net.erabbit.ble.utils.BleUtility;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ziv on 2017/4/24.
 */

public class CharacteristicAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    List<Characteristic> list;
    BleDevice bleDevice;

    HashMap<String, byte[]> dataMap;

    public CharacteristicAdapter(List<Characteristic> list, Context context) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        dataMap = new HashMap<>();
    }

    public void setDataMap(HashMap hashMap) {
        this.dataMap = hashMap;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_view_of_characteristic, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.uuid = (TextView) convertView.findViewById(R.id.uuid);
            holder.character = (TextView) convertView.findViewById(R.id.characteristic);
            holder.data = (TextView) convertView.findViewById(R.id.data);
            holder.operateLayout = (LinearLayout) convertView.findViewById(R.id.operateLayout);
            holder.write = (Button) convertView.findViewById(R.id.write);
            holder.read = (Button) convertView.findViewById(R.id.read);
            holder.swithLayout = (LinearLayout) convertView.findViewById(R.id.swithLayout);
            holder.switchBtn = (Switch) convertView.findViewById(R.id.switchBtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Characteristic characteristic = list.get(position);
        holder.name.setText("特征名： " + characteristic.name);
        holder.uuid.setText("uuid： " + characteristic.uuid);
        holder.data.setText("数据：<" + BleUtility.MakeHexString(dataMap.get(characteristic.name)) + ">");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < characteristic.properties.size(); i++) {
            String p = characteristic.properties.get(i);
            sb.append(p).append("    ");
        }
        String properties = sb.toString();

        holder.character.setText("属性： " + properties);

        if (!properties.contains("write") && !properties.contains("read")) {
            holder.operateLayout.setVisibility(View.GONE);
        } else {
            holder.operateLayout.setVisibility(View.VISIBLE);
            if (properties.contains("write")) {
                holder.write.setVisibility(View.VISIBLE);
            } else {
                holder.write.setVisibility(View.GONE);
            }

            if (properties.contains("read")) {
                holder.read.setVisibility(View.VISIBLE);
            } else {
                holder.read.setVisibility(View.GONE);
            }
        }

        if (properties.contains("notify")) {
            holder.swithLayout.setVisibility(View.VISIBLE);
        } else {
            holder.swithLayout.setVisibility(View.GONE);
        }


        holder.write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dlg = new AlertDialog.Builder(context).create();
                dlg.show();

                dlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                //dlg.getWindow().setContentView(R.layout.dialog_input);
                LayoutInflater factory = LayoutInflater.from(context);
                View view = factory.inflate(R.layout.dialog_input, null);

                final EditText content = (EditText) view.findViewById(R.id.content);
                Button cancel = (Button) view.findViewById(R.id.cancel);
                Button ok = (Button) view.findViewById(R.id.ok);
                //content.setInputType(InputType.TYPE_CLASS_TEXT);
                dlg.getWindow().setContentView(view);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bleDevice = BleDevicesManager.getInstance(context).getCurDevice();
                        String data = content.getText().toString().trim();
                        byte b = 0;
                        try {
                            b = Byte.parseByte(data);
                            bleDevice.sendData(characteristic.name, new byte[]{b});
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "只能输入数字", Toast.LENGTH_SHORT).show();
                        } finally {
                            dlg.dismiss();
                        }
                    }
                });
            }
        });


        holder.read.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                bleDevice = BleDevicesManager.getInstance(context).getCurDevice();
                bleDevice.readData(characteristic.name);
            }
        });


        holder.switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()

        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bleDevice = BleDevicesManager.getInstance(context).getCurDevice();

                if (isChecked) {
                    bleDevice.startReceiveData(characteristic.name);
                } else {
                    bleDevice.stopReceiveData(characteristic.name);
                }
            }
        });

        return convertView;
    }

    class ViewHolder {
        public TextView name;
        public TextView uuid;
        public TextView character;
        public TextView data;
        public LinearLayout operateLayout;
        public Button write;
        public Button read;
        public LinearLayout swithLayout;
        public Switch switchBtn;

    }
}
