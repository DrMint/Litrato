package com.example.retouchephoto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;

import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

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
     * Four lists of all filters. The order is the same as shown by the spinner.
     */
    private final List<Filter> filtersColor = new ArrayList<>();
    private final List<Filter> filtersFancy = new ArrayList<>();
    private final List<Filter> filtersBlur = new ArrayList<>();
    private final List<Filter> filtersContour = new ArrayList<>();

    /**
     * A list of all tools. The order is the same as shown by the spinner.
     */
    private final List<Filter> tools = new ArrayList<>();

    /**
     * A list of all presets. The order is the same as shown by the spinner.
     */
    private final List<Filter> presets = new ArrayList<>();

    private final List<TextView> textView = new ArrayList<>();

    private Boolean presetsOpen = false;
    private Boolean toolsOpen = false;
    private Boolean filtersOpen = false;
    private Boolean colorOpen = true;
    private Boolean fancyOpen = false;
    private Boolean blurOpen = false;
    private Boolean contourOpen = false;

    Integer MINIATURE_BMP_SIZE = 300;


    //private Point cropStart = new Point();
    //private Point cropEnd = new Point();

    private ImageViewZoomScroll layoutImageView;
    private Button      layoutButtonOriginal;
    private Button      oldVersion;
    private Button      toolsButton;
    private Button      presetsButton;
    private Button      filtersButton;
    private Button      colorButton;
    private Button      fancyButton;
    private Button      blurButton;
    private Button      contourButton;
    private Spinner     layoutSpinner;
    private TableLayout toolsBar;
    private HorizontalScrollView presetsBar;
    private LinearLayout filtersBar;
    private LinearLayout presetsLinearLayout;
    private LinearLayout colorBar;
    private LinearLayout fancyBar;
    private LinearLayout blurBar;
    private LinearLayout contourBar;

    private LinearLayout rotationButton;
    private LinearLayout cropButton;
    private LinearLayout flipButton;
    private LinearLayout stickersButton;
    private LinearLayout luminosityButton;
    private LinearLayout contrastButton;
    private LinearLayout sharpnessButton;
    private LinearLayout autoButton;
    private LinearLayout saturationButton;
    private LinearLayout addNoiseButton;
    private LinearLayout temperatureButton;
    private LinearLayout tintButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));
        layoutButtonOriginal    = findViewById(R.id.originalButton);
        oldVersion              = findViewById(R.id.oldVersionButton);
        toolsButton             = findViewById(R.id.toolsButton);
        presetsButton           = findViewById(R.id.presetsButton);
        filtersButton           = findViewById(R.id.filtersButton);
        colorButton             = findViewById(R.id.colorButton);
        fancyButton             = findViewById(R.id.fancyButton);
        blurButton              = findViewById(R.id.blurButton);
        contourButton           = findViewById(R.id.contourButton);
        layoutSpinner           = findViewById(R.id.spinner);
        toolsBar                = findViewById(R.id.toolsBar);
        presetsBar              = findViewById(R.id.presetsBar);
        filtersBar              = findViewById(R.id.filtersBar);
        presetsLinearLayout     = findViewById(R.id.presetsLinearLayout);
        colorBar                = findViewById(R.id.colorMenu);
        fancyBar                = findViewById(R.id.fancyMenu);
        blurBar                 = findViewById(R.id.blurMenu);
        contourBar              = findViewById(R.id.contourMenu);
        rotationButton          = findViewById(R.id.rotationButton);
        cropButton              = findViewById(R.id.cropButton);
        flipButton              = findViewById(R.id.flipButton);
        stickersButton          = findViewById(R.id.stickersButton);
        luminosityButton        = findViewById(R.id.luminosityButton);
        contrastButton          = findViewById(R.id.contrastButton);
        sharpnessButton         = findViewById(R.id.sharpnessButton);
        autoButton              = findViewById(R.id.autoButton);
        saturationButton        = findViewById(R.id.saturationButton);
        addNoiseButton          = findViewById(R.id.noiseButton);
        temperatureButton       = findViewById(R.id.temperatureButton);
        tintButton              = findViewById(R.id.tintButton);

        // Selects the default image in the resource folder and set it
        setBitmap(FileInputOutput.getBitmap(getResources(), R.drawable.default_image));
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        // Create the lists of filters
        generatePresets();
        generateTools();
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

    private void previewOrApply(boolean apply) {
        // If the spinner has yet to be initialize, aborts.
        if (layoutSpinner.getSelectedItemPosition() == -1) return;

        // filteredImage is now a fresh copy of beforeLastFilterImage
        filteredImage = beforeLastFilterImage.copy(beforeLastFilterImage.getConfig(), true);

        /*Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());

        Bitmap result;
        if (apply) {
            result = selectedFilter.apply(
                    filteredImage,
                    getApplicationContext(),
                    layoutColorSeekBar.getProgress(),
                    layoutSeekBar1.getProgress(),
                    layoutSeekBar2.getProgress(),
                    layoutSwitch1.isChecked());
        } else {
            result = selectedFilter.preview(
                    filteredImage,
                    getApplicationContext(),
                    layoutColorSeekBar.getProgress(),
                    layoutSeekBar1.getProgress(),
                    layoutSeekBar2.getProgress(),
                    layoutSwitch1.isChecked());
        }


        // If the filter return a bitmap, filteredImage becomes this bitmap
        if (result != null) {
            filteredImage = result;
        }*/

        // Refresh the image viewer and the histogram.
        refreshImageView();
    }

    /**
     * Applies whichever filter is selected in the spinner, with the appropriate parameters from the
     * seek bars and color bar. Refreshes the histogram and imageViewer after.
     */
    private void applyFilter() {
        previewOrApply(true);
    }

    private void previewFilter() {
        previewOrApply(false);
    }

    /**
     * Displays filteredImage on the imageView, also refreshes Histogram and ImageInfo
     */
    private void refreshImageView() {
        layoutImageView.setImageBitmap(filteredImage);
        //refreshHistogram();
        //refreshImageInfo();
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
    /*private void refreshHistogram() {
        layoutHistogram.setImageBitmap(ImageTools.generateHistogram(filteredImage));
    }*/

    /*private void refreshImageInfo() {

        final String infoString = String.format(
                Locale.ENGLISH,"%s%d  |  %s%d",
                getResources().getString(R.string.width),
                filteredImage.getWidth(),
                getResources().getString(R.string.height),
                filteredImage.getHeight());

        layoutImageInfo.setText(infoString);
    }*/

    private void initializeListener() {

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

        // The default behavior of imageView.
        final View.OnTouchListener defaultImageViewTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                myScaleDetector.onTouchEvent(event);
                myGestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        };

        // When the user clicks on the reset button, puts back the original image
        /*layoutButtonOriginal.setOnClickListener(new View.OnClickListener() {

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

                    //Puts the spinner back to the default position
                    layoutSpinner.setSelection(0);
                    ((Button) v).setText(getResources().getString(R.string.loadButtonString));
                }
            }
        });*/

        /*oldVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOldActivity();
            }
        });*/

       // layoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

         //   /**
         //    * Handles when an item is selected in the spinner.
         //    */
          /*  @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // This values is used to avoid applying filters while the seek bar are modified.
                // Changing the seek bar minimum, progress or maximum values would normally call the
                // seek bar listener, which would apply the filter 3 time for no reason.
                inputsReady = false;

                Filter selectedFilter = filters.get(position);

                selectedFilter.init();

                // Apply the custom filterTouchListener to layoutImageView if it exists, else revert to the default one.
                View.OnTouchListener filterTouchListener = selectedFilter.getImageViewTouchListener();
                if (filterTouchListener == null) {
                    layoutImageView.setOnTouchListener(defaultImageViewTouchListener);
                } else {
                    layoutImageView.setOnTouchListener(filterTouchListener);
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

                previewFilter();

                // The seek bars listener can be triggered again.
                inputsReady = true;
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });*/


        // When the user click on the apply button, apply the selected filter in the spinner
       /* layoutButtonApply.setOnClickListener(new View.OnClickListener() {

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
                        applyFilter();
                        beforeLastFilterImage = filteredImage.copy(filteredImage.getConfig(), true);

                        // Put the spinner back to the default position
                        layoutSpinner.setSelection(0);
                        ((Button)v).setText(getResources().getString(R.string.saveButtonString));
                    }
                }
            }
        });*/

        toolsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toolsBar.getVisibility()==View.VISIBLE){
                    closeMenu();
                }else{
                    closeMenu();
                    toolsBar.setVisibility(View.VISIBLE);
                    toolsOpen = true;
                }
            }
        });

        presetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(presetsBar.getVisibility()==View.VISIBLE){
                    closeMenu();
                }else{
                    closeMenu();
                    presetsBar.setVisibility(View.VISIBLE);
                    presetsOpen = true;
                }
            }
        });

        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filtersBar.getVisibility()==View.VISIBLE){
                    closeMenu();
                }else{
                    closeMenu();
                    filtersBar.setVisibility(View.VISIBLE);
                    filtersOpen = true;
                }
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorBar.getVisibility()==View.GONE){
                    closeSubMenu();
                    colorBar.setVisibility(View.VISIBLE);
                    colorOpen = true;
                }
            }
        });

        fancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fancyBar.getVisibility()==View.GONE){
                    closeSubMenu();
                    fancyBar.setVisibility(View.VISIBLE);
                    fancyOpen = true;
                }
            }
        });

        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blurBar.getVisibility()==View.GONE){
                    closeSubMenu();
                    blurBar.setVisibility(View.VISIBLE);
                    blurOpen = true;
                }
            }
        });

        contourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contourBar.getVisibility()==View.GONE){
                    closeSubMenu();
                    contourBar.setVisibility(View.VISIBLE);
                    contourOpen = true;
                }
            }
        });

        generateButton(this);
    }

    private void generatePresets(){
        Filter newPresets;

        newPresets = new Filter("2 Strip");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.removeAColor(bmp, context, 79, 72);
                FilterFunction.removeAColor(bmp, context, 129, 99);
                FilterFunction.removeAColor(bmp, context, 294, 40);
                FilterFunction.hueShift(bmp, context, -15);
                return null;
            }
        });
        presets.add(newPresets);

        newPresets = new Filter("Invert");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.invert(bmp, context);
                return null;
            }
        });
        presets.add(newPresets);

        newPresets = new Filter("Bleach Bypass");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.toExtDyn(bmp, context, 25, 255);
                FilterFunction.saturation(bmp, context, 0.7f);
                FilterFunction.brightness(bmp, context, 100);
                return null;
            }
        });
        presets.add(newPresets);

        newPresets = new Filter("Candle light");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.saturation(bmp, context, 0.4f);
                FilterFunction.temperature(bmp, context, 5.8f);
                return bmp;
            }
        });
        presets.add(newPresets);

        /*newPresets = new Filter("Crisp Warm");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, 0.08f);
                FilterFunction.temperature(bmp, context, 2f);
                return bmp;
            }
        });
        presets.add(newPresets);*/

        newPresets = new Filter("Crisp Winter");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.brightness(bmp, context, 60);
                FilterFunction.temperature(bmp, context, -8f);
                return bmp;
            }
        });
        presets.add(newPresets);

        newPresets = new Filter("Drop Blues");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.removeAColor(bmp, context, 232, 109);
                FilterFunction.removeAColor(bmp, context, 189, 83);
                return bmp;
            }
        });
        presets.add(newPresets);

        /*newPresets = new Filter("Old analog");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.gaussianBlur(bmp, context, 2);
                FilterFunction.saturation(bmp, context, 0);
                FilterFunction.temperature(bmp, context, 10);
                Bitmap texture = FileInputOutput.getBitmap(getResources(), R.drawable.grunge_texture, bmp.getWidth(), bmp.getHeight());
                Bitmap texture2 = FileInputOutput.getBitmap(getResources(), R.drawable.white_noise, bmp.getWidth(), bmp.getHeight());
                FilterFunction.applyTexture(bmp, texture, context, BlendType.MULTIPLY);
                FilterFunction.applyTexture(bmp, texture2, context, BlendType.ADD);
                return null;
            }
        });
        presets.add(newPresets);*/

        newPresets = new Filter("Tension Green");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.removeAColor(bmp, context, 270, 108);
                FilterFunction.saturation(bmp, context, 0.71f);
                FilterFunction.tint(bmp, context, -3.6f);
                return null;
            }
        });
        presets.add(newPresets);

        /*newPresets = new Filter("Edgy Amber");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, -0.1f);
                FilterFunction.burnValues(bmp, context, -0.2f);
                FilterFunction.saturation(bmp, context, 0.4f);
                FilterFunction.temperature(bmp, context, 10);
                FilterFunction.temperature(bmp, context, 5);
                return null;
            }
        });
        presets.add(newPresets);*/

        newPresets = new Filter("Night from Day");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.noise(bmp, context, 20, false);
                FilterFunction.gaussianBlur(bmp, context, 2);
                FilterFunction.saturation(bmp, context, 0.6f);
                FilterFunction.brightness(bmp, context, -110);
                FilterFunction.temperature(bmp, context, -8.6f);
                return null;
            }
        });
        presets.add(newPresets);

        /*newPresets = new Filter("Late Sunset");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.gamma(bmp, context, 0.6f);
                FilterFunction.saturation(bmp, context, 0.3f);
                FilterFunction.tint(bmp, context, 2.9f);
                FilterFunction.temperature(bmp, context, 5f);
                FilterFunction.brightness(bmp, context, 30);

                return null;
            }
        });
        presets.add(newPresets);*/

        /*newPresets = new Filter("Futuristic Bleak");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, -0.29f);
                FilterFunction.saturation(bmp, context, 0.6f);
                FilterFunction.tint(bmp, context, -1f);

                return null;
            }
        });
        presets.add(newPresets);*/

        /*newPresets = new Filter("Soft Warming");
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, -0.23f);
                FilterFunction.brightness(bmp, context, 20);
                FilterFunction.saturation(bmp, context, 0.7f);
                FilterFunction.tint(bmp, context, 1f);
                FilterFunction.temperature(bmp, context, 0.7f);

                return null;
            }
        });
        presets.add(newPresets);*/


        // End of Presets
    }

    private void generateTools(){
        Filter newTools;

        // Tools

        newTools = new Filter("Rotation");
        newTools.setSeekBar1(-180, 0, 180, "deg");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                return FilterFunction.rotate(bmp, seekBar);
            }
        });
        tools.add(newTools);

        /*newTools = new Filter("Crop");
        final Point cropStart = new Point();
        final Point cropEnd = new Point();

        newTools.setImageViewTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        cropStart.x = (int) event.getX();
                        cropStart.y = (int) event.getY();
                        cropStart.set(layoutImageView.imageViewTouchPointToBmpCoordinates(cropStart));
                        layoutImageView.sanitizeBmpCoordinates(cropStart);
                    }
                    case MotionEvent.ACTION_MOVE: {
                        cropEnd.x = (int) event.getX();
                        cropEnd.y = (int) event.getY();
                        cropEnd.set(layoutImageView.imageViewTouchPointToBmpCoordinates(cropEnd));
                        layoutImageView.sanitizeBmpCoordinates(cropEnd);
                    }
                    case MotionEvent.ACTION_UP: break;
                }
                previewFilter();
                v.performClick();
                return true;
            }
        });
        newTools.setFilterInitFunction(new FilterInitInterface() {
            @Override
            public void init() {
                cropStart.set(0,0);
                cropEnd.set(0,0);
                layoutImageView.reset();
            }
        });
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                ImageTools.drawRectangle(bmp, cropStart, cropEnd, Color.argb(Settings.CROP_OPACITY, 255,255,255));
                ImageTools.drawRectangle(bmp, cropStart, cropEnd, Color.argb(Settings.CROP_OPACITY, 0,0,0), Settings.CROP_BORDER_SIZE);
                return null;
            }
        });
        newTools.setFilterApplyFunction(new FilterApplyInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                return FilterFunction.crop(bmp, cropStart, cropEnd);
            }
        });
        tools.add(newTools);*/


        newTools = new Filter("Flip");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.mirror(bmp, context);
                return null;
            }
        });
        tools.add(newTools);

        //newTools = new Filter("Stickers");
        //tools.add(newTools);

        /*newTools = new Filter("Luminosity");
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setSeekBar2(-100, 0, 100, "");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                if (seekBar <= 0) seekBar *= -seekBar;
                FilterFunction.brightness(bmp, context, seekBar * 2.55f);
                FilterFunction.gamma(bmp, context, seekBar2 / 100f + 1f);
                return null;
            }
        });
        tools.add(newTools);*/

        /*newTools = new Filter("Contrast");
        newTools.setSeekBar1(-50, 0, 50, "%");
        newTools.setSeekBar2(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, seekBar / 100f);
                FilterFunction.burnValues(bmp, context, seekBar2 / 50f);
                return null;
            }
        });
        tools.add(newTools);*/

        newTools = new Filter("Sharpness");
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.sharpen(bmp, context, seekBar / 200f);
                return null;
            }
        });
        tools.add(newTools);

        newTools = new Filter("Auto");
        newTools.setSwitch1(false, "Linear", "Dynamic");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                if (switch1) {
                    FilterFunction.histogramEqualization(bmp, context);
                } else {
                    FilterFunction.toExtDyn(bmp, context,0, 255);
                }
                return null;
            }
        });
        tools.add(newTools);

        newTools = new Filter("Saturation");
        newTools.setSeekBar1(0, 100, 200, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.saturation(bmp, context,seekBar / 100f);
                return null;
            }
        });
        tools.add(newTools);

        newTools = new Filter("Add noise");
        newTools.setSeekBar1(0, 0, 255, "");
        newTools.setSwitch1(false,"B&W Noise", "Color Noise");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.noise(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        tools.add(newTools);

        newTools = new Filter("Temperature");
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.temperature(bmp, context, seekBar / 10f);
                return null;
            }
        });
        tools.add(newTools);

        newTools = new Filter("Tint");
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.tint(bmp, context, seekBar / 10f);
                return null;
            }
        });
        tools.add(newTools);


        // End of Tools
    }

    private void generateFilters() {
        // Creates the filters
        Filter newFilter;

        // Filters > Color

        newFilter = new Filter("Colorize");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(0,100,100,"");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.colorize(bmp, context, colorSeekHue, seekBar / 100f, true);
                return null;
            }
        });
        filtersColor.add(newFilter);

        newFilter = new Filter("Change hue");
        newFilter.setColorSeekBar();
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.colorize(bmp, context, colorSeekHue,0, false);
                return null;
            }
        });
        filtersColor.add(newFilter);

        newFilter = new Filter("Selective coloring");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 25, 360, "deg");
        newFilter.setSwitch1(false, "Keep,", "Remove");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                if (switch1) {
                    FilterFunction.removeAColor(bmp, context, colorSeekHue,(int)seekBar);
                } else {
                    FilterFunction.keepAColor(bmp, context, colorSeekHue,(int)seekBar);
                }
                return null;
            }
        });
        filtersColor.add(newFilter);

        newFilter = new Filter("Hue shift");
        newFilter.setSeekBar1(-180, 0, 180, "deg");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.hueShift(bmp, context,seekBar);
                return null;
            }
        });
        filtersColor.add(newFilter);


        // End of Filters > Color





        // Filters > Fancy

        newFilter = new Filter("Threshold");
        newFilter.setSeekBar1(0, 128, 256, "");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.threshold(bmp, context, seekBar / 256f);
                return null;
            }
        });
        filtersFancy.add(newFilter);

        newFilter = new Filter("Posterize");
        newFilter.setSeekBar1(2, 10, 32, "steps");
        newFilter.setSwitch1(true,"Color", "B&W");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.posterize(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filtersFancy.add(newFilter);

        // End of Filters > Fancy






        // Filters > Blur

        newFilter = new Filter("Average blur");
        newFilter.setSeekBar1(1, 2, 19, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.averageBlur(bmp, context, (int) seekBar);
                return null;
            }
        });
        filtersBlur.add(newFilter);

        newFilter = new Filter("Gaussian blur");
        newFilter.setSeekBar1(1, 2, 25, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.gaussianBlur(bmp, context, (int) seekBar);
                return null;
            }
        });
        filtersBlur.add(newFilter);

        newFilter = new Filter("Directional blur");
        newFilter.setSeekBar1(2, 2, 30, "");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.directionalBlur(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filtersBlur.add(newFilter);

        // End of Filters > Blur





        // Filters > Contour

        newFilter = new Filter("Laplacian");
        newFilter.setSeekBar1(1, 2, 14, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.laplacian(bmp, context, seekBar);
                return null;
            }
        });
        filtersContour.add(newFilter);

        newFilter = new Filter("Sobel");
        newFilter.setSeekBar1(1, 2, 14, "px");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.sobel(bmp, context, seekBar, switch1);
                return null;
            }
        });
        filtersContour.add(newFilter);

        /*newFilter = new Filter("Sketch");
        newFilter.setSeekBar1(1, 4, 14, "");
        newFilter.setSeekBar2(0, 20, 100, "");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                Bitmap texture = FileInputOutput.getBitmap(getResources(), R.drawable.canvas_texture, bmp.getWidth(), bmp.getHeight());
                FilterFunction.sketch(bmp, context, (int) seekBar, seekBar2 / 100f);
                FilterFunction.applyTexture(bmp, texture, context);
                return null;
            }
        });
        filtersContour.add(newFilter);*/

        newFilter = new Filter("Cartoon");
        newFilter.setSeekBar1(1, 0, 100, "px");
        newFilter.setSeekBar2(2, 4, 14, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.cartoon(bmp, context, (int) seekBar, (int) seekBar2);
                return null;
            }
        });
        filtersContour.add(newFilter);

        // End of Filters > Contour

        /*// Adds all filter names in a array that will be used by the spinner
        String[] arraySpinner = new String[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            arraySpinner[i] = filters.get(i).getName();
        }

        // Initialize the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        layoutSpinner.setAdapter(adapter);*/
    }

    private void generateButton(Context context){
        TextView textView;
        Drawable drawable;
        for(int i = 0; i< presets.size(); i++){
            drawable = new BitmapDrawable(getResources(),ImageTools.toSquare(filteredImage,MINIATURE_BMP_SIZE));
            drawable.setBounds(0,0,300,300);
            textView = new TextView(context);
            textView.setClickable(true);
            textView.setText(presets.get(i).getName());
            textView.setTextAppearance(context,R.style.TextAppearance_AppCompat_Body2);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setCompoundDrawables(null, drawable,null,null);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(15,15,15,15);
            textView.setLayoutParams(params);
            this.presetsLinearLayout.addView(textView);

            /*textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFiltersActivity();
                }
            });*/

            this.textView.add(textView);
        }
        for(int i = 0; i< filtersColor.size(); i++){
            drawable = new BitmapDrawable(getResources(),ImageTools.toSquare(filteredImage,MINIATURE_BMP_SIZE));
            drawable.setBounds(0,0,300,300);
            textView = new TextView(context);
            textView.setClickable(true);
            textView.setText(filtersColor.get(i).getName());
            textView.setTextAppearance(context,R.style.TextAppearance_AppCompat_Body2);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setCompoundDrawables(null, drawable,null,null);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(15,15,15,15);
            textView.setLayoutParams(params);
            this.colorBar.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFiltersActivity();
                }
            });

            this.textView.add(textView);
        }
        for(int i = 0; i< filtersFancy.size(); i++){
            drawable = new BitmapDrawable(getResources(),ImageTools.toSquare(filteredImage,MINIATURE_BMP_SIZE));
            drawable.setBounds(0,0,300,300);
            textView = new TextView(context);
            textView.setClickable(true);
            textView.setText(filtersFancy.get(i).getName());
            textView.setTextAppearance(context,R.style.TextAppearance_AppCompat_Body2);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setCompoundDrawables(null, drawable,null,null);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(15,15,15,15);
            textView.setLayoutParams(params);
            this.fancyBar.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFiltersActivity();
                }
            });

            this.textView.add(textView);
        }
        for(int i = 0; i< filtersBlur.size(); i++){
            drawable = new BitmapDrawable(getResources(),ImageTools.toSquare(filteredImage,MINIATURE_BMP_SIZE));
            drawable.setBounds(0,0,300,300);
            textView = new TextView(context);
            textView.setClickable(true);
            textView.setText(filtersBlur.get(i).getName());
            textView.setTextAppearance(context,R.style.TextAppearance_AppCompat_Body2);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setCompoundDrawables(null, drawable,null,null);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(15,15,15,15);
            textView.setLayoutParams(params);
            this.blurBar.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFiltersActivity();
                }
            });

            this.textView.add(textView);
        }
        for(int i = 0; i< filtersContour.size(); i++){
            drawable = new BitmapDrawable(getResources(),ImageTools.toSquare(filteredImage,MINIATURE_BMP_SIZE));
            drawable.setBounds(0,0,300,300);
            textView = new TextView(context);
            textView.setClickable(true);
            textView.setText(filtersContour.get(i).getName());
            textView.setTextAppearance(context,R.style.TextAppearance_AppCompat_Body2);
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setCompoundDrawables(null, drawable,null,null);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(15,15,15,15);
            textView.setLayoutParams(params);
            this.contourBar.addView(textView);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFiltersActivity();
                }
            });

            this.textView.add(textView);
        }
        /*rotationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        stickersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        luminosityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        contrastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        sharpnessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        saturationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        addNoiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        temperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });
        tintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });*/
    }

    void closeMenu(){
        if(presetsOpen){
            presetsBar.setVisibility(View.GONE);
            this.presetsOpen = false;
        }
        if(toolsOpen){
            toolsBar.setVisibility(View.GONE);
            this.toolsOpen = false;
        }
        if (filtersOpen){
            filtersBar.setVisibility(View.GONE);
            this.filtersOpen = false;
        }
    }

    void closeSubMenu(){
        if (colorOpen){
            colorBar.setVisibility(View.GONE);
            this.colorOpen = false;
        }
        if (fancyOpen){
            fancyBar.setVisibility(View.GONE);
            this.fancyOpen = false;
        }
        if (blurOpen){
            blurBar.setVisibility(View.GONE);
            this.blurOpen = false;
        }
        if (contourOpen){
            contourBar.setVisibility(View.GONE);
            this.contourOpen = false;
        }
    }

    private void openFiltersActivity(){
        Intent intent = new Intent(this,FiltersActivity.class);
        startActivity(intent);
    }

    private void openOldActivity(){
        Intent intent = new Intent(this,MainActivity_old.class);
        startActivity(intent);
    }
}

