package ru.arvalon.gallery.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;

import java.io.File;
import java.io.IOException;

import ru.arvalon.gallery.model.ListItem;

import static ru.arvalon.gallery.HostActivity.TAG;

/** Загрузка файла из YandexDisk'а в Internal директорию приложения */
public class FileLoader extends AsyncTaskLoader<File> implements ProgressListener {

    private Credentials credentials;
    private ListItem item;

    boolean showTotalLenght;

    public FileLoader(@NonNull Context context, Credentials credentials, ListItem item) {
        super(context);
        this.credentials = credentials;
        this.item = item;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG,getClass().getSimpleName()+" onStartLoading");
        forceLoad();
    }

    @Nullable
    @Override
    public File loadInBackground() {
        Log.d(TAG,getClass().getSimpleName()+" loadInBackground...");

        File file = new File(getContext().getFilesDir(),new File(item.getPath()).getName());

        RestClient client = RestClientUtil.getInstance(credentials);

        try {
            client.downloadFile(item.getPath(),file,this);
        } catch (IOException | ServerException e) {
            e.printStackTrace();
        }

        return file;
    }

    @Override
    public void updateProgress(long loaded, long total) {

        if (!showTotalLenght){
            Log.d(TAG,getClass().getSimpleName()+" total "+total);
            showTotalLenght=true;
        }
    }

    @Override
    public boolean hasCancelled() {
        return false;
    }
}
