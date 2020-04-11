package com.example.retouchephoto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;

import android.util.Log;
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
        [0002] - When swipping on the black area bellow the image when launching the app, it crashes
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

    static Filter selectedFilter;
    static Bitmap selectedBitmap;

    private final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private final int FILTER_ACTIVITY_IS_FINISHED = 3;



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
    private final List<Filter> filters = new ArrayList<>();

    private final List<DisplayedFilter> displayedFilters = new ArrayList<>();

    private ImageViewZoomScroll layoutImageView;
    private Button      layoutButtonOpen;
    private Button      layoutButtonSave;
    private Button      layoutOldVersion;
    private Button      toolsButton;
    private Button      presetsButton;
    private Button      filtersButton;
    private Button      colorButton;
    private Button      fancyButton;
    private Button      blurButton;
    private Button      contourButton;
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

    Typeface submenuUnselected;
    Typeface submenuSelected;

    boolean needToRefreshMiniature;

    //private View layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));

        layoutButtonOpen        = findViewById(R.id.buttonOpen);
        layoutButtonSave        = findViewById(R.id.buttonSave);
        layoutOldVersion        = findViewById(R.id.buttonOldVersion);

        toolsButton             = findViewById(R.id.buttonTools);
        presetsButton           = findViewById(R.id.buttonPresets);
        filtersButton           = findViewById(R.id.buttonFilters);

        colorButton             = findViewById(R.id.buttonColor);
        fancyButton             = findViewById(R.id.buttonFancy);
        blurButton              = findViewById(R.id.buttonBlur);
        contourButton           = findViewById(R.id.buttonContour);

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

        submenuSelected = colorButton.getTypeface();
        submenuUnselected = fancyButton.getTypeface();

        // Selects the default image in the resource folder and set it
        setBitmap(FileInputOutput.getBitmap(getResources(), R.drawable.default_image));
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        // Create the lists of filters
        generatePresets();
        generateTools();
        generateFilters();

        // If this is the first launch, compiles the RenderScript's functions
        if (appGetFirstTimeRun() == 0) {
            initializeRenderScriptCaching();
        }

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
                //layoutSpinner.setSelection(0);

                setBitmap(mBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Load the last taken photo.
            setBitmap(FileInputOutput.getLastTakenBitmap());
        }

        if (requestCode == FILTER_ACTIVITY_IS_FINISHED) {
            Bitmap result = FiltersActivity.result;
            if (result != null) {
                layoutImageView.reset();
                beforeLastFilterImage = ImageTools.bitmapClone(result);
                refreshImageView();
            }
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
                    layoutButtonOpen,
                    "Image resized to " + bmp.getWidth() + "px by " + bmp.getHeight() + "px",
                    Snackbar.LENGTH_SHORT);
            sb.show();
        }

        // Set this image as the originalImage and reset the UI
        originalImage = bmp;
        resetImage();
    }

    /**
     * Displays filteredImage on the imageView, also refreshes Histogram and ImageInfo
     */
    private void refreshImageView() {
        layoutImageView.setImageBitmap(beforeLastFilterImage);
        generateMiniatureForOpenedMenu();
    }

    /**
     * Display the original image in "imageView" and refresh the histogram.
     */
    private void resetImage() {
        beforeLastFilterImage = ImageTools.bitmapClone(originalImage);
        filteredImage = ImageTools.bitmapClone(originalImage);
        refreshImageView();
    }

    private void initializeListener() {

        layoutButtonOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

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
                                Uri result = FileInputOutput.createUri(MainActivity.this);
                                if (result != null) {
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, result);
                                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                }

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
            }
        });


        // When the user click on the apply button, apply the selected filter in the spinner
        layoutButtonSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (FileInputOutput.saveImage(beforeLastFilterImage, MainActivity.this)) {
                    Snackbar.make(v, getString(R.string.savingMessage), Snackbar.LENGTH_SHORT).show();
                }
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

        // The default behavior of imageView.
        final View.OnTouchListener defaultImageViewTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                closeMenus();
                myScaleDetector.onTouchEvent(event);
                myGestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        };
        layoutImageView.setOnTouchListener(defaultImageViewTouchListener);

        layoutOldVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOldActivity();
            }
        });

        presetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = isVisible(presetsBar);
                closeMenus();
                if (!visible) {
                    v.setBackgroundColor(Settings.COLOR_SELECTED);
                    presetsButton.setTypeface(submenuSelected);
                    presetsBar.setVisibility(View.VISIBLE);
                    generateMiniatureForOpenedMenu();
                }
            }
        });

        toolsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = isVisible(toolsBar);
                closeMenus();
                if (!visible) {
                    v.setBackgroundColor(Settings.COLOR_SELECTED);
                    toolsButton.setTypeface(submenuSelected);
                    toolsBar.setVisibility(View.VISIBLE);
                    generateMiniatureForOpenedMenu();
                }
            }
        });

        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = isVisible(filtersBar);
                closeMenus();
                if (!visible) {
                    v.setBackgroundColor(Settings.COLOR_SELECTED);
                    filtersButton.setTypeface(submenuSelected);
                    filtersBar.setVisibility(View.VISIBLE);
                    generateMiniatureForOpenedMenu();
                }
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSubMenus();
                colorButton.setTypeface(submenuSelected);
                colorBar.setVisibility(View.VISIBLE);
                generateMiniatureForOpenedMenu();
            }
        });

        fancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSubMenus();
                fancyButton.setTypeface(submenuSelected);
                fancyBar.setVisibility(View.VISIBLE);
                generateMiniatureForOpenedMenu();
            }
        });

        blurButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSubMenus();
                blurButton.setTypeface(submenuSelected);
                blurBar.setVisibility(View.VISIBLE);
                generateMiniatureForOpenedMenu();
            }
        });

        contourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSubMenus();
                contourButton.setTypeface(submenuSelected);
                contourBar.setVisibility(View.VISIBLE);
                generateMiniatureForOpenedMenu();
            }
        });
        initializeMenus();


    }

    private void generatePresets(){
        Filter newPresets;

        newPresets = new Filter("2 Strip");
        newPresets.setFilterCategory(FilterCategory.PRESET);
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
        filters.add(newPresets);

        newPresets = new Filter("Invert");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.invert(bmp, context);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Bleach Bypass");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.toExtDyn(bmp, context, 25, 255);
                FilterFunction.saturation(bmp, context, 0.7f);
                FilterFunction.brightness(bmp, context, 100);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Candle light");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.saturation(bmp, context, 0.4f);
                FilterFunction.temperature(bmp, context, 5.8f);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Crisp Warm");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, 0.08f);
                FilterFunction.temperature(bmp, context, 2f);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Crisp Winter");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.brightness(bmp, context, 60);
                FilterFunction.temperature(bmp, context, -8f);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Drop Blues");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.removeAColor(bmp, context, 232, 109);
                FilterFunction.removeAColor(bmp, context, 189, 83);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Old analog");
        newPresets.setFilterCategory(FilterCategory.PRESET);
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
        filters.add(newPresets);

        newPresets = new Filter("Tension Green");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.removeAColor(bmp, context, 270, 108);
                FilterFunction.saturation(bmp, context, 0.71f);
                FilterFunction.tint(bmp, context, -3.6f);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Edgy Amber");
        newPresets.setFilterCategory(FilterCategory.PRESET);
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
        filters.add(newPresets);

        newPresets = new Filter("Night from Day");
        newPresets.setFilterCategory(FilterCategory.PRESET);
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
        filters.add(newPresets);

        newPresets = new Filter("Late Sunset");
        newPresets.setFilterCategory(FilterCategory.PRESET);
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
        filters.add(newPresets);

        newPresets = new Filter("Futuristic Bleak");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.constrastBurn(bmp, context, -0.29f);
                FilterFunction.saturation(bmp, context, 0.6f);
                FilterFunction.tint(bmp, context, -1f);

                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Soft Warming");
        newPresets.setFilterCategory(FilterCategory.PRESET);
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
        filters.add(newPresets);
    }

    private void generateTools(){
        Filter newTools;

        // Tools

        newTools = new Filter("Rotation");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setSeekBar1(-180, 0, 180, "deg");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                return FilterFunction.rotate(bmp, seekBar);
            }
        });
        filters.add(newTools);

        newTools = new Filter("Crop");
        newTools.setFilterCategory(FilterCategory.TOOL);
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
                //previewFilter();
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
        filters.add(newTools);


        newTools = new Filter("Flip");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.mirror(bmp, context);
                return null;
            }
        });
        filters.add(newTools);

        //newTools = new Filter("Stickers");
        //tools.add(newTools);

        newTools = new Filter("Luminosity");
        newTools.setFilterCategory(FilterCategory.TOOL);
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
        filters.add(newTools);

        newTools = new Filter("Contrast");
        newTools.setFilterCategory(FilterCategory.TOOL);
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
        filters.add(newTools);

        newTools = new Filter("Sharpness");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.sharpen(bmp, context, seekBar / 200f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Auto");
        newTools.setFilterCategory(FilterCategory.TOOL);
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
        filters.add(newTools);

        newTools = new Filter("Saturation");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setSeekBar1(0, 100, 200, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.saturation(bmp, context,seekBar / 100f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Add noise");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setSeekBar1(0, 0, 255, "");
        newTools.setSwitch1(false,"B&W Noise", "Color Noise");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.noise(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Temperature");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.temperature(bmp, context, seekBar / 10f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Tint");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.hueShift(bmp, context,seekBar);
                return null;
            }
        });
        filters.add(newTools);


        // End of Tools
    }

    private void generateFilters() {
        Filter newFilter;

        // Filters > Color

        newFilter = new Filter("Colorize");
        newFilter.setFilterCategory(FilterCategory.COLOR);
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(0,100,100,"");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.colorize(bmp, context, colorSeekHue, seekBar / 100f, true);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Change hue");
        newFilter.setFilterCategory(FilterCategory.COLOR);
        newFilter.setColorSeekBar();
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.colorize(bmp, context, colorSeekHue,0, false);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Selective coloring");
        newFilter.setFilterCategory(FilterCategory.COLOR);
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
        filters.add(newFilter);

        newFilter = new Filter("Hue shift");
        newFilter.setFilterCategory(FilterCategory.COLOR);
        newFilter.setSeekBar1(-180, 0, 180, "deg");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.hueShift(bmp, context,seekBar);
                return null;
            }
        });
        filters.add(newFilter);


        // End of Filters > Color


        // Filters > Fancy

        newFilter = new Filter("Threshold");
        newFilter.setFilterCategory(FilterCategory.FANCY);
        newFilter.setSeekBar1(0, 128, 256, "");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.threshold(bmp, context, seekBar / 256f);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Posterize");
        newFilter.setFilterCategory(FilterCategory.FANCY);
        newFilter.setSeekBar1(2, 10, 32, "steps");
        newFilter.setSwitch1(true,"Color", "B&W");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.posterize(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        // End of Filters > Fancy


        // Filters > Blur

        newFilter = new Filter("Average blur");
        newFilter.setFilterCategory(FilterCategory.BLUR);
        newFilter.setSeekBar1(1, 2, 19, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.averageBlur(bmp, context, (int) seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Gaussian blur");
        newFilter.setFilterCategory(FilterCategory.BLUR);
        newFilter.setSeekBar1(1, 2, 25, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.gaussianBlur(bmp, context, (int) seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Directional blur");
        newFilter.setFilterCategory(FilterCategory.BLUR);
        newFilter.setSeekBar1(2, 2, 30, "");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.directionalBlur(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        // End of Filters > Blur


        // Filters > Contour

        newFilter = new Filter("Laplacian");
        newFilter.setFilterCategory(FilterCategory.CONTOUR);
        newFilter.setSeekBar1(1, 2, 14, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.laplacian(bmp, context, seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sobel");
        newFilter.setFilterCategory(FilterCategory.CONTOUR);
        newFilter.setSeekBar1(1, 2, 14, "px");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.sobel(bmp, context, seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sketch");
        newFilter.setFilterCategory(FilterCategory.CONTOUR);
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
        filters.add(newFilter);

        newFilter = new Filter("Cartoon");
        newFilter.setFilterCategory(FilterCategory.CONTOUR);
        newFilter.setSeekBar1(1, 0, 100, "px");
        newFilter.setSeekBar2(2, 4, 14, "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1) {
                FilterFunction.cartoon(bmp, context, (int) seekBar, (int) seekBar2);
                return null;
            }
        });
        filters.add(newFilter);
    }


    /**
     * Applies a filter to beforeLastFilterImage and refreshes ImageView.
     * @param filter the filter to apply
     */
    private void apply(Filter filter) {
        Bitmap result = filter.apply(beforeLastFilterImage, getApplicationContext());

        // If the filter return a bitmap, filteredImage becomes this bitmap
        if (result != null) {
            beforeLastFilterImage = result;
        }

        refreshImageView();
    }

    /**
     * Refreshes/Generates the miniatures of the currently opened.
     * If no menu is opened, no image is generated.
     */
    private void generateMiniatureForOpenedMenu() {
        if (isVisible(presetsBar)) {
            generateMiniatures(FilterCategory.PRESET);
        } else if (isVisible(toolsBar)) {
            generateMiniatures(FilterCategory.TOOL);
        } else if (isVisible(filtersBar)) {
            if (isVisible(colorBar)) {
                generateMiniatures(FilterCategory.COLOR);
            } else if (isVisible(fancyBar)) {
                generateMiniatures(FilterCategory.FANCY);
            } else if (isVisible(blurBar)) {
                generateMiniatures(FilterCategory.BLUR);
            } else if (isVisible(contourBar)) {
                generateMiniatures(FilterCategory.CONTOUR);
            }
        }
    }

    private void generateMiniatures(FilterCategory onlyThisCategory) {
        Bitmap resizedMiniature = ImageTools.toSquare(beforeLastFilterImage, Settings.MINIATURE_BMP_SIZE);

        for (DisplayedFilter displayedFilter:displayedFilters) {

            // Only generate the miniature if the displayedFilter of this category
            if (displayedFilter.filter.getFilterCategory() == onlyThisCategory) {
                Bitmap filteredMiniature =  ImageTools.bitmapClone(resizedMiniature);

                // Apply the filter to the miniature
                Bitmap result = displayedFilter.filter.apply(filteredMiniature, getApplicationContext());
                if (result != null) filteredMiniature = result;

                // Add the image on top of the text
                Drawable drawable = new BitmapDrawable(getResources(), filteredMiniature);
                drawable.setBounds(0,0,Settings.MINIATURE_DISPLAYED_SIZE, Settings.MINIATURE_DISPLAYED_SIZE);
                displayedFilter.textView.setCompoundDrawablePadding(25);
                displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
            }
        }
    }

    private TextView generateATextView(Filter filter) {
        TextView textView;
        textView = new TextView(this);
        textView.setClickable(true);
        textView.setText(filter.getName());
        textView.setAllCaps(true);
        textView.setMaxLines(2);
        textView.setHorizontallyScrolling(false);
        textView.setMaxWidth(Settings.MINIATURE_DISPLAYED_SIZE);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(12);
        textView.setHeight((int) (Settings.MINIATURE_DISPLAYED_SIZE * 1.4));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
        params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
        textView.setLayoutParams(params);
        return textView;
    }

    private void initializeMenus(){

        TextView textView;

        for (final Filter currentFilter:filters) {
            textView = generateATextView(currentFilter);

            // Add the filter to its right category
            switch (currentFilter.getFilterCategory()){
                case COLOR: this.colorBar.addView(textView); break;
                case FANCY: this.fancyBar.addView(textView); break;
                case BLUR: this.blurBar.addView(textView); break;
                case CONTOUR: this.contourBar.addView(textView); break;
                case PRESET: this.presetsLinearLayout.addView(textView); break;
                case TOOL: break;
            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentFilter.getFilterCategory() == FilterCategory.PRESET) {
                        apply(currentFilter);
                    } else {
                        openFiltersActivity(currentFilter, beforeLastFilterImage);
                    }
                }
            });
            displayedFilters.add(new DisplayedFilter(textView, currentFilter));
        }

        /*
        rotationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFiltersActivity();
            }
        });

         */
    }

    private void closeMenus(){
        presetsButton.setTypeface(submenuUnselected);
        toolsButton.setTypeface(submenuUnselected);
        filtersButton.setTypeface(submenuUnselected);

        presetsButton.setBackgroundColor(Settings.COLOR_GREY);
        toolsButton.setBackgroundColor(Settings.COLOR_GREY);
        filtersButton.setBackgroundColor(Settings.COLOR_GREY);

        presetsBar.setVisibility(View.GONE);
        toolsBar.setVisibility(View.GONE);
        filtersBar.setVisibility(View.GONE);
    }

    private void closeSubMenus(){
        colorButton.setTypeface(submenuUnselected);
        fancyButton.setTypeface(submenuUnselected);
        blurButton.setTypeface(submenuUnselected);
        contourButton.setTypeface(submenuUnselected);

        colorBar.setVisibility(View.GONE);
        fancyBar.setVisibility(View.GONE);
        blurBar.setVisibility(View.GONE);
        contourBar.setVisibility(View.GONE);
    }

    private void openFiltersActivity(Filter filter, Bitmap bmp){
        selectedFilter = filter;
        selectedBitmap = bmp;
        Intent intent = new Intent(this, FiltersActivity.class);
        startActivityForResult(intent, FILTER_ACTIVITY_IS_FINISHED);

    }

    private void openOldActivity(){
        Intent intent = new Intent(this,MainActivity_old.class);
        startActivity(intent);
    }

    static boolean isVisible(View view) {
        return (view.getVisibility() == View.VISIBLE);
    }


    private void initializeRenderScriptCaching() {
        Bitmap dummyBmp = ImageTools.bitmapCreate(10,10);
        for (Filter filter:filters) {
            if (filter.getFilterCategory() != FilterCategory.PRESET) {
                filter.preview(dummyBmp, getApplicationContext());
                Log.wtf("test", "test");
            }
        }
    }


    private int appGetFirstTimeRun() {
        //Check if App Start First Time
        SharedPreferences appPreferences = getSharedPreferences("MyAPP", 0);
        int appCurrentBuildVersion = BuildConfig.VERSION_CODE;
        int appLastBuildVersion = appPreferences.getInt("app_first_time", 0);

        if (appLastBuildVersion == appCurrentBuildVersion ) {
            return 1; //It has being used already.

        } else {
            appPreferences.edit().putInt("app_first_time",
                    appCurrentBuildVersion).apply();
            if (appLastBuildVersion == 0) {
                return 0; //Never used
            } else {
                return 2; //It has been used but not this version
            }
        }
    }



}

