package ru.arvalon.gallery.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.arvalon.gallery.R;

import static ru.arvalon.gallery.authorization.LoginActivity.TAG;

/** Корневой фрагмент для вкладки "Файлы" для возможности замены накладываемых фрагментов.
 * Необходим для существования корневого контейнера в разметке */
public class FilesRootFragment extends Fragment {

    /** флаг созданности фрагмента */
    private static final String CREATED="hasCreated";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (savedInstanceState==null){

            Log.d(TAG,getClass().getSimpleName()+" onCreateView first run");

            View view = inflater.inflate(R.layout.fragment_files_root,container,false);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_files_root_container,new FilesListFragment());

            transaction.commit();

            return view;

        }else {
            Log.d(TAG,getClass().getSimpleName() +" onCreateView Bundle hasCreated");

            View view = inflater.inflate(R.layout.fragment_files_root,container,false);

            return view;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG,getClass().getSimpleName()+" onSaveInstanceState");

        outState.putInt(CREATED,1);
        super.onSaveInstanceState(outState);
    }
}