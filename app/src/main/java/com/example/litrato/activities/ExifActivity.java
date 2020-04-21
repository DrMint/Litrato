package com.example.litrato.activities;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.regex.Pattern;

import com.example.litrato.R;
import com.example.litrato.activities.ui.ColorTheme;
import com.example.litrato.tools.FileInputOutput;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * An activity to view the image EXIF data.
 * EXIF is a meta-data format used by a lot of image formats and even sounds files.
 * It contains most notably the camera model and manufacturer, the exposure, ISO, focal length...
 * Also the GPS coordinates where the image was taken.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class ExifActivity extends FragmentActivity implements OnMapReadyCallback {

    private ImageButton returnButton;

    private TextView exifTitle;

    private TextView cameraModel;
    private TextView cameraConstructor;
    private TextView timeDate;
    private TextView iso;
    private TextView fNumber;
    private TextView exposureTime;
    private TextView exposureBias;
    private TextView focalLength;
    private TextView exposureMode;
    private TextView exposureProgram;
    private TextView flash;
    private TextView colorMode;
    private TextView fileName;
    private TextView imageMpx;
    private TextView imageResolution;
    private TextView imageSize;

    private TextView latitude;
    private TextView longitude;
    private TextView altitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        exifTitle = findViewById(R.id.exifTitle);

        returnButton = findViewById(R.id.returnButton);
        cameraModel = findViewById(R.id.cameraModel);
        cameraConstructor = findViewById(R.id.cameraConstructor);
        timeDate = findViewById(R.id.timeDate);
        iso = findViewById(R.id.iso);
        fNumber = findViewById(R.id.fNumber);
        exposureTime = findViewById(R.id.exposureTime);
        exposureBias = findViewById(R.id.exposureBias);
        focalLength = findViewById(R.id.focalLenght);
        exposureMode = findViewById(R.id.exposureMode);
        exposureProgram = findViewById(R.id.exposureProgram);
        flash = findViewById(R.id.flash);
        colorMode = findViewById(R.id.colorMode);
        fileName = findViewById(R.id.fileName);
        imageMpx = findViewById(R.id.imageMpx);
        imageResolution = findViewById(R.id.imageResolution);
        imageSize = findViewById(R.id.imageSize);

        latitude = findViewById(R.id.latitude);
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
                Log.e("ExifActivity - Get Exif", Objects.requireNonNull(ex.getMessage()));
            }
        }



    }

    private void applyColorTheme() {
        ColorTheme.setColorTheme(getApplicationContext());
        ColorTheme.window(getApplicationContext(), getWindow());

        ColorTheme.textView(exifTitle);
        ColorTheme.textView(cameraModel);
        ColorTheme.textView(cameraConstructor);
        ColorTheme.textView(timeDate);
        ColorTheme.textView(iso);
        ColorTheme.textView(fNumber);
        ColorTheme.textView(exposureTime);
        ColorTheme.textView(exposureBias);
        ColorTheme.textView(focalLength);
        ColorTheme.textView(exposureMode);
        ColorTheme.textView(exposureProgram);
        ColorTheme.textView(flash);
        ColorTheme.textView(colorMode);
        ColorTheme.textView(fileName);
        ColorTheme.textView(imageMpx);
        ColorTheme.textView(imageResolution);
        ColorTheme.textView(imageSize);
        ColorTheme.textView(latitude);
        ColorTheme.textView(longitude);
        ColorTheme.textView(altitude);

        ColorTheme.icon(getApplicationContext(), returnButton, R.drawable.icon_goback);
    }



    @SuppressLint("SetTextI18n")
    private void refreshValues(ExifInterface exif, File imgFile) {
        cameraModel.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
        cameraConstructor.setText(exif.getAttribute(ExifInterface.TAG_MAKE));

        timeDate.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));

        //noinspection deprecation
        if (exif.hasAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)) {
            //noinspection deprecation
            iso.setText(exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS));
            fNumber.setText(MessageFormat.format("f/{0}", exif.getAttribute(ExifInterface.TAG_F_NUMBER)));
        }

        if (exif.hasAttribute(ExifInterface.TAG_FOCAL_LENGTH)) {
            String tmpFocalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String[] parts = Objects.requireNonNull(tmpFocalLength).split(Pattern.quote("/"));
            focalLength.setText(MessageFormat.format("{0}mm", Integer.parseInt(parts[0]) / Integer.parseInt(parts[1])));
        }


        if (exif.hasAttribute(ExifInterface.TAG_EXPOSURE_TIME)) {
            int tmpExposure = (int) (1 / Double.parseDouble(Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME))));
            exposureTime.setText(MessageFormat.format("1/{0}s", tmpExposure));

        }

        if (exif.hasAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE)) {
            String value = exif.getAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE);
            if (Objects.requireNonNull(value).startsWith("0")) {
                exposureBias.setText(R.string.EV_0);
            } else {
                exposureBias.setText(MessageFormat.format("{0} EV", value));
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
            if (Integer.parseInt(Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_COLOR_SPACE))) == ExifInterface.COLOR_SPACE_S_RGB) {
                colorMode.setText("RBG");
            } else {
                colorMode.setText("UNCALIBRATED");
            }

        }

        DecimalFormat df = new DecimalFormat("0.000000");

        if (exif.hasAttribute(ExifInterface.TAG_GPS_LATITUDE)) {
            double[] latLong;
            latLong = exif.getLatLong();
            latitude.setText(df.format(Objects.requireNonNull(latLong)[0]));
            longitude.setText(df.format(latLong[1]));

            if (exif.hasAttribute(ExifInterface.TAG_GPS_ALTITUDE)) {
                String tmpAltitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
                String[] parts = Objects.requireNonNull(tmpAltitude).split(Pattern.quote("/"));
                if (parts.length == 2) {
                    altitude.setText(MessageFormat.format("{0}m", Integer.parseInt(parts[0]) / Integer.parseInt(parts[1])));
                } else if (parts.length == 1) {
                    altitude.setText(MessageFormat.format("{0}m", Integer.parseInt(parts[0])));
                }
            }
        }


        df = new DecimalFormat("0.00");

        String tmpPath = imgFile.getPath();
        fileName.setText(tmpPath.substring(tmpPath.lastIndexOf("/") + 1));
        int imageWidth = Integer.parseInt(Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)));
        int imageHeight = Integer.parseInt(Objects.requireNonNull(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)));
        imageResolution.setText(MessageFormat.format("{0} x {1}", imageWidth, imageHeight));
        int mpx = (imageWidth * imageHeight) / 1000 / 1000;
        imageMpx.setText(MessageFormat.format("{0}MP", mpx));
        imageSize.setText(df.format(imgFile.length() / 1024.0 / 1024.0) + " MB");
    }


    /**
     * Generated by the GoogleMap app Template.
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

        File imgFile = new  File(FileInputOutput.getLastImportedImagePath());
        try {

            ExifInterface exif = new ExifInterface(imgFile.getPath());

            if (exif.hasAttribute(ExifInterface.TAG_GPS_LATITUDE)) {
                // Get the coordinates
                double[] latLong;
                latLong = exif.getLatLong();

                // If dark theme, applies dark theme for the map
                ColorTheme.googleMap(getApplicationContext(), googleMap);

                // Add a marker in Sydney and move the camera
                LatLng photoGPS = new LatLng(Objects.requireNonNull(latLong)[0], Objects.requireNonNull(latLong)[1]);
                googleMap.addMarker(new MarkerOptions().position(photoGPS).title("Photo location"));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(photoGPS, 8.0f));

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



