package com.hxcy.autologin.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * @author kevin
 * @date 2022/6/6
 * @desc
 */
public class AccessService extends AccessibilityService {
    private static Map<String, String> map = new HashMap<>();

    private static boolean flag = new Boolean(false);

    private static String fileName = "account.txt";

    public static String selectAccount;

    /**
     * 当无障碍服务连接之后回调
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    /**
     * AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED，
     * 而会调用AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED，
     * 而AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED只要内容改变后都会调用，
     * 所以一般是使用AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED来作为监测事件的
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        synchronized ("") {
            try {
                // 此方法是在主线程中回调过来的，所以消息是阻塞执行的
                if ("com.bcy.fsapp".equals(event.getPackageName())) {
                    switch (event.getEventType()) {
                        case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                            Log.i(TAG, "TYPE_WINDOW_CONTENT_CHANGED 界面改变");
                            String fileTxt = readFromSD(fileName).trim();//readFromSD(fileName).trim();
                            //System.out.println(fileTxt);
                            for (String entry : fileTxt.split("\n")) {
                                String[] arr = entry.split(" ");
                                map.put(arr[0], arr[1]);
                            }
                            AccessibilityNodeInfo rootNodeInfo = getRootNodeInfo(event);
                            AccessibilityNodeInfo viewList = rootNodeInfo.getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0);
                            if (viewList.getChildCount() == 10) {
                                //点击图让页面变化
                                viewList.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                //先清空
                                AccessibilityNodeInfo login_email = viewList.getChild(2).getChild(0);
                                String loginEmail = login_email.getText().toString().split(",")[0];
                                if (map.get(loginEmail) != null & !loginEmail.equals("Email address")) {
                                    Bundle arguments = new Bundle();
                                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                                    login_email.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                                    AccessibilityNodeInfo child = viewList.getChild(9);
                                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                } else {
                                    AccessibilityNodeInfo child = viewList.getChild(9);
                                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                            if (viewList.getChildCount() == 6) {
                                //点击图让页面变化
                                viewList.getChild(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                //账号
                                AccessibilityNodeInfo username = viewList.getChild(2);
                                //点击
                                username.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Bundle arguments2 = new Bundle();
                                arguments2.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                                username.getChild(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments2);
                                //密码
                                username.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Bundle arguments3 = new Bundle();
                                arguments3.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, AccessService.selectAccount);
                                username.getChild(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments3);

                                String email = username.getChild(0).getText().toString().split(",")[0];
                                if (map.get(email) != null) {
                                    //密码
                                    AccessibilityNodeInfo pass = viewList.getChild(3);
                                    //if (!flag) {
                                    pass.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Bundle arguments4 = new Bundle();
                                    arguments4.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                                    pass.getChild(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments4);
                                    //点击
                                    pass.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Bundle arguments1 = new Bundle();
                                    arguments1.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, map.get(email).trim());
                                    pass.getChild(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments1);
                                    //登录
                                    if (!pass.getChild(0).getText().toString().equals("Password")) {
                                        viewList.getChild(4).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        Thread.sleep(6000);
                                        AccessService.selectAccount = "";
                                    }
                                }
                            }
                            if (viewList.getChildCount() == 3) {
                                AccessibilityNodeInfo confirm = viewList.getChild(2);
                                if (confirm.getContentDescription().toString().equals("Confirm")) {
                                    flag = true;
                                }
                            }
                            break;
                        case AccessibilityEvent.TYPE_VIEW_CLICKED:
                            Log.i(TAG, "TYPE_VIEW_CLICKED view被点击");
                            Thread.sleep(2000);
                            break;
                    }
                }
            } catch (InterruptedException | IOException | NullPointerException e) {
                //e.printStackTrace();
            }
        }
    }

    //id查找
    //nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
    //Text查找
    //nodeInfo.findAccessibilityNodeInfosByText(text);
    //点击节点
    //node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    //修改文本
    //node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);


    /**
     * 回归函数遍历每一个节点
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.isClickable() & info.getClass().toString().equals("android.widget.Button")) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }


    /**
     * 无障碍服务断开后回调
     */
    @Override
    public void onInterrupt() {

    }

    private AccessibilityNodeInfo getRootNodeInfo(AccessibilityEvent event) {
        AccessibilityEvent curEvent = event;
        AccessibilityNodeInfo nodeInfo = null;
        if (Build.VERSION.SDK_INT >= 16) {
            if (this != null) {
                nodeInfo = this.getRootInActiveWindow();
                Log.d(TAG, "getRootNodeInfo: " + nodeInfo);
            }
        } else {
            nodeInfo = curEvent.getSource();
        }
        return nodeInfo;
    }

    //读取SD卡中文件的方法
    //定义读取文件的方法:
    public String readFromSD(String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        //if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        //filename = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/data/" + filename;
        Context context = getApplicationContext();
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(context, null);
        String externalStorageVolumePath1 = externalStorageVolumes[0].getPath();
        //System.out.println("ContextCompat.getExternalFilesDirs()[0]=" + externalStorageVolumePath1);
        //System.out.println("file:" + filename);
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

}



