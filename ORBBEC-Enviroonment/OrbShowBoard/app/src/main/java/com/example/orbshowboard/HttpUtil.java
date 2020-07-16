package com.example.orbshowboard;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class HttpUtil {
    public interface  HttpCallbackListener{
        void onFinish(String response);
        void onError(Exception e);
    }
    public static void sendHttpRequestGet(final String address,final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection =(HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine())!= null){
                        response.append(line);
                    }
                    if (listener != null){
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
                    listener.onError(e);
                }finally {
                    if(connection != null)
                        connection.disconnect();
                }
            }
        }).start();
    }

    public static void sendOkHttpRequestGet(String address,okhttp3.Callback callback){
        Log.d("HttpUtil",address+" "+new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static void sendOkHttpRequestPost(String address,String jsonStr ,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON= MediaType.parse("application/json; charset=utf-8");;//数据类型为json格式，
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, jsonStr);
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();
        Log.d("HttpUtil","获取数据");

        client.newCall(request).enqueue(callback);
    }

}