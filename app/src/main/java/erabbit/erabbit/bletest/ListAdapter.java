package erabbit.erabbit.bletest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.erabbit.BleDevice;

import java.util.List;

/**
 * Created by ziv on 2017/4/24.
 */

public class ListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    List<BleDevice> list;

    public ListAdapter(List<BleDevice> list, Context context) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
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
            convertView = inflater.inflate(R.layout.list_view_of_device, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.addr = (TextView) convertView.findViewById(R.id.addr);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BleDevice device = list.get(position);
        holder.name.setText("设备名： " + device.getDeviceName());
        holder.addr.setText("地址： " + device.getDeviceKey());

        return convertView;
    }

    class ViewHolder {
        public TextView name;
        public TextView addr;
    }
}
