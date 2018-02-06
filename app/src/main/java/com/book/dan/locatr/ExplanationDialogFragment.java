package com.book.dan.locatr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class ExplanationDialogFragment extends DialogFragment {
    private iAccept mIAccept;

    public void setIAccept(iAccept item){
        mIAccept = item;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title)
                .setMessage(R.string.text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIAccept.accept();
                    }
                })
                .create();
    }

    public interface iAccept{
        public void accept();
    }
}
