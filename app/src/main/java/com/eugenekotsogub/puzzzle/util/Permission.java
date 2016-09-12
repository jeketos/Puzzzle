package com.eugenekotsogub.puzzzle.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

public class Permission {

    public static final int REQUEST_CAMERA = 200;
    public static final int REQUEST_CAMERA_WRITE = 220;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 201;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 202;
    //        public static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 203;
    //        public static final int PERMISSION_REQUEST_GET_ACCOUNTS = 204;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 205;
    public static final int READ_EXTERNAL_STORAGE_FOR_SHARE = 206;

    public static final int GRANTED = 1;
    public static final int DENIED = 2;
    public static final int WAIT = 3;

    public static boolean isGranted(Fragment fragment, String permission, int permissionRequest) {
        return getGranted(fragment, permission, permissionRequest) == GRANTED;
    }

    public static boolean isGranted(Activity activity, int permissionRequest, String... permissions) {
        return getGranted(activity,permissionRequest, permissions) == GRANTED;
    }

    public static int getGranted(Activity activity, int permissionRequest, String... permissions) {
        if (activity != null) {
            ArrayList<String> permissionsNotGrantedList = new ArrayList<>();
            for(String permission : permissions){
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        return DENIED;
                    } else {
                        permissionsNotGrantedList.add(permission);
                    }
                }
            }
            if(permissionsNotGrantedList.size() > 0) {
                ActivityCompat.requestPermissions(activity,
                        permissionsNotGrantedList.toArray(new String[permissionsNotGrantedList.size()]), permissionRequest);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return WAIT;
            }
        }
        return GRANTED;
    }

    public static int getGranted(Fragment fragment, String permission, int permissionRequest) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    return DENIED;
                } else {
                    // No explanation needed, we can request the permission.
                    fragment.requestPermissions(new String[]{permission}, permissionRequest);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                    return WAIT;
                }
            }
        }
        return GRANTED;
    }

    public static boolean isGranted(int[] grantResults) {
        return  grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
