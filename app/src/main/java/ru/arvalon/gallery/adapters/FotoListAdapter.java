package ru.arvalon.gallery.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.CustomHeader;
import com.yandex.disk.rest.DownloadListener;
import com.yandex.disk.rest.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ru.arvalon.gallery.R;
import ru.arvalon.gallery.model.FotoViewHolder;
import ru.arvalon.gallery.model.ListItem;

import static ru.arvalon.gallery.authorization.LoginActivity.TAG;

/** адаптер для заполнения GridView с фотографиями во вкладке Фото */
public class FotoListAdapter extends ArrayAdapter<ListItem> {

    private Picasso picassoInstance;

    public FotoListAdapter(@NonNull Context context,
                           Credentials credentials,
                           @NonNull List<ListItem> objects) {

        super(context, R.layout.item_foto, objects);

        picassoInstance = new Picasso.Builder(getContext())
                .downloader(new YandexDownloader(credentials))
                .build();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ListItem item = getItem(position);

        if (convertView==null){

            convertView= LayoutInflater.from(getContext())
                    .inflate(R.layout.item_foto,parent,false);

            ImageView iv = convertView.findViewById(R.id.foto);

            FotoViewHolder holder = new FotoViewHolder(iv);

            convertView.setTag(holder);
        }

        FotoViewHolder holder = (FotoViewHolder)convertView.getTag();

        picassoInstance.load(item.getPreview())
                .placeholder(R.drawable.foto_placeholder_smart)
                .error(R.drawable.foto_load_error_smart)
                .fit()
                .centerCrop()
                .into(holder.iv);

        return convertView;
    }

    /** Кастомный загрузчик данных для Picasso, OkHttpClient с добавлением OAuth-токена. Метод
     * ownloadFile из Yandex SDK почему-то не скачивает файл по preview-ссылке. Наверное там
     * что-то с обработкой пути, используется внутренний путь на диск а для того что бы downloadFile
     * мог скачивать и из пути-preview downloadFile надо допиливать.
     * Не пришлось импортировать OkHttpClient, он есть в com.yandex.android:disk-restapi-sdk*/
    private static class YandexDownloader implements Downloader {

        private Credentials credentials;

        public YandexDownloader(Credentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public Response load(Uri uri, int networkPolicy) throws IOException {

            Headers.Builder headersBuilder =  new Headers.Builder();

            for (CustomHeader customHeader : credentials.getHeaders()) {
                headersBuilder.add(customHeader.getName(),customHeader.getValue());
            }

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(uri.toString())
                    .headers(headersBuilder.build())
                    .build();

            com.squareup.okhttp.Response response = client.newCall(request).execute();

            if (!response.isSuccessful()){
                Log.d(TAG,getClass().getSimpleName()+" load error, body "+response.message());
            }

            InputStream in = response.body().byteStream();

            return new Response(in,false,-1);
        }

        @Override
        public void shutdown() { }
    }
}