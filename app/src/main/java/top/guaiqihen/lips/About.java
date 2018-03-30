package top.guaiqihen.lips;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;


public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#FF4081"));
        }
        this.setTitle("关于");
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
