package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    @SuppressLint("HandlerLeak")
    private Handler RegResult = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
                Toast.makeText(getApplicationContext(), "注册成功！", Toast.LENGTH_LONG + 5).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "注册失败！", Toast.LENGTH_LONG + 5).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SpannableString msp = new SpannableString("注册");
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

        TextView username = findViewById(R.id.username2);
        TextView password = findViewById(R.id.password2);
        TextView nickname = findViewById(R.id.nickname2);

        Button register = findViewById(R.id.register2);

        String userregex = "^[A-Za-z0-9]+$";
        String passregex = "^[A-Za-z0-9!@#$%^&*()\\[\\]\\\\-_=+{}|:\'\"<,>./?]+$";
        String nickregex = "^[\u4e00-\u9fa5A-Za-z0-9!@#$%^&*()\\[\\]\\\\-_=+{}|:\'\"<,>./?]+$";


        register.setOnClickListener(view -> {
            Pattern pattern = Pattern.compile(userregex);
            Matcher matcher = pattern.matcher(username.getText());
            if (!matcher.matches()){
                Toast.makeText(getApplicationContext(), "用户名中只能使用大小写字母以及数字！", Toast.LENGTH_LONG + 5).show();
                return;
            }

            pattern = Pattern.compile(passregex);
            matcher = pattern.matcher(password.getText());
            if (!matcher.matches()){
                Toast.makeText(getApplicationContext(), "密码中只能使用大小写字母、数字以及特殊符号！", Toast.LENGTH_LONG + 5).show();
                return;
            }

            pattern = Pattern.compile(nickregex);
            matcher = pattern.matcher(nickname.getText());
            if (!matcher.matches()){
                Toast.makeText(getApplicationContext(), "昵称中只能使用中文、大小写字母、数字以及特殊符号！", Toast.LENGTH_LONG + 5).show();
                return;
            }

            GlobalSettings.username = username.getText().toString();
            GlobalSettings.password = GlobalSettings.sha1(password.getText().toString());
            GlobalSettings.nickname = nickname.getText().toString();

            class RegThread extends Thread{
                @Override
                public void run(){
                    if (GlobalSettings.Register()) RegResult.sendEmptyMessage(1);
                    else RegResult.sendEmptyMessage(0);
                }
            }
            new RegThread().start();
        });



    }
}
