package top.guaiqihen.lips;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GlobalSettings {
    static boolean AutoUpdate;
    static int ThemeColor;
    static boolean isLogged = false;
    static String username;
    static String password;
    static String nickname;
    static SQLiteDatabase db;

    static void LoadSettings(String path) {
        db = SQLiteDatabase.openOrCreateDatabase(path + "/databases/settings.db", null);
        String sql = "create table if not exists settings(AutoUpdate text , ThemeColor text, username text, password text, nickname text)";
        db.execSQL(sql);
        Cursor cursor = db.query("settings", new String[]{"AutoUpdate", "ThemeColor", "username", "password", "nickname"}, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            sql = "insert into settings values('true'," + Integer.toString(Color.parseColor("#FF4081")) + ",null,null)";
            db.execSQL(sql);
            AutoUpdate = true;
            ThemeColor = Color.parseColor("#FF4081");
            username = "";
            password = "";
            nickname = "";
        } else {
            cursor.moveToFirst();
            AutoUpdate = cursor.getString(0).equals("true");
            ThemeColor = cursor.getInt(1);
            username = cursor.getString(2);
            password = cursor.getString(3);
            nickname = cursor.getString(4);
            Login();
        }
        cursor.close();
    }

    static void SetAutoUpdate(boolean is) {
        AutoUpdate = is;
        String sql;
        if (is) sql = "update settings set AutoUpdate='true'";
        else sql = "update settings set AutoUpdate='true'";
        db.execSQL(sql);
    }

    static void SetThemeColor(int color) {
        ThemeColor = color;
        String sql = "update settings set ThemeColor = '" + Integer.toString(color) + "'";
        db.execSQL(sql);
    }

    static void Login() {
        class login extends Thread {
            @Override
            public void run() {
                try {
                    HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_login.php?username=" + username + "&password=" + password).openConnection();
                    url.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
                    br.readLine();
                    JSONObject json = new JSONObject(br.readLine());
                    String success = json.getString("status");
                    String nick = json.getString("nickname");
                    if (success.equals("success")) {
                        isLogged = true;
                        nickname = nick;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        new login().start();
    }

    static void Logout() {
        isLogged = false;
        username = "";
        password = "";
        nickname = "";
    }
}
