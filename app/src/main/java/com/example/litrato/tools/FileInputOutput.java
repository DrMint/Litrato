package com.example.litrato.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.litrato.activities.tools.Settings;
import com.example.litrato.filters.FilterFunction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/** This class is managing the input and output files.
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since 2020-19-04
 */
public class FileInputOutput {

    private static String lastTakenImagePath;
    private static String getLastImportedImagePath;

    public static boolean saveImageToGallery(Bitmap bmp, Activity activity) {
        if (!askPermissionToReadWriteFiles(activity)) return false;
        MediaStore.Images.Media.insertImage(activity.getContentResolver(), bmp, createUniqueFileName(activity.getApplicationContext()) , "");
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean saveImageToSaveFolder(Bitmap bmp, Activity activity) {

        if (!askPermissionToReadWriteFiles(activity)) return false;

        try {

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

    public static Uri createUri(Activity activity) {

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

    public static Bitmap getBitmap(Resources resources, int index) {
        return BitmapFactory.decodeResource(resources, index);
    }

    public static Bitmap getBitmap(Resources resources, int index, int width, int height) {
        Bitmap bmp = getBitmap(resources, index);
        return Bitmap.createScaledBitmap(bmp, width, height, true);
    }

    @SuppressWarnings("WeakerAccess")
    public static Bitmap getBitmap(String fullPath) {
        if (fullPath.startsWith("/raw/")) fullPath = fullPath.replaceFirst("/raw/", "");
        getLastImportedImagePath = fullPath;
        return rotateImgAccordingToExif(fullPath);
    }

    public static Bitmap getBitmap(Uri uri, Context context) {
        String[] strings = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, uri, strings, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return getBitmap(result);
    }

    public static Bitmap getLastTakenBitmap() {
        return getBitmap(lastTakenImagePath);
    }

    public static String getLastImportedImagePath() {
        return getLastImportedImagePath;
    }

    public static boolean askPermissionToReadWriteFiles(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        int checkVal = activity.getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (checkVal == PackageManager.PERMISSION_GRANTED);
    }

    private static Bitmap rotateImgAccordingToExif(String fullPath) {
        File imgFile = new  File(fullPath);
        if(imgFile.exists()){
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            // Try to rotate the image according to EXIF info
            try {
                ExifInterface exif = new ExifInterface(imgFile.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                return FilterFunction.rotate(bmp, exifToDegrees(rotation));

            } catch(IOException ex){
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

    private static String createUniqueFileName(Context context) {
        //noinspection SpellCheckingInspection
        return new SimpleDateFormat("yyyyMMdd_HHmmss", context.getResources().getConfiguration().getLocales().get(0)).format(new Date());
    }

}
