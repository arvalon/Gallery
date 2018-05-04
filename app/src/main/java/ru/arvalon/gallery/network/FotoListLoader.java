package ru.arvalon.gallery.network;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Resource;
import com.yandex.disk.rest.json.ResourceList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.arvalon.gallery.model.ListItem;

import static ru.arvalon.gallery.HostActivity.TAG;

public class FotoListLoader extends AsyncTaskLoader<List<ListItem>> {

    /** загружаются только ресурсы соответсвующего типа */
    private static final String IMAGE = "image";

    /**  */
    private RestClient client;

    /** сдвиг запроса*/
    private int offset;

    /** количество запрашиваемых ресурсов за 1 запрос к YandexDisk'у */
    private int item_per_request;

    public FotoListLoader(Context context,
                          Credentials credentials,
                          int offset,
                          int item_per_request) {

        super(context);

        Log.d(TAG,getClass().getSimpleName()+" constructor");

        client=RestClientUtil.getInstance(credentials);
        this.offset=offset;
        this.item_per_request=item_per_request;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG,getClass().getSimpleName()+" onStartLoading");
        forceLoad();
    }

    @Override
    public List<ListItem> loadInBackground() {

        Log.d(TAG,getClass().getSimpleName()+" loadInBackground...");

        ResourceList resourceList=null;

        try {
            resourceList = client.getFlatResourceList(new ResourcesArgs.Builder()
                    .setMediaType(IMAGE)
                    .setOffset(offset)
                    .setLimit(item_per_request)
                    .build());

        } catch (IOException | ServerIOException e) {
            e.printStackTrace();
        }

        List<ListItem> listItems = new ArrayList<>();
        for (Resource resource : resourceList.getItems()) {
            listItems.add(new ListItem(resource));
        }

        return listItems;
    }
}
