package ru.arvalon.gallery.authorization;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.arvalon.gallery.HostActivity;

/** стартовая активность приложения, запрашивает токен к YandexDisk'у.
 * На основе Java SDK for Yandex.Disk REST API */
public class LoginActivity extends AppCompatActivity {
    
    /** ID приложения Gallery на https://oauth.yandex.ru/ */
    public static final String CLIENT_ID = "687d4a6bb5674102a321d11ac1422adb";

    public static final String AUTH_URL =
            "https://oauth.yandex.ru/authorize?response_type=token&client_id="+CLIENT_ID;

    public static final String USERNAME = "username";
    public static final String TOKEN = "token";

    private static final String AUTHDIALOGTAG = "auth";

    private static final String ACCESS_TOKEN = "access_token=(.*?)(&|$)";

    public static final String TAG = "vga"; // для логов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,getClass().getSimpleName()+" onCreate");

        if (getIntent() != null && getIntent().getData() != null){
            onLogin();
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String token = sp.getString(TOKEN,null);
        if (token==null){
            startLogin();
            return;
        }

        if (savedInstanceState==null){
            openTabs();
        }
    }

    /** запускаем диалог получения токена */
    private void startLogin() {
        Log.d(TAG,getClass().getSimpleName()+" startLogin");
        new AuthDialog().show(getSupportFragmentManager(), AUTHDIALOGTAG);
    }

    /** смотрим в Intent Data Bundle на предмет токена и сохраняем в SP */
    private void onLogin() {
        Log.d(TAG,getClass().getSimpleName()+" startLogin, intent: "+getIntent().getData());
        Uri uri = getIntent().getData();
        setIntent(null);
        Pattern pattern = Pattern.compile(ACCESS_TOKEN);
        Matcher matcher = pattern.matcher(uri.toString());

        if (matcher.find()){
            final String token = matcher.group(1);
            if(!TextUtils.isEmpty(token)){
                saveToken(token);
            }else {
                Log.d(TAG,getClass().getSimpleName()+" empty token");
            }
        }else {
            Log.d(TAG,getClass().getSimpleName()+" token not found in uri");
        }
    }

    /** сохранить токен в SP */
    private void saveToken(String token) {
        Log.d(TAG,getClass().getSimpleName()+" saveToken");

        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();

        editor.putString(USERNAME,"");
        editor.putString(TOKEN,token);
        editor.apply();
    }

    /** токен есть, переходим ко вкладкам */
    private void openTabs() {
        startActivity(new Intent(this,HostActivity.class));
        finish();
    }
}
