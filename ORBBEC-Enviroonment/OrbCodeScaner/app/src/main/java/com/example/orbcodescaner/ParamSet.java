package com.example.orbcodescaner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ParamSet extends AppCompatActivity {
    private EditText et_IP;
    private EditText et_port;
    private Button btn_cancel;
    private Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param_set);

        et_IP = (EditText)findViewById(R.id.et_ip);
        et_port = (EditText)findViewById(R.id.et_port);
        btn_cancel = (Button)findViewById(R.id.btn_cancel);
        btn_save = (Button)findViewById(R.id.btn_save);

        Intent serIntent = getIntent();
        String ServIpAndPort = serIntent.getStringExtra("ServIpAndPort");
        if (!TextUtils.isEmpty(ServIpAndPort)){
            String[] words = ServIpAndPort.split("\\:");
            if (words.length ==2){
                et_IP.setText(words[0]);
                et_port.setText(words[1]);
            }
        }
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED,intent);
                finish();
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                String strIP = et_IP.getText().toString().trim();
                String strPort = et_port.getText().toString().trim();
                if (!TextUtils.isEmpty(strIP)&& !TextUtils.isEmpty(strPort)){
                    intent.putExtra("ip",strIP);
                    intent.putExtra("port",strPort);
                    setResult(RESULT_OK,intent);
                    finish();
                }else {
                    Looper.prepare();
                    Toast.makeText(ParamSet.this,"请确认IP或者端口号不为空",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            }
        });
    }
}
