package ru.arvalon.gallery.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.yandex.disk.rest.Credentials;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.arvalon.gallery.BuildConfig;
import ru.arvalon.gallery.HostActivity;
import ru.arvalon.gallery.R;
import ru.arvalon.gallery.adapters.FotoListAdapter;
import ru.arvalon.gallery.model.ListItem;
import ru.arvalon.gallery.network.FileLoader;
import ru.arvalon.gallery.network.FotoListLoader;

import static ru.arvalon.gallery.authorization.LoginActivity.TAG;

/**
 * Фрагмент с фото-лентой
 */

public class FotosFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks {

    private static final int FOTOLIST_LOADER = 0;
    private static final int FILEDOWNLOAD_LOADER = 1;

    private static final String ITEM_KEY = "item";
    private static final String PROVIDER = ".provider";

    /** порог кол-ва элементов, меньше которого начинается дозагрузка следующей партии фото */
    private static final int threshold = 8;

    /** Количество загружаемых за 1 запрос pewview фотографий */
    public static final int ITEMS_PER_REQUEST = 20;

    /** Токен Yandex-диска */
    private Credentials credentials;

    /** общая коллекция файлов фотографий */
    private List<ListItem> listItems = new ArrayList<>();

    private GridView gridView;
    private FotoListAdapter adapter;

    /** элемент по которому кликнули */
    private ListItem listItem;

    /** заглушка на время загрузки фотографий */
    private ProgressBar progressBar;

    /** Paging, перенесён во фрагмент */
    private int offset;

    public FotosFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Log.d(HostActivity.TAG,getClass().getSimpleName()+" onCreate");

        if (getActivity() instanceof HostActivity){
            try{
                credentials = ((HostActivity) getActivity()).getCredentials();

            }catch (ClassCastException ex){ Log.d(TAG,"Class cast ex",ex); }
        }

        adapter=new FotoListAdapter(getContext(),credentials,listItems);

        getLoaderManager().initLoader(FOTOLIST_LOADER,null,this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(HostActivity.TAG,getClass().getSimpleName()+" onCreateView");

        return inflater.inflate(R.layout.fragment_fotos, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(HostActivity.TAG,getClass().getSimpleName()+" onActivityCreated");

        gridView=getView().findViewById(R.id.grid_view);

        // https://stackoverflow.com/questions/39411738/collapsingtoolbarlayout-issue-with-gridview
        // надо было вообще с самого начала использовать RecuclerView и на этой вкладке как с
        // файлами но переезжать переделывать уже нет времени
        gridView.setNestedScrollingEnabled(true);

        gridView.setAdapter(adapter);

        progressBar=getView().findViewById(R.id.pb);

        // Загрузить новые картинки при прокрутке до конца списка
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState==SCROLL_STATE_IDLE){
                    if (gridView.getLastVisiblePosition() >= gridView.getCount()-threshold){

                        progressBar.setVisibility(View.VISIBLE);

                        getLoaderManager().initLoader(FOTOLIST_LOADER,
                                null,FotosFragment.this);

                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) { }
        });

        // клик по фото скачивает и открывает полноразмерное фото
        gridView.setOnItemClickListener((parent, view, position, id) -> {

            progressBar.setVisibility(View.VISIBLE);

            listItem=listItems.get(position);

            Bundle args = new Bundle();
            args.putParcelable(ITEM_KEY,listItem);
            getLoaderManager()
                    .restartLoader(FILEDOWNLOAD_LOADER,args,FotosFragment.this);
        });
    }

    /** Несколько кривой костыль для отслеживания и управления ProgressBar'ом в случаях
     * поворота экрана и воостановления приложения из фона. Проверка на null нужна для случая
     * возврата назад из отктыого файла т.к. лоадер уничтожается. Можно было бы перенести управние
     * ProgressBar'ом в Loader но прокидывать туда активность было бы ещё более сильным связывнием,
     * даже через интерфейс*/
    @Override
    public void onResume() {
        super.onResume();

        if (getLoaderManager().getLoader(FOTOLIST_LOADER)!=null &&
                getLoaderManager().getLoader(FOTOLIST_LOADER).isStarted()){
            progressBar.setVisibility(View.VISIBLE);
        }else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /** открыть скачанный файл для просмотра штатным средством имеющимся в ОС */
    private void openFile(File file){

        Log.d(HostActivity.TAG,getClass().getSimpleName()
                +" openFile "+file.getAbsolutePath()+" exists "+file.exists());

        Uri uri = FileProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID+PROVIDER,
                file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri,listItem.getContentType());

        startActivity(Intent.createChooser(intent,getString(R.string.file_chooser_title)));

        getLoaderManager().destroyLoader(FILEDOWNLOAD_LOADER);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {

        switch (id){
            case FOTOLIST_LOADER:
                return new FotoListLoader(
                        getContext(),credentials,offset,ITEMS_PER_REQUEST);

            case FILEDOWNLOAD_LOADER:
                ListItem listItem = args.getParcelable(ITEM_KEY);
                return new FileLoader(getContext(),credentials,listItem);
        }
        return null;
    }

    /** в зависимости от лоадера загрузка новой партии фотографий или открыте фото на простотр */
    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {

        progressBar.setVisibility(View.INVISIBLE);

        switch (loader.getId()){
            case FOTOLIST_LOADER:

                Log.d(HostActivity.TAG,getClass().getSimpleName()
                        +" onLoadFinished offset "+offset+" - "+(offset+ITEMS_PER_REQUEST));

                List<ListItem> newListItems = (List<ListItem>)data;

                if (newListItems!=null && newListItems.size()!=0){
                    adapter.addAll(newListItems);
                    offset+=ITEMS_PER_REQUEST;
                    getLoaderManager().destroyLoader(FOTOLIST_LOADER);
                    Toast.makeText(getContext(),R.string.fotos_loaded,Toast.LENGTH_SHORT).show();
                }
                break;

            case FILEDOWNLOAD_LOADER:
                File file = (File)data;
                openFile(file);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) { }
}