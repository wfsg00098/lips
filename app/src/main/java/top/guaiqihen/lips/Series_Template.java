package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class Series_Template extends AppCompatActivity {
    String describe;
    SwipeRefreshLayout srl;
    private Vector<ImageView> list = new Vector<>();
    private Vector<String> urls = new Vector<>();
    private boolean first = true;
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

                    final String show = covert_quot(jsonobj.getString("describ" + Integer.toString(i + 1)));
                    final String con = jsonobj.getString("name" + Integer.toString(i + 1));
                    final String img_link = jsonobj.getString("img" + Integer.toString(i + 1));

                    if (!img_link.equals("null")) {
                        ImageButton ibtn = new ImageButton(Series_Template.this);
                        ibtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        ibtn.setScaleType(ImageButton.ScaleType.FIT_XY);
                        ibtn.setAdjustViewBounds(true);
                        TypedValue typedValue = new TypedValue();
                        ibtn.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
                        int[] attribute = new int[]{android.R.attr.selectableItemBackground};
                        TypedArray typedArray = ibtn.getContext().getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
                        ibtn.setBackground(typedArray.getDrawable(0));
                        list.add(ibtn);
                        urls.add(img_link);
                        ibtn.setOnClickListener(v -> {
                            try {
                                Series_Template series = new Series_Template();
                                Intent it = new Intent(getApplicationContext(), series.getClass());
                                Bundle bundle = new Bundle();
                                bundle.putString("des", describe + "_" + con);
                                bundle.putString("show", show);
                                it.putExtras(bundle);
                                startActivity(it);
                            } catch (Exception e) {
                                Toast.makeText(Series_Template.this, e.toString(), Toast.LENGTH_LONG + 5).show();
                            }
                        });
                        ll_t.addView(ibtn);
                    }


                    Button btn = new Button(Series_Template.this);
                    btn.setText(show);
                    btn.setAllCaps(false);
                    btn.setTextColor(Color.parseColor("#000000"));
                    TypedValue typedValue = new TypedValue();
                    btn.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
                    int[] attribute = new int[]{android.R.attr.selectableItemBackground};
                    TypedArray typedArray = btn.getContext().getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
                    btn.setBackground(typedArray.getDrawable(0));

                    btn.setOnClickListener(v -> {
                        try {
                            Number_Template series = new Number_Template();
                            Intent it = new Intent(getApplicationContext(), series.getClass());
                            Bundle bundle = new Bundle();
                            bundle.putString("des", describe + "_" + con);
                            bundle.putString("show", show);
                            it.putExtras(bundle);
                            startActivity(it);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
                        }
                    });
                    ll_t.addView(btn);
                }
                if (list.size() != 0) new getImage().start();
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

    private String covert_quot(String str) {
        return str.replace("&quot;", "\"");
    }

    private Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        File fl = new File(getExternalCacheDir().getAbsolutePath(), GlobalSettings.sha1(url));
        try {

            if (!first || !fl.exists()) {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(6000);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.connect();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(fl);
                int temp;
                while ((temp = is.read()) != -1) fos.write(temp);
                fos.flush();
                is.close();
            }
            FileInputStream fis = new FileInputStream(fl);
            bmp = BitmapFactory.decodeStream(fis);
            fis.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
        }
        return bmp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.series_template);
        Bundle bundle = this.getIntent().getExtras();
        assert bundle != null;
        describe = bundle.getString("des");
        String show = bundle.getString("show");


        SpannableString msp = new SpannableString("选择系列 - " + show);

        msp.setSpan(new ForegroundColorSpan(Color.WHITE), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(GlobalSettings.ThemeColor));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(GlobalSettings.ThemeColor);
            getWindow().setStatusBarColor(GlobalSettings.ThemeColor);
        }

        if (GlobalSettings.reverse()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            msp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setTitle(msp);

        srl = findViewById(R.id.series_srl);
        srl.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        srl.setOnRefreshListener(() -> {
            srl.setRefreshing(true);
            LinearLayout ll = findViewById(R.id.ll_t);
            ll.removeAllViews();
            list.clear();
            urls.clear();
            first = false;
            new isNetworkOk().start();
        });
        srl.setRefreshing(true);
        LinearLayout ll_t = findViewById(R.id.ll_t);
        ll_t.removeAllViews();
        list.clear();
        urls.clear();


        new isNetworkOk().start();
        if (GlobalSettings.isLogged) new GlobalSettings.logger("浏览品牌_" + show).start();

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
}
