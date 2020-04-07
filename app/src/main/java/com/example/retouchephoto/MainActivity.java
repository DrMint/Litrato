package com.example.retouchephoto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;

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

import java.io.IOException;
import java.util.ArrayList;
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
        [0002] - There is a bug when not applying a filter and directly going to crop.
        -------------------------------------------------------------------------------------------
    New functions:
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

    /**
     * This is the image as it was before applying any filter.
     * The image has been resized if necessary.
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

    private boolean cropGoingOn = false;
    private Point cropStart = new Point();
    private Point cropEnd = new Point();

    private ImageViewZoomScroll layoutImageView;
    private Button      layoutButtonApply;
    private Button      layoutButtonOriginal;
    private ImageView   layoutHistogram;
    private TextView    layoutImageInfo;
    private SeekBar     layoutSeekBar1;
    private SeekBar     layoutSeekBar2;
    private SeekBar     layoutColorSeekBar;
    private TextView    layoutSeekBarValue1;
    private TextView    layoutSeekBarValue2;
    private Switch      layoutSwitch1;
    private Spinner     layoutSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));
        layoutButtonApply       = findViewById(R.id.applyButton);
        layoutButtonOriginal    = findViewById(R.id.originalButton);
        layoutHistogram         = findViewById(R.id.histogram);
        layoutImageInfo         = findViewById(R.id.imageInformation);
        layoutSeekBar1          = findViewById(R.id.seekBar1);
        layoutSeekBar2          = findViewById(R.id.seekBar2);
        layoutColorSeekBar      = findViewById(R.id.colorSeekBar);
        layoutSeekBarValue1     = findViewById(R.id.seekBarValue1);
        layoutSeekBarValue2     = findViewById(R.id.seekBarValue2);
        layoutSwitch1           = findViewById(R.id.switch1);
        layoutSpinner           = findViewById(R.id.spinner);

        // Selects the default image in the resource folder and set it
        setBitmap(FileInputOutput.getBitmap(getResources(), R.drawable.default_image));
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        // Create the list of filters
        generateFilters();

        // Initialize all the different listeners.
        initializeListener();

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
                layoutSpinner.setSelection(0);

                setBitmap(mBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Load the last taken photo.
            setBitmap(FileInputOutput.getLastTakenBitmap());
        }
    }

    /**
     * Function called when a new image is loaded by the program.
     * @param bmp the image to load
     */
    private void setBitmap(Bitmap bmp) {

        // If the bmp is null, aborts
        if (bmp == null) return;

        // Resize the image before continuing, if necessary
        if (bmp.getHeight() > Settings.IMPORTED_BMP_SIZE || bmp.getWidth() > Settings.IMPORTED_BMP_SIZE) {
            bmp = ImageTools.resizeAsContainInARectangle(bmp, Settings.IMPORTED_BMP_SIZE);
            Snackbar sb = Snackbar.make(
                    layoutSpinner,
                    "Image resized to " + bmp.getWidth() + "px by " + bmp.getHeight() + "px",
                    Snackbar.LENGTH_SHORT);
            sb.show();
        }

        // Set this image as the originalImage and reset the UI
        originalImage = bmp;
        resetImage();
    }

    /**
     * Applies whichever filter is selected in the spinner, with the appropriate parameters from the
     * seek bars and color bar. Refreshes the histogram and imageViewer after.
     * @param finalApply is this apply is not a preview but the final apply of the filter
     */
    private void applyCorrectFilter(boolean finalApply) {

        // If the spinner has yet to be initialize, aborts.
        if (layoutSpinner.getSelectedItemPosition() == -1) return;

        Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
        if (selectedFilter.onlyApplyOnce && !finalApply) return;

        // Otherwise, applies the filter selected in the spinner.
        filteredImage = beforeLastFilterImage.copy(beforeLastFilterImage.getConfig(), true);

        Bitmap result = selectedFilter.apply(
                filteredImage,
                getApplicationContext(),
                layoutColorSeekBar.getProgress(),
                layoutSeekBar1.getProgress(),
                layoutSeekBar2.getProgress(),
                layoutSwitch1.isChecked());

        if (result != null) {
            filteredImage = result;
        }

        // Refresh the image viewer and the histogram.
        refreshImageView();
    }

    private void applyCorrectFilter() {
        applyCorrectFilter(false);
    }

    /**
     * Displays filteredImage on the imageView, also refreshes Histogram and ImageInfo
     */
    private void refreshImageView() {
        layoutImageView.setImageBitmap(filteredImage);
        refreshHistogram();
        refreshImageInfo();
    }

    /**
     * Display the original image in "imageView" and refresh the histogram.
     */
    private void resetImage() {
        beforeLastFilterImage = originalImage.copy(originalImage.getConfig(), true);
        filteredImage = originalImage.copy(originalImage.getConfig(), true);
        refreshImageView();
    }

    /**
     * Display the histogram of filteredImage on layoutHistogram
     */
    private void refreshHistogram() {
        layoutHistogram.setImageBitmap(ImageTools.generateHistogram(filteredImage));
    }

    private void refreshImageInfo() {

        final String infoString = String.format(
                Locale.ENGLISH,"%s%d  |  %s%d",
                getResources().getString(R.string.width),
                filteredImage.getWidth(),
                getResources().getString(R.string.height),
                filteredImage.getHeight());

        layoutImageInfo.setText(infoString);
    }



    private void initializeListener() {
        // Adds listener for the first seek bar
        layoutSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
                Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
                layoutSeekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar1Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the second seek bar
        layoutSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
                Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
                layoutSeekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar2Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the color seek bar
        layoutColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the first switch
        layoutSwitch1.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
                if (layoutSwitch1.isChecked()) {
                    layoutSwitch1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitch1.setText(selectedFilter.switch1UnitFalse);
                }
                if (inputsReady) applyCorrectFilter();
            }
        });

        // Create the GestureDetector which handles the scrolling and double tap.
        final GestureDetector myGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                layoutImageView.translate((int) (distanceX / layoutImageView.getZoom()), (int) (distanceY / layoutImageView.getZoom()));
                refreshImageView();
                return false;
            }

            //Not used
            public boolean onDown(MotionEvent e) {return false;}
            public void onShowPress(MotionEvent e) {}
            public boolean onSingleTapUp(MotionEvent e) {return false;}
            public void onLongPress(MotionEvent e) {}
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {return false;}
        });

        //myGestureDetector.setIsLongpressEnabled(true);
        myGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {

            public boolean onDoubleTap(MotionEvent e) {

                if (layoutImageView.getZoom() != 1f) {
                    layoutImageView.reset();
                } else {
                    Point touch = layoutImageView.imageViewTouchPointToBmpCoordinates(new Point(e.getX(), e.getY()));
                    layoutImageView.setZoom(Settings.DOUBLE_TAP_ZOOM);
                    layoutImageView.setCenter(touch);
                }
                refreshImageView();
                return true;
            }

            // Not used
            public boolean onSingleTapConfirmed(MotionEvent e) {return false;}
            public boolean onDoubleTapEvent(MotionEvent e) {return false;}

        });

        // Create the ScaleGestureDetector which handles the scaling.
        final ScaleGestureDetector myScaleDetector = new ScaleGestureDetector(MainActivity.this, new ScaleGestureDetector.OnScaleGestureListener() {
            float lastZoomFactor;

            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastZoomFactor = layoutImageView.getZoom();
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                layoutImageView.setZoom(lastZoomFactor * detector.getScaleFactor());
                refreshImageView();
                return false;
            }

            //Not used
            public void onScaleEnd(ScaleGestureDetector detector) {}
        });

        // When the imageView is touched in any fashion, call both the ScaleGestureDetector and GestureDetector.
        layoutImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (cropGoingOn) {
                    //TODO: See if you can't use filteredImage directly instead of another bitmap
                    final Bitmap mybmp = createBitmap(filteredImage);
                    layoutImageView.getImageView().setImageBitmap(mybmp);
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
                            cropStart = layoutImageView.imageViewTouchPointToBmpCoordinates(cropStart);
                            layoutImageView.sanitizeBmpCoordinates(cropStart);
                        }
                        case MotionEvent.ACTION_MOVE: {
                            cropEnd.x = (int) event.getX();
                            cropEnd.y = (int) event.getY();
                            cropEnd = layoutImageView.imageViewTouchPointToBmpCoordinates(cropEnd);
                            layoutImageView.sanitizeBmpCoordinates(cropEnd);
                        }
                        case MotionEvent.ACTION_UP: {
                            break;
                        }
                    }

                } else {
                    myScaleDetector.onTouchEvent(event);
                    myGestureDetector.onTouchEvent(event);
                }

                v.performClick();
                return true;
            }
        });

        // When the user clicks on the reset button, puts back the original image
        layoutButtonOriginal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // If the button is displaying Load an image
                if (((Button) v).getText().toString().equals(getResources().getString(R.string.loadButtonString))) {

                    // Makes sure the phone has a camera module.
                    PackageManager pm = getApplicationContext().getPackageManager();
                    if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                        final CharSequence[] items = {"Take Photo", "Choose from Library"};
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Select a photo...");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {

                                if (items[item].equals("Take Photo")) {

                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileInputOutput.getUriForNewFile(MainActivity.this));
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
                    layoutSpinner.setSelection(0);
                    ((Button) v).setText(getResources().getString(R.string.loadButtonString));
                }
            }
        });

        // When the user clicks on the histogram, makes it collapse or bring it back up.
        layoutHistogram.setOnClickListener(new View.OnClickListener() {

            boolean collapsed = false;

            @Override
            public void onClick(View v) {

                int dimensionInDp;

                if (collapsed) {
                    // Convert px to dp
                    dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
                    layoutImageInfo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    layoutImageInfo.setVisibility(View.VISIBLE);
                } else {
                    // Convert px to dp
                    dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                    layoutImageInfo.setVisibility(View.GONE);
                }

                collapsed = !collapsed;
                v.getLayoutParams().height = dimensionInDp;
                v.requestLayout();

                //TODO: Correct bug 0001
            }
        });

        layoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Handles when an item is selected in the spinner.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // This values is used to avoid applying filters while the seek bar are modified.
                // Changing the seek bar minimum, progress or maximum values would normally call the
                // seek bar listener, which would apply the filter 3 time for no reason.
                inputsReady = false;

                Filter selectedFilter = filters.get(position);

                if (selectedFilter.getName().equals("Crop")) {
                    cropGoingOn = true;
                    cropStart = new Point(0,0);
                    cropStart = new Point(0,0);
                    layoutImageView.reset();
                } else {
                    cropGoingOn = false;
                }

                if (position != 0) {
                    layoutButtonApply.setText(getResources().getString(R.string.applyButtonString));
                    layoutButtonOriginal.setText(getResources().getString(R.string.originalButtonString));
                    layoutButtonApply.setEnabled(true);
                }

                if (selectedFilter.colorSeekBar) {
                    layoutColorSeekBar.setVisibility(View.VISIBLE);
                } else {
                    layoutColorSeekBar.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.seekBar1) {
                    layoutSeekBar1.setVisibility(View.VISIBLE);
                    layoutSeekBar1.setMin(selectedFilter.seekBar1Min);
                    layoutSeekBar1.setMax(selectedFilter.seekBar1Max);
                    layoutSeekBar1.setProgress(selectedFilter.seekBar1Set);
                } else {
                    layoutSeekBar1.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.seekBar2) {
                    layoutSeekBar2.setVisibility(View.VISIBLE);
                    layoutSeekBar2.setMin(selectedFilter.seekBar2Min);
                    layoutSeekBar2.setMax(selectedFilter.seekBar2Max);
                    layoutSeekBar2.setProgress(selectedFilter.seekBar2Set);
                } else {
                    layoutSeekBar2.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.switch1) {
                    layoutSwitch1.setVisibility(View.VISIBLE);
                    layoutSwitch1.setChecked(selectedFilter.switch1Default);
                    if (layoutSwitch1.isChecked()) {
                        layoutSwitch1.setText(selectedFilter.switch1UnitTrue);
                    } else {
                        layoutSwitch1.setText(selectedFilter.switch1UnitFalse);
                    }


                    if (!selectedFilter.seekBar2) {
                        layoutSeekBar2.setVisibility(View.INVISIBLE);
                    }

                } else {
                    layoutSwitch1.setVisibility(View.INVISIBLE);

                }

                // Only shows the seekBarValues when the seekBars are visible.
                layoutSeekBarValue1.setVisibility(layoutSeekBar1.getVisibility());
                layoutSeekBarValue2.setVisibility(layoutSeekBar2.getVisibility());
                layoutSeekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", layoutSeekBar1.getProgress(), selectedFilter.seekBar1Unit));
                layoutSeekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", layoutSeekBar2.getProgress(), selectedFilter.seekBar2Unit));

                applyCorrectFilter();

                // The seek bars listener can be triggered again.
                inputsReady = true;
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // When the user click on the apply button, apply the selected filter in the spinner
        layoutButtonApply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (originalImage != null) {

                    // If the spinner has no filter selected, it is a save button
                    if (layoutSpinner.getSelectedItemPosition() == 0) {

                        // Did it save properly?
                        if (FileInputOutput.saveImage(filteredImage, MainActivity.this)) {
                            v.setEnabled(false);
                            Snackbar.make(v, getString(R.string.savingMessage), Snackbar.LENGTH_SHORT).show();
                        }

                        // Else it is an apply button
                    } else {
                        // Finds the imageView and makes it display original_image
                        applyCorrectFilter(true);
                        cropGoingOn = false;
                        beforeLastFilterImage = filteredImage.copy(filteredImage.getConfig(), true);

                        /* Put the spinner back to the default position */
                        layoutSpinner.setSelection(0);
                        ((Button)v).setText(getResources().getString(R.string.saveButtonString));
                    }
                }
            }
        });
    }






    private void generateFilters() {
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
                return FilterFunction.crop(bmp, cropStart, cropEnd);
            }
        });
        filters.add(newFilter);







        // Adds all filter names in a array that will be used by the spinner
        String[] arraySpinner = new String[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            arraySpinner[i] = filters.get(i).getName();
        }

        // Initialize the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutSpinner.setAdapter(adapter);
    }


}

