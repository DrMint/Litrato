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
import android.view.Window;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Objects;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.litrato.activities.ui.BottomMenu;
import com.example.litrato.activities.ui.DisplayedFilter;
import com.example.litrato.activities.tools.History;
import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.R;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.activities.ui.ImageViewZoomScroll;
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

/*TODO:
   Bugs:
        [0001] - If we switch from dark theme and white theme, the icon stay dark in Tools.
                 A restart solve the problem.
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

    /* We call subActivities, activities started by the current activity.
       Those subActivities needs values and object provided by the current activity.
       Which is why those static values exits. By convention, those values are reverted to null
       by the subActivity once it received them.
     */

    /**
     * The filter to use in subActivities.
     */
    static Filter subActivityFilter;

    /**
     * The image to use in subActivities.
     */
    static Bitmap subActivityBitmap;

    /**
     * The mask image to use in subActivities.
     */
    static Bitmap subActivityMaskBmp;

    public static Context ActivityContext;

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
    private History history = new History();

    private ImageViewZoomScroll layoutImageView;
    private Toolbar     layoutToolbar;
    private Button      toolsButton;
    private Button      presetsButton;
    private Button      filtersButton;
    private Button      colorButton;
    private Button      fancyButton;
    private Button      blurButton;
    private Button      contourButton;
    private TableLayout toolsBar;
    private HorizontalScrollView presetsBar;
    private HorizontalScrollView buttonBar;
    private LinearLayout filtersBar;
    private LinearLayout presetsLinearLayout;
    private LinearLayout colorBar;
    private LinearLayout fancyBar;
    private LinearLayout blurBar;
    private LinearLayout contourBar;

    private RelativeLayout historyBar;
    private TextView    historyTitle;
    private SeekBar     historySeekBar;
    private Button      historyConfirmButton;

    private TableRow    toolsLineOne;
    private TableRow    toolsLineTwo;
    private TableRow    toolsLineThree;

    private int numberOfTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(layoutToolbar);

        ActivityContext = getApplicationContext();

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));

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

        buttonBar               = findViewById(R.id.buttonBar);

        presetsLinearLayout     = findViewById(R.id.presetsLinearLayout);

        historyBar              = findViewById(R.id.historyBar);
        historyTitle            = findViewById(R.id.historyTitle);
        historySeekBar          = findViewById(R.id.historySeekBar);
        historyConfirmButton    = findViewById(R.id.historyConfirmButton);

        colorBar                = findViewById(R.id.colorMenu);
        fancyBar                = findViewById(R.id.fancyMenu);
        blurBar                 = findViewById(R.id.blurMenu);
        contourBar              = findViewById(R.id.contourMenu);

        toolsLineOne            = findViewById(R.id.toolsLineOne);
        toolsLineTwo            = findViewById(R.id.toolsLineTwo);
        toolsLineThree          = findViewById(R.id.toolsLineThree);



        FileInputOutput.askPermissionToReadWriteFiles(this);

        // Create the lists of filters and create renderscript object
        Filter.generateFilters(getApplicationContext());
        FilterFunction.initializeRenderScript(getApplicationContext());

        // Initialize all the different listeners.
        // The filters / tools / presets must already be ready
        initializeListener();

        new BottomMenu(presetsButton, presetsBar, Category.PRESET);
        new BottomMenu(toolsButton, toolsBar, Category.TOOL);
        BottomMenu myMenu = new BottomMenu(filtersButton, filtersBar, null);

        new BottomMenu(colorButton, colorBar, Category.COLOR, myMenu);
        new BottomMenu(fancyButton, fancyBar, Category.FANCY, myMenu);
        new BottomMenu(blurButton, blurBar, Category.BLUR, myMenu);
        new BottomMenu(contourButton, contourBar, Category.CONTOUR, myMenu);
        BottomMenu.closeMenus();

        BottomMenu.submenuSelected = colorButton.getTypeface();
        BottomMenu.submenuUnselected = fancyButton.getTypeface();

        // Selects the default image in the resource folder and set it
        setBitmap(FileInputOutput.getBitmap(getResources(), R.drawable.default_image));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        applyColorTheme();
        return true;
    }

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
                    BottomMenu.closeMenus();
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
                    Snackbar.make(toolsBar, getString(R.string.savingMessage), Snackbar.LENGTH_SHORT).show();
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
                        new PointPercentage()
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
                        new PointPercentage()
                );

                currentImage = lastUsedFilter.apply(currentImage, getApplicationContext());
                addToHistory(lastUsedFilter);
                refreshImageView();
                break;
            }

            case R.id.action_settings: {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, CONFIG_REQUEST);
                break;
            }

            case R.id.action_exif: {
                Intent intent = new Intent(getApplicationContext(), ExifActivity.class);
                startActivity(intent);
                break;
            }

        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_history) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            layoutImageView.setInternalValues();
            layoutImageView.setImageBitmap(currentImage);
            layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);
        }
    }


    private void applyColorTheme() {

        Settings.setColorTheme(PreferenceManager.getBoolean(getApplicationContext(), Preference.DARK_MODE));

        historyBar.setBackgroundColor(Settings.COLOR_GREY);
        historyTitle.setTextColor(Settings.COLOR_TEXT);

        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Settings.COLOR_BACKGROUND);
        window.getDecorView().setBackgroundColor(Settings.COLOR_BACKGROUND);

        if (!PreferenceManager.getBoolean(getApplicationContext(), Preference.DARK_MODE)) {
        //if (!MainActivity.preferences.getBoolean(Settings.PREFERENCE_DARK_MODE, Settings.DEFAULT_DARK_MODE)) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            layoutToolbar.setPopupTheme(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            layoutToolbar.setPopupTheme(View.SYSTEM_UI_FLAG_VISIBLE);
        }

        layoutToolbar.getMenu().getItem(0).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.open));
        layoutToolbar.getMenu().getItem(1).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.history));
        layoutToolbar.getMenu().getItem(2).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.save));
        layoutToolbar.getMenu().getItem(4).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.rotateleft));
        layoutToolbar.getMenu().getItem(5).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.rotateright));
        layoutToolbar.setOverflowIcon(ImageTools.getThemedIcon(this, R.drawable.overflow));

        BottomMenu.applyColorTheme();

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case CONFIG_REQUEST:

                BottomMenu.invalidateMiniatures();
                closeHistory();
                BottomMenu.closeMenus();
                applyColorTheme();
                break;

            case PICK_IMAGE_REQUEST:

                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    setBitmap(FileInputOutput.getBitmap(data.getData(), getApplicationContext()));
                    layoutToolbar.getMenu().getItem(7).setEnabled(true);
                } else {
                    int x = 0/0;
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
                    BottomMenu.closeMenus();
                }
                closeHistory();
                break;
        }
    }

    /**
     * Function called when a new image is loaded by the program.
     * @param bmp the image to load
     */
    private void setBitmap(Bitmap bmp) {

        // If the bmp is null, aborts
        if (bmp == null) return;

        int importedBmpSize = PreferenceManager.getInt(getApplicationContext(), Preference.IMPORTED_BMP_SIZE);

        // Resize the image before continuing, if necessary
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
     * Displays currentImage on the imageView, also refreshes Histogram and ImageInfo
     */
    private void refreshImageView() {

        layoutImageView.setImageBitmap(currentImage);
        BottomMenu.currentImage = currentImage;
        BottomMenu.invalidateMiniatures();
        //generateMiniatureForOpenedMenu();
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
                BottomMenu.closeMenus();
                if (ViewTools.isVisible(historyBar)) closeHistory();
                myScaleDetector.onTouchEvent(event);
                myGestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        };
        layoutImageView.setOnTouchListener(defaultImageViewTouchListener);

        initializeMenus();

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
                //refreshImageView();
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

    }

    /**
     * Applies a filter to beforeLastFilterImage and refreshes ImageView.
     * @param filter the filter to apply
     */
    private void apply(Filter filter) {

        AppliedFilter lastUsedFilter = new AppliedFilter(filter);
        Bitmap result = lastUsedFilter.apply(currentImage, getApplicationContext());
        // If the filter return a bitmap, currentImage becomes this bitmap
        if (result != null) {
            currentImage = result;
        }

        addToHistory(lastUsedFilter);
        refreshImageView();
    }

    private void addToHistory(AppliedFilter appliedFilter) {
        history.addFilter(appliedFilter);
        historySeekBar.setMax(history.size() - 1);
        historySeekBar.setProgress(historySeekBar.getMax());
    }


    private void initializeMenus(){

        TextView textView;

        for (final Filter currentFilter:Filter.filters) {
            textView = BottomMenu.generateATextView(currentFilter, getApplicationContext());

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
                        openFiltersActivity(currentFilter, currentImage);
                    } else {
                        apply(currentFilter);
                    }
                }
            });
            BottomMenu.displayedFilters.add(new DisplayedFilter(textView, currentFilter));
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

    private void closeHistory() {
        historyBar.setVisibility(View.GONE);
        if (historySeekBar.getProgress() != historySeekBar.getMax()) {
            historySeekBar.setProgress(historySeekBar.getMax());
            refreshImageView();
        }
    }

    private void openFiltersActivity(Filter filter, Bitmap bmp){
        subActivityFilter = filter;
        subActivityBitmap = bmp;
        Intent intent = new Intent(getApplicationContext(), FiltersActivity.class);
        intent.putExtra(Settings.ACTIVITY_EXTRA_CALLER, this.getClass().getName());
        startActivityForResult(intent, FILTER_ACTIVITY_IS_FINISHED);
    }
}