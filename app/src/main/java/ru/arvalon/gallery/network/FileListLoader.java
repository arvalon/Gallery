/*
 * (C) 2015 Yandex LLC (https://yandex.com/)
 *
 * The source code of Java SDK for Yandex.Disk REST API
 * is available to use under terms of Apache License,
 * Version 2.0. See the file LICENSE for the details.
 */
package ru.arvalon.gallery.network;

import android.content.Context;
import android.util.Log;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.ResourcesHandler;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Resource;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.arvalon.gallery.model.ListItem;

import static ru.arvalon.gallery.HostActivity.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

/** загрузчик списка файлов данной папки, без порционного возврата данных. Из оригинального
 * загрузчика так же взята сортировка */
public class FileListLoader extends AsyncTaskLoader<List<ListItem>> {

    private String dir;

    private RestClient client;

    private List<ListItem> fileItemList=new ArrayList<>();

    private static final int ITEMS_PER_REQUEST = 20;

    private static Collator collator = Collator.getInstance();

    static {
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
    }

    private final Comparator<ListItem> FILE_ITEM_COMPARATOR = (f1, f2) -> {
        if (f1.isDir() && !f2.isDir()) {
            return -1;
        } else if (f2.isDir() && !f1.isDir()) {
            return 1;
        } else {
            return collator.compare(f1.getName(), f2.getName());
        }
    };

    public FileListLoader(@NonNull Context context, Credentials credentials, String dir) {
        super(context);
        Log.d(TAG,getClass().getSimpleName()+" constructor");
        this.dir = dir;
        client=RestClientUtil.getInstance(credentials);
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG,getClass().getSimpleName()+" onStartLoading");
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public List<ListItem> loadInBackground() {
        Log.d(TAG,getClass().getSimpleName()+" loadInBackground...");
        int offset = 0; // сдвиг-страница
        int lastitemcount=0; // количество вернувшихся элементов в последнем запросе

        try {

            do {

                Resource resource = client.getResources(new ResourcesArgs.Builder()
                        .setPath(dir)
                        .setSort(ResourcesArgs.Sort.name)
                        .setOffset(offset)
                        .setLimit(ITEMS_PER_REQUEST).setParsingHandler(new ResourcesHandler() {
                            @Override
                            public void handleItem(Resource item) {
                                fileItemList.add(new ListItem(item));
                            }
                        }).build());

                lastitemcount=resource.getResourceList().getItems().size();

                offset+=ITEMS_PER_REQUEST;

            }while (lastitemcount>=ITEMS_PER_REQUEST);

        } catch (IOException | ServerIOException e) {
            e.printStackTrace();
        }

        Collections.sort(fileItemList,FILE_ITEM_COMPARATOR);

        return fileItemList;
    }
}
