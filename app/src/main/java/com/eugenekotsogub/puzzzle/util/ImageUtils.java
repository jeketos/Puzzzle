package com.eugenekotsogub.puzzzle.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by eugene.kotsogub on 4/18/16.
 * Utils for images operations (add image, rotate e/t/c)
 */
public class ImageUtils {
    private static final String TMP_DIR = "tmp";

    private static String generateFilename() {
        return new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(new Date()) + ".jpg";
    }

    @NonNull
    public static File getTempDirectory(Context context) {
        File externalDir = context.getExternalFilesDir(null);

        if(externalDir == null){
            throw new IllegalStateException("Media storage isn't available");
        }

        return new File(externalDir, TMP_DIR);
    }

    /**
     * get path to local photo
     * @param context - context
     * @param uri - photo uri
     * @return path to local photo on device
     */
    public static String getPath(Context context, Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String string = cursor.getString(column_index);
            cursor.close();
            return string;
        }
        return uri.getPath();
    }

    /**
     * @param selectedImageUri scale -> rotate -> copy from content provider to app local path
     * @return - return copied image path
     */
    public static String getImageFile(Context context, Uri selectedImageUri) {
        Bitmap bitmap = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(selectedImageUri);
            if (is != null) {
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap == null) {
            return null;
        }
        File destFile = createImageFile(context);
        String path = getPath(context, selectedImageUri);
        if(path != null) {
            bitmap = fixImageOrientation(path, bitmap);
        } else {
            bitmap = getScaledBitmap(bitmap,500,500);
        }
        try {
            FileOutputStream fOut = new FileOutputStream(destFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.close();
            return destFile.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File createImageFile(Context context){
        String pictureFileName = generateFilename();
        File dstDirectory = getTempDirectory(context);
        //noinspection ResultOfMethodCallIgnored
        dstDirectory.mkdirs();
        File file = new File(dstDirectory, pictureFileName);
        return file;
    }

    /**
     * @param fragment - result receiver
     * @return photoPath - path to the destination taken photo
     */
    @RequiresPermission(allOf = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public static String takePhoto(Fragment fragment, int cameraRequest) {
        String photoPath;
        File photoFile = createImageFile(fragment.getContext());
                photoPath = photoFile.getAbsolutePath();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                fragment.startActivityForResult(Intent.createChooser(intent, ""), cameraRequest);
        return photoPath;
    }

    @RequiresPermission(allOf = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    public static String takePhoto(Activity activity, int cameraRequest) {
        String photoPath;
        File photoFile = createImageFile(activity);
        photoPath = photoFile.getAbsolutePath();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photoFile));
        activity.startActivityForResult(Intent.createChooser(intent, ""), cameraRequest);
        return photoPath;
    }

    /**
     * @param fragment -result receiver
     * @param galleryRequest - gallery request code
     */
    //todo посмотреть, почему READ_EXTERNAL_STORAGE с 16 апи и как решить
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public static void getPhoto(Fragment fragment, int galleryRequest) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK);
            intent.setType("image/*");
            fragment.startActivityForResult(
                    Intent.createChooser(intent, ""),
                    galleryRequest);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public static void getPhoto(Activity activity, int galleryRequest) {
        Intent intent = new Intent(
                Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(
                Intent.createChooser(intent, ""),
                galleryRequest);

    }

    public static Bitmap fixImageOrientation(String photoPath, Bitmap b){
        ExifInterface exifInterface = null;
        Bitmap bitmap;
        try {
            Log.d("image__", "path to exif - " + photoPath);
            exifInterface = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        if (b == null){
            bitmap = BitmapFactory.decodeFile(photoPath,bmOptions);
        } else {
            bitmap = b;
        }
        Bitmap photoBitmap = bitmap;
        if(exifInterface != null) {
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Log.d("image__", "orientation - " + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    photoBitmap = rotateImage(photoBitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    photoBitmap = rotateImage(photoBitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    photoBitmap = rotateImage(photoBitmap, 270);
                    break;
            }
        }
        photoBitmap = getScaledBitmap(photoBitmap,1024,768);
        return photoBitmap;
    }

    public static Bitmap rotateImage(Bitmap bitmap, float angle) {
        if(bitmap == null) return null;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    public static Bitmap getScaledBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if(bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        double ratio = (double) height/width;
        if(width>maxWidth){
            width = maxWidth;
        }
        height = (int)(ratio*width);
        if(height >maxHeight){
            height = maxHeight;
            width = (int)(height/ratio);
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        bitmap = getCroppedBitmap(bitmap);

        return bitmap;
    }

    public static Bitmap getCroppedBitmap(Bitmap srcBmp){
        if(srcBmp == null) return null;

        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
}
