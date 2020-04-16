package com.example.retouchephoto;

import androidx.fragment.app.FragmentActivity;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;



public class ExifActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ImageButton returnButton;

    TextView exifTitle;

    TextView cameraModel;
    TextView cameraConstructor;
    TextView timeDate;
    TextView iso;
    TextView fNumber;
    TextView exposureTime;
    TextView exposureBias;
    TextView focalLenght;
    TextView exposureMode;
    TextView exposureProgram;
    TextView flash;
    TextView colorMode;
    TextView fileName;
    TextView imageMpx;
    TextView imageResolution;
    TextView imageSize;

    TextView lattitude;
    TextView longitude;
    TextView altitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        exifTitle = findViewById(R.id.exifTitle);

        returnButton = findViewById(R.id.returnButton);
        cameraModel = findViewById(R.id.cameraModel);
        cameraConstructor = findViewById(R.id.cameraConstructor);
        timeDate = findViewById(R.id.timeDate);
        iso = findViewById(R.id.iso);
        fNumber = findViewById(R.id.fNumber);
        exposureTime = findViewById(R.id.exposureTime);
        exposureBias = findViewById(R.id.exposureBias);
        focalLenght = findViewById(R.id.focalLenght);
        exposureMode = findViewById(R.id.exposureMode);
        exposureProgram = findViewById(R.id.exposureProgram);
        flash = findViewById(R.id.flash);
        colorMode = findViewById(R.id.colorMode);
        fileName = findViewById(R.id.fileName);
        imageMpx = findViewById(R.id.imageMpx);
        imageResolution = findViewById(R.id.imageResolution);
        imageSize = findViewById(R.id.imageSize);

        lattitude = findViewById(R.id.lattitude);
        longitude = findViewById(R.id.longitude);
        altitude = findViewById(R.id.altitude);

        initializeListener();
        applyColorTheme();


        File imgFile = new  File(FileInputOutput.getLastImportedImagePath());
        if(imgFile.exists()){

            // Try to rotate the image according to EXIF info
            try {

                ExifInterface exif = new ExifInterface(imgFile.getPath());
                refreshValues(exif, imgFile);

            } catch(IOException ex){

            }
        }



    }

    private void applyColorTheme() {

        Settings.setColorTheme(MainActivity.preferences.getBoolean(Settings.PREFERENCE_DARK_MODE, Settings.DEFAULT_DARK_MODE));

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Settings.COLOR_BACKGROUND);
        window.getDecorView().setBackgroundColor(Settings.COLOR_BACKGROUND);


        exifTitle.setTextColor(Settings.COLOR_TEXT);
        cameraModel.setTextColor(Settings.COLOR_TEXT);
        cameraConstructor.setTextColor(Settings.COLOR_TEXT);
        timeDate.setTextColor(Settings.COLOR_TEXT);
        iso.setTextColor(Settings.COLOR_TEXT);
        fNumber.setTextColor(Settings.COLOR_TEXT);
        exposureTime.setTextColor(Settings.COLOR_TEXT);
        exposureBias.setTextColor(Settings.COLOR_TEXT);
        focalLenght.setTextColor(Settings.COLOR_TEXT);
        exposureMode.setTextColor(Settings.COLOR_TEXT);
        exposureProgram.setTextColor(Settings.COLOR_TEXT);
        flash.setTextColor(Settings.COLOR_TEXT);
        colorMode.setTextColor(Settings.COLOR_TEXT);
        fileName.setTextColor(Settings.COLOR_TEXT);
        imageMpx.setTextColor(Settings.COLOR_TEXT);
        imageResolution.setTextColor(Settings.COLOR_TEXT);
        imageSize.setTextColor(Settings.COLOR_TEXT);

        lattitude.setTextColor(Settings.COLOR_TEXT);
        longitude.setTextColor(Settings.COLOR_TEXT);
        altitude.setTextColor(Settings.COLOR_TEXT);

        returnButton.setImageDrawable(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.goback));

    }



    void refreshValues(ExifInterface exif, File imgFile) {
        cameraModel.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
        cameraConstructor.setText(exif.getAttribute(ExifInterface.TAG_MAKE));

        timeDate.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));

        if (exif.hasAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)) {
            iso.setText(exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS));
            fNumber.setText("f/" + exif.getAttribute(ExifInterface.TAG_F_NUMBER));
        }

        if (exif.hasAttribute(ExifInterface.TAG_FOCAL_LENGTH)) {
            String tmpFocalLenght = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String[] parts = tmpFocalLenght.split(Pattern.quote("/"));
            focalLenght.setText(Integer.parseInt(parts[0]) / Integer.parseInt(parts[1]) + " mm");
        }


        if (exif.hasAttribute(ExifInterface.TAG_EXPOSURE_TIME)) {
            int tmpExposure = (int) (1 / Double.parseDouble(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)));
            exposureTime.setText("1/" + tmpExposure + " s");

        }

        if (exif.hasAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE)) {
            String value = exif.getAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE);
            if (value.startsWith("0")) {
                exposureBias.setText("0 EV");
            } else {
                exposureBias.setText(value + " EV");
            }

        } else {
            exposureBias.setText("");
        }

        if (exif.hasAttribute(ExifInterface.TAG_EXPOSURE_MODE)) {
            short tmpExposureMode = Short.parseShort(exif.getAttribute(ExifInterface.TAG_EXPOSURE_MODE));
            if (tmpExposureMode == ExifInterface.EXPOSURE_MODE_AUTO) {
                exposureMode.setText("AUTO");
            } else if (tmpExposureMode == ExifInterface.EXPOSURE_MODE_MANUAL) {
                exposureMode.setText("MANUEL");
            } else if (tmpExposureMode == ExifInterface.EXPOSURE_MODE_AUTO_BRACKET) {
                exposureMode.setText("AUTO BRACKET");
            }
        }

        if (exif.hasAttribute(ExifInterface.TAG_EXPOSURE_MODE)) {
            short tmpExposureProgram = Short.parseShort(exif.getAttribute(ExifInterface.TAG_EXPOSURE_MODE));
            if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_NOT_DEFINED) {
                exposureProgram.setText("?");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_MANUAL) {
                exposureProgram.setText("M");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_NORMAL) {
                exposureProgram.setText("N");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_APERTURE_PRIORITY) {
                exposureProgram.setText("Av");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_SHUTTER_PRIORITY) {
                exposureProgram.setText("Tv");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_CREATIVE) {
                exposureProgram.setText("Creative");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_ACTION) {
                exposureProgram.setText("Action");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_PORTRAIT_MODE) {
                exposureProgram.setText("Portrait");
            } else if (tmpExposureProgram == ExifInterface.EXPOSURE_PROGRAM_LANDSCAPE_MODE) {
                exposureProgram.setText("Landscape");
            }
        }

        if (exif.hasAttribute(ExifInterface.TAG_FLASH)) {
            short tmpFlash = Short.parseShort(exif.getAttribute(ExifInterface.TAG_FLASH));
            Log.wtf("Test", "" + tmpFlash);
            if (tmpFlash == ExifInterface.FLAG_FLASH_FIRED) {
                flash.setText("Flash");
            } else if (tmpFlash == ExifInterface.FLAG_FLASH_MODE_AUTO) {
                flash.setText("Flash Auto");
            } else if (tmpFlash == ExifInterface.FLAG_FLASH_MODE_COMPULSORY_FIRING) {
                flash.setText("Flash A");
            } else if (tmpFlash == ExifInterface.FLAG_FLASH_MODE_COMPULSORY_SUPPRESSION) {
                flash.setText("No Flash");
            } else if (tmpFlash == ExifInterface.FLAG_FLASH_NO_FLASH_FUNCTION) {
                flash.setText("Flash C");
            } else if (tmpFlash == ExifInterface.FLAG_FLASH_RETURN_LIGHT_DETECTED) {
                flash.setText("Flash D");
            } else if (tmpFlash == ExifInterface.FLAG_FLASH_RETURN_LIGHT_NOT_DETECTED) {
                flash.setText("Flash E");
            } else {
                flash.setText("Flash");
            }
        }


        if (exif.hasAttribute(ExifInterface.TAG_COLOR_SPACE)) {
            if (Integer.parseInt(exif.getAttribute(ExifInterface.TAG_COLOR_SPACE)) == ExifInterface.COLOR_SPACE_S_RGB) {
                colorMode.setText("RBG");
            } else {
                colorMode.setText("UNCALIBRATED");
            }

        }

        DecimalFormat df = new DecimalFormat("0.000000");

        if (exif.hasAttribute(ExifInterface.TAG_GPS_LATITUDE)) {
            double[] latLong;
            latLong = exif.getLatLong();
            lattitude.setText(df.format(latLong[0]));
            longitude.setText(df.format(latLong[1]));

            if (exif.hasAttribute(ExifInterface.TAG_GPS_ALTITUDE)) {
                String tmpAltitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
                String[] parts = tmpAltitude.split(Pattern.quote("/"));
                if (parts.length == 2) {
                    altitude.setText(Integer.parseInt(parts[0]) / Integer.parseInt(parts[1]) + " m");
                } else if (parts.length == 1) {
                    altitude.setText(Integer.parseInt(parts[0]) + " m");
                }
            }
        }


        df = new DecimalFormat("0.00");

        String tmpPath = imgFile.getPath();
        fileName.setText(tmpPath.substring(tmpPath.lastIndexOf("/") + 1));
        int imageWidth = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
        int imageHeight = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
        imageResolution.setText(imageWidth + " x " + imageHeight);
        int mpx = (imageWidth * imageHeight) / 1000 / 1000;
        imageMpx.setText(mpx + "MP");
        imageSize.setText(df.format(imgFile.length() / 1024.0 / 1024.0) + " MB");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        File imgFile = new  File(FileInputOutput.getLastImportedImagePath());
        try {

            ExifInterface exif = new ExifInterface(imgFile.getPath());

            if (exif.hasAttribute(ExifInterface.TAG_GPS_LATITUDE)) {
                // Get the coordinates
                double[] latLong;
                latLong = exif.getLatLong();

                // If dark theme, applies dark theme for the map
                if (MainActivity.preferences.getBoolean(Settings.PREFERENCE_DARK_MODE, Settings.DEFAULT_DARK_MODE)) {
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.style_gmap_night));
                }

                // Add a marker in Sydney and move the camera
                LatLng photoGPS = new LatLng(latLong[0], latLong[1]);
                mMap.addMarker(new MarkerOptions().position(photoGPS).title("Photo location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(photoGPS, 8.0f));

            } else {
                findViewById(R.id.map).setVisibility(View.GONE);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    private void initializeListener() {
        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        returnButton.performClick();
    }


}



