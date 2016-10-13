package com.example.djurus.rainman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by djurus on 10/13/16.
 */

public class AlertDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context c = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.error_title).setMessage(R.string.error_message).setPositiveButton(R.string.error_buttontext,null);
        AlertDialog dialog= builder.create();
        return dialog;


    }
}