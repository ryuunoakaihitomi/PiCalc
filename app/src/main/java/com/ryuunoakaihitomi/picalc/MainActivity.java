package com.ryuunoakaihitomi.picalc;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    //列表操控
    static SimpleAdapter sa;
    static ListView lv;
    static List<Map<String, Object>> data = new ArrayList<>();
    //计算进程
    static ProgressDialog pd;
    int style;
    //数据存储
    static Long e;
    static String d, st;
    static SharedPreferences sp;
    //应用实例
    static Context c;
    static Activity a;
    //子线程UI
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    lv.setAdapter(sa);
                    break;
                case 1:
                    Toast.makeText(c, c.getString(R.string.calc_completed), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //loading.view...
        lv = (ListView) findViewById(R.id.fst);
        a = MainActivity.this;
        c = getApplicationContext();
        sp = getSharedPreferences("_", MODE_PRIVATE);
        //兼容进度框UI样式
        if (Build.VERSION.SDK_INT >= 21)
            style = android.R.style.Theme_Material_Dialog_Alert;
        else
            style = AlertDialog.THEME_DEVICE_DEFAULT_DARK;
        //loading.list...
        addTag(getString(R.string.hard_info)
                , getString(R.string.phone_mod) + UtilTools.getModelInfo() + "\n" + getString(R.string.cpu_mod) + UtilTools.getCPUModel());
        addTag(getString(R.string.calc), getString(R.string.click_calc));
        if (TextUtils.isEmpty(cfgOperator(true, "st", null)) || TextUtils.isEmpty(cfgOperator(true, "d", null)) || TextUtils.isEmpty(cfgOperator(true, "wt", null)))
            addTag(getString(R.string.his), getString(R.string.no_histroy));
        else
            addTag(getString(R.string.his), String.format(getString(R.string.his_list)
                    , cfgOperator(true, "st", null), cfgOperator(true, "d", null), cfgOperator(true, "wt", null)));
        addTag(getString(R.string.help), getString(R.string.info));
        refreshView();
        //loading.operating...
        lv.setOnItemClickListener((AdapterView<?> p, View v, int pos, long id) -> {
            switch (pos) {
                case 1:
                    //保持常亮
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    AlertDialog.Builder ab = new AlertDialog.Builder(this, style);
                    EditText et = new EditText(this);
                    et.setTextColor(Color.WHITE);
                    et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                    ab.setTitle(getString(R.string.set_acc))
                            .setView(et)
                            .setIcon(android.R.drawable.ic_input_add)
                            .setNegativeButton(getString(R.string.mil), (dialog, which) -> startCalc("100000"))
                            .setPositiveButton(getString(R.string.detemine), (dialog, which) -> {
                                if (TextUtils.isEmpty(et.getText().toString()))
                                    Toast.makeText(getApplicationContext(), getString(R.string.null_acc), Toast.LENGTH_SHORT).show();
                                else
                                    startCalc(et.getText().toString());
                            })
                            .show();
                    break;
                case 3:
            }
        });
        lv.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 2:
                    String s = UtilTools.readFile(cfgOperator(true, "resPath", null));
                    if (TextUtils.isEmpty(s))
                        Toast.makeText(getApplicationContext(), getString(R.string.no_histroy), Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.read_res), Toast.LENGTH_SHORT).show();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this, style);
                                    ab.setTitle(getString(R.string.res_view))
                                            .setMessage(s)
                                            //图案是一只眼睛
                                            .setIcon(android.R.drawable.ic_menu_view)
                                            .setPositiveButton(getString(R.string.clip_cpy), (dialog, which) -> {
                                                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                                cm.setPrimaryClip(ClipData.newPlainText(null, s));
                                            })
                                            .show();
                                });
                            }
                        }, 500);
                    }
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), getString(R.string.developer), Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        });
        //BBP算法分量（废弃）
        Log.e(TAG, String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    void addTag(String title, String body) {
        Map<String, Object> item = new HashMap<>();
        item.put("t", title);
        item.put("b", body);
        data.add(item);
    }

    static void calcEndWork(long et, String res) {
        e = et;
        pd.cancel();
        UtilTools.handlerMessager(handler, 1);
        cfgOperator(false, "wt", String.valueOf(e));
        cfgOperator(false, "st", st);
        cfgOperator(false, "d", d);
        String s = c.getExternalCacheDir() + UtilTools.now() + ".txt";
        cfgOperator(false, "resPath", s);
        UtilTools.writeFile(res, s);
        //重启应用
        Intent intent = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
        a.finish();
    }

    static void refreshView() {
        sa = new SimpleAdapter(
                c,
                data,
                android.R.layout.simple_list_item_2,
                new String[]{"t", "b"},
                new int[]{android.R.id.text1, android.R.id.text2});
        UtilTools.handlerMessager(handler, 0);
    }

    //配置管理器
    static String cfgOperator(boolean isRead, String key, String val) {
        if (isRead)
            return sp.getString(key, null);
        else
            sp.edit().putString(key, val).commit();
        return null;
    }

    void startCalc(String arg) {
        st = UtilTools.now();
        d = arg;
        //创建进度对话框
        pd = new ProgressDialog(MainActivity.this, style);
        pd.setTitle(getString(R.string.calc_ing));
        pd.setIcon(android.R.drawable.ic_menu_rotate);
        pd.setMessage(getString(R.string.calcing_warning));
        pd.setCancelable(false);
        //解除常亮
        pd.setOnCancelListener((dialog1) -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
        //展示
        pd.show();
        //创建运算线程
        new Thread(() -> PiCalc.main(Integer.valueOf(arg))).start();
    }

    @Override
    protected void onStop() {
        c = null;
        Process.killProcess(Process.myPid());
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            onStop();
        return super.onKeyDown(keyCode, event);
    }
}
