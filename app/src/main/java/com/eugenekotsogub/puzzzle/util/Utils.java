package com.eugenekotsogub.puzzzle.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

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

    public static void createViewRotateAnimation(int startDegrees, int stopDegrees, int time, @NonNull View viewToAnimate) {
        RotateAnimation rotateAnimation = new RotateAnimation(startDegrees,stopDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(time);
        viewToAnimate.startAnimation(rotateAnimation);
    }

    public static void setBackground(View view, Bitmap bitmap) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(view.getResources(), bitmap);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            //noinspection deprecation
            view.setBackgroundDrawable(bitmapDrawable);
        } else {
            view.setBackground(bitmapDrawable);
        }
    }

}
