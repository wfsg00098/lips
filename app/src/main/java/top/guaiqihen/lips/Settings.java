package top.guaiqihen.lips;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SpannableString msp = new SpannableString("设置");
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

        try {
            TextView tv = findViewById(R.id.CacheSizeTextView);
            String size = GlobalSettings.getCacheSize(getExternalCacheDir());
            tv.setText(size);

            findViewById(R.id.imageView2).setOnClickListener(new JumpToWebSite());
            findViewById(R.id.textView9).setOnClickListener(new JumpToWebSite());
            findViewById(R.id.textView11).setOnClickListener(new JumpToWebSite());
            tv = findViewById(R.id.loginTextView);
            if (GlobalSettings.isLogged) tv.setText("欢迎你，" + GlobalSettings.nickname + "！");
            else tv.setText("未登录，请点击登录");
        } catch (Exception ignored) {}




        ImageView img = findViewById(R.id.imageView3);
        img.setImageDrawable(new ColorDrawable(GlobalSettings.ThemeColor));

        Button btn = findViewById(R.id.AboutButton);
        btn.setOnClickListener(v -> startActivity(new Intent(Settings.this, About.class)));

        btn = findViewById(R.id.LoginButton);
        btn.setOnClickListener(v -> {
            if (GlobalSettings.isLogged) startActivity(new Intent(Settings.this, User_Manager.class));
            else startActivity(new Intent(Settings.this, Login.class));
        });

        btn = findViewById(R.id.CacheButton);
        btn.setOnClickListener(v -> {
            try{
                GlobalSettings.deleteFilesByDirectory(getExternalCacheDir());
                Toast.makeText(Settings.this, "清理完毕！", Toast.LENGTH_LONG + 5).show();
                TextView tv = findViewById(R.id.CacheSizeTextView);
                String size = GlobalSettings.getCacheSize(getExternalCacheDir());
                tv.setText(size);
            }catch (Exception ignored){}
        });

        btn = findViewById(R.id.ThemeColorButton);
        btn.setOnClickListener(v -> {
            try{
                View view = View.inflate(Settings.this,R.layout.select_color,null);
                final SeekBar redBar = view.findViewById(R.id.red);
                final SeekBar greenBar = view.findViewById(R.id.green);
                final SeekBar blueBar = view.findViewById(R.id.blue);

                final TextView redtext = view.findViewById(R.id.redtext);
                final TextView greentext = view.findViewById(R.id.greentext);
                final TextView bluetext = view.findViewById(R.id.bluetext);

                final ImageView colorview = view.findViewById(R.id.ColorView);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(view)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消", null)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnShowListener(dialogInterface -> {
                    redBar.setProgress((GlobalSettings.ThemeColor & 0xff0000) >> 16);
                    greenBar.setProgress((GlobalSettings.ThemeColor & 0x00ff00) >> 8);
                    blueBar.setProgress(GlobalSettings.ThemeColor & 0x0000ff);
                    colorview.setImageDrawable(new ColorDrawable(GlobalSettings.ThemeColor));
                });

                class listener implements SeekBar.OnSeekBarChangeListener{
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        int color = Color.rgb(redBar.getProgress(),greenBar.getProgress(),blueBar.getProgress());
                        switch (seekBar.getId()){
                            case R.id.red:
                                redtext.setText(String.valueOf(redBar.getProgress()));break;
                            case R.id.green:
                                greentext.setText(String.valueOf(greenBar.getProgress()));break;
                            case R.id.blue:
                                bluetext.setText(String.valueOf(blueBar.getProgress()));break;
                            default:break;
                        }
                        colorview.setImageDrawable(new ColorDrawable(color));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
                redBar.setOnSeekBarChangeListener(new listener());
                greenBar.setOnSeekBarChangeListener(new listener());
                blueBar.setOnSeekBarChangeListener(new listener());
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    int color = Color.rgb(redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
                    GlobalSettings.SetThemeColor(color);
                    android.os.Process.killProcess(android.os.Process.myPid());

                    dialog.dismiss();
                });

            }catch (Exception ignored){}
        });


        final Switch sw = findViewById(R.id.switch2);
        if (GlobalSettings.AutoUpdate) sw.setChecked(true);
        else sw.setChecked(false);
        sw.setOnClickListener(v -> GlobalSettings.SetAutoUpdate(sw.isChecked()));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            TextView tv = findViewById(R.id.loginTextView);
            if (GlobalSettings.isLogged) tv.setText("欢迎你，" + GlobalSettings.nickname + "！");
            else tv.setText("未登录，请点击登录");
            Button btn = findViewById(R.id.LoginButton);
            btn.setOnClickListener(v -> {
                if (GlobalSettings.isLogged) startActivity(new Intent(Settings.this, User_Manager.class));
                else startActivity(new Intent(Settings.this, Login.class));
            });
        }
    }
}
