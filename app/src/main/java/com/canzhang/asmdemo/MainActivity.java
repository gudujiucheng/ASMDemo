package com.canzhang.asmdemo;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
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

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                testMethodCallOrFieldLod();
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

    /**
     * 测试方法调用或则字段加载的 插桩效果
     */
    private void testMethodCallOrFieldLod() {
        try {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            getPhoneNumber(MainActivity.this);



           Settings.System.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
           Settings.Secure.getString(MainActivity.this.getContentResolver(),Settings.Secure.ANDROID_ID);



            final PackageManager packageManager = MainActivity.this.getPackageManager();
            //获取所有已安装程序的包信息
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);

            List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);


            TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                tm.getDeviceId();
            } else {
                Method method = tm.getClass().getMethod("getImei");
                method.invoke(tm);
            }



            WifiManager wifi = (WifiManager)MainActivity.this.getSystemService("wifi");
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null) {
                 info.getMacAddress();
                 info.getSSID();
            }

            String   model= Build.MODEL;

            getPsdnIp();

            ActivityManager.RunningAppProcessInfo myProcess = null;
            ActivityManager activityManager =
                    (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager
                    .getRunningAppProcesses();
            activityManager
                    .getRunningServices(0);


            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> resolveInfos =  MainActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
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


    /**
     * 用来获取手机拨号上网（包括CTWAP和CTNET）时由PDSN分配给手机终端的源IP地址。
     *
     * @return
     * @author SHANHY
     */
    public static String getPsdnIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }






}
