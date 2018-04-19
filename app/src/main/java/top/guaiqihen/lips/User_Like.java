package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class User_Like extends AppCompatActivity {
    SwipeRefreshLayout srl;
    private Vector<ImageView> list = new Vector<>();
    private Vector<String> urls = new Vector<>();

    @SuppressLint("HandlerLeak")
    private Handler stoprefresh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (srl.isRefreshing()) srl.setRefreshing(false);
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler ToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG + 5).show();
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler handler2 = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            String temp = (String) msg.obj;
            try {
                JSONObject jsonobj = new JSONObject(temp);
                int count = jsonobj.getInt("count");
                LinearLayout ll_n = findViewById(R.id.ll_n1);
                int i;
                for (i = 0; i < count; i++) {
                    Button btn = new Button(User_Like.this);
                    final String con = jsonobj.getString("item" + Integer.toString(i + 1));
                    final String show = covert_quot(jsonobj.getString("show" + Integer.toString(i + 1)));
                    int color = jsonobj.getInt("color" + Integer.toString(i + 1));
                    btn.setText(show);
                    btn.setAllCaps(false);
                    int red = (color & 0xff0000) >> 16;
                    int green = (color & 0x00ff00) >> 8;
                    int blue = color & 0x0000ff;
                    String Red = Integer.toHexString(red);
                    String Green = Integer.toHexString(green);
                    String Blue = Integer.toHexString(blue);
                    if (Red.length() == 1) Red = "0" + Red;
                    if (Green.length() == 1) Green = "0" + Green;
                    if (Blue.length() == 1) Blue = "0" + Blue;
                    final String color_str = "#" + Red + Green + Blue;
                    if (GlobalSettings.reverse(red, green, blue)) {
                        btn.setTextColor(Color.parseColor("#000000"));
                    } else {
                        btn.setTextColor(Color.parseColor("#ffffff"));
                    }
                    btn.setBackgroundColor(color);
                    btn.setOnClickListener(v -> {
                        try {
                            Content_Template series = new Content_Template();
                            Intent it = new Intent(getApplicationContext(), series.getClass());
                            Bundle bundle = new Bundle();
                            bundle.putString("des", con);
                            bundle.putString("color", color_str);
                            bundle.putString("show", show);
                            it.putExtras(bundle);
                            startActivity(it);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
                        }
                    });
                    ll_n.addView(btn);
                }
                if (i == 0) {
                    Toast.makeText(getApplicationContext(), "你还没有收藏任何内容呢～", Toast.LENGTH_LONG + 5).show();
                    User_Like.this.finish();
                }
                if (list.size() == 0 && srl.isRefreshing()) srl.setRefreshing(false);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
            }


        }
    };

    private String covert_quot(String str) {
        return str.replace("&quot;", "\"");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_like);

        SpannableString msp = new SpannableString("我的收藏");
        msp.setSpan(new ForegroundColorSpan(Color.WHITE), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(GlobalSettings.ThemeColor));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(GlobalSettings.ThemeColor);
            getWindow().setStatusBarColor(GlobalSettings.ThemeColor);
        }

        if (GlobalSettings.reverse()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.BLACK);
                }
            }
            msp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setTitle(msp);

        srl = findViewById(R.id.number_srl1);
        srl.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        srl.setOnRefreshListener(() -> {
            srl.setRefreshing(true);
            LinearLayout ll = findViewById(R.id.ll_n1);
            ll.removeAllViews();
            list.clear();
            urls.clear();
            new isNetworkOk().start();
        });


        if (GlobalSettings.isLogged) new GlobalSettings.logger("查看收藏").start();
    }


    final private class isNetworkOk extends Thread {
        @Override
        public void run() {
            try {
                if (!isNetWorkOK(getApplicationContext())) {
                    Message msg = new Message();
                    msg.obj = "无网络，请检查网络连接！";
                    ToastHandler.sendMessage(msg);
                }
                while (!isNetWorkOK(getApplicationContext())) {
                    sleep(1000);
                }
                new getString().start();
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                ToastHandler.sendMessage(msg);
            }
        }

        boolean isNetWorkOK(Context context) {
            try {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                assert manager != null;
                NetworkInfo ni = manager.getActiveNetworkInfo();
                return ni != null && ni.isConnected();
            } catch (Exception e) {
                return false;
            }


        }
    }

    final private class getString extends Thread {
        @Override
        public void run() {
            try {
                HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_getlike.php?username=" + GlobalSettings.username + "&item=!all").openConnection();
                url.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
                br.readLine();
                Message msg = new Message();
                msg.obj = br.readLine();
                handler2.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                ToastHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            srl.setRefreshing(true);
            LinearLayout ll = findViewById(R.id.ll_n1);
            ll.removeAllViews();
            list.clear();
            urls.clear();
            new isNetworkOk().start();
        }
    }
}
