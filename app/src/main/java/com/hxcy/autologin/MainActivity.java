package com.hxcy.autologin;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.hxcy.autologin.service.AccessService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static String fileName = "account.txt";
    public static ListView listView;
    public static ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1); //系统sdk里面的R文件
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //读取账号
        String fileTxt = null;
        try {
            fileTxt = readFromSD(fileName).trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String entry : fileTxt.split("\n")) {
            String[] arr = entry.split(" ");
            adapter.add(arr[0]);
        }
        listView.setAdapter(adapter);
        listView.setSelected(false);

        listView = findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "你点击的是:"+adapter.getItem(position),Toast.LENGTH_SHORT).show();
                AccessService.selectAccount = adapter.getItem(position);
                Context context = getApplicationContext();
                if (checkPackInfo("com.bcy.fsapp")) {
                    openPackage(MainActivity.this, "com.bcy.fsapp");
                } else {
                    Toast.makeText(MainActivity.this, "没有安装" + "", Toast.LENGTH_LONG).show();
                    //TODO 下载操作
                }
            }
        });
    }

    //读取SD卡中文件的方法
    //定义读取文件的方法:
    public String readFromSD(String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        Context context = getApplicationContext();
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);
        String externalStorageVolumePath1 = externalStorageVolumes[0].getPath();
        //打开文件输入流
        FileInputStream input = new FileInputStream(externalStorageVolumePath1 + "/" + filename);
        byte[] temp = new byte[1024];

        int len = 0;
        //读取文件内容:
        while ((len = input.read(temp)) > 0) {
            sb.append(new String(temp, 0, len));
        }
        //关闭输入流
        input.close();
        return sb.toString();
    }


    public static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        //Activity完整名
        String mainAct = null;
        //根据包名寻找
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("WrongConstant")List list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;

    }

    public static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            // 创建第三方应用的上下文环境
            try {
                pkgContext = context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY
                                | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }

    public static boolean openPackage(Context context, String packageName) {
        Context pkgContext = getPackageContext(context, packageName);
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (pkgContext != null && intent != null) {
            intent.putExtra("openMoudle", "serviceHall");
            pkgContext.startActivity(intent);
            return true;
        }
        return false;
    }

    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }
}