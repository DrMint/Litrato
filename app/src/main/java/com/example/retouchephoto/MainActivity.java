package com.example.retouchephoto;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import static android.graphics.Bitmap.createBitmap;

/*TODO:
   Bugs:
        [0001] - When the histogram is resize, the image can get stretch because the imageView gets bigger or smaller.
        Refreshing the image doesn't seem to work. I suspect this is because requestLayout is asynchronous, and
        when the image refresh, it utilizes the imageView's aspect ratio before it actually changed.
        Thus, refreshing the image will actually make the problem worse.
        -------------------------------------------------------------------------------------------
    New functions:
        - When taking an image, we have to store it to get it at full resolution.
        - Rotation of the image (at first 90, -90, 180 then any degrees).
        - Crop an image, possibly merging the rotation and crop function UI wise.
        - Makes sure that all filter functions are using RenderScript.
        - An idea to keep the UI interactive while saving the image at high resolution would be to
          make all the modification on a smaller size image, save all the filter applied, and then
          apply them again to the full size image when saving. It is okay for the user to wait a few
          seconds when saving, but not while using a seekBar.
 */

/**
 * This apps is an image processing app for android.
 * It can load an image, apply some filter and, eventually, it will be able to save that image.
 * Please read the README file for more information.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */
public class MainActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private PackageManager pm;
    //private ImageViewZoomScroll cropView;

    /**
     * This is the image as it was before applying any filter.
     */
    private Bitmap originalImage;

    /**
     * This is the image as it was after the last apply button click.
     * This is the image filter are applied to.
     */
    private Bitmap beforeLastFilterImage;

    /**
     * This is the image with the current changes from the filter.
     * This is the image that is shown to the user.
     */
    private Bitmap filteredImage;

    /**
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;

    /**
     * A list of all filters. The order is the same as shown by the spinner.
     */
    private final List<Filter> filters = new ArrayList<>();

    private ImageViewZoomScroll myImageView;


    private boolean cropGoingOn = false;
    Point cropStart = new Point();
    Point cropEnd = new Point();

    String lastTakenImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        pm = getApplicationContext().getPackageManager();

        // Adds listener for the first seek bar
        final SeekBar seekBar = findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
                final TextView seekBarValue = findViewById(R.id.seekBarValue1);
                final Spinner sp = findViewById(R.id.spinner);
                Filter selectedFilter = filters.get(sp.getSelectedItemPosition());
                seekBarValue.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar1Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the second seek bar
        final SeekBar seekBar2 = findViewById(R.id.seekBar2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
                final TextView seekBarValue = findViewById(R.id.seekBarValue2);
                final Spinner sp = findViewById(R.id.spinner);
                Filter selectedFilter = filters.get(sp.getSelectedItemPosition());
                seekBarValue.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar2Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the color seek bar
        final SeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
        colorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the first switch
        final Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Spinner sp = findViewById(R.id.spinner);
                Filter selectedFilter = filters.get(sp.getSelectedItemPosition());
                if (switch1.isChecked()) {
                    switch1.setText(selectedFilter.switch1UnitTrue + "   ");
                } else {
                    switch1.setText(selectedFilter.switch1UnitFalse + "   ");
                }
                if (inputsReady) applyCorrectFilter();
            }
        });

        myImageView = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));
        myImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        // Selects the default image in the resource folder.
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        loadBitmap(mBitmap);

        // Create the GestureDetector which handles the scrolling and double tap.
        //TODO: Make sure that it is okay to use GestureDetector as it seems to be deprecated.
        final GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            @Override

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                myImageView.translate((int) (distanceX / myImageView.getZoom()), (int) (distanceY / myImageView.getZoom()));

                //TODO: Because of bug 0001, we are obligated to refresh the image when scrolling.
                myImageView.refresh();
                refreshImageView();
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // This will zoom *3 when double click, and revert to normal zoom when double clicked again;
                if (myImageView.getZoom() != 1f) {
                    myImageView.reset();
                } else {
                    Point touch = myImageView.imageViewTouchPointToBmpCoordinates(new Point(e.getX(), e.getY()));
                    myImageView.setZoom(Settings.DOUBLE_TAP_ZOOM);
                    myImageView.setCenter(touch);
                }
                refreshImageView();
                return super.onDoubleTap(e);
            }

        });

        // Create the ScaleGestureDetector which handles the scaling.
        final ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(MainActivity.this, new ScaleGestureDetector.OnScaleGestureListener() {
            float lastZoomFactor;

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastZoomFactor = myImageView.getZoom();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                myImageView.setZoom(lastZoomFactor * detector.getScaleFactor());
                refreshImageView();
                return false;
            }
        });

        // When the imageView is touched in any fashion, call both the ScaleGestureDetector and GestureDetector.
        findViewById(R.id.imageView).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cropGoingOn) {
                    final Bitmap mybmp = createBitmap(filteredImage);
                    myImageView.getiView().setImageBitmap(mybmp);
                    Canvas myCanvas = new Canvas(mybmp);

                    Paint paintFiller = new Paint();
                    paintFiller.setStyle(Paint.Style.FILL);
                    paintFiller.setARGB(Settings.CROP_OPACITY, 255,255,255);

                    Paint paintStroke = new Paint();
                    paintStroke.setStyle(Paint.Style.STROKE);
                    paintStroke.setStrokeWidth(Settings.CROP_BORDER_SIZE);
                    paintStroke.setARGB(Settings.CROP_OPACITY, 0,0,0);

                    myCanvas.drawRect(cropStart.x, cropStart.y, cropEnd.x, cropEnd.y, paintFiller);
                    myCanvas.drawRect(cropStart.x, cropStart.y, cropEnd.x, cropEnd.y, paintStroke);

                    //switch (action) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            cropStart.x = (int) event.getX();
                            cropStart.y = (int) event.getY();
                            cropStart = myImageView.imageViewTouchPointToBmpCoordinates(cropStart);
                            myImageView.sanitizeBmpCoordinates(cropStart);
                        }
                        case MotionEvent.ACTION_MOVE: {
                            cropEnd.x = (int) event.getX();
                            cropEnd.y = (int) event.getY();
                            cropEnd = myImageView.imageViewTouchPointToBmpCoordinates(cropEnd);
                            myImageView.sanitizeBmpCoordinates(cropEnd);
                        }
                        case MotionEvent.ACTION_UP: {
                            break;
                        }
                    }

                } else {
                    mScaleDetector.onTouchEvent(event);
                    mGestureDetector.onTouchEvent(event);
                }

                return true;
            }
        });

        // When the user clicks on the reset button, puts back the original image
        findViewById(R.id.originalButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final Spinner sp = findViewById(R.id.spinner);

                // If the button is displaying Load an image
                if (((Button) v).getText().toString() == getResources().getString(R.string.loadButtonString)) {

                    // Makes sure the phone has a camera module.
                    if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                        final CharSequence[] items = {"Take Photo", "Choose from Library"};
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Select a photo...");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {

                                if (items[item].equals("Take Photo")) {

                                    String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name) + "/Original/";

                                    File dir = new File(fullPath);
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }

                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                    lastTakenImagePath = fullPath + timeStamp + ".jpg";
                                    File file = new File(lastTakenImagePath);
                                    Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

                                } else if (items[item].equals("Choose from Library")) {
                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent,PICK_IMAGE_REQUEST);
                                }
                            }
                        });
                        builder.show();

                        // Else if the phone has no camera
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent,PICK_IMAGE_REQUEST);
                    }

                    // Else it is a Original button
                } else {
                    resetImage();

                    /* Puts the spinner back to the default position */
                    sp.setSelection(0);
                    ((Button) v).setText(getResources().getString(R.string.loadButtonString));
                }
            }
        });

        // When the user clicks on the histogram, makes it collapse or bring it back up.
        findViewById(R.id.histogram).setOnClickListener(new View.OnClickListener() {

            boolean collapsed = false;

            @Override
            public void onClick(View v) {

                int dimensionInDp;
                final TextView imageInfo = findViewById(R.id.imageInformation);

                if (collapsed) {
                    // Convert px to dp
                    dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
                    imageInfo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imageInfo.setVisibility(View.VISIBLE);
                } else {
                    // Convert px to dp
                    dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                    imageInfo.setVisibility(View.GONE);
                }

                collapsed = !collapsed;
                v.getLayoutParams().height = dimensionInDp;
                v.requestLayout();

                //TODO: Correct bug 0001
            }
        });



        // Creates the filters
        Filter newFilter = new Filter("Select a filter...");
        filters.add(newFilter);

        newFilter = new Filter("Brightness");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                if (seekBar <= 0) seekBar *= -seekBar;
                FilterFunction.brightness(bmp, context, seekBar * 2.55f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Saturation");
        newFilter.setSeekBar1(0, 100, 200, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.saturation(bmp, context,seekBar / 100f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Temperature");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.temperature(bmp, context, seekBar / 10f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Tint");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.tint(bmp, context, seekBar / 10f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sharpening");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.sharpen(bmp, context, seekBar / 200f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Colorize");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(0,100,100,"");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.colorize(bmp, context, colorSeekHue, seekBar / 100f, true);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Change hue");
        newFilter.setColorSeekBar();
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.colorize(bmp, context, colorSeekHue,0, false);
                return null;
            }
        });
        filters.add(newFilter);


        newFilter = new Filter("Hue shift");
        newFilter.setSeekBar1(-180, 0, 180, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.hueShift(bmp, context,seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Invert");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.invert(bmp, context);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Keep a color");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 25, 360, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.keepAColor(bmp, context, colorSeekHue,(int)seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Remove a color");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 25, 360, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.removeAColor(bmp, context, colorSeekHue,(int)seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Posterize");
        newFilter.setSeekBar1(2, 10, 32, "steps");
        newFilter.setSwitch1(true,"Color", "B&W");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.posterize(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Threshold");
        newFilter.setSeekBar1(0, 128, 256, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.threshold(bmp, context, seekBar / 256f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Add noise");
        newFilter.setSeekBar1(0, 0, 255, "");
        newFilter.setSwitch1(false,"B&W Noise", "Color Noise");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.noise(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Linear contrast stretching");
        newFilter.setSeekBar1(0, 0, 255, "");
        newFilter.setSeekBar2(0, 255, 255, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.toExtDyn(bmp, context,(int)(seekBar), (int)(seekBar2));
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Histogram equalization");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.histogramEqualization(bmp, context);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Average blur");
        newFilter.setSeekBar1(1, 2, 19, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.averageBlur(bmp, context, (int) seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Gaussian blur");
        newFilter.setSeekBar1(1, 2, 25, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.gaussianBlur(bmp, context, (int) seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Directional blur");
        newFilter.setSeekBar1(2, 2, 30, "");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.directionalBlur(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Laplacian");
        newFilter.setSeekBar1(1, 2, 14, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.laplacian(bmp, context, seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sobel");
        newFilter.setSeekBar1(1, 2, 14, "px");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.sobel(bmp, context, seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sketch");
        newFilter.setSeekBar1(1, 4, 14, "");
        newFilter.setSeekBar2(0, 20, 100, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                Bitmap texture = BitmapFactory.decodeResource(getResources(), R.drawable.canvas_texture);
                texture = Bitmap.createScaledBitmap(texture, bmp.getWidth(), bmp.getHeight(), true);
                FilterFunction.sketch(bmp, texture, context, (int) seekBar, seekBar2 / 100f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Cartoon");
        newFilter.setSeekBar1(1, 0, 100, "px");
        newFilter.setSeekBar2(2, 4, 14, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.cartoon(bmp, context, (int) seekBar, (int) seekBar2);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Mirror");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.mirror(bmp, context);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Rotation");
        newFilter.setSeekBar1(-180, 0, 180, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                return FilterFunction.rotate(bmp, seekBar);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Crop");
        newFilter.onlyApplyOnce = true;
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                if (!cropStart.isEquals(cropEnd)) {
                    return FilterFunction.crop(bmp, cropStart, cropEnd);
                }
                return null;
            }
        });
        filters.add(newFilter);

        // Adds all filter names in a array that will be used by the spinner
        String[] arraySpinner = new String[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            arraySpinner[i] = filters.get(i).getName();
        }

        final Spinner sp = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Handles when an item is selected in the spinner.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // This values is used to avoid applying filters while the seek bar are modified.
                // Changing the seek bar minimum, progress or maximum values would normally call the
                // seek bar listener, which would apply the filter 3 time for no reason.
                inputsReady = false;

                final SeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
                final SeekBar seekBar1 = findViewById(R.id.seekBar1);
                final SeekBar seekBar2 = findViewById(R.id.seekBar2);
                final Switch switch1 = findViewById(R.id.switch1);
                final TextView seekBarValue1 = findViewById(R.id.seekBarValue1);
                final TextView seekBarValue2 = findViewById(R.id.seekBarValue2);
                final Button applyButton = findViewById(R.id.applyButton);
                final Button originalButton = findViewById(R.id.originalButton);

                Filter selectedFilter = filters.get(position);

                if (selectedFilter.getName() == "Crop") {
                    cropGoingOn = true;
                    cropStart = new Point(0,0);
                    cropStart = new Point(0,0);
                }

                if (position != 0) {
                    applyButton.setText(getResources().getString(R.string.applyButtonString));
                    originalButton.setText(getResources().getString(R.string.originalButtonString));
                    applyButton.setEnabled(true);
                }

                if (selectedFilter.colorSeekBar) {
                    colorSeekBar.setVisibility(View.VISIBLE);
                } else {
                    colorSeekBar.setVisibility(View.GONE);
                }

                if (selectedFilter.seekBar1) {
                    seekBar1.setVisibility(View.VISIBLE);
                    seekBar1.setMin(selectedFilter.seekBar1Min);
                    seekBar1.setMax(selectedFilter.seekBar1Max);
                    seekBar1.setProgress(selectedFilter.seekBar1Set);
                } else {
                    seekBar1.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.seekBar2) {
                    seekBar2.setVisibility(View.VISIBLE);
                    seekBar2.setMin(selectedFilter.seekBar2Min);
                    seekBar2.setMax(selectedFilter.seekBar2Max);
                    seekBar2.setProgress(selectedFilter.seekBar2Set);
                } else {
                    seekBar2.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.switch1) {
                    switch1.setVisibility(View.VISIBLE);
                    switch1.setChecked(selectedFilter.switch1Default);
                    if (switch1.isChecked()) {
                        switch1.setText(selectedFilter.switch1UnitTrue);
                    } else {
                        switch1.setText(selectedFilter.switch1UnitFalse);
                    }


                    if (!selectedFilter.seekBar2) {
                        seekBar2.setVisibility(View.GONE);
                    }

                } else {
                    switch1.setVisibility(View.GONE);

                }

                // Only shows the seekBarValues when the seekBars are visible.
                seekBarValue1.setVisibility(seekBar1.getVisibility());
                seekBarValue2.setVisibility(seekBar2.getVisibility());
                seekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", seekBar1.getProgress(), selectedFilter.seekBar1Unit));
                seekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", seekBar2.getProgress(), selectedFilter.seekBar2Unit));

                // Finds the imageView and makes it display beforeLastFilterImage
                if (originalImage != null) {
                    final ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(beforeLastFilterImage);
                    applyCorrectFilter();
                }

                // The seek bars listener can be triggered again.
                inputsReady = true;

            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // When the user click on the apply button, apply the selected filter in the spinner
        findViewById(R.id.applyButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (originalImage != null) {
                    final Spinner sp = findViewById(R.id.spinner);

                    // If the spinner has no filter selected, it is a save button
                    if (sp.getSelectedItemPosition() == 0) {

                        saveImage(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name) + "/", filteredImage);
                        v.setEnabled(false);
                        Snackbar.make(v, getString(R.string.savingMessage), Snackbar.LENGTH_SHORT).show();

                    // Else it is an apply button
                    } else {
                        // Finds the imageView and makes it display original_image
                        applyCorrectFilter(true);
                        cropGoingOn = false;
                        beforeLastFilterImage = filteredImage.copy(filteredImage.getConfig(), true);

                        /* Put the spinner back to the default position */
                        sp.setSelection(0);
                        ((Button)v).setText(getResources().getString(R.string.saveButtonString));
                    }
                }
            }
        });
    }


    void saveImage(String fullPath, Bitmap bmp) {

        int MY_PERMISSIONS = 10;
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS);

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut;
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(fullPath, timeStamp + ".jpg");
            file.createNewFile();
            fOut = new FileOutputStream(file);

            bmp.compress(Bitmap.CompressFormat.JPEG, Settings.OUTPUT_JPG_QUALITY, fOut);
            fOut.flush();
            fOut.close();

            // This asks the MediaStore to add this image to the gallery. This seems to duplicate the file in the Picture folder.
            //MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
        }
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // After the user as selected an image, loads it and stores it in this.original_image
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Bitmap mBitmap;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                /* Puts the spinner back to the default position */
                final Spinner sp = findViewById(R.id.spinner);
                sp.setSelection(0);

                loadBitmap(mBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new  File(lastTakenImagePath);
            if(imgFile.exists()){
                Bitmap mBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                loadBitmap(mBitmap);
                //ImageView myImage = (ImageView) findViewById(R.id.lastTakenImagePath);
                //myImage.setImageBitmap(myBitmap);

            }
        }

        /*
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap mBitmap;
            if (extras != null) {
                mBitmap = (Bitmap) extras.get("data");
                if (mBitmap != null) {
                    //TODO: Right now, this method will only load a miniature of the image.
                    //      We have to ask the system to create a temporary file in order to store the full image.
                    loadBitmap(mBitmap);
                }
            }

        }

         */

    }

    /**
     * Function called when a new image is loaded by the program.
     * @param bmp the image to load
     */
    private void loadBitmap(Bitmap bmp) {
        final ImageView imgViewer = findViewById(R.id.imageView);
        imgViewer.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Limits the image size to MAXSIZE * MAXSIZE
        if (bmp.getHeight() > Settings.IMPORTED_BMP_SIZE || bmp.getWidth() > Settings.IMPORTED_BMP_SIZE) {
            if (bmp.getHeight() >  bmp.getWidth()) {
                bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() * Settings.IMPORTED_BMP_SIZE / bmp.getHeight() , Settings.IMPORTED_BMP_SIZE, true);
            } else {
                bmp = Bitmap.createScaledBitmap(bmp, Settings.IMPORTED_BMP_SIZE, bmp.getHeight() * Settings.IMPORTED_BMP_SIZE / bmp.getWidth(), true);
            }
            Snackbar sb = Snackbar.make(imgViewer, "Image resized to " + bmp.getWidth() + "px by " + bmp.getHeight() + "px", Snackbar.LENGTH_SHORT);
            sb.show();
        }

        this.originalImage = bmp;

        // reset the image which also refresh the imageViewer and histogram, and reset the zoom factor
        resetImage();

        refreshImageInfo();
    }


    private void refreshImageInfo() {
        final TextView imageInfo = findViewById(R.id.imageInformation);
        final String infoString = String.format(Locale.ENGLISH,"%s%d  |  %s%d", getResources().getString(R.string.width), filteredImage.getWidth(), getResources().getString(R.string.height), filteredImage.getHeight());
        imageInfo.setText(infoString);
    }

    /**
     * Applies whichever filter is selected in the spinner, with the appropriate parameters from the
     * seek bars and color bar. Refreshes the histogram and imageViewer after.
     * @param finalApply is this apply is not a preview but the final apply of the filter
     */
    private void applyCorrectFilter(boolean finalApply) {

        final Spinner sp = findViewById(R.id.spinner);

        // If the spinner has yet to be initialize, aborts.
        if (sp.getSelectedItemPosition() == -1) return;

        Filter selectedFilter = filters.get(sp.getSelectedItemPosition());
        if (selectedFilter.onlyApplyOnce && !finalApply) return;

        final SeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
        final SeekBar seekBar = findViewById(R.id.seekBar1);
        final SeekBar seekBar2 = findViewById(R.id.seekBar2);
        final Switch switch1 = findViewById(R.id.switch1);

        // Otherwise, applies the filter selected in the spinner.
        filteredImage = beforeLastFilterImage.copy(beforeLastFilterImage.getConfig(), true);
        Bitmap result = selectedFilter.apply(filteredImage, getApplicationContext(), colorSeekBar.getProgress(), seekBar.getProgress(), seekBar2.getProgress(), switch1.isChecked());
        if (result != null) {
            filteredImage = result;
        }

        // Refresh the image viewer and the histogram.
        refreshImageView();
        refreshHistogram();

    }

    /**
     * Applies whichever filter is selected in the spinner, with the appropriate parameters from the
     * seek bars and color bar. Refreshes the histogram and imageViewer after.
     */
    private void applyCorrectFilter() {
        applyCorrectFilter(false);
    }

    /**
     * Displays filteredImage on the imageView.
     */
    private void refreshImageView() {
        // If the image size has been modify between two refreshes, reset the display and change the values.
        if (filteredImage.getWidth() != myImageView.getBmpWidth() || filteredImage.getHeight() != myImageView.getBmpHeight()) {
            myImageView.setBmp(filteredImage.getWidth(), filteredImage.getHeight());
            myImageView.reset();
            refreshImageInfo();
        }
        Bitmap newBmp = createBitmap(filteredImage, myImageView.getX(), myImageView.getY(), myImageView.getNewWidth(), myImageView.getNewHeight());
        final ImageView imgViewer = findViewById(R.id.imageView);
        imgViewer.setImageBitmap(newBmp);
    }

    /**
     * Display the original image in "imageView" and refresh the histogram.
     */
    private void resetImage() {
        // Finds the imageView and makes it display original_image
        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(originalImage);
        myImageView.setBmp(originalImage.getWidth(), originalImage.getHeight());
        myImageView.reset();
        beforeLastFilterImage = originalImage.copy(originalImage.getConfig(), true);
        filteredImage = originalImage.copy(originalImage.getConfig(), true);

        // Get the pixels into a pixel array and refresh the histogram.
        refreshHistogram();
        refreshImageInfo();
    }


    /**
     *  Generates the histogram and displays it in "histogram"
     */
    private void refreshHistogram() {

        int[] pixels = new int[filteredImage.getWidth() * filteredImage.getHeight()];
        filteredImage.getPixels(pixels, 0, filteredImage.getWidth(), 0, 0, filteredImage.getWidth(), filteredImage.getHeight());

        int[] Rvalues = new int[256];
        int[] Gvalues = new int[256];
        int[] Bvalues = new int[256];

        // Stores how many occurrences of all color intensities, for each channel.
        for (int pixel : pixels) {
            Rvalues[(pixel >> 16) & 0x000000FF] += 1;
            Gvalues[(pixel >>8 ) & 0x000000FF] += 1;
            Bvalues[(pixel) & 0x000000FF] += 1;
        }

        int max = 0;

        // Finds the intensity (in all three channels) with the maximum number of occurrences.
        for (int i = 0; i < 256; i++) {
            max = Math.max(max, Rvalues[i]);
            max = Math.max(max, Gvalues[i]);
            max = Math.max(max, Bvalues[i]);
        }

        Bitmap hist = createBitmap(256, 200, Bitmap.Config.ARGB_8888);
        int histHeight = hist.getHeight() - 1;
        int histWidth = hist.getWidth();

        int[] histPixels = new int[hist.getHeight() * hist.getWidth()];

        // If the image is blank, return with a black histogram.
        if (max == 0) {
            for (int x = 0; x < histWidth; x++) {
                for (int y = 0; y < histHeight; y++) {
                    histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(0, 0, 0);
                }
            }

        } else {

            int colorR;
            int colorG;
            int colorB;

            for (int x = 0; x < histWidth; x++) {
                for (int y = 0; y < histHeight; y++) {

                    colorR = 0;
                    colorG = 0;
                    colorB = 0;

                    if (Math.sqrt(Rvalues[x] * histHeight / max) * 14 >= y) {colorR = 255;}
                    if (Math.sqrt(Gvalues[x] * histHeight / max) * 14 >= y) {colorG = 255;}
                    if (Math.sqrt(Bvalues[x] * histHeight / max) * 14 >= y) {colorB = 255;}

                    histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(colorR, colorG, colorB);

                }
            }
        }

        hist.setPixels(histPixels, 0, hist.getWidth(), 0, 0, hist.getWidth(), hist.getHeight());
        final ImageView histogram = findViewById(R.id.histogram);
        histogram.setImageBitmap(hist);

    }


    static Bitmap toSquare(Bitmap bmp, int newSize) {

        int currentWidth = bmp.getWidth();
        int currentHeight =  bmp.getHeight();

        int newWidth = currentWidth;
        int newHeight = currentHeight;

        int newX = 0;
        int newY = 0;

        if (currentWidth > currentHeight) {
            newWidth = currentHeight;
            newX = (currentWidth - currentHeight) / 2;
        } else {
            newHeight = currentWidth;
            newY = (currentHeight - currentWidth) / 2;
        }

        bmp = Bitmap.createBitmap(bmp, newX, newY, newWidth, newHeight);

        // L'image est maintenant un carrée

        return Bitmap.createScaledBitmap(bmp, newSize, newSize, true);

    }

}

