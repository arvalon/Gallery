package ru.arvalon.gallery.authorization;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ru.arvalon.gallery.R;

/** Диалог вызова перехода на получение oAuth токена */
public class AuthDialog extends DialogFragment {

    private static final String AUTHDIALOGTITLE = "Авторизация";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setTitle(AUTHDIALOGTITLE)
                .setMessage(R.string.auth_dialog_message)
                .setPositiveButton(R.string.auth_dialog_positive_button, (dialogInterface, i) ->
                {
                    dismiss();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LoginActivity.AUTH_URL)));
                })
                .setNegativeButton(R.string.auth_dialog_negative_button, (dialogInterface, i) ->
                {
                    dismiss();
                    getActivity().finish();
                });

        return builder.create();
    }
}