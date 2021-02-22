package com.canzhang.asmdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import asm.canzhang.com.asmdemo.R;

public class MainActivity extends AppCompatActivity {
    ExecutorService mExecutorService;
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
                testThreadPoolExecutor();
            }
        });


        ThreadFactory threadFactory = new ThreadFactory(){

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"线程池中的线程" + '-' + mNumber.getAndIncrement());
            }
        };
        mExecutorService = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),threadFactory);

    }

    private  static int  index=0;

    private void testThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setName("单独创建的线程-"+index);
        thread.start();
        index++;

    }


    private final AtomicInteger mNumber = new AtomicInteger();
    private void testThreadPoolExecutor() {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //测试异常捕获
    private float div(int a, int b) {
        return a / b;
    }
}
