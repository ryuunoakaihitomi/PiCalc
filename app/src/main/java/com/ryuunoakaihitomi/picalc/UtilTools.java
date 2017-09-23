package com.ryuunoakaihitomi.picalc;

import android.os.Build;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ZQY on 2017/9/11.
 * 作者版权所有。用途:工具集
 */

public class UtilTools {

    //取CPU型号,通过Linux的cpuinfo接口读取
    static String getCPUModel() {
        String raw = cmd("cat /proc/cpuinfo");
        //不同ABI要读取的键值不同:x86和arm
        String x86 = "model name	: ";
        String arm = "Hardware	: ";
        String abi;
        //以21为界,读取ABI方法不同
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            abi = stringArrayToString(Build.SUPPORTED_ABIS);
        else
            abi = Build.CPU_ABI + Build.CPU_ABI2;
        if (abi.contains("x86"))
            return raw.subSequence(raw.indexOf(x86) + x86.length(), raw.indexOf("\n", raw.indexOf(x86) + x86.length())).toString();
        else
            return raw.subSequence(raw.indexOf(arm) + arm.length(), raw.indexOf("\n", raw.indexOf(arm) + arm.length())).toString();
    }

    //取设备厂商和型号
    static String getModelInfo() {
        return Build.MANUFACTURER + " → " + Build.MODEL;
    }

    //shell命令行和取标准输出流
    static String cmd(String c) {
        StringBuilder sb = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(c);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String l = "";
            while ((l = br.readLine()) != null) {
                sb.append(l + System.getProperty("line.separator"));
            }
            p.getErrorStream().close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    //字符串数组转字符串:foreach遍历追加
    private static String stringArrayToString(String[] in) {
        StringBuilder sb = new StringBuilder();
        for (String s : in)
            sb.append(s);
        return sb.toString();
    }

    //取当前时间（简化）
    static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
    }

    /*
        文件读写
    */

    static String readFile(String fileName) {
        String res = "";
        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            int length = fis.available();
            byte[] buffer = new byte[length];
            fis.read(buffer);
            res = new String(buffer, "UTF-8");
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    static void writeFile(String write_str, String fileName) {
        try {
            File file = new File(fileName);
            File dir = new File(fileName.subSequence(0, fileName.lastIndexOf("/")).toString());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = write_str.getBytes();
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理机信使，用于处理子线程的UI操作
    static void handlerMessager(Handler handler, int x) {
        Message message = new Message();
        message.what = x;
        handler.sendMessage(message);
    }
}