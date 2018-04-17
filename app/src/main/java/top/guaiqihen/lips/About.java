package top.guaiqihen.lips;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;


public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        SpannableString msp = new SpannableString("关于");
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

        TextView tv = findViewById(R.id.copyright);
        Calendar date = Calendar.getInstance();
        String year = String.valueOf(date.get(Calendar.YEAR));
        String data = getString(R.string.copyright);
        tv.setText(String.format(data, year, MainActivity.version));


        class JumpToWebSite implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("https://lips.guaiqihen.com");
                intent.setData(content_url);
                startActivity(intent);
            }
        }

        findViewById(R.id.imageView).setOnClickListener(new JumpToWebSite());
        findViewById(R.id.textView3).setOnClickListener(new JumpToWebSite());

    }
}
