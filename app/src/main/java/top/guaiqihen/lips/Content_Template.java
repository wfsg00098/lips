package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.security.MessageDigest;
import java.util.Vector;

public class Content_Template extends AppCompatActivity {
    String color;
    String describe;
    SwipeRefreshLayout srl;
    private Vector<ImageView> list = new Vector<>();
    private Vector<String> urls = new Vector<>();
    private boolean first = true;

    int max(int a, int b) {
        if (a >= b) return a;
        else return b;
    }

    int min(int a, int b) {
        if (a <= b) return a;
        else return b;
    }

    boolean reverse(int r, int g, int b) {
        double MIN, MAX, L;
        MAX = (double) max(r, max(g, b)) / 255.0;
        MIN = (double) min(r, min(g, b)) / 255.0;
        L = (MAX + MIN) / 2.0;
        return L > 0.5;
    }
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
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            String temp = (String) msg.obj;
            try {
                JSONObject jsonobj = new JSONObject(temp);
                int count = jsonobj.getInt("count");
                LinearLayout ll_c = findViewById(R.id.ll_c);
                int i;
                for (i = 0; i < count; i++) {
                    if (jsonobj.getString("type" + Integer.toString(i + 1)).equals("str")) {
                        TextView view = new TextView(getApplicationContext());
                        view.setTextAppearance(getApplicationContext(), R.style.TextAppearance_AppCompat_Body1);
                        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(jsonobj.getString("size" + Integer.toString(i + 1))));
                        if (jsonobj.getString("align" + Integer.toString(i + 1)).equals("left")) {
                            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        } else if (jsonobj.getString("align" + Integer.toString(i + 1)).equals("center")) {
                            view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        } else if (jsonobj.getString("align" + Integer.toString(i + 1)).equals("right")) {
                            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        }
                        if (jsonobj.getString("bold" + Integer.toString(i + 1)).equals("true"))
                            view.getPaint().setFakeBoldText(true);
                        if (jsonobj.getString("italic" + Integer.toString(i + 1)).equals("true"))
                            view.getPaint().setTextSkewX(-0.5f);
                        if (jsonobj.getString("underline" + Integer.toString(i + 1)).equals("true"))
                            view.getPaint().setUnderlineText(true);
                        if (jsonobj.getString("color" + Integer.toString(i + 1)).equals("null")) {
                            view.setTextColor(Color.BLACK);
                        } else {
                            view.setTextColor(Color.parseColor(jsonobj.getString("color" + Integer.toString(i + 1))));
                        }
                        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        view.setText(jsonobj.getString("content" + Integer.toString(i + 1)));
                        ll_c.addView(view);
                    }
                    if (jsonobj.getString("type" + Integer.toString(i + 1)).equals("link")) {
                        TextView view = new TextView(getApplicationContext());
                        view.setAutoLinkMask(Linkify.WEB_URLS);
                        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(jsonobj.getString("size" + Integer.toString(i + 1))));
                        if (jsonobj.getString("align" + Integer.toString(i + 1)).equals("left")) {
                            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        } else if (jsonobj.getString("align" + Integer.toString(i + 1)).equals("center")) {
                            view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        } else if (jsonobj.getString("align" + Integer.toString(i + 1)).equals("right")) {
                            view.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        }
                        if (jsonobj.getString("bold" + Integer.toString(i + 1)).equals("true"))
                            view.getPaint().setFakeBoldText(true);
                        if (jsonobj.getString("italic" + Integer.toString(i + 1)).equals("true"))
                            view.getPaint().setTextSkewX(-0.5f);
                        if (jsonobj.getString("underline" + Integer.toString(i + 1)).equals("true"))
                            view.getPaint().setUnderlineText(true);
                        view.setLinkTextColor(Color.parseColor("#FF4081"));
                        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        view.setText(jsonobj.getString("content" + Integer.toString(i + 1)));
                        ll_c.addView(view);
                    }
                    if (jsonobj.getString("type" + Integer.toString(i + 1)).equals("img")) {
                        ImageView img = new ImageView(getApplicationContext());
                        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        img.setScaleType(ImageView.ScaleType.FIT_XY);
                        img.setAdjustViewBounds(true);
                        list.add(img);
                        ll_c.addView(img);
                        urls.add(jsonobj.getString("content" + Integer.toString(i + 1)));
                    }
                    if (list.size() != 0) new getImage().start();
                }

                if (i == 0) {
                    Toast.makeText(getApplicationContext(), "这里还没有东西呢，过会儿再来吧～", Toast.LENGTH_LONG + 5).show();
                    Content_Template.this.finish();
                }
                if (list.size() == 0 && srl.isRefreshing()) srl.setRefreshing(false);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
            }


        }
    };

    public static String sha1(String info) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            alga.update(info.getBytes());
            digesta = alga.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byte2hex(digesta);
    }

    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString();
    }

    private String covert_quot(String str) {
        return str.replace("&quot;", "\"");
    }

    private Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        File fl = new File(getExternalCacheDir().getAbsolutePath(), sha1(url));
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
        setContentView(R.layout.content_template);


        srl = findViewById(R.id.content_srl);
        srl.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srl.setRefreshing(true);
                LinearLayout ll = findViewById(R.id.ll_c);
                ll.removeAllViews();
                list.clear();
                urls.clear();
                first = false;
                new isNetworkOk().start();
            }
        });
        srl.setRefreshing(true);

        LinearLayout ll_c = findViewById(R.id.ll_c);
        ll_c.removeAllViews();
        list.clear();
        urls.clear();
        Bundle bundle = this.getIntent().getExtras();
        assert bundle != null;
        describe = bundle.getString("des");
        color = bundle.getString("color");
        String show = bundle.getString("show");
        SpannableString msp = new SpannableString("色号介绍 - " + show);
        msp.setSpan(new ForegroundColorSpan(Color.WHITE), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int cl = Color.parseColor(color);
        int red = (cl & 0xff0000) >> 16;
        int green = (cl & 0x00ff00) >> 8;
        int blue = cl & 0x0000ff;
        if (reverse(red, green, blue)) {
            msp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(cl));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(cl);
            getWindow().setStatusBarColor(cl);
        }

        this.setTitle(msp);
        new isNetworkOk().start();

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
}
