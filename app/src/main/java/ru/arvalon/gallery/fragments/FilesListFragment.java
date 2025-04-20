package ru.arvalon.gallery.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yandex.disk.rest.Credentials;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.arvalon.gallery.BuildConfig;
import ru.arvalon.gallery.HostActivity;
import ru.arvalon.gallery.R;
import ru.arvalon.gallery.adapters.FilesAdapter;
import ru.arvalon.gallery.authorization.LoginActivity;
import ru.arvalon.gallery.model.ListItem;
import ru.arvalon.gallery.network.FileListLoader;
import ru.arvalon.gallery.network.FileLoader;
import ru.arvalon.gallery.uitools.RecyclerTouchListener;

import static ru.arvalon.gallery.HostActivity.TAG;

/**
 * Фрагмент со списком файлов.
 */

public class FilesListFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks {

    private static final String DIR_KEY = "key";
    private static final String ITEM_KEY = "item";
    private static final String ROOT = "/";
    private static final String PROVIDER = ".provider";

    private static final int FILELIST_LOADER = 0;
    private static final int FILEDOWNLOAD_LOADER = 1;

    /** Токен Yandex-диска */
    Credentials credentials;

    /** текущая открытая директория */
    private String currentDir;

    /** общая коллекция файлов фотографий */
    private List<ListItem> listItems = new ArrayList<>();

    /** элемент по которому кликнули */
    private ListItem listItem;

    /** заглушка на время загрузки фотографий */
    private ProgressBar progressBar;

    /** адаптер списка файлов */
    private FilesAdapter adapter;

    /** список файлов */
    RecyclerView recyclerView;

    /** Текстовая заглушка отображаемая в случае перехода в пустую директорию */
    private TextView emptyView;

    public FilesListFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,getClass().getSimpleName()+" onCreate");
        setRetainInstance(true);

        if (getActivity() instanceof HostActivity){
            try{
                credentials = ((HostActivity) getActivity()).getCredentials();
            }catch (ClassCastException ex){
                Log.d(LoginActivity.TAG,"Class cast ex",ex);
            }
        }

        // достали папку, которую нужно открыть
        Bundle args = getArguments();

        if (args!=null){ currentDir=args.getString(DIR_KEY); }

        if (currentDir==null){ currentDir=ROOT; }

        adapter = new FilesAdapter(getContext(),listItems);

        getLoaderManager().initLoader(FILELIST_LOADER,null,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,getClass().getSimpleName()+" onCreateView");
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG,getClass().getSimpleName()+" onActivityCreated open dir "+currentDir);

        progressBar=getView().findViewById(R.id.pb);

        emptyView=getView().findViewById(R.id.tv_empty);

        recyclerView = getView().findViewById(R.id.files_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.addItemDecoration(
                new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL));

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                recyclerView,
                (view, position) -> { clickOnItem(view); })
        );

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        getLoaderManager().destroyLoader(FILELIST_LOADER);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,getClass().getSimpleName()+" onResume");

        if (getLoaderManager().getLoader(FILELIST_LOADER)!=null &&
                getLoaderManager().getLoader(FILELIST_LOADER).isStarted()){
            progressBar.setVisibility(View.VISIBLE);
        }else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /** клик по элементу списка */
    private void clickOnItem(View view) {

        //Log.d(TAG,getClass().getSimpleName()+" clickOnItem показали бублик");
        progressBar.setVisibility(View.VISIBLE);

        listItem = (ListItem)view.getTag();

        if (listItem.isDir()){
            changeDir(listItem.getPath());
        }else {
            Bundle args = new Bundle();
            args.putParcelable(ITEM_KEY,listItem);
            getLoaderManager().restartLoader(FILEDOWNLOAD_LOADER,args,this);
        }
    }

    /** навигация по папкам, переход в новую папку */
    private void changeDir(String path) {
        Log.d(TAG,"Open dir "+path);

        Bundle bundle = new Bundle();
        bundle.putString(DIR_KEY,path);

        FilesListFragment fragment = new FilesListFragment();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_files_root_container,fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);

        transaction.commit();

        getLoaderManager().destroyLoader(FILELIST_LOADER);
    }

    /** показать заглушку в случае пустой папки */
    private void showEmptyStub(){
        if (listItems.size()==0){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void openFile(File file){

        progressBar.setVisibility(View.INVISIBLE);

        Uri uri = FileProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID+PROVIDER,
                file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri,listItem.getContentType());

        startActivity(Intent.createChooser(intent,getString(R.string.file_chooser_title)));

        getLoaderManager().destroyLoader(FILELIST_LOADER);
        getLoaderManager().destroyLoader(FILEDOWNLOAD_LOADER);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {

        switch (id){

            case FILELIST_LOADER:
                Log.d(TAG,getClass().getSimpleName()+" onCreateLoader filelist");
                return new FileListLoader(getActivity(),credentials,currentDir);

            case FILEDOWNLOAD_LOADER:
                Log.d(TAG,getClass().getSimpleName()+" onCreateLoader fileload");
                ListItem listItem = args.getParcelable(ITEM_KEY);
                return new FileLoader(getActivity(),credentials,listItem);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {

        progressBar.setVisibility(View.INVISIBLE);

        switch (loader.getId()){
            case FILELIST_LOADER:
                List<ListItem> newListItems = (List<ListItem>)data;
                Log.d(TAG,getClass().getSimpleName()
                        +" onLoadFinished filelist loader item count "+newListItems.size());

                adapter.setData(newListItems);
                showEmptyStub();

                break;

            case FILEDOWNLOAD_LOADER:
                Log.d(TAG,getClass().getSimpleName()+" onLoadFinished fileloader");
                File file = (File)data;
                openFile(file);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.d(TAG,getClass().getSimpleName()+" "
                +loader.getClass().getSimpleName()+" onLoaderReset");
    }
}
