package com.example.orbcodescaner;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    protected static String m_ipAndPort="";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setparam:
                Intent intent = new Intent(BaseActivity.this,ParamSet.class);
                startActivityForResult(intent,1);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case 1:

                if(resultCode == RESULT_OK){
                    String ip = data.getStringExtra("ip");
                    String port=data.getStringExtra("port");
                    m_ipAndPort = ip+":"+port;
                    Toast.makeText(this,"保存参数成功",Toast.LENGTH_SHORT).show();
                }else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(this,"未保存参数",Toast.LENGTH_SHORT).show();
                }
                break;
                default:
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
}
