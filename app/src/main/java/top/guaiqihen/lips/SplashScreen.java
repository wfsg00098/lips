package top.guaiqihen.lips;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashScreen extends Activity {

    @SuppressLint("HandlerLeak")
    private Handler ToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG + 5).show();
        }
    };

    @SuppressLint("HandlerLeak")
    Handler start = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                MainActivity main = new MainActivity();
                Intent it = new Intent(getApplicationContext(), main.getClass());
                startActivity(it);
                SplashScreen.this.finish();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG + 5).show();
            }
        }
    };


    class wait extends Thread {
        @Override
        public void run() {
            try {
                sleep(2000);
                start.sendEmptyMessage(1);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.toString();
                ToastHandler.sendMessage(msg);
            }

        }
    }


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat);
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);


        getWindow().setBackgroundDrawableResource(R.drawable.splash);



        new wait().start();

    }
}
