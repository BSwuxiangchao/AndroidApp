package com.example.orbshowboard;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Call;
import okhttp3.Response;


public class ChooseID extends AppCompatActivity {
    private final static String g_PreURL = "http://";
    private static String g_ipAndPort="";
    private final static String g_SufURL1 ="/v1/env/getsensornodes";

    Button btn_sure;
    TextView tv_notes;
    private String strUrlGetnotes="";
    private String  m_ipAndPort = "";
    private static String m_SensorNodesInfo = "";
    private final static String TAG = "ChooseID";

    public static ArrayList<myGroup> groups;
    ExpandableListView listView;
    private String m_strArrayFloor = "";
    ListView lt_floor;
    EListAdapter adapter;
    Set<String> m_listArray;
    Set<DeviceInfo> m_listDeviceInfos;
    long m_adjustTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_id);

        groups = new ArrayList<myGroup>();

        g_ipAndPort = getResources().getString(R.string.ServerIP)+":"+getResources().getString(R.string.ServerPort);
        Intent intentparent = getIntent();
        m_ipAndPort ="";
        m_SensorNodesInfo = "";

        m_ipAndPort = intentparent.getStringExtra("ServIpAndPort");
        tv_notes = (TextView)findViewById(R.id.tv_notes);
        btn_sure = (Button)findViewById(R.id.sureID);
        listView = (ExpandableListView) findViewById(R.id.listView);
        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_listArray= adapter.GetSelectIDArray();
                m_listDeviceInfos = adapter.GetSelectDeviceInfo();
                ArrayList<String > listArrayDeviceInfos=new ArrayList<>();
                for(DeviceInfo it:m_listDeviceInfos){
                    try{
                        JSONObject jsonObject = new JSONObject();
                        /*int ID = 0;
                        String Nickname ="";
                        int MacID =0;
                        int FloorID = 0;
                        int RoomId = 0;
                        int LineID = 0;
                        String LineState = "";*/
                        jsonObject.put("ID",it.ID);
                        jsonObject.put("Nickname",it.Nickname);
                        jsonObject.put("MacID",it.MacID);
                        jsonObject.put("FloorID",it.FloorID);
                        jsonObject.put("RoomId",it.RoomId);
                        jsonObject.put("LineID",it.LineID);
                        jsonObject.put("LineState",it.LineState);
                        jsonObject.put("fmaxTemperature",it.fmaxTemperature);
                        jsonObject.put("fmaxHumidity",it.fmaxHumidity);
                        jsonObject.put("fmaxAirpressure",it.fmaxAirpressure);
                        jsonObject.put("fminTemperature",it.fminTemperature);
                        jsonObject.put("fminHumidity",it.fminHumidity);
                        jsonObject.put("fminAirpressure",it.fminAirpressure);

                        listArrayDeviceInfos.add(jsonObject.toString());
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                Log.d(TAG,m_listArray.toString());
                Intent intent = new Intent(ChooseID.this,MainActivity.class) ;
                intent.putExtra("adjusttime",m_adjustTime);
                intent.putExtra("data",m_listArray.toString());
                intent.putExtra("selectdeviceInfos",listArrayDeviceInfos.toString());
                startActivity(intent);

            }
        });

        setTitle("选择要展示的设备");

        /*String[] str = {"a","b"};
        m_strArrayFloor = tv_notes.getText().toString().trim();
        //ShowToastInfo(m_strArrayFloor);
        ArrayAdapter<String> adapterfloor = new ArrayAdapter<String>(ChooseID.this,
                android.R.layout.simple_list_item_single_choice,str);
        lt_floor = (ListView)findViewById(R.id.list_floor);
        lt_floor.setAdapter(adapterfloor);
        */


        GetSensorNotedeInfo();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //TODO  todo somthing here
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GetExpandableListViewData(m_SensorNodesInfo);
                        Log.d(TAG+" +oncreate ",m_SensorNodesInfo);
                        adapter = new EListAdapter(ChooseID.this, groups);
                        listView.setAdapter(adapter);
                        listView.setOnChildClickListener(adapter);
                    }
                });
            }
        },1 * 100);  //延迟执行



    }

    private void GetExpandableListViewData(String response){
        try {
            JSONObject Alljson = new JSONObject(response);
            if (Alljson.optInt("ret")!=0 || !Alljson.optString("msg").equals("ok"))
                ShowToastInfo("服务器返回信息错误");
            String strServerTime = Alljson.optString("time");
            String thisTime = strServerTime;
            thisTime = thisTime.replace('-','/');
            long sec = new Date(thisTime).getTime();
            long  strCurTime = System.currentTimeMillis();
            m_adjustTime = sec - strCurTime;
            JSONArray infoJsonArray = Alljson.getJSONObject("info").getJSONArray("info");
            JSONArray s_infoJsonArray = infoJsonArray;
            boolean bAddToGroup[] =new boolean[s_infoJsonArray.length()];
            for(int in = 0;in<infoJsonArray.length();in++){
                bAddToGroup[in] =false;
            }
            for (int i = 0; i < infoJsonArray.length(); i++) {
                if(bAddToGroup[i] ==false)
                {
                    JSONObject f_groupObj = (JSONObject) infoJsonArray.get(i);
                    JSONObject f_binfoObj = f_groupObj.getJSONObject("binfo");
                    myGroup f_group = new myGroup("房间 "+f_binfoObj.getString("RoomId"));
                    for(int j = 0;j<s_infoJsonArray.length();j++){
                        if (bAddToGroup[j] == false)
                        {
                            JSONObject s_groupObj = (JSONObject) s_infoJsonArray.get(j);
                            JSONObject s_binfoObj = s_groupObj.getJSONObject("binfo");

                            Log.d(TAG,s_binfoObj.getString("RoomId")+" : "+s_binfoObj.getString("ID"));

                            if(f_binfoObj.getString("RoomId").equals(s_binfoObj.getString("RoomId"))) {
                                Log.d(TAG,"2nd  "+s_binfoObj.getString("RoomId")+" : "+s_binfoObj.getString("ID"));

                                DeviceInfo childDevice = new DeviceInfo();
                                childDevice.ID = Integer.parseInt(s_binfoObj.getString("ID"));
                                childDevice.FloorID =Integer.parseInt(s_binfoObj.getString("FloorID"));
                                childDevice.LineID =Integer.parseInt(s_binfoObj.getString("LineID"));
                                childDevice.MacID =Integer.parseInt(s_binfoObj.getString("MacID"));
                                childDevice.RoomId =Integer.parseInt(s_binfoObj.getString("RoomId"));
                                childDevice.Nickname =s_binfoObj.getString("Nickname");
                                childDevice.LineState =s_binfoObj.getString("LineState");
                                childDevice.fmaxTemperature =Double.parseDouble(s_binfoObj.getString("Maxdataa"));
                                childDevice.fmaxHumidity = Double.parseDouble(s_binfoObj.getString("Maxdatab"));
                                childDevice.fmaxAirpressure = Double.parseDouble(s_binfoObj.getString("Maxdatac"));
                                childDevice.fminTemperature = Double.parseDouble(s_binfoObj.getString("Mindataa"));
                                childDevice.fminHumidity = Double.parseDouble(s_binfoObj.getString("Mindatab"));
                                childDevice.fminAirpressure = Double.parseDouble(s_binfoObj.getString("Mindatab"));

                                Child child = new Child(s_binfoObj.getString("Nickname"),childDevice);
                                f_group.addChildrenItem(child);
                                bAddToGroup[j] =true;
                            }
                        }
                    }
                    groups.add(f_group);
                }

                /*JSONArray childrenList = groupObj.getJSONArray("CommunityUsersList");

                for (int j = 0; j < childrenList.length(); j++) {
                    JSONObject childObj = (JSONObject) childrenList.get(j);
                    Child child = new Child(childObj.getString("userid"), childObj.getString("fullname"),
                            childObj.getString("username"));
                    group.addChildrenItem(child);
                }*/

            }
        }catch (JSONException e){
            Log.d(TAG,e.toString());
        }
    }

    private void GetListViewData(String response){
        try {
            Set<String> setFloorID = new LinkedHashSet<>();
            JSONObject Alljson = new JSONObject(response);
            if (Alljson.optInt("ret")!=0 || !Alljson.optString("msg").equals("ok"))
                ShowToastInfo("服务器返回信息错误");
            String strServerTime = Alljson.optString("time");
            String thisTime = strServerTime;
            thisTime = thisTime.replace('-','/');
            long sec = new Date(thisTime).getTime();
            long  strCurTime = System.currentTimeMillis();
            m_adjustTime = sec - strCurTime;
            JSONArray infoJsonArray = Alljson.getJSONObject("info").getJSONArray("info");
            for (int i = 0; i < infoJsonArray.length(); i++) {
                {
                    JSONObject f_groupObj = (JSONObject) infoJsonArray.get(i);
                    JSONObject f_binfoObj = f_groupObj.getJSONObject("binfo");

                    setFloorID.add("楼层 "+f_binfoObj.getString("FloorID"));
                }
            }
            showResponseUI(setFloorID.toString().substring(1,setFloorID.toString().length()-1));
            Log.d(TAG,m_strArrayFloor.toString());

        }catch (JSONException e){
            Log.d(TAG,e.toString());
        }
    }

    private void GetSensorNotedeInfo(){
        if (TextUtils.isEmpty(m_ipAndPort)){
            strUrlGetnotes = g_PreURL+g_ipAndPort+g_SufURL1;
        }else{
            strUrlGetnotes =g_PreURL+m_ipAndPort+g_SufURL1;
        }
        ShowToastInfo(strUrlGetnotes);
        HttpUtil.sendOkHttpRequestGet(strUrlGetnotes.trim(),new okhttp3.Callback(){
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException{
                if (response.isSuccessful()) {
                    //GetListViewData(m_SensorNodesInfo);
                    String tempInfos = response.body().string().trim();
                    //GetExpandableListViewData(tempInfos);
                    m_SensorNodesInfo = tempInfos;

                    Log.d(TAG,"获取到节点");
                    Log.d(TAG,m_SensorNodesInfo);
                }
                else{
                    m_SensorNodesInfo = "";
                    ShowToastInfo("未获取到传感器信息");
                    Log.e(TAG,"服务器错误");
                }
            }
            @Override
            public void onFailure(Call call, IOException e){
                m_SensorNodesInfo = "";
                Log.e(TAG,"访问失败");

            }
        });
    }

    private void ShowToastInfo(String res){
        Toast.makeText(this,res,Toast.LENGTH_SHORT).show();
    }
    private void showResponseUI(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_notes.setText(response);
            }
        });
    }
}
