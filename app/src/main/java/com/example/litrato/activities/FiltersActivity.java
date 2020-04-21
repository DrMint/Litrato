package com.example.litrato.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.activities.ui.ColorTheme;
import com.example.litrato.activities.ui.ImageViewZoomScroll;
import com.example.litrato.R;
import com.example.litrato.activities.ui.ViewTools;
import com.example.litrato.filters.AppliedFilter;
import com.example.litrato.filters.BlendType;
import com.example.litrato.filters.Filter;
import com.example.litrato.filters.FilterFunction;
import com.example.litrato.tools.ImageTools;
import com.example.litrato.tools.Point;
import com.example.litrato.tools.PointPercentage;

import java.util.Locale;
import java.util.Objects;

/**
 * An activity used to prompt the user to tweak the filter parameter.
 * This activity can also start a new instance of itself, most notably to create a mask.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class FiltersActivity extends AppCompatActivity {

    /* We call subActivities, activities started by the current activity.
       Those subActivities needs values and object provided by the current activity.
       Which is why those static values exits. By convention, those values are reverted to null
       by the subActivity once it received them.
     */

    /**
     * This is the AppliedFilter returned to this activity Caller.
     * This a object representing the action done on the image.
     */
    static AppliedFilter activityAppliedFilter;

    /**
     * This is the Bitmap returned to this activity Caller.
     * This is the image after applying the filter.
     */
    static Bitmap activityBitmap;

    /**
     * This is the Filter given to the called Activity.
     * FilterActivity calls a new instance of itself to create a mask.
     */
    private static Filter subActivityFilter;

    /**
     * This is the Bitmap given to the called Activity.
     * This is usually originalImage.
     */
    private static Bitmap subActivityBitmap;

    /**
     * This is the Mask given to the called Activity.
     * This is usually maskBmp.
     */
    private static Bitmap subActivityMaskBmp;

    /**
     * The image as it was before applying any filter.
     */
    private Bitmap originalImage;

    /**
     * The current image displayed on the ImageView
     */
    private Bitmap filteredImage;

    /**
     * The mask used to only apply the filter to a part of the image.
     * A mask is a black and white image, black means that the image won't be applied, and
     * white is where the image is applied.
     */
    private Bitmap maskBmp;

    /**
     * This is the originalImage masked by the inverse mask of maskBmp.
     */
    private Bitmap originalImageMasked;

    /**
     * The filter used in this FilterActivity.
     */
    private Filter selectedFilter;

    /**
     * A value used to know when the subActivity is finished.
     */
    private final int GET_MASK_IMAGE = 4;

    /**
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;

    /**
     * Is true if the user is using pick-a-color.
     */
    private boolean isUsingPick = false;

    /**
     * This value is false by default.
     * If it's true, the filter is only apply to the masked part of the image.
     */
    private boolean shouldUseMask = false;

    private ImageViewZoomScroll layoutImageView;
    private ImageButton layoutButtonApply;
    private ImageButton layoutCancel;
    private Button      layoutFilterMenuButton;
    private ImageButton layoutPickButton;
    private ImageButton layoutMaskButton;
    private ImageButton layoutHistogramButton;
    private ImageView   layoutHistogramView;
    private SeekBar     layoutSeekBar1;
    private SeekBar     layoutSeekBar2;
    private SeekBar     layoutColorSeekBar;
    private TextView    layoutSeekBarValue1;
    private TextView    layoutSeekBarValue2;
    private TextView    layoutSwitchValue1;
    private Switch      layoutSwitch1;
    private RelativeLayout filterMenu;
    private LinearLayout stickersMenu;

    /**
     * Where the user started touching ImageView.
     */
    private Point imageTouchDown;

    /**
     * Where the user last touched ImageView.
     */
    private Point imageTouchCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));
        layoutButtonApply       = findViewById(R.id.applyButton);
        layoutCancel            = findViewById(R.id.cancelButton);
        layoutFilterMenuButton  = findViewById(R.id.filterNameButton);
        layoutPickButton        = findViewById(R.id.pickButton);
        layoutMaskButton        = findViewById(R.id.maskButton);
        layoutHistogramButton   = findViewById(R.id.histogramButton);
        layoutHistogramView     = findViewById(R.id.histogramView);
        layoutSeekBar1          = findViewById(R.id.seekBar1);
        layoutSeekBar2          = findViewById(R.id.seekBar2);
        layoutColorSeekBar      = findViewById(R.id.colorSeekBar);
        layoutSeekBarValue1     = findViewById(R.id.seekBarValue1);
        layoutSeekBarValue2     = findViewById(R.id.seekBarValue2);
        layoutSwitch1           = findViewById(R.id.switch1);
        layoutSwitchValue1      = findViewById(R.id.switchValue1);
        filterMenu              = findViewById(R.id.filtersMenu);
        stickersMenu            = findViewById(R.id.itemMenu);

        // Gets the resources from the caller activity. If something crucial is missing aborts.
        // Once resources have been gathered, they are deleted from they original location to avoid
        // unwanted behavior.
        if (Objects.requireNonNull(getCallingActivity()).getClassName().equals(MainActivity.class.getName())) {

            if (MainActivity.subActivityBitmap != null) {
                setBitmap(MainActivity.subActivityBitmap);
                MainActivity.subActivityBitmap = null;
            } else {
                layoutCancel.performClick();
            }

            if (MainActivity.subActivityFilter != null) {
                selectedFilter = MainActivity.subActivityFilter;
                MainActivity.subActivityFilter = null;
            } else {
                layoutCancel.performClick();
            }

            generateMasks(MainActivity.subActivityMaskBmp);
            MainActivity.subActivityMaskBmp = null;

        } else if (getCallingActivity().getClassName().equals(FiltersActivity.class.getName())) {

            if (FiltersActivity.subActivityBitmap != null) {
                setBitmap(FiltersActivity.subActivityBitmap);
                FiltersActivity.subActivityBitmap = null;
            } else {
                layoutCancel.performClick();
            }

            if (FiltersActivity.subActivityFilter != null) {
                selectedFilter = FiltersActivity.subActivityFilter;
                FiltersActivity.subActivityFilter = null;
            } else {
                layoutCancel.performClick();
            }

            generateMasks(FiltersActivity.subActivityMaskBmp);
            FiltersActivity.subActivityMaskBmp = null;

        }

        layoutFilterMenuButton.setText(selectedFilter.getName());
        layoutHistogramView.setVisibility(View.GONE);

        // Initialize all the different listeners, the interface and the masks
        initializeListener();
        initializeInterface();

        applyColorTheme();

        previewFilter();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            layoutImageView.setInternalValues();
            layoutImageView.setImageBitmap(filteredImage);
            layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);
        }
    }


    private void applyColorTheme() {
        ColorTheme.setColorTheme(getApplicationContext());
        ColorTheme.window(getApplicationContext(), getWindow());

        ColorTheme.background(filterMenu, true);
        ColorTheme.background(stickersMenu,true);
        ColorTheme.background(layoutButtonApply, false);
        ColorTheme.background(layoutCancel, false);

        ColorTheme.textView(layoutSwitchValue1);
        ColorTheme.textView(layoutSeekBarValue1);
        ColorTheme.textView(layoutSeekBarValue2);

        ColorTheme.button(layoutFilterMenuButton, true);

        ColorTheme.switchL(layoutSwitch1);

        ColorTheme.seekBar(layoutSeekBar1);
        ColorTheme.seekBar(layoutSeekBar2);

        ColorTheme.icon(getApplicationContext(), layoutPickButton, R.drawable.icon_pick);
        ColorTheme.icon(getApplicationContext(), layoutMaskButton, R.drawable.icon_mask);
        ColorTheme.icon(getApplicationContext(), layoutHistogramButton, R.drawable.icon_histogram);
        ColorTheme.icon(getApplicationContext(), layoutCancel, R.drawable.icon_cancel);
        ColorTheme.icon(getApplicationContext(), layoutButtonApply, R.drawable.icon_valid);
    }


    /**
     * The mask are only generated when the mask is changed or loaded.
     * If no mask is provided, creates a completely black mask.
     * @param bmp the mask.
     */
    private void generateMasks(Bitmap bmp) {

        if (bmp == null) {
            bmp = ImageTools.bitmapClone(originalImage);
            ImageTools.fillWithColor(bmp, Color.BLACK);
        }

        maskBmp = bmp;

        Bitmap invertedMaskBmp = ImageTools.bitmapClone(maskBmp);
        FilterFunction.invert(invertedMaskBmp);

        originalImageMasked = ImageTools.bitmapClone(originalImage);
        FilterFunction.applyTexture(originalImageMasked, invertedMaskBmp, BlendType.MULTIPLY);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_MASK_IMAGE) {
            Bitmap result = FiltersActivity.activityBitmap;
            if (result != null) {
                shouldUseMask = true;
                generateMasks(result);
                previewFilter();
            }
        }
    }

    /**
     * Makes sure the the interface correspond to what the filter demands.
     */
    private void initializeInterface() {
        inputsReady = false;

        if (selectedFilter.allowFilterMenu) {

            // We make everything GONE
            layoutColorSeekBar.setVisibility(View.GONE);
            layoutMaskButton.setVisibility(View.GONE);
            layoutHistogramButton.setVisibility(View.GONE);
            layoutSeekBar1.setVisibility(View.GONE);
            layoutSeekBar2.setVisibility(View.GONE);
            layoutSwitch1.setVisibility(View.GONE);
            layoutSwitchValue1.setVisibility(View.GONE);
            stickersMenu.setVisibility(View.GONE);


            // And add anything we need.
            if (selectedFilter.colorSeekBar) layoutColorSeekBar.setVisibility(View.VISIBLE);

            if (selectedFilter.allowMasking) layoutMaskButton.setVisibility(View.VISIBLE);
            if (selectedFilter.allowHistogram) {
                layoutHistogramButton.setVisibility(View.VISIBLE);
                if (PreferenceManager.getBoolean(getApplicationContext(), Preference.OPEN_HISTOGRAM_BY_DEFAULT)) {
                    layoutHistogramButton.performClick();
                }
            }

            if (selectedFilter.seekBar1) {
                layoutSeekBar1.setVisibility(View.VISIBLE);
                layoutSeekBar1.setMin(selectedFilter.seekBar1Min);
                layoutSeekBar1.setMax(selectedFilter.seekBar1Max);
                layoutSeekBar1.setProgress(selectedFilter.seekBar1Set);
            }

            if (selectedFilter.seekBar2) {
                layoutSeekBar2.setVisibility(View.VISIBLE);
                layoutSeekBar2.setMin(selectedFilter.seekBar2Min);
                layoutSeekBar2.setMax(selectedFilter.seekBar2Max);
                layoutSeekBar2.setProgress(selectedFilter.seekBar2Set);
            }

            if (selectedFilter.switch1) {
                layoutSwitch1.setVisibility(View.VISIBLE);
                layoutSwitch1.setChecked(selectedFilter.switch1Default);

                if (layoutSwitch1.isChecked()) {
                    layoutSwitchValue1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitchValue1.setText(selectedFilter.switch1UnitFalse);
                }
            }

            // Only shows the seekBarValues when the seekBars are visible.
            layoutSeekBarValue1.setVisibility(layoutSeekBar1.getVisibility());
            layoutSeekBarValue2.setVisibility(layoutSeekBar2.getVisibility());
            layoutSwitchValue1.setVisibility(layoutSwitch1.getVisibility());

            // Only shows the pick tool if there is a seekBar
            layoutPickButton.setVisibility(layoutColorSeekBar.getVisibility());

        } else {

            filterMenu.setVisibility(View.GONE);
            ColorTheme.button(layoutFilterMenuButton, false);

        }

        // The seek bars listener can be triggered again.
        inputsReady = true;

        // Call the listeners of layout object for them to refresh.
        layoutSeekBarValue1.setText(layoutSeekBarValue1.getText());
        layoutSeekBarValue2.setText(layoutSeekBarValue2.getText());
        layoutSwitch1.setChecked(layoutSwitch1.isChecked());
    }



    private void setBitmap(Bitmap bmp) {

        // If the bmp is null, aborts
        if (bmp == null) return;

        // Set this image as the originalImage and reset the UI
        originalImage = bmp;
        filteredImage = ImageTools.bitmapClone(originalImage);
        refreshImageView();
    }

    private void previewOrApply(boolean apply) {

        // filteredImage is now a fresh copy of originalImage
        filteredImage = ImageTools.bitmapClone(originalImage);

        Bitmap result;
        if (apply) {
            result = selectedFilter.apply(
                    filteredImage,
                    maskBmp,
                    getApplicationContext(),
                    layoutColorSeekBar.getProgress(),
                    layoutSeekBar1.getProgress(),
                    layoutSeekBar2.getProgress(),
                    layoutSwitch1.isChecked(),
                    imageTouchDown,
                    imageTouchCurrent
            );
        } else {
            result = selectedFilter.preview(
                    filteredImage,
                    maskBmp,
                    getApplicationContext(),
                    layoutColorSeekBar.getProgress(),
                    layoutSeekBar1.getProgress(),
                    layoutSeekBar2.getProgress(),
                    layoutSwitch1.isChecked(),
                    imageTouchDown,
                    imageTouchCurrent
            );
        }

        // If the filter return a bitmap, filteredImage becomes this bitmap
        if (result != null) {
            filteredImage = result;
        }

        // Keep the filtered part only where the maskBmp is white.
        if (shouldUseMask) {
            FilterFunction.applyTexture(filteredImage, maskBmp,BlendType.MULTIPLY);
            FilterFunction.applyTexture(filteredImage, originalImageMasked, BlendType.ADD);
        }

        // Refresh the image viewer and the histogram.
        if (!apply) refreshImageView();
    }

    private void applyFilter() {
        previewOrApply(true);
    }

    private void previewFilter() {
        previewOrApply(false);
    }

    /**
     * Displays filteredImage on the imageView, also refreshes Histogram.
     */
    private void refreshImageView() {
        layoutImageView.setImageBitmap(filteredImage);
        refreshHistogram();
    }

    /**
     * Display the histogram of filteredImage on layoutHistogram
     */
    private void refreshHistogram() {
        if(ViewTools.isVisible(layoutHistogramView)) {
            layoutHistogramView.setImageBitmap(ImageTools.generateHistogram(filteredImage));
        }
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

        final View.OnTouchListener defaultImageViewListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (selectedFilter.allowScrollZoom) {
                    myScaleDetector.onTouchEvent(event);
                    myGestureDetector.onTouchEvent(event);
                    //layoutImageView.refresh();

                } else {

                    if (imageTouchDown == null) imageTouchDown = new Point();
                    if (imageTouchCurrent == null) imageTouchCurrent = new Point();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            imageTouchDown.x = (int) event.getX();
                            imageTouchDown.y = (int) event.getY();
                            imageTouchDown.set(layoutImageView.imageViewTouchPointToBmpCoordinates(imageTouchDown));
                            layoutImageView.sanitizeBmpCoordinates(imageTouchDown);
                        }
                        case MotionEvent.ACTION_MOVE: {
                            imageTouchCurrent.x = (int) event.getX();
                            imageTouchCurrent.y = (int) event.getY();
                            imageTouchCurrent.set(layoutImageView.imageViewTouchPointToBmpCoordinates(imageTouchCurrent));
                            layoutImageView.sanitizeBmpCoordinates(imageTouchCurrent);
                            break;
                        }
                        case MotionEvent.ACTION_UP: break;
                    }

                    previewFilter();

                }

                v.performClick();
                return true;
            }
        };
        layoutImageView.setOnTouchListener(defaultImageViewListener);

        layoutFilterMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedFilter.allowFilterMenu) {
                    if (!ViewTools.isVisible(filterMenu)) {
                        ColorTheme.button(layoutFilterMenuButton, true);
                        filterMenu.setVisibility(View.VISIBLE);
                    } else {
                        ColorTheme.button(layoutFilterMenuButton, false);
                        filterMenu.setVisibility(View.GONE);
                    }
                }


            }
        });

        // Adds listener for the first seek bar
        layoutSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady && selectedFilter.seekBar1AutoRefresh) previewFilter();
                layoutSeekBarValue1.setText(String.format(
                        Locale.ENGLISH,
                        "%s (%d%s)",
                        selectedFilter.seekBar1Title,
                        seekBar.getProgress(),
                        selectedFilter.seekBar1Unit
                ));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the second seek bar
        layoutSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady && selectedFilter.seekBar2AutoRefresh) previewFilter();
                layoutSeekBarValue2.setText(String.format(
                        Locale.ENGLISH,
                        "%s (%d%s)",
                        selectedFilter.seekBar2Title,
                        seekBar.getProgress(),
                        selectedFilter.seekBar2Unit
                ));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the color seek bar
        layoutColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady && selectedFilter.colorSeekBarAutoRefresh) previewFilter();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the first switch
        layoutSwitch1.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutSwitchValue1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitchValue1.setText(selectedFilter.switch1UnitFalse);
                }
                if (inputsReady && selectedFilter.switch1AutoRefresh) previewFilter();
            }
        });


        layoutCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                activityBitmap = null;
                finish();
            }
        });

       layoutButtonApply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (originalImage != null) {
                    applyFilter();

                    PointPercentage touchDown = null;
                    PointPercentage touchCurrent = null;
                    if (imageTouchDown != null) {
                        touchDown = new PointPercentage(imageTouchDown, originalImage);
                        touchCurrent = new PointPercentage(imageTouchCurrent, originalImage);
                    }

                    if (shouldUseMask) {
                        activityAppliedFilter = new AppliedFilter(
                                selectedFilter,
                                maskBmp,
                                layoutColorSeekBar.getProgress(),
                                layoutSeekBar1.getProgress(),
                                layoutSeekBar2.getProgress(),
                                layoutSwitch1.isChecked(),
                                touchDown,
                                touchCurrent
                        );
                    } else {
                        activityAppliedFilter = new AppliedFilter(
                                selectedFilter,
                                null,
                                layoutColorSeekBar.getProgress(),
                                layoutSeekBar1.getProgress(),
                                layoutSeekBar2.getProgress(),
                                layoutSwitch1.isChecked(),
                                touchDown,
                                touchCurrent
                        );
                    }

                    activityBitmap = filteredImage;
                    finish();
                }
            }
        });

        layoutMaskButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                subActivityFilter = Filter.getFilterByName(Settings.FILTER_MASK_NAME);
                subActivityBitmap = originalImage;
                subActivityMaskBmp = ImageTools.bitmapClone(maskBmp);

                Intent intent = new Intent(getApplicationContext(), FiltersActivity.class);
                startActivityForResult(intent, GET_MASK_IMAGE);
            }
        });


        final View.OnTouchListener pickTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE: {
                        int newHue = ImageTools.getHueFromColor(layoutImageView.getPixelAt(new Point(event.getX(), event.getY())));
                        if (newHue >= 0) layoutColorSeekBar.setProgress(newHue);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        layoutPickButton.performClick();
                        break;
                    }

                }
                v.performClick();
                return true;
            }
        };


        layoutPickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                isUsingPick = !isUsingPick;
                if (isUsingPick) {
                    inputsReady = false;
                    layoutImageView.setImageBitmap(originalImage);
                    layoutImageView.setOnTouchListener(pickTouchListener);
                } else {
                    inputsReady = true;
                    layoutImageView.setOnTouchListener(defaultImageViewListener);
                    previewFilter();
                }
            }
        });

        filterMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        layoutHistogramButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!ViewTools.isVisible(layoutHistogramView)) {
                    layoutHistogramView.setVisibility(View.VISIBLE);
                    refreshHistogram();
                } else {
                    layoutHistogramView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        layoutCancel.performClick();
    }

}
