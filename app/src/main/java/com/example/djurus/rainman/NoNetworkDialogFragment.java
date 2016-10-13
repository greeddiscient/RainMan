package com.example.djurus.rainman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by djurus on 10/13/16.
 */

public class NoNetworkDialogFragment extends DialogFragment{
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Network unavailable").setMessage("Please check that you are connected to the Internet.").setPositiveButton("OK",null);
        AlertDialog dialog= builder.create();
        return dialog;

    }
}
