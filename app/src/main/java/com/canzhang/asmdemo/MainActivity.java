package com.canzhang.asmdemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.canzhang.asmdemo.thread.test.MyThread;

import asm.canzhang.com.asmdemo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "普通点击事件", Toast.LENGTH_SHORT).show();
//                float div = div(10, 0);
                testThread();
            }
        });



    }

    private void testThread() {
        new Thread().start();
        new MyThread().start();
    }

    //测试异常捕获
    private float div(int a, int b) {
        return a / b;
    }
}
