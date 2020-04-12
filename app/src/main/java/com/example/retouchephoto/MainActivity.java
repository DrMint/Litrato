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
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

/*TODO:
   Bugs:
        [0001] - When the histogram is resize, the image can get stretch because the imageView gets bigger or smaller.
        Refreshing the image doesn't seem to work. I suspect this is because requestLayout is asynchronous, and
        when the image refresh, it utilizes the imageView's aspect ratio before it actually changed.
        Thus, refreshing the image will actually make the problem worse.
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

    static Filter subActivityFilter;
    static Bitmap subActivityBitmap;

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

    private final Boolean[] hasChanged = {true, true, true, true, true, true};

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

    private TableRow    toolsLineOne;
    private TableRow    toolsLineTwo;
    private TableRow    toolsLineThree;

    private Typeface submenuUnselected;
    private Typeface submenuSelected;

    private int numberOfTools;

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

        toolsLineOne            = findViewById(R.id.toolsLineOne);
        toolsLineTwo            = findViewById(R.id.toolsLineTwo);
        toolsLineThree          = findViewById(R.id.toolsLineThree);

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
            Bitmap result = FiltersActivity.activityBitmap;
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

            /*
            Snackbar sb = Snackbar.make(
                    layoutButtonOpen,
                    "Image resized to " + bmp.getWidth() + "px by " + bmp.getHeight() + "px",
                    Snackbar.LENGTH_SHORT);
            sb.show();
             */
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
        hasChanged[0] = true;
        hasChanged[2] = true;
        hasChanged[3] = true;
        hasChanged[4] = true;
        hasChanged[5] = true;
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
            }
        });

        presetsBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        filtersBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        toolsBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        contourBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        fancyBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        blurBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.invert(bmp, context);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Bleach Bypass");
        newPresets.setFilterCategory(FilterCategory.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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

        newTools = new Filter("Rotation");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.rotate));
        newTools.allowMasking = false;
        newTools.allowHistogram = false;
        newTools.setSeekBar1(-180, 0, 180, "deg");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                return FilterFunction.rotate(bmp, seekBar);
            }
        });
        filters.add(newTools);

        newTools = new Filter("Crop");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.crop));
        newTools.allowMasking = false;
        newTools.allowScrollZoom = false;
        newTools.allowHistogram = false;
        newTools.setSwitch1(false, "Keep ratio", "Free ratio");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                if (switch1) ImageTools.forceRectangleRatio(bmp, touchDown, touchUp);
                ImageTools.drawRectangle(bmp, touchDown, touchUp, Color.argb(Settings.CROP_OPACITY, 255,255,255));
                ImageTools.drawRectangle(bmp, touchDown, touchUp, Color.argb(Settings.CROP_OPACITY, 0,0,0), Settings.CROP_BORDER_SIZE);
                return null;
            }
        });
        newTools.setFilterApplyFunction(new FilterApplyInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                return FilterFunction.crop(bmp, touchDown, touchUp);
            }
        });
        filters.add(newTools);

        newTools = new Filter("Flip");
        newTools.needFilterActivity = false;
        newTools.allowMasking = false;
        newTools.allowHistogram = false;
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.flip));
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.mirror(bmp, context);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Stickers");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.allowMasking = false;
        newTools.allowHistogram = false;
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.stickers));
        filters.add(newTools);

        newTools = new Filter("Luminosity");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.luminosity));
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setSeekBar2(-100, 0, 100, "");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                if (seekBar <= 0) seekBar *= -seekBar;
                FilterFunction.brightness(bmp, context, seekBar * 2.55f);
                FilterFunction.gamma(bmp, context, seekBar2 / 100f + 1f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Contrast");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.contrast));
        newTools.setSeekBar1(-50, 0, 50, "%");
        newTools.setSeekBar2(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.constrastBurn(bmp, context, seekBar / 100f);
                FilterFunction.burnValues(bmp, context, seekBar2 / 50f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Sharpness");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.sharpness));
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.sharpen(bmp, context, seekBar / 200f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Auto");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.auto));
        newTools.setSwitch1(false, "Linear", "Dynamic");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.saturation));
        newTools.setSeekBar1(0, 100, 200, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.saturation(bmp, context,seekBar / 100f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Add noise");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.add_noise));
        newTools.setSeekBar1(0, 0, 255, "");
        newTools.setSwitch1(false,"B&W Noise", "Color Noise");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.noise(bmp, context, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Temperature");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.temperature));
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.temperature(bmp, context, seekBar / 10f);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Tint");
        newTools.setFilterCategory(FilterCategory.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.tint));
        newTools.setSeekBar1(-100, 0, 100, "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.hueShift(bmp, context,seekBar);
                return null;
            }
        });
        filters.add(newTools);

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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.colorize(bmp, context, colorSeekHue,0, false);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Selective coloring");
        newFilter.setFilterCategory(FilterCategory.COLOR);
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 25, 360, "deg");
        newFilter.setSwitch1(false, "Keep", "Remove");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
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
        if (isVisible(presetsBar) && hasChanged[0]) {
            generateMiniatures(FilterCategory.PRESET);
            hasChanged[0] = false;
        } else if (isVisible(toolsBar) && hasChanged[1]) {
            generateMiniatures(FilterCategory.TOOL);
            hasChanged[1] = false;
        } else if (isVisible(filtersBar)) {
            if (isVisible(colorBar) && hasChanged[2]) {
                generateMiniatures(FilterCategory.COLOR);
                hasChanged[2] = false;
            } else if (isVisible(fancyBar) && hasChanged[3]) {
                generateMiniatures(FilterCategory.FANCY);
                hasChanged[3] = false;
            } else if (isVisible(blurBar) && hasChanged[4]) {
                generateMiniatures(FilterCategory.BLUR);
                hasChanged[4] = false;
            } else if (isVisible(contourBar) && hasChanged[5]) {
                generateMiniatures(FilterCategory.CONTOUR);
                hasChanged[5] = false;
            }
        }
    }

    private void generateMiniatures(FilterCategory onlyThisCategory) {
        Bitmap resizedMiniature = ImageTools.toSquare(beforeLastFilterImage, Settings.MINIATURE_BMP_SIZE);

        for (DisplayedFilter displayedFilter:displayedFilters) {

            // Only generate the miniature if the displayedFilter of this category
            if (displayedFilter.filter.getFilterCategory() == onlyThisCategory) {

                if(onlyThisCategory == FilterCategory.TOOL) {

                    // Add the image on top of the text
                    Drawable drawable = new BitmapDrawable(getResources(), ImageTools.bitmapClone(displayedFilter.filter.getIcon()));
                    drawable.setBounds(0, 0, Settings.TOOL_DISPLAYED_SIZE, Settings.TOOL_DISPLAYED_SIZE);
                    displayedFilter.textView.setCompoundDrawablePadding(25);
                    displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
                } else {
                    Bitmap filteredMiniature =  ImageTools.bitmapClone(resizedMiniature);

                    // Apply the filter to the miniature
                    Bitmap result = displayedFilter.filter.apply(filteredMiniature, getApplicationContext());
                    if (result != null) filteredMiniature = result;

                    // Add the image on top of the text
                    Drawable drawable = new BitmapDrawable(getResources(), filteredMiniature);
                    drawable.setBounds(0, 0, Settings.MINIATURE_DISPLAYED_SIZE, Settings.MINIATURE_DISPLAYED_SIZE);
                    displayedFilter.textView.setCompoundDrawablePadding(25);
                    displayedFilter.textView.setCompoundDrawables(null, drawable, null, null);
                }
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
        if(filter.getFilterCategory()==FilterCategory.TOOL){
            TableRow.LayoutParams params = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT,4);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);
        }else{
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);
        }
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
                case TOOL: addToolsButton(textView); break;
            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentFilter.needFilterActivity) {
                        openFiltersActivity(currentFilter, beforeLastFilterImage);
                    } else {
                        apply(currentFilter);
                    }
                }
            });
            displayedFilters.add(new DisplayedFilter(textView, currentFilter));
        }
    }

    private void addToolsButton(TextView textView){
        switch (numberOfTools / 4){
            case 0: this.toolsLineOne.addView(textView); break;
            case 1: this.toolsLineTwo.addView(textView); break;
            case 2: this.toolsLineThree.addView(textView); break;
        }
        numberOfTools++;
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
        subActivityFilter = filter;
        subActivityBitmap = bmp;
        Intent intent = new Intent(getApplicationContext(), FiltersActivity.class);
        intent.putExtra(Settings.ACTIVITY_EXTRA_CALLER, this.getClass().getName());
        startActivityForResult(intent, FILTER_ACTIVITY_IS_FINISHED);
    }

    static boolean isVisible(View view) {
        return (view.getVisibility() == View.VISIBLE);
    }

    private void initializeRenderScriptCaching() {
        Bitmap dummyBmp = ImageTools.bitmapCreate(10,10);
        for (Filter filter:filters) {
            if (filter.getFilterCategory() != FilterCategory.PRESET) {
                filter.preview(dummyBmp, getApplicationContext());
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

