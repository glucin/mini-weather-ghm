//用来实现选择城市的操作
package com.example.ghm.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }

    //选择城市图标响应
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
                //发送数据给MainActivity
                Intent i = new Intent();
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
                default:
                    break;
        }
    }
}
