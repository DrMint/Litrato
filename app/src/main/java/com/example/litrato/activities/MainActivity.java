package com.example.litrato.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.litrato.BuildConfig;
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
     * A list of all filters, associated with their corresponding TextView (their visual
     * representation on the UI).
     */
    private final List<DisplayedFilter> displayedFilters = new ArrayList<>();

    /**
     * The object history is used to save all prior image state and revert to any of them when
     * the user so desire.
     */
    private History history = new History();


    private final Boolean[] hasChanged = {true, true, true, true, true, true};

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

    private Typeface submenuUnselected;
    private Typeface submenuSelected;

    private int numberOfTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(layoutToolbar);

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

        submenuSelected = colorButton.getTypeface();
        submenuUnselected = fancyButton.getTypeface();

        // If this is the first launch, compiles the RenderScript's functions
        if (appGetFirstTimeRun() == 0) {
            initializeRenderScriptCaching();
        }

        // Create the lists of filters and create renderscript object
        Filter.generateFilters(getApplicationContext());
        FilterFunction.initializeRenderScript(getApplicationContext());

        // Initialize all the different listeners.
        // The filters / tools / presets must already be ready
        initializeListener();

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
                    closeMenus();
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
        colorBar.setBackgroundColor(Settings.COLOR_SELECTED);
        fancyBar.setBackgroundColor(Settings.COLOR_SELECTED);
        blurBar.setBackgroundColor(Settings.COLOR_SELECTED);
        contourBar.setBackgroundColor(Settings.COLOR_SELECTED);
        toolsBar.setBackgroundColor(Settings.COLOR_SELECTED);
        presetsBar.setBackgroundColor(Settings.COLOR_SELECTED);
        filtersBar.setBackgroundColor(Settings.COLOR_SELECTED);
        buttonBar.setBackgroundColor(Settings.COLOR_SELECTED);

        presetsButton.setBackgroundColor(Settings.COLOR_GREY);
        toolsButton.setBackgroundColor(Settings.COLOR_GREY);
        filtersButton.setBackgroundColor(Settings.COLOR_GREY);

        presetsButton.setTextColor(Settings.COLOR_TEXT);
        toolsButton.setTextColor(Settings.COLOR_TEXT);
        filtersButton.setTextColor(Settings.COLOR_TEXT);

        presetsButton.setTypeface(submenuUnselected);
        toolsButton.setTypeface(submenuUnselected);
        filtersButton.setTypeface(submenuUnselected);

        colorButton.setTextColor(Settings.COLOR_TEXT);
        fancyButton.setTextColor(Settings.COLOR_TEXT);
        blurButton.setTextColor(Settings.COLOR_TEXT);
        contourButton.setTextColor(Settings.COLOR_TEXT);

        colorButton.setBackgroundColor(Settings.COLOR_SELECTED);
        fancyButton.setBackgroundColor(Settings.COLOR_SELECTED);
        blurButton.setBackgroundColor(Settings.COLOR_SELECTED);
        contourButton.setBackgroundColor(Settings.COLOR_SELECTED);

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

        for (DisplayedFilter displayedFilter:displayedFilters) {
            displayedFilter.textView.setTextColor(Settings.COLOR_TEXT);
            if (displayedFilter.filter.getFilterCategory() == Category.TOOL) {
                Drawable drawable = ImageTools.getThemedIcon(getApplicationContext(), displayedFilter.filter.getIcon());
                displayedFilter.textView.setCompoundDrawablePadding(25);
                displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
            }
        }

        layoutToolbar.getMenu().getItem(0).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.open));
        layoutToolbar.getMenu().getItem(1).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.history));
        layoutToolbar.getMenu().getItem(2).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.save));
        layoutToolbar.getMenu().getItem(4).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.rotateleft));
        layoutToolbar.getMenu().getItem(5).setIcon(ImageTools.getThemedIcon(getApplicationContext(), R.drawable.rotateright));
        layoutToolbar.setOverflowIcon(ImageTools.getThemedIcon(this, R.drawable.overflow));

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case CONFIG_REQUEST:
                hasChanged[0] = true;
                hasChanged[2] = true;
                hasChanged[3] = true;
                hasChanged[4] = true;
                hasChanged[5] = true;
                closeHistory();
                closeMenus();
                applyColorTheme();
                break;

            case PICK_IMAGE_REQUEST:

                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    setBitmap(FileInputOutput.getBitmap(data.getData()));
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
                    closeMenus();
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

        hasChanged[0] = true;
        hasChanged[2] = true;
        hasChanged[3] = true;
        hasChanged[4] = true;
        hasChanged[5] = true;

        generateMiniatureForOpenedMenu();
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
                closeMenus();
                if (ViewTools.isVisible(historyBar)) closeHistory();
                myScaleDetector.onTouchEvent(event);
                myGestureDetector.onTouchEvent(event);
                //layoutImageView.refresh();
                v.performClick();
                return true;
            }
        };
        layoutImageView.setOnTouchListener(defaultImageViewTouchListener);

        presetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = ViewTools.isVisible(presetsBar);
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
                boolean visible = ViewTools.isVisible(toolsBar);
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
                boolean visible = ViewTools.isVisible(filtersBar);
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


        // We leave those because if no onClickListener is set, there are permeable to touch events.
        // That means that clicking on the their background will trigger an event to the object behind.
        presetsBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
        filtersBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
        toolsBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
        contourBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
        fancyBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
        blurBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
        historyBar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
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


    /**
     * Refreshes/Generates the miniatures of the currently opened.
     * If no menu is opened, no image is generated.
     */
    private void generateMiniatureForOpenedMenu() {
        if (ViewTools.isVisible(presetsBar) && hasChanged[0]) {
            generateMiniatures(Category.PRESET);
            hasChanged[0] = false;
        } else if (ViewTools.isVisible(toolsBar) && hasChanged[1]) {
            generateMiniatures(Category.TOOL);
            hasChanged[1] = false;
        } else if (ViewTools.isVisible(filtersBar)) {
            if (ViewTools.isVisible(colorBar) && hasChanged[2]) {
                generateMiniatures(Category.COLOR);
                hasChanged[2] = false;
            } else if (ViewTools.isVisible(fancyBar) && hasChanged[3]) {
                generateMiniatures(Category.FANCY);
                hasChanged[3] = false;
            } else if (ViewTools.isVisible(blurBar) && hasChanged[4]) {
                generateMiniatures(Category.BLUR);
                hasChanged[4] = false;
            } else if (ViewTools.isVisible(contourBar) && hasChanged[5]) {
                generateMiniatures(Category.CONTOUR);
                hasChanged[5] = false;
            }
        }
    }

    private void generateMiniatures(Category onlyThisCategory) {
        Bitmap resizedMiniature = ImageTools.toSquare(
                currentImage,
                PreferenceManager.getInt(getApplicationContext(), Preference.MINIATURE_BMP_SIZE)
        );

        for (DisplayedFilter displayedFilter:displayedFilters) {

            // Only generate the miniature if the displayedFilter of this category
            if (displayedFilter.filter.getFilterCategory() == onlyThisCategory) {

                Drawable drawable;
                if(onlyThisCategory != Category.TOOL) {
                    Bitmap filteredMiniature =  ImageTools.bitmapClone(resizedMiniature);

                    // Apply the filter to the miniature
                    Bitmap result = displayedFilter.filter.apply(filteredMiniature, getApplicationContext());
                    if (result != null) filteredMiniature = result;

                    // Add the image on top of the text
                    drawable = new BitmapDrawable(getResources(), filteredMiniature);
                    drawable.setBounds(0, 0, Settings.MINIATURE_DISPLAYED_SIZE, Settings.MINIATURE_DISPLAYED_SIZE);

                    displayedFilter.textView.setCompoundDrawablePadding(25);
                    displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
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
        textView.setTextColor(Settings.COLOR_TEXT);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setBackgroundColor(Color.TRANSPARENT);

        if (filter.getFilterCategory() == Category.TOOL) {

            textView.setMaxWidth(Settings.TOOL_DISPLAYED_SIZE);
            textView.setHeight((int) (Settings.TOOL_DISPLAYED_SIZE * 1.8));
            TableRow.LayoutParams params = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT,4);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);

        } else {

            textView.setMaxWidth(Settings.MINIATURE_DISPLAYED_SIZE);
            textView.setHeight((int) (Settings.MINIATURE_DISPLAYED_SIZE * 1.4));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);

        }
        return textView;
    }

    private void initializeMenus(){

        TextView textView;

        for (final Filter currentFilter:Filter.filters) {
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
                        openFiltersActivity(currentFilter, currentImage);
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
        closeHistory();
    }

    private void closeHistory() {
        historyBar.setVisibility(View.GONE);
        if (historySeekBar.getProgress() != historySeekBar.getMax()) {
            historySeekBar.setProgress(historySeekBar.getMax());
            refreshImageView();
        }
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

    private void initializeRenderScriptCaching() {
        Bitmap dummyBmp = ImageTools.bitmapCreate(10,10);
        for (Filter filter:Filter.filters) {
            if (filter.getFilterCategory() != Category.PRESET) {
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