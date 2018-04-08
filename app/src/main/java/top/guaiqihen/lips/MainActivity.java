package top.guaiqihen.lips;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;


public class MainActivity extends AppCompatActivity {
    final static int version = 106;
    private Vector<ImageView> list = new Vector<>();
    private Vector<String> urls = new Vector<>();
    SwipeRefreshLayout srl;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler stoprefresh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (srl.isRefreshing()) srl.setRefreshing(false);
        }
    };
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
    private Handler return_update = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what <= version) {
                return;
            }
            if (msg.what > version) {
                ShowUpdate(msg.what, msg.obj.toString());
                return;
            }
            Toast.makeText(MainActivity.this, "无法检查更新", Toast.LENGTH_LONG + 5).show();
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler ToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG + 5).show();
        }
    };

    private String covert_quot(String str) {
        return str.replace("&quot;", "\"");
    }

    @SuppressLint("HandlerLeak")
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String temp = (String) msg.obj;
            try {
                JSONObject jsonobj = new JSONObject(temp);
                int count = jsonobj.getInt("count");
                LinearLayout ll = findViewById(R.id.LL);
                int i;
                for (i = 0; i < count; i++) {
                    Button btn = new Button(MainActivity.this);
                    final String show = covert_quot(jsonobj.getString("describ" + Integer.toString(i + 1)));
                    btn.setText(show);
                    final String con = jsonobj.getString("name" + Integer.toString(i + 1));
                    btn.setTextColor(Color.parseColor("#000000"));
                    btn.setBackgroundColor(Color.parseColor("#eaeaea"));
                    btn.setAllCaps(false);
                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                Series_Template series = new Series_Template();
                                Intent it = new Intent(getApplicationContext(), series.getClass());
                                Bundle bundle = new Bundle();
                                bundle.putString("des", con);
                                bundle.putString("show", show);
                                it.putExtras(bundle);
                                startActivity(it);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG + 5).show();
                            }
                        }
                    });
                    ll.addView(btn);
                }
                if (i == 0) {
                    Toast.makeText(MainActivity.this, "这里还没有东西呢，过会儿再来吧～", Toast.LENGTH_LONG + 5).show();
                    MainActivity.this.finish();
                }
                if (list.size() == 0 && srl.isRefreshing()) srl.setRefreshing(false);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG + 5).show();
            }


        }
    };

    final private class GetUpdate extends Thread {
        @Override
        public void run() {
            try {
                HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/main.php?cat=ver").openConnection();
                url.connect();
                if (HttpURLConnection.HTTP_OK != url.getResponseCode()) {
                    Message msg = new Message();
                    msg.obj = "无法检查更新！";
                    ToastHandler.sendMessage(msg);
                    return;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
                br.readLine();
                JSONObject json = new JSONObject(br.readLine());
                int ver = json.getInt("version");
                String size = json.getString("size");
                Message msg = new Message();
                msg.what = ver;
                msg.obj = size;
                return_update.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                ToastHandler.sendMessage(msg);
            }
        }
    }


    private void ShowUpdate(final int ver, String size) {
        final AlertDialog.Builder confirm = new AlertDialog.Builder(MainActivity.this)
                .setTitle("发现新版本")
                .setMessage("新版本：" + Integer.toString(ver) + "，大小" + size + "，您是否要下载？");

        confirm.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ProgressDialog pd = new ProgressDialog(MainActivity.this);

                @SuppressLint("HandlerLeak") final Handler progressed = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        pd.cancel();
                        File file = new File(getExternalCacheDir().getAbsolutePath(), Integer.toString(ver) + ".apk");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                };
                pd.setTitle("下载中");
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.show();
                pd.setCanceledOnTouchOutside(false);
                pd.setCancelable(false);
                class DownloadUpdate extends Thread {
                    private int version;

                    private DownloadUpdate(int version) {
                        this.version = version;
                    }

                    @Override
                    public void run() {
                        try {
                            verifyStoragePermissions(MainActivity.this);
                            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/update/" + Integer.toString(version) + ".apk").openConnection();
                            url.setRequestProperty("Accept-Encoding", "identity");
                            url.connect();
                            if (HttpURLConnection.HTTP_OK != url.getResponseCode()) {
                                Message msg = new Message();
                                msg.obj = "无法定位更新文件";
                                ToastHandler.sendMessage(msg);
                                return;
                            }
                            int contentsize = url.getContentLength();
                            pd.setMax(contentsize);
                            int currentsize = 0;
                            InputStream is = url.getInputStream();
                            File file = new File(getExternalCacheDir().getAbsolutePath(), Integer.toString(version) + ".apk");
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] temp = new byte[1024];
                            int len;
                            while ((len = is.read(temp)) != -1) {
                                currentsize += len;
                                pd.setProgress(currentsize);
                                fos.write(temp, 0, len);
                                fos.flush();
                            }
                            fos.close();
                            progressed.sendEmptyMessage(0);
                        } catch (Exception e) {
                            Message msg = new Message();
                            msg.obj = e.toString();
                            ToastHandler.sendMessage(msg);
                        }
                    }
                }
                new DownloadUpdate(ver).start();
            }
        });
        confirm.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "更新已取消", Toast.LENGTH_LONG + 5).show();
                    }
                });
        confirm.create().show();
    }


    final private class getString extends Thread {
        @Override
        public void run() {
            try {
                HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/main.php?cat=main").openConnection();
                url.connect();
                while (HttpURLConnection.HTTP_OK != url.getResponseCode()) {
                    Message msg = new Message();
                    msg.obj = "无法获取内容";
                    ToastHandler.sendMessage(msg);
                    sleep(2000);
                }
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("选择品牌");
        new GetUpdate().start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#FF4081"));
        }


        srl = findViewById(R.id.main_srl);
        srl.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srl.setRefreshing(true);
                LinearLayout ll = findViewById(R.id.LL);
                ll.removeAllViews();
                list.clear();
                urls.clear();
                new isNetworkOk().start();
            }
        });
        srl.setRefreshing(true);
        LinearLayout ll = findViewById(R.id.LL);
        ll.removeAllViews();
        list.clear();
        urls.clear();
        new isNetworkOk().start();
        try {
            verifyStoragePermissions(this);
            File fl = new File(getExternalCacheDir().getAbsolutePath(), "TestCache.txt");
            OutputStream outputStream1 = new FileOutputStream(fl);
            outputStream1.write("Badegg is very 6666666".getBytes());
            outputStream1.flush();
            outputStream1.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG + 5).show();
        }


    }


    private int mBackKeyPressedTimes = 0;

    @Override
    public void onBackPressed() {
        if (mBackKeyPressedTimes == 0) {
            Toast.makeText(MainActivity.this, "再按一次返回键退出！", Toast.LENGTH_LONG + 5).show();
            mBackKeyPressedTimes = 1;
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        mBackKeyPressedTimes = 0;
                    }
                }
            }.start();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            String data = getString(R.string.action_clear);
            String size = getCacheSize(getExternalCacheDir());
            menu.findItem(R.id.action_clear).setTitle(String.format(data, size));
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG + 5).show();
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, About.class));
            return true;
        }

        if (id == R.id.action_clear) {
            try {
                deleteFilesByDirectory(this.getExternalCacheDir());
                Toast.makeText(MainActivity.this, "清理完成！", Toast.LENGTH_LONG + 5).show();
                return true;
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG + 5).show();
            }
        }


        if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    size = size + getFolderSize(aFileList);
                } else {
                    size = size + aFileList.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String getCacheSize(File file) throws Exception {
        return getFormatSize(getFolderSize(file));
    }

    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

}