package com.example.retouchephoto;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

class FileInputOutput {

    private static String lastTakenImagePath;

    static boolean saveImage(Bitmap bmp, Activity activity) {

        if (!askPermissionToReadWriteFiles(activity)) return false;

        try {

            /*
            File dir = new File(Settings.SAVE_PATH);
            if (!dir.exists()) {
                // If the directory cannot be created, aborts.
                if (!dir.mkdirs()) return false;
            }

            File file = new File(Settings.SAVE_PATH, createUniqueFileName(activity.getApplicationContext()) + ".jpg");
            if (!file.createNewFile()) return false;
            OutputStream fOut = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, Settings.OUTPUT_JPG_QUALITY, fOut);
            fOut.flush();
            fOut.close();

             */

            MediaStore.Images.Media.insertImage(activity.getContentResolver(), bmp, createUniqueFileName(activity.getApplicationContext()) , "");

        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e("saveToExternalStorage()", e.getMessage());
            } else {
                Log.e("saveToExternalStorage()", "Can't save");
            }
            return false;
        }
        return true;
    }

    private static String createUniqueFileName(Context context) {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", context.getResources().getConfiguration().getLocales().get(0)).format(new Date());
    }

    static Uri createUri(Activity activity) {

        if (askPermissionToReadWriteFiles(activity)) {
            File dir = new File(Settings.SAVE_PATH_ORIGINAL);
            if (!dir.exists()) {
                // If the file cannot be create aborts
                if (!dir.mkdirs()) return null;
            }

            lastTakenImagePath = Settings.SAVE_PATH_ORIGINAL + createUniqueFileName(activity.getApplicationContext()) + ".jpg";
            File file = new File(lastTakenImagePath);
            return FileProvider.getUriForFile(activity.getApplicationContext(), activity.getApplicationContext().getPackageName() + ".provider", file);
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    static Bitmap getBitmap(String fullPath) {

        File imgFile = new  File(fullPath);
        if(imgFile.exists()){
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            // Try to rotate the image according to EXIF info
            try {
                ExifInterface exif = new ExifInterface(imgFile.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                return FilterFunction.rotate(bmp, exifToDegrees(rotation));

            }catch(IOException ex){
                return bmp;
            }
        }
        return null;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    @SuppressWarnings("SameParameterValue")
    static Bitmap getBitmap(Resources resources, int index) {
        return BitmapFactory.decodeResource(resources, index);
    }

    static Bitmap getBitmap(Resources resources, int index, int width, int height) {
        Bitmap bmp = getBitmap(resources, index);
        return Bitmap.createScaledBitmap(bmp, width, height, true);
    }

    static Bitmap getLastTakenBitmap() {
        return getBitmap(lastTakenImagePath);
    }

    private static boolean askPermissionToReadWriteFiles(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        int checkVal = activity.getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (checkVal == PackageManager.PERMISSION_GRANTED);
    }

}
