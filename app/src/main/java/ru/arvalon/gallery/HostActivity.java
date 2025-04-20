package ru.arvalon.gallery;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.yandex.disk.rest.Credentials;

import ru.arvalon.gallery.fragments.FilesRootFragment;
import ru.arvalon.gallery.fragments.FotosFragment;
import ru.arvalon.gallery.adapters.ViewPagerAdapter;

import static ru.arvalon.gallery.authorization.LoginActivity.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

/** основная активность со вкладками Фото и Файлы */
public class HostActivity extends AppCompatActivity {

    public static final String TAG = "vga";

    public TabLayout tabLayout;

    private ViewPager viewPager;

    public ViewPagerAdapter adapter;

    private Credentials credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,getClass().getSimpleName()+" onCreate");
        setContentView(R.layout.activity_host);

        // подготовка тулбара
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // подготовка ViewPager'а
        viewPager=findViewById(R.id.viewpager);
        setViewPager();

        // подготовка вкладок, связывание с ViewPager'ом
        tabLayout=findViewById(R.id.tabs);
        setTabs();

        getToken();
    }

    /** заполнение ViewPager'а фрагментами */
    private void setViewPager() {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new FotosFragment(),getString(R.string.fotos_fragment_title));
        adapter.addFragment(new FilesRootFragment(),getString(R.string.files_fragment_title));

        viewPager.setAdapter(adapter);

    }

    /** Заполнение шапок вкладок */
    private void setTabs() {
        tabLayout.setupWithViewPager(viewPager,true);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_foto);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_file);

    }

    /** обращение за токеном */
    private void getToken(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString(USERNAME, null);
        String token = preferences.getString(TOKEN, null);

        credentials = new Credentials(username,token);
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
