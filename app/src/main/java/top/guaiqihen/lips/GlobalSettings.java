package top.guaiqihen.lips;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class GlobalSettings {
    static boolean AutoUpdate = true;
    static int ThemeColor = Color.parseColor("#FF4081");
    static boolean isLogged = false;
    static String username;
    static String password;
    static String nickname;
    static SQLiteDatabase db;



    static boolean isLike(String item){
        try{
            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_getlike.php?username=" + username + "&item=" + item).openConnection();
            url.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
            br.readLine();
            JSONObject json = new JSONObject(br.readLine());
            String status = json.getString("status");
            return status.equals("true");
        }catch (Exception e){
            return false;
        }
    }

    static JSONObject getLike(){
        try{
            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_getlike.php?username=" + username + "&item=!all").openConnection();
            url.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
            br.readLine();
            return new JSONObject(br.readLine());
        }catch (Exception e){
            return null;
        }
    }

    static boolean setLike(String type ,String item){
        try{
            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_setlike.php?username=" + username + "&type=" + type + "&item=" + item).openConnection();
            url.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
            br.readLine();
            JSONObject json = new JSONObject(br.readLine());
            String status = json.getString("status");
            return status.equals("success");
        }catch (Exception e){
            return false;
        }
    }

    static boolean disLike(String item){
        try{
            HttpURLConnection url = (HttpURLConnection) new URL("https://lips.guaiqihen.top/user_dislike.php?username=" + username  + "&item=" + item).openConnection();
            url.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
            br.readLine();
            JSONObject json = new JSONObject(br.readLine());
            String status = json.getString("status");
            return status.equals("success");
        }catch (Exception e){
            return false;
        }
    }


    static int max(int a, int b) {
        if (a >= b) return a;
        else return b;
    }

    static int min(int a, int b) {
        if (a <= b) return a;
        else return b;
    }

    static boolean reverse(int r, int g, int b) {
        double MIN, MAX, L;
        MAX = (double) max(r, max(g, b)) / 255.0;
        MIN = (double) min(r, min(g, b)) / 255.0;
        L = (MAX + MIN) / 2.0;
        return L > 0.7;
    }
    static boolean reverse() {
        int r = (ThemeColor & 0xff0000) >> 16;
        int g = (ThemeColor & 0x00ff00) >> 8;
        int b = ThemeColor & 0x0000ff;
        double MIN, MAX, L;
        MAX = (double) max(r, max(g, b)) / 255.0;
        MIN = (double) min(r, min(g, b)) / 255.0;
        L = (MAX + MIN) / 2.0;
        return L > 0.7;
    }

    static void LoadSettings(String path) {
        db = SQLiteDatabase.openOrCreateDatabase(path + "/settings.db", null);
        String sql = "create table if not exists settings(AutoUpdate text , ThemeColor text, username text, password text, nickname text)";
        db.execSQL(sql);
        Cursor cursor = db.query("settings", new String[]{"AutoUpdate", "ThemeColor", "username", "password", "nickname"}, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            sql = "insert into settings values('true'," + Integer.toString(Color.parseColor("#FF4081")) + ",null,null,null)";
            db.execSQL(sql);
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
        else sql = "update settings set AutoUpdate='false'";
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


    private static String getFormatSize(double size) {
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

    private static long getFolderSize(File file) throws Exception {
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

    static String getCacheSize(File file) throws Exception {
        return getFormatSize(getFolderSize(file));
    }

    static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

}