package top.guaiqihen.lips;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;

public class User_Manager extends AppCompatActivity {

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


        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            GlobalSettings.Logout();
            if (GlobalSettings.isLogged) new GlobalSettings.logger("注销").start();
        });
    }
}
