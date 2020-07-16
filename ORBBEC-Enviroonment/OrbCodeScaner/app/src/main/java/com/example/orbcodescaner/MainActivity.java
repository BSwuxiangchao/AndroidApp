package com.example.orbcodescaner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrcode.Constant;
import com.example.qrcode.ScannerActivity;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends BaseActivity {
    private Button btn_scanner_test;
    private Button btn_scanner_standard;

    private Button btn_Check;

    private TextView responsetextView;
    private EditText gold_editText;
    private EditText test_editText;

    private static final String TAG_log = "SocketClient_Actdebug";

    private static final int RESULT_REQUEST_CODE_TEST=1;
    private static final int RESULT_REQUEST_CODE_STANDARD=2;
    private static final int RESULT_REQUEST_CODE_SETPARAM=3;

    private final static String g_PreURL = "http://";
    private static String g_ipAndPort="10.10.6.76:12901";
    private final static String g_midSufURL1 ="/v1/env/devicecorrect?gid=";
    private final static String g_midSufURL2="&aid=";
    private static String m_strStandderUrlCorrectData = "";

    private IntentFilter intentFilter;

    private Params m_GetDataParamStandard = new Params() ,m_GetDataParamTest = new Params();
    private String m_response_Check;

    private final static String TAG = "MainActivity";
    //test_editText.getText()


    private static String strUrlCheckData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        g_ipAndPort = getResources().getString(R.string.ServerIP)+":"+getResources().getString(R.string.ServerPort);
        m_strStandderUrlCorrectData = g_PreURL+g_ipAndPort+g_midSufURL1;
        btn_scanner_test = (Button) findViewById(R.id.scanner_test);
        btn_scanner_standard = (Button) findViewById(R.id.scanner_standard);
        btn_Check = (Button)findViewById(R.id.btn_sendMessage);
        gold_editText = (EditText) findViewById(R.id.gold_editText);
        test_editText= (EditText) findViewById(R.id.test_editText);
        responsetextView = (TextView) findViewById(R.id.response_text);

        btn_scanner_test.setOnClickListener(mScannerListener_test);
        btn_scanner_standard.setOnClickListener(mScannerListener_standard);

        //校准
        btn_Check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(gold_editText.getText().toString().trim()) || TextUtils.isEmpty(test_editText.getText().toString().trim())) {
                    ShowToastInfo("请先扫描测试传感器或者标准传感器");
                    return;
                }
                showResponseUI(true,"");

                final  ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("当前正在校验传感器，请耐心等待");
                progressDialog.setMessage("校验中。。。。。");
                progressDialog.setCancelable(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //TODO  todo somthing here
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //testHttpGet();
                                progressDialog.dismiss();
                                GetRequestDataWithOkhttp_Check();

                            }
                        });
                    }
                },10 * 1000);  //延迟10秒执行
            }
        });
    }



    private View.OnClickListener mScannerListener_test = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str;
            str = test_editText.getText().toString().trim();
            if (TextUtils.isEmpty(str))
                requestPermission(RESULT_REQUEST_CODE_TEST);
            else
                ShowToastInfo("设备已扫描，ID为："  +str);
        }
    };

    private View.OnClickListener mScannerListener_standard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str;
            str = gold_editText.getText().toString().trim();
            if (TextUtils.isEmpty(str))
                requestPermission(RESULT_REQUEST_CODE_STANDARD);
            else
                ShowToastInfo("设备已扫描，ID为："  +str);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showResponseUI(final boolean res,final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (res){
                    responsetextView.setTextColor(Color.GREEN);
                    responsetextView.setText(response);
                }else{
                    responsetextView.setTextColor(Color.RED);;
                    responsetextView.setText(response);
                }
            }
        });
    }


    private void testHttpGet(){

            HttpUtil.sendOkHttpRequestGet("http://10.10.6.186:12901/v1/env/getrecord?id=2",new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseData = response.body().string();
                showResponseUI(true,responseData);
            }
            @Override
            public void onFailure(Call call,IOException e){

            }
        });
        ShowToastInfo("should show the context");

    }

    private void requestPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {
                ShowToastInfo("二维码扫码需要相机权限");


            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        0);
            }
        }else{
            goScanner(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    goScanner(requestCode);
                }
                return;
            }

        }
    }

    private void goScanner(int requestCode){
       /* IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();*/
        Intent intent = new Intent(this, ScannerActivity.class);
        //这里可以用intent传递一些参数，比如扫码聚焦框尺寸大小，支持的扫码类型。
//        //设置扫码框的宽
//        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, 400);
//        //设置扫码框的高
//        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, 400);
//        //设置扫码框距顶部的位置
//        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, 100);
//        //设置是否启用从相册获取二维码(默认为FALSE，不启用)。
//        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true);
//        Bundle bundle = new Bundle();
//        //设置支持的扫码类型
//        bundle.putSerializable(Constant.EXTRA_SCAN_CODE_TYPE, mHashMap);
//        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);

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
            case R.id.setparam:
                Intent intent2 = new Intent(MainActivity.this,ParamSet.class);
                if (!TextUtils.isEmpty(m_ipAndPort)){
                    intent2.putExtra("ServIpAndPort",m_ipAndPort);
                }
                startActivityForResult(intent2,RESULT_REQUEST_CODE_SETPARAM);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data == null)
                return;
            String type = data.getStringExtra(Constant.EXTRA_RESULT_CODE_TYPE);
            String content = data.getStringExtra(Constant.EXTRA_RESULT_CONTENT);

            switch (requestCode) {
                case RESULT_REQUEST_CODE_TEST:
                    m_GetDataParamTest = parseRaram(content);
                    test_editText.setText(String.valueOf(m_GetDataParamTest.deviceid));
                    ShowToastInfo("设备已扫描，ID为： " +String.valueOf(m_GetDataParamTest.deviceid));

                    break;
                case RESULT_REQUEST_CODE_STANDARD:
                    m_GetDataParamStandard = parseRaram(content);
                    gold_editText.setText(String.valueOf(m_GetDataParamStandard.deviceid));
                    ShowToastInfo("设备已扫描，ID为： " +String.valueOf(m_GetDataParamStandard.deviceid));
                    break;
                case RESULT_REQUEST_CODE_SETPARAM:
                    ShowToastInfo("保存参数成功");
                    String ip = data.getStringExtra("ip");
                    String port=data.getStringExtra("port");
                    if (isIP(ip)){
                        m_ipAndPort = ip+":"+port;
                        m_strStandderUrlCorrectData = g_PreURL+m_ipAndPort+g_midSufURL1;
                    }else{
                        ShowToastInfo("IP不合法！！\n请重新设置IP");
                    }
                    break;
                default:
                    break;
            }
        }else{
            ShowToastInfo("未保存参数");

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //解析从平板获取到传感器二维码信息
    private Params parseRaram( String responseData){
        Params tempParam = new Params();
        try {
            //单个json
            JSONObject jsonObject = new JSONObject(responseData);
            tempParam.floorid= jsonObject.optInt("floorid");
            tempParam.roomid= jsonObject.optInt("roomid");
            tempParam.lineid= jsonObject.optInt("lineid");
            tempParam.linestate= jsonObject.optString("linestate");
            tempParam.macaddr= jsonObject.optString("macaddr");
            tempParam.deviceid= jsonObject.optInt("deviceid");
            Log.d(TAG,"floorid: "+tempParam.floorid);
            Log.d(TAG,"roomid: "+tempParam.roomid);
            Log.d(TAG,"lineid: "+tempParam.lineid);
            Log.d(TAG,"linestate: "+tempParam.linestate);
            Log.d(TAG,"macaddr: "+tempParam.macaddr);
            Log.d(TAG,"deviceid: "+tempParam.deviceid);

        }catch (Exception e){
            e.printStackTrace();
        }
        return tempParam;
    }

    private void GetRequestDataWithOkhttp_Check(){
            if(m_ipAndPort.equals("")){
                strUrlCheckData = m_strStandderUrlCorrectData+gold_editText.getText().toString().trim()+g_midSufURL2+test_editText.getText().toString().trim();
            }else
                strUrlCheckData = m_strStandderUrlCorrectData+gold_editText.getText().toString().trim()+g_midSufURL2+test_editText.getText().toString().trim();

        HttpUtil.sendOkHttpRequestGet(strUrlCheckData,new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                if (response.isSuccessful()) {
                    m_response_Check = response.body().string();
                    if (parseJsonFromServer_CheckData(m_response_Check))
                    {
                        showResponseUI(true,"PASS");
                    }
                    else
                        {
                        showResponseUI(false,"NG");
                    }
                    Log.d(TAG,"获取到数据");
                }
                else{
                    Log.e(TAG,"服务器错误");
                    showResponseUI(false,"NODATA");
                }
            }
            @Override
            public void onFailure(Call call,IOException e){
                Log.d(TAG,"发生异常");
                showResponseUI(false,"NODATA");
            }
        });
    }

    //解析strUrlCheckData URL 获取到的数据
    private boolean parseJsonFromServer_CheckData( String responseData){
        try {
            String log = null;
            JSONObject jsonObject = new JSONObject(responseData);
            if (jsonObject.optInt("ret") ==0 &&jsonObject.optString("msg").equals("ok"))
                return true;
            else
                return false;

        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
}


