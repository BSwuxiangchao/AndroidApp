package com.example.orbshowboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    public class returnData{
        public double fTemperature = 0;
        public double fHumidity = 0;
        public double fAirpressure =0;
    }
    private final static String g_PreURL = "http://";
    private static String g_ipAndPort="";
    private final static String g_SufURL1 = "/v1/env/getrecord?id=";
    private static String m_strStandderUrlGetData = "";

    private TextView server_textView;
    private TextView tv_time;

    private TextView tv_adress1;
    private TextView tv_temperature1;
    private TextView tv_humidity1;
    private TextView tv_difPressure1;

    private TextView tv_adress2;
    private TextView tv_temperature2;
    private TextView tv_humidity2;
    private TextView tv_difPressure2;

    private TextView tv_adress3;
    private TextView tv_temperature3;
    private TextView tv_humidity3;
    private TextView tv_difPressure3;

    private TextView tv_adress4;
    private TextView tv_temperature4;
    private TextView tv_humidity4;
    private TextView tv_difPressure4;

    private static final int port =6666;


    static String m_strUrlGetData = "";
    protected static String m_ipAndPort="";

    final static String TAG = "MainActivity";

    private String m_response_Get;

    private boolean isShowData = true;
    private static boolean m_bStartedTimeThread = false;
    private static boolean m_bStartedData2UI = false;
    private static Thread m_TimeThread;
    private static Thread m_dataToUIThread;

    private long m_stcAdjustTime;
    private String m_stcChoosedDevice;
    private String m_selectDeviceInfo;
    final private static int m_iShowTime = 10;
    final private static int m_iServerUpdateTime = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitleColor(Color.BLACK);
        g_ipAndPort = getResources().getString(R.string.ServerIP)+":"+getResources().getString(R.string.ServerPort);
        m_strStandderUrlGetData=g_PreURL+g_ipAndPort+g_SufURL1;

        server_textView = (TextView) findViewById(R.id.server_textView);
        tv_time = (TextView)findViewById(R.id.tv_time);
        tv_adress1 = (TextView)findViewById(R.id.tv_address1);
        tv_temperature1 =(TextView)findViewById(R.id.tv_temperature1);
        tv_humidity1 = (TextView)findViewById(R.id.tv_humidity1);
        tv_difPressure1 =(TextView)findViewById(R.id.tv_difPressure1);

        tv_adress2 = (TextView)findViewById(R.id.tv_address2);
        tv_temperature2 =(TextView)findViewById(R.id.tv_temperature2);
        tv_humidity2 = (TextView)findViewById(R.id.tv_humidity2);
        tv_difPressure2 =(TextView)findViewById(R.id.tv_difPressure2);

        tv_adress3 = (TextView)findViewById(R.id.tv_address3);
        tv_temperature3 =(TextView)findViewById(R.id.tv_temperature3);
        tv_humidity3 = (TextView)findViewById(R.id.tv_humidity3);
        tv_difPressure3 =(TextView)findViewById(R.id.tv_difPressure3);

        tv_adress4 = (TextView)findViewById(R.id.tv_address4);
        tv_temperature4 =(TextView)findViewById(R.id.tv_temperature4);
        tv_humidity4 = (TextView)findViewById(R.id.tv_humidity4);
        tv_difPressure4 =(TextView)findViewById(R.id.tv_difPressure4);
        Intent intent =getIntent();
        m_stcChoosedDevice=intent.getStringExtra("data");
        if(m_stcChoosedDevice.equals("[]")){
            isShowData =false;
            ShowToastInfo("未选择需要展示的设备");
        }else {
            isShowData = true;
            //ShowToastInfo("要展示的设备ID： "+m_stcChoosedDevice);
            setTitle("要展示的设备ID： "+m_stcChoosedDevice);
        }
        Log.d(TAG,m_stcChoosedDevice);

        m_selectDeviceInfo = intent.getStringExtra("selectdeviceInfos");
        if(m_selectDeviceInfo.equals("[]")){
            isShowData =false;
            ShowToastInfo("信息有误");
        }else {
            isShowData = true;
            ShowToastInfo("要展示的信息： "+m_selectDeviceInfo);
        }
        Log.d(TAG,m_selectDeviceInfo);

        m_stcAdjustTime =intent.getLongExtra("adjusttime",0);
        if(TextUtils.isEmpty(m_ipAndPort)){
            m_strStandderUrlGetData = g_PreURL+g_ipAndPort+g_SufURL1;

        }else{
            m_strStandderUrlGetData = g_PreURL+m_ipAndPort+g_SufURL1;

        }
        m_TimeThread = new TimeThread();
        m_TimeThread.start();
        m_dataToUIThread = new Data2UI();
        m_dataToUIThread.start();

    }

    private void showResponseUI(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray(m_selectDeviceInfo);
                    for(int i= 0 ;i<jsonArray.length();i++){
                        JSONObject singleObject = (JSONObject)jsonArray.get(i);
                        if(singleObject.getString("ID").equals(response)){
                            server_textView.setText("当前设备信息： "+singleObject.toString());

                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void ShowToastInfo(String res){
        Toast.makeText(this,res,Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.choose_id:
                Toast.makeText(this,"选择需要展示的传感器ID",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,ChooseID.class);
                intent.putExtra("ServIpAndPort",m_ipAndPort);
                startActivity(intent);
                break;
            case R.id.setparam:
                Intent intent2 = new Intent(MainActivity.this,ParamSet.class);
                if (!TextUtils.isEmpty(m_ipAndPort)){
                    intent2.putExtra("ServIpAndPort",m_ipAndPort);
                }
                startActivityForResult(intent2,1);
                break;
                default:
        }
        return true;
    }

    public boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        //判断IP格式和范围
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        //============对之前的ip判断的bug在进行判断
        if (ipAddress == true) {
            String ips[] = addr.split("\\.");
            if (ips.length == 4) {
                try {
                    for (String ip : ips) {
                        if (Integer.parseInt(ip) < 0 || Integer.parseInt(ip) > 255) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        return ipAddress;
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 1:

                if(resultCode == RESULT_OK){
                    ShowToastInfo("保存参数成功");
                    String ip = data.getStringExtra("ip");
                    String port=data.getStringExtra("port");
                    if (isIP(ip)){
                        m_ipAndPort = ip+":"+port;
                        m_strStandderUrlGetData = g_PreURL+m_ipAndPort+g_SufURL1;
                        Log.d(TAG,m_strStandderUrlGetData);
                    }else{
                        ShowToastInfo("IP不合法！！\n请重新设置IP");
                    }

                }else if (resultCode == RESULT_CANCELED){
                    ShowToastInfo("未保存参数");
                }
                break;
            default:
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    public class TimeThread extends Thread{
        @Override
        public void run(){
            super.run();
            m_bStartedTimeThread = true;
            do{
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }while (m_bStartedTimeThread);
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what){
                case 1:
                    tv_time.setText(new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()+m_stcAdjustTime)));
                    break;
                    default:
                        break;
            }
            return false;
        }
    });

    @Override
    protected void onStop(){
        super.onStop();
        m_bStartedData2UI = true;
    }

    @Override
    protected void onStart(){
        super.onStart();
        m_bStartedData2UI = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //m_bStartedData2UI = true;

        /*m_TimeThread.interrupt();
        m_dataToUIThread.join();*/
        /*mHandler.removeCallbacks(m_TimeThread);
        mHandler.removeCallbacks(m_dataToUIThread);*/

    }
    public class Data2UI extends Thread{
        @Override
        public void run(){
            super.run();
            do{
                try {
                    returnData DataTest;

                    String demosub = m_stcChoosedDevice.substring(1,m_stcChoosedDevice.length()-1);
                    String demoArray[] = demosub.split(",");
                    List<String> demoList = Arrays.asList(demoArray);
                    for (String str:demoList){
                        if(!TextUtils.isEmpty(str)){
                            Log.d(TAG,"Cur Array ID is "+String.valueOf(demoList.indexOf(str)));
                            showResponseUI(str.trim());//展示传感器详细信息
                            String strTempUrl =m_strStandderUrlGetData+str.trim();
                            //m_strUrlGetData = strTempUrl;
                            for(int i = 0 ;i<m_iShowTime;i++){
                                //服务器数据10秒刷新一次
                                if (i%m_iServerUpdateTime ==0 )
                                {
                                    GetRequestDataWithOkhttp_Get(strTempUrl);
                                    Thread.sleep(100);

                                    DataTest =parseJsonFromServer_GetData(m_response_Get);
                                    showResponseUI(DataTest,str.trim(),demoList.indexOf(str)%4);
                                }
                                Thread.sleep(1000);
                            }
                        }
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }while (!m_bStartedData2UI);
        }
    }

    //数据去抖动并取得平均值
    private double StabDataAndGetAverage(double group[]){
        //数组排序
        Arrays.sort(group);
        double sum =0;
        for (int i =1;i<m_iShowTime-1;i++)
            sum =sum+group[m_iShowTime - 2];
        return sum/(8);
    }

    private void GetRequestDataWithOkhttp_Get(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.sendOkHttpRequestGet(url,new okhttp3.Callback(){
                    @Override
                    public void onResponse(Call call, Response response) throws IOException{
                        if (response.isSuccessful()) {
                            m_response_Get = response.body().string();
                            Log.d(TAG,"获取到数据");

                        }
                        else
                            Log.e(TAG,"服务器错误");
                    }
                    @Override
                    public void onFailure(Call call,IOException e){
                        Log.e(TAG,"访问失败");

                    }
                });
            }
        }).start();

    }

    //解析strUrlGetData URL 获取到的数据
    private returnData parseJsonFromServer_GetData( String responseData){
        returnData tempData = new returnData();
        try {
            JSONObject info = null;
            JSONObject jsonObject = new JSONObject(responseData);
            if(jsonObject.optInt("ret") != 0 ||!jsonObject.optString("msg").equals("ok")){
                ShowToastInfo("数据获取失败！！");
                return null;
            }
            info = jsonObject.optJSONObject("info").optJSONObject("info");
            tempData.fAirpressure =info.optDouble("Press");
            tempData.fHumidity =info.optDouble("Humid");
            tempData.fTemperature = info.getDouble("Temp");

        }catch (Exception e){
            e.printStackTrace();
        }
        return tempData;
    }

    private void showResponseUI(final returnData data, final String strID, final int index){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DecimalFormat df = new DecimalFormat("######0.00");
                double fmaxTemperature = 0,fmaxHumidity = 0,fmaxAirpressure = 0;
                double fminTemperature = 0,fminHumidity = 0,fminAirpressure = 0;
                String strAdress = "";
                try {
                    JSONArray jsonArray = new JSONArray(m_selectDeviceInfo);
                    for(int i= 0 ;i<jsonArray.length();i++){
                        JSONObject singleObject = (JSONObject)jsonArray.get(i);
                        if(singleObject.getString("ID").equals(strID)){
                            fmaxTemperature = Double.parseDouble(singleObject.getString("fmaxTemperature"));
                            fmaxHumidity = Double.parseDouble(singleObject.getString("fmaxHumidity"));
                            fmaxAirpressure = Double.parseDouble(singleObject.getString("fmaxAirpressure"));
                            fminTemperature = Double.parseDouble(singleObject.getString("fminTemperature"));
                            fminHumidity = Double.parseDouble(singleObject.getString("fminHumidity"));
                            fminAirpressure = Double.parseDouble(singleObject.getString("fminAirpressure"));
                            strAdress = singleObject.getString("Nickname");
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                if (index ==0){
                    tv_adress1.setText(strAdress);
                    if(data.fTemperature>fmaxTemperature || data.fTemperature <fminTemperature){
                        tv_temperature1.setTextColor(Color.RED);
                    }else {
                        tv_temperature1.setTextColor(Color.BLUE);
                    }
                    tv_temperature1.setText(String.valueOf(df.format(data.fTemperature)));
                    if(data.fHumidity> fmaxHumidity || data.fHumidity <fminHumidity){
                        tv_humidity1.setTextColor(Color.RED);
                    }else {
                        tv_humidity1.setTextColor(Color.BLUE);
                    }
                    tv_humidity1.setText(String.valueOf(df.format(data.fHumidity)));
                    if(data.fAirpressure> fmaxAirpressure || data.fAirpressure <fminAirpressure){
                        tv_difPressure1.setTextColor(Color.RED);
                    }else {
                        tv_difPressure1.setTextColor(Color.BLUE);
                    }
                    tv_difPressure1.setText(String.valueOf(df.format(data.fAirpressure)));
                }   else if(index ==1){
                    tv_adress2.setText(strAdress);
                    if(data.fTemperature>fmaxTemperature || data.fTemperature <fminTemperature){
                        tv_temperature2.setTextColor(Color.RED);
                    }else {
                        tv_temperature2.setTextColor(Color.BLUE);
                    }
                    tv_temperature2.setText(String.valueOf(df.format(data.fTemperature)));
                    if(data.fHumidity> fmaxHumidity || data.fHumidity <fminHumidity){
                        tv_humidity2.setTextColor(Color.RED);
                    }else {
                        tv_humidity2.setTextColor(Color.BLUE);
                    }
                    tv_humidity2.setText(String.valueOf(df.format(data.fHumidity)));
                    if(data.fAirpressure> fmaxAirpressure || data.fAirpressure <fminAirpressure){
                        tv_difPressure2.setTextColor(Color.RED);
                    }else {
                        tv_difPressure2.setTextColor(Color.BLUE);
                    }
                    tv_difPressure2.setText(String.valueOf(df.format(data.fAirpressure)));
                }else if(2 == index){
                    tv_adress3.setText(strAdress);
                    if(data.fTemperature>fmaxTemperature || data.fTemperature <fminTemperature){
                        tv_temperature3.setTextColor(Color.RED);
                    }else {
                        tv_temperature3.setTextColor(Color.BLUE);
                    }
                    tv_temperature3.setText(String.valueOf(df.format(data.fTemperature)));
                    if(data.fHumidity> fmaxHumidity || data.fHumidity <fminHumidity){
                        tv_humidity3.setTextColor(Color.RED);
                    }else {
                        tv_humidity3.setTextColor(Color.BLUE);
                    }
                    tv_humidity3.setText(String.valueOf(df.format(data.fHumidity)));
                    if(data.fAirpressure> fmaxAirpressure || data.fAirpressure <fminAirpressure){
                        tv_difPressure3.setTextColor(Color.RED);
                    }else {
                        tv_difPressure3.setTextColor(Color.BLUE);
                    }
                    tv_difPressure3.setText(String.valueOf(df.format(data.fAirpressure)));
                }else if(3 == index){
                    tv_adress4.setText(strAdress);
                    if(data.fTemperature>fmaxTemperature || data.fTemperature <fminTemperature){
                        tv_temperature4.setTextColor(Color.RED);
                    }else {
                        tv_temperature4.setTextColor(Color.BLUE);
                    }
                    tv_temperature4.setText(String.valueOf(df.format(data.fTemperature)));
                    if(data.fHumidity> fmaxHumidity || data.fHumidity <fminHumidity){
                        tv_humidity4.setTextColor(Color.RED);
                    }else {
                        tv_humidity4.setTextColor(Color.BLUE);
                    }
                    tv_humidity4.setText(String.valueOf(df.format(data.fHumidity)));
                    if(data.fAirpressure> fmaxAirpressure || data.fAirpressure <fminAirpressure){
                        tv_difPressure4.setTextColor(Color.RED);
                    }else {
                        tv_difPressure4.setTextColor(Color.BLUE);
                    }
                    tv_difPressure4.setText(String.valueOf(df.format(data.fAirpressure)));
                }

            }
        });
    }

}
