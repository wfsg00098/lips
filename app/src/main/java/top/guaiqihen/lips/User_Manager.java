package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User_Manager extends AppCompatActivity {
    String logs = "";
    @SuppressLint("HandlerLeak")
    private Handler changePassResult = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AlertDialog dialog = (AlertDialog) msg.obj;
            if (msg.what == 1) {
                Toast.makeText(getApplicationContext(), "修改成功！", Toast.LENGTH_LONG + 5).show();
                new GlobalSettings.logger("修改密码").start();
                dialog.dismiss();
            } else {
                Toast.makeText(getApplicationContext(), "修改失败！", Toast.LENGTH_LONG + 5).show();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler changeNickResult = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AlertDialog dialog = (AlertDialog) msg.obj;
            if (msg.what == 1) {
                Toast.makeText(getApplicationContext(), "修改成功！", Toast.LENGTH_LONG + 5).show();
                new GlobalSettings.logger("修改昵称").start();
                dialog.dismiss();
            } else {
                Toast.makeText(getApplicationContext(), "修改失败！", Toast.LENGTH_LONG + 5).show();
            }
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler getLogResult = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            View view = View.inflate(User_Manager.this, R.layout.user_log, null);

            TextView log = view.findViewById(R.id.log);

            AlertDialog dialog = new AlertDialog.Builder(User_Manager.this)
                    .setView(view)
                    .create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            log.setText((String) msg.obj);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manager);
        SpannableString msp = new SpannableString("个人信息");
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


        TextView tv = findViewById(R.id.textView7);
        tv.setText("欢迎你，" + GlobalSettings.nickname);


        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            GlobalSettings.Logout();
            if (GlobalSettings.isLogged) new GlobalSettings.logger("注销").start();
            Toast.makeText(getApplicationContext(), "已注销！", Toast.LENGTH_LONG + 5).show();
            finish();
        });

        Button btn = findViewById(R.id.ChangePass);
        btn.setOnClickListener(v -> {
            try {
                View view = View.inflate(User_Manager.this, R.layout.change_pass, null);

                EditText oldpass = view.findViewById(R.id.oldPass);
                EditText newpass = view.findViewById(R.id.newPass);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(view)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消", null)
                        .create();
                dialog.setCanceledOnTouchOutside(false);

                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {

                    String passregex = "^[A-Za-z0-9!@#$%^&*()\\[\\]\\\\-_=+{}|:\'\"<,>./?]+$";

                    Pattern pattern = Pattern.compile(passregex);
                    Matcher matcher1 = pattern.matcher(oldpass.getText());
                    Matcher matcher2 = pattern.matcher(newpass.getText());
                    if (!matcher1.matches() || !matcher2.matches()) {
                        Toast.makeText(getApplicationContext(), "密码中只能使用大小写字母、数字以及特殊符号！", Toast.LENGTH_LONG + 5).show();
                        return;
                    }

                    if (!GlobalSettings.sha1(oldpass.getText().toString()).equals(GlobalSettings.password)) {
                        Toast.makeText(getApplicationContext(), "旧密码错误！", Toast.LENGTH_LONG + 5).show();
                        return;
                    }
                    new Thread(() -> {
                        try {
                            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_change_pass.php?username=" + GlobalSettings.username + "&oldpassword=" + GlobalSettings.sha1(oldpass.getText().toString()) + "&newpassword=" + GlobalSettings.sha1(newpass.getText().toString())).openConnection();
                            url.connect();
                            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
                            br.readLine();
                            JSONObject json = new JSONObject(br.readLine());
                            String success = json.getString("status");
                            Message msg = new Message();
                            msg.obj = dialog;
                            if (success.equals("success")) {
                                msg.what = 1;
                                GlobalSettings.password = GlobalSettings.sha1(newpass.getText().toString());
                                GlobalSettings.SavePass();
                            } else msg.what = 0;
                            changePassResult.sendMessage(msg);
                        } catch (Exception e) {
                            Message msg = new Message();
                            msg.obj = dialog;
                            msg.what = 0;
                            changePassResult.sendMessage(msg);
                        }
                    }).start();
                });

            } catch (Exception ignored) {
            }
        });

        btn = findViewById(R.id.ChangeNick);
        btn.setOnClickListener(v -> {
            try {
                View view = View.inflate(User_Manager.this, R.layout.change_nick, null);


                EditText newnick = view.findViewById(R.id.newNick);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(view)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消", null)
                        .create();
                dialog.setCanceledOnTouchOutside(false);

                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {

                    String nickregex = "^[\u4e00-\u9fa5A-Za-z0-9!@#$%^&*()\\[\\]\\\\-_=+{}|:\'\"<,>./?]+$";

                    Pattern pattern = Pattern.compile(nickregex);
                    Matcher matcher = pattern.matcher(newnick.getText());
                    if (!matcher.matches()) {
                        Toast.makeText(getApplicationContext(), "昵称中只能使用中文、大小写字母、数字以及特殊符号！", Toast.LENGTH_LONG + 5).show();
                        return;
                    }

                    new Thread(() -> {
                        try {
                            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_change_nick.php?username=" + GlobalSettings.username + "&nickname=" + newnick.getText().toString()).openConnection();
                            url.connect();
                            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
                            br.readLine();
                            JSONObject json = new JSONObject(br.readLine());
                            String success = json.getString("status");
                            Message msg = new Message();
                            msg.obj = dialog;
                            if (success.equals("success")) {
                                msg.what = 1;
                                GlobalSettings.nickname = newnick.getText().toString();
                                GlobalSettings.SaveNick();
                            } else msg.what = 0;
                            changeNickResult.sendMessage(msg);
                        } catch (Exception e) {
                            Message msg = new Message();
                            msg.obj = dialog;
                            msg.what = 0;
                            changeNickResult.sendMessage(msg);
                        }
                    }).start();
                });

            } catch (Exception ignored) {
            }
        });

        btn = findViewById(R.id.listlog);
        btn.setOnClickListener(v -> new Thread(() -> {
            try {
                JSONObject json = GlobalSettings.getLog();
                assert json != null;
                int count = json.getInt("count");
                int i;
                logs = "";
                for (i = 0; i < count; i++)
                    logs = json.getString("operation" + String.valueOf(i + 1)) + "\n" + logs;

            } catch (Exception e) {
                logs = "获取失败";
            } finally {
                Message msg = new Message();
                msg.obj = logs;
                getLogResult.sendMessage(msg);
            }
        }).start());

        btn = findViewById(R.id.listLIke);
        btn.setOnClickListener(v -> startActivity(new Intent(User_Manager.this, User_Like.class)));


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            TextView tv = findViewById(R.id.textView7);
            tv.setText("欢迎你，" + GlobalSettings.nickname);
        }
    }
}
