package com.canzhang.asmdemo;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Map;
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

        findViewById(R.id.bt_test0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
//                    getPhoneNumber(MainActivity.this);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.bt_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "创建线程和线程池", Toast.LENGTH_SHORT).show();
//                float div = div(10, 0);
                testThread();
                testThreadPoolExecutor();
            }
        });


        findViewById(R.id.bt_test_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "查询线程", Toast.LENGTH_SHORT).show();
                getAllStackTraces();
            }
        });


        ThreadFactory threadFactory = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "线程池中的线程" + '-' + mNumber.getAndIncrement());
            }
        };
        mExecutorService = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), threadFactory);

        Thread.getAllStackTraces();
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    private static int index = 0;

    private void testThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setName("单独创建的线程-" + index);
        thread.start();
        index++;

    }


    private final AtomicInteger mNumber = new AtomicInteger();

    private void testThreadPoolExecutor() {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getAllStackTraces() {
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread thread = entry.getKey();

            StackTraceElement[] stackTraceElements = entry.getValue();

            if (thread.equals(Thread.currentThread())) {
                continue;
            }

            Log.e("Test", "\n线程： " + thread.getName() + "\n");
            for (StackTraceElement element : stackTraceElements) {
                Log.e("Test", "\t 调用栈分析" + element + "\n");
            }
        }
    }

    //测试异常捕获
    private float div(int a, int b) {
        return a / b;
    }
}
