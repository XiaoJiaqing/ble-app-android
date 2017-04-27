package net.erabbit.bletest;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by ziv on 2017/4/26.
 */

public class JsonUtil {

    public static JSONObject readJSON(Context context) {
        JSONObject testjson = null;
        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("testjson.json"), "UTF-8");
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
}
