package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class Series_Template extends AppCompatActivity {
    private Vector<ImageView> list = new Vector<>();
    private Vector<String> urls = new Vector<>();
    String describe;
    SwipeRefreshLayout srl;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int temp = msg.what;
            if (temp < list.size()) {
                Bitmap bmp = (Bitmap) msg.obj;
                list.get(temp).setImageBitmap(bmp);
            }
        }
    };

    private String covert_quot(String str) {
        return str.replace("&quot;", "\"");
    }

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
        @Override
        public void handleMessage(Message msg) {
            String temp = (String) msg.obj;
            try {
                JSONObject jsonobj = new JSONObject(temp);
                int count = jsonobj.getInt("count");
                LinearLayout ll_t = findViewById(R.id.ll_t);
                int i;
                for (i = 0; i < count; i++) {
                    Button btn = new Button(getApplicationContext());
                    btn.setText(covert_quot(jsonobj.getString("describ" + Integer.toString(i + 1))));
                    final String con = jsonobj.getString("name" + Integer.toString(i + 1));
                    btn.setAllCaps(false);
                    btn.setTextColor(Color.parseColor("#000000"));
                    btn.setBackgroundColor(Color.parseColor("#eaeaea"));
                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                Number_Template series = new Number_Template();
                                Intent it = new Intent(getApplicationContext(), series.getClass());
                                Bundle bundle = new Bundle();
                                bundle.putString("des", describe + "_" + con);
                                it.putExtras(bundle);
                                startActivity(it);
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
                            }
                        }
                    });
                    ll_t.addView(btn);
                }
                if (i == 0) {
                    Toast.makeText(getApplicationContext(), "这里还没有东西呢，过会儿再来吧～", Toast.LENGTH_LONG + 5).show();
                    Series_Template.this.finish();
                }
                if (list.size() == 0 && srl.isRefreshing()) srl.setRefreshing(false);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
            }
        }
    };

    private Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(6000);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
        }
        return bmp;
    }


    final private class getImage extends Thread {
        @Override
        public void run() {
            try {
                for (int i = 0; i < list.size(); i++) {
                    Bitmap bmp = getURLimage(urls.get(i));
                    Message msg = new Message();
                    msg.obj = bmp;
                    msg.what = i;
                    handler.sendMessage(msg);
                }
                stoprefresh.sendEmptyMessage(1);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                ToastHandler.sendMessage(msg);
            }
        }
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
                new getString(describe).start();
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
        String cat;

        getString(String des) {
            cat = des;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/main.php?cat=" + cat).openConnection();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.series_template);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#FF4081"));
        }

        srl = findViewById(R.id.series_srl);
        srl.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srl.setRefreshing(true);
                LinearLayout ll = findViewById(R.id.ll_t);
                ll.removeAllViews();
                list.clear();
                urls.clear();
                new isNetworkOk().start();
            }
        });
        srl.setRefreshing(true);
        LinearLayout ll_t = findViewById(R.id.ll_t);
        ll_t.removeAllViews();
        list.clear();
        urls.clear();
        Bundle bundle = this.getIntent().getExtras();
        assert bundle != null;
        describe = bundle.getString("des");
        this.setTitle("选择系列");
        new isNetworkOk().start();

    }
}
