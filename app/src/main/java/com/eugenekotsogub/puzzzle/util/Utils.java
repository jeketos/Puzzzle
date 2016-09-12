package com.eugenekotsogub.puzzzle.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;

import com.eugenekotsogub.puzzzle.R;

/**
 * Created by eugene.kotsogub on 8/30/16.
 *
 */
public class Utils {

    public static ProgressDialog showProgress(
            Activity activity, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);

        if(cancelListener != null) dialog.setOnCancelListener(cancelListener);

        dialog.show();
        dialog.setContentView(R.layout.progress_dialog_item);

        return dialog;
    }

}
