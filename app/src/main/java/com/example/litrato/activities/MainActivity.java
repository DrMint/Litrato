package com.example.litrato.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.TextView;

import com.example.litrato.activities.ui.BottomMenu;
import com.example.litrato.activities.tools.History;
import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.R;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.activities.ui.ColorTheme;
import com.example.litrato.activities.ui.ImageViewZoomScroll;
import com.example.litrato.activities.ui.MenuType;
import com.example.litrato.activities.ui.ViewTools;
import com.example.litrato.filters.AppliedFilter;
import com.example.litrato.filters.Filter;
import com.example.litrato.filters.Category;
import com.example.litrato.filters.FilterFunction;
import com.example.litrato.tools.FileInputOutput;
import com.example.litrato.tools.ImageTools;
import com.example.litrato.tools.Point;
import com.example.litrato.tools.PointPercentage;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/*TODO:
   Bugs:
        [0001] - If we switch from dark theme and white theme, the icon stay dark in Tools.
                 A restart solve the problem.
        -------------------------------------------------------------------------------------------
    New functions:
 */

/**
 * This apps is an image processing app for android.
 * It can load an image, apply some filter and, eventually, it will be able to save that image.
 * Please read the README file for more information.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class MainActivity extends AppCompatActivity {

    /* We call subActivities, activities started by the current activity.
       Those subActivities needs values and object provided by the current activity.
       Which is why those static values exits. By convention, those values are reverted to null
       by the subActivity once it received them.
     */

    /**
     * The filter to use in subActivities.
     */
    public static Filter subActivityFilter;

    /**
     * The image to use in subActivities.
     */
    static Bitmap subActivityBitmap;

    /**
     * The mask image to use in subActivities.
     */
    static Bitmap subActivityMaskBmp;

    /**
     * We shouldn't do this, at least that's what the Android Documentation and IDE seems to
     * indicate. Right now, only BottomMenu uses this value.
     */
    private static Context appContext;

    // Numerous set values for REQUESTS. They have to differ from one another that's all.
    private final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private final int FILTER_ACTIVITY_IS_FINISHED = 3;
    private final int CONFIG_REQUEST = 4;

    /**
     * This is the image as it was before applying any filter.
     * The image has been resized if necessary which means it's not quite the "original image".
     * To get the real original image, you should use FileInputOutput.getLastImportedImagePath().
     */
    private Bitmap originalImage;

    /**
     * This is the current state of the image, the one presented on the ImageView (unless you're
     * in the history). This is the image filter are applied to.
     */
    private Bitmap currentImage;

    /**
     * The object history is used to save all prior image state and revert to any of them when
     * the user so desire.
     */
    private final History history = new History();


    private final List<BottomMenu> bottomMenus = new ArrayList<>();

    private ImageViewZoomScroll layoutImageView;
    private Toolbar     layoutToolbar;

    private ViewGroup   historyBar;
    private TextView    historyTitle;
    private SeekBar     historySeekBar;
    private Button      historyConfirmButton;

    /**
     * This is a listener used by menuItem in BottomMenu.
     */
    static private View.OnClickListener menuItemListener;
    static private View.OnClickListener menuButtonListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        Settings.setDPValuesInPixel(getAppContext());

        layoutToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(layoutToolbar);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));

        ViewGroup presetsLinearLayout = findViewById(R.id.presetsLinearLayout);

        historyBar              = findViewById(R.id.historyBar);
        historyTitle            = findViewById(R.id.historyTitle);
        historySeekBar          = findViewById(R.id.historySeekBar);
        historyConfirmButton    = findViewById(R.id.historyConfirmButton);

        ViewGroup colorBar = findViewById(R.id.colorMenu);
        ViewGroup fancyBar = findViewById(R.id.fancyMenu);
        ViewGroup blurBar = findViewById(R.id.blurMenu);
        ViewGroup contourBar = findViewById(R.id.contourMenu);


        menuButtonListener = new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                BottomMenu clickedOne = BottomMenu.getMenuByItsButton(bottomMenus, (Button) v);

                // If we clicked on a opened menu button
                if (clickedOne.isOpened()) {
                    // If it's not a submenu, close the menu
                    if (clickedOne.parent == null) clickedOne.close();
                } else {
                    // Close everything
                    BottomMenu.closeMenus(bottomMenus);

                    // Open just the one we want
                    clickedOne.open();

                    // If its a parent, opens its first child.
                    for (BottomMenu bottomMenu:bottomMenus) {
                        if (bottomMenu.parent == clickedOne) {
                            bottomMenu.open();
                            break;
                        }
                    }

                }
            }
        };


        menuItemListener = new View.OnClickListener()  {

            @Override
            public void onClick(View v) {

                if (subActivityFilter.needFilterActivity) {

                    subActivityBitmap = currentImage;
                    Intent intent = new Intent(getApplicationContext(), FiltersActivity.class);
                    intent.putExtra(Settings.ACTIVITY_EXTRA_CALLER, this.getClass().getName());
                    startActivityForResult(intent, FILTER_ACTIVITY_IS_FINISHED);

                } else {

                    AppliedFilter lastUsedFilter = new AppliedFilter(subActivityFilter);
                    Bitmap result = lastUsedFilter.apply(currentImage, getApplicationContext());
                    // If the filter return a bitmap, currentImage becomes this bitmap
                    if (result != null) {
                        currentImage = result;
                    }

                    addToHistory(lastUsedFilter);
                    refreshImageView();
                }
            }
        };

        FileInputOutput.askPermissionToReadWriteFiles(this);

        // Create the lists of filters and create renderscript object
        Filter.generateFilters(getApplicationContext());
        FilterFunction.initializeRenderScript(getApplicationContext());




        // Menu creation
        {
            Button toolsButton = findViewById(R.id.buttonTools);
            Button presetsButton = findViewById(R.id.buttonPresets);
            Button filtersButton = findViewById(R.id.buttonFilters);

            Button colorButton = findViewById(R.id.buttonColor);
            Button fancyButton = findViewById(R.id.buttonFancy);
            Button blurButton = findViewById(R.id.buttonBlur);
            Button contourButton = findViewById(R.id.buttonContour);

            ViewGroup toolsBar = findViewById(R.id.toolsBar);
            ViewGroup presetsBar = findViewById(R.id.presetsBar);
            ViewGroup filtersBar = findViewById(R.id.filtersBar);

            BottomMenu.selected = colorButton.getTypeface();
            BottomMenu.unselected = fancyButton.getTypeface();

            bottomMenus.add(new BottomMenu(presetsButton, presetsBar, presetsLinearLayout, Category.PRESET, MenuType.MINIATURE, null));
            BottomMenu menuTools = new BottomMenu(toolsButton, toolsBar, Category.TOOL, MenuType.TOOLS, null);
            BottomMenu menuFilters = new BottomMenu(filtersButton, filtersBar, null, MenuType.PARENT, null);
            bottomMenus.add(menuTools);
            bottomMenus.add(menuFilters);

            {
                ViewGroup toolsLineOne = findViewById(R.id.toolsLineOne);
                ViewGroup toolsLineTwo = findViewById(R.id.toolsLineTwo);
                ViewGroup toolsLineThree = findViewById(R.id.toolsLineThree);
                menuTools.setToolsRows(toolsLineOne, toolsLineTwo, toolsLineThree);
            }

            bottomMenus.add(new BottomMenu(colorButton, colorBar, Category.COLOR, MenuType.MINIATURE, menuFilters));
            bottomMenus.add(new BottomMenu(fancyButton, fancyBar, Category.FANCY, MenuType.MINIATURE, menuFilters));
            bottomMenus.add(new BottomMenu(blurButton, blurBar, Category.BLUR, MenuType.MINIATURE, menuFilters));
            bottomMenus.add(new BottomMenu(contourButton, contourBar, Category.CONTOUR, MenuType.MINIATURE, menuFilters));
        }

        for (BottomMenu bottomMenu:bottomMenus) bottomMenu.initialize(Filter.filters);



        // Initialize all the different listeners.
        initializeListener();

        // Selects the default image in the resource folder and set it
        setBitmap(FileInputOutput.getBitmap(getResources(), R.drawable.img_default));



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        applyColorTheme();
        return true;
    }

    /**
     * This is the actions done when clicking on a element on the Toolbar.
     * @param item the element clicked.
     * @return whatever super is returning.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_history: {
                if (ViewTools.isVisible(historyBar)) {
                    closeHistory();
                } else {
                    BottomMenu.closeMenus(bottomMenus);
                    historyBar.setVisibility(View.VISIBLE);
                }
                break;
            }

            case R.id.action_open: {
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
                break;
            }
            case R.id.action_save: {
                Bitmap saveImage = currentImage;
                if (PreferenceManager.getBoolean(getApplicationContext(), Preference.SAVE_ORIGINAL_RESOLUTION) && FileInputOutput.getLastImportedImagePath() != null) {
                    Bitmap fullSize = FileInputOutput.getBitmap(FileInputOutput.getLastImportedImagePath());
                    fullSize = history.goUntilFilter(fullSize, history.size() - 1, getApplicationContext());
                    saveImage = fullSize;
                }
                if (FileInputOutput.saveImageToGallery(saveImage, MainActivity.this)) {
                    Snackbar.make(layoutToolbar, getString(R.string.savingMessage), Snackbar.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.action_rotate_left: {

                AppliedFilter lastUsedFilter =  new AppliedFilter(
                        Filter.getFilterByName(Settings.FILTER_ROTATION),
                        null,
                        0,
                        270,
                        0,
                        false,
                        new PointPercentage(),
                        new PointPercentage(),
                        0
                );

                currentImage = lastUsedFilter.apply(currentImage, getApplicationContext());
                addToHistory(lastUsedFilter);
                refreshImageView();
                break;
            }

            case R.id.action_rotate_right: {

                AppliedFilter lastUsedFilter =  new AppliedFilter(
                        Filter.getFilterByName(Settings.FILTER_ROTATION),
                        null,
                        0,
                        90,
                        0,
                        false,
                        new PointPercentage(),
                        new PointPercentage(),
                        0
                );

                currentImage = lastUsedFilter.apply(currentImage, getApplicationContext());
                addToHistory(lastUsedFilter);
                refreshImageView();
                break;
            }

            case R.id.action_settings: {
                Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivityForResult(intent, CONFIG_REQUEST);
                break;
            }

            case R.id.action_exif: {
                Intent intent = new Intent(getApplicationContext(), ExifActivity.class);
                startActivity(intent);
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get called when the interface is properly loaded.
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            layoutImageView.setInternalValues();
            layoutImageView.setImageBitmap(currentImage);
            layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);
        }
    }


    private void applyColorTheme() {
        ColorTheme.setColorTheme(getAppContext());
        ColorTheme.window(getAppContext(), getWindow());

        ColorTheme.toolBar(getAppContext(), layoutToolbar, getSupportActionBar());

        ColorTheme.background(historyBar, false);
        ColorTheme.textView(historyTitle);

        ColorTheme.bottomMenu(bottomMenus);
    }

    /**
     * This is how the Activity reacts to REQUESTS result.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case CONFIG_REQUEST:

                BottomMenu.invalidateMiniatures(bottomMenus);
                BottomMenu.closeMenus(bottomMenus);
                closeHistory();
                applyColorTheme();
                break;

            case PICK_IMAGE_REQUEST:

                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    setBitmap(FileInputOutput.getBitmap(data.getData(), getApplicationContext()));
                    layoutToolbar.getMenu().getItem(7).setEnabled(true);
                }
                break;

            case REQUEST_IMAGE_CAPTURE:
                //Load the last taken photo.
                setBitmap(FileInputOutput.getLastTakenBitmap());
                layoutToolbar.getMenu().getItem(7).setEnabled(true);
                break;

            case FILTER_ACTIVITY_IS_FINISHED:
                Bitmap result = FiltersActivity.activityBitmap;
                if (result != null) {
                    layoutImageView.reset();
                    currentImage = ImageTools.bitmapClone(result);
                    addToHistory(FiltersActivity.activityAppliedFilter);
                    refreshImageView();
                    BottomMenu.closeMenus(bottomMenus);
                }
                closeHistory();
                break;
        }
    }

    /**
     * Function called when a new image is loaded by the program.
     * From the camera or the gallery.
     * @param bmp the image to load
     */
    private void setBitmap(Bitmap bmp) {

        // If the bmp is null, aborts
        if (bmp == null) return;

        // Resize the image before continuing, if necessary
        int importedBmpSize = PreferenceManager.getInt(getApplicationContext(), Preference.IMPORTED_BMP_SIZE);

        if (bmp.getHeight() > importedBmpSize || bmp.getWidth() > importedBmpSize) {
            bmp = ImageTools.scaleToBeContainedInSquare(bmp, importedBmpSize);
        }

        // Set this image as the originalImage and reset the UI
        originalImage = bmp;
        currentImage = ImageTools.bitmapClone(originalImage);

        history.clear();
        addToHistory(new AppliedFilter(new Filter("Original", Category.SPECIAL)));
        refreshImageView();
    }

    /**
     * Displays currentImage on the imageView
     */
    private void refreshImageView() {
        layoutImageView.setImageBitmap(currentImage);
        BottomMenu.currentImage = currentImage;
        BottomMenu.invalidateMiniatures(bottomMenus);
    }

    private void initializeListener() {

        // Create the GestureDetector which handles the scrolling and double tap.
        final GestureDetector myGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                layoutImageView.translate((int) (distanceX / layoutImageView.getZoom()), (int) (distanceY / layoutImageView.getZoom()));
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

                // it it's zoomed
                if (layoutImageView.hasVerticalScroll() || layoutImageView.hasHorizontalScroll()) {
                //if (layoutImageView.getZoom() != 1f) {
                    layoutImageView.reset();
                } else {
                    Point touch = layoutImageView.imageViewTouchPointToBmpCoordinates(new Point(e.getX(), e.getY()));
                    layoutImageView.setZoom(layoutImageView.getZoom() * Settings.DOUBLE_TAP_ZOOM);
                    //layoutImageView.setZoom(Settings.DOUBLE_TAP_ZOOM);
                    layoutImageView.setCenter(touch);
                }
                return true;
            }

            // Not used
            public boolean onSingleTapConfirmed(MotionEvent e) {return false;}
            public boolean onDoubleTapEvent(MotionEvent e) {return false;}

        });

        // Create the ScaleGestureDetector which handles the scaling.
        final ScaleGestureDetector myScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            float lastZoomFactor;

            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastZoomFactor = layoutImageView.getZoom();
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                layoutImageView.setZoom(lastZoomFactor * detector.getScaleFactor());
                return false;
            }

            //Not used
            public void onScaleEnd(ScaleGestureDetector detector) {}
        });

        // The default behavior of imageView.
        final View.OnTouchListener defaultImageViewTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                BottomMenu.closeMenus(bottomMenus);
                if (ViewTools.isVisible(historyBar)) closeHistory();
                myScaleDetector.onTouchEvent(event);
                myGestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        };
        layoutImageView.setOnTouchListener(defaultImageViewTouchListener);

        historySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                historyConfirmButton.setEnabled(progress != seekBar.getMax());
                historyTitle.setText(history.get(progress).getName());
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() != seekBar.getMax()) {
                    Bitmap tmp = history.goUntilFilter(originalImage, historySeekBar.getProgress(), getApplicationContext());
                    layoutImageView.setImageBitmap(tmp);
                }
                else{
                    layoutImageView.setImageBitmap(currentImage);
                }
            }
        });

        historyConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImage = history.goUntilFilter(originalImage, historySeekBar.getProgress(), getApplicationContext());
                history.removeUntil(historySeekBar.getProgress());
                historySeekBar.setMax(history.size() - 1);
                closeHistory();
                refreshImageView();
            }
        });

        // We leave those because if no onClickListener is set, there are permeable to touch events.
        // That means that clicking on the their background will trigger an event to the object behind.
        historyBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
    }

    /**
     * Add a appliedFilter to history, and move the history's seekbar accordingly.
     * @param appliedFilter the appliedFilter to add.
     */
    private void addToHistory(AppliedFilter appliedFilter) {
        history.addFilter(appliedFilter);
        historySeekBar.setMax(history.size() - 1);
        historySeekBar.setProgress(historySeekBar.getMax());
    }

    /**
     * Close the history menu.
     */
    private void closeHistory() {
        historyBar.setVisibility(View.GONE);
        if (historySeekBar.getProgress() != historySeekBar.getMax()) {
            historySeekBar.setProgress(historySeekBar.getMax());
            refreshImageView();
        }
    }

    public static Context getAppContext() {return appContext;}
    public static View.OnClickListener getMenuItemListener() {return menuItemListener;}
    public static View.OnClickListener getMenuButtonListener() {return menuButtonListener;}
}