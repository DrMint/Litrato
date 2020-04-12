package com.example.retouchephoto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class FiltersActivity extends AppCompatActivity {

    static Filter subActivityFilter;
    static Bitmap subActivityBitmap;

    static Bitmap activityBitmap;

    private Bitmap originalImage;
    private Bitmap filteredImage;
    private Filter selectedFilter;

    /**
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;
    private boolean pickBool = false;
    private boolean brushBool = false;

    private ImageViewZoomScroll layoutImageView;
    private Button      layoutButtonApply;
    private Button      layoutCancel;
    private Button      layoutFilterMenuButton;
    private Button      layoutPickButton;
    private Button      layoutMaskButton;
    private Button      layoutHistogramButton;
    private ImageView   layoutHistogramView;
    private SeekBar     layoutSeekBar1;
    private SeekBar     layoutSeekBar2;
    private SeekBar     layoutColorSeekBar;
    private TextView    layoutSeekBarValue1;
    private TextView    layoutSeekBarValue2;
    private Switch      layoutSwitch1;
    private LinearLayout filterMenu;

    private Point imageTouchDown = new Point(0,0);
    private Point imageTouchUp = new Point(0,0);

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
        filterMenu              = findViewById(R.id.filtersMenu);

        // Selects the default image in the resource folder and set it
        setBitmap(MainActivity.subActivityBitmap);
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        layoutHistogramView.setVisibility(View.GONE);

        selectedFilter = MainActivity.subActivityFilter;
        layoutFilterMenuButton.setText(selectedFilter.getName());

        // Initialize all the different listeners.
        initializeListener();
        initializeInterface();

        //Intent intent = new Intent(this, FiltersActivity.class);
        //startActivity(intent);

        previewFilter();
    }


    private void initializeInterface() {
        inputsReady = false;

        //selectedFilter.init();

        if (selectedFilter.allowFilterMenu) {

            // We make everything GONE
            layoutColorSeekBar.setVisibility(View.GONE);
            layoutMaskButton.setVisibility(View.GONE);
            layoutHistogramButton.setVisibility(View.GONE);
            layoutSeekBar1.setVisibility(View.GONE);
            layoutSeekBar2.setVisibility(View.GONE);
            layoutSwitch1.setVisibility(View.GONE);

            // And add anything we need.

            if (selectedFilter.colorSeekBar) layoutColorSeekBar.setVisibility(View.VISIBLE);


            if (selectedFilter.allowMasking) layoutMaskButton.setVisibility(View.VISIBLE);
            if (selectedFilter.allowHistogram) layoutHistogramButton.setVisibility(View.VISIBLE);

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
                    layoutSwitch1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitch1.setText(selectedFilter.switch1UnitFalse);
                }
            }

            // Only shows the seekBarValues when the seekBars are visible.
            layoutSeekBarValue1.setVisibility(layoutSeekBar1.getVisibility());
            layoutSeekBarValue2.setVisibility(layoutSeekBar2.getVisibility());
            layoutSeekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", layoutSeekBar1.getProgress(), selectedFilter.seekBar1Unit));
            layoutSeekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", layoutSeekBar2.getProgress(), selectedFilter.seekBar2Unit));

            // Only shows the pick tool if there is a seekBar
            layoutPickButton.setVisibility(layoutColorSeekBar.getVisibility());

        } else {

            filterMenu.setVisibility(View.GONE);
            layoutFilterMenuButton.setBackgroundColor(Settings.COLOR_GREY);

        }

        // The seek bars listener can be triggered again.
        inputsReady = true;
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
                    getApplicationContext(),
                    layoutColorSeekBar.getProgress(),
                    layoutSeekBar1.getProgress(),
                    layoutSeekBar2.getProgress(),
                    layoutSwitch1.isChecked(),
                    imageTouchDown,
                    imageTouchUp
            );
        } else {
            result = selectedFilter.preview(
                    filteredImage,
                    getApplicationContext(),
                    layoutColorSeekBar.getProgress(),
                    layoutSeekBar1.getProgress(),
                    layoutSeekBar2.getProgress(),
                    layoutSwitch1.isChecked(),
                    imageTouchDown,
                    imageTouchUp
            );
        }

        // If the filter return a bitmap, filteredImage becomes this bitmap
        if (result != null) {
            filteredImage = result;
        }

        // Refresh the image viewer and the histogram.
        if (!apply) refreshImageView();
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
        refreshHistogram();
        //refreshImageInfo();
    }

    /**
     * Display the histogram of filteredImage on layoutHistogram
     */
    private void refreshHistogram() {
        if(MainActivity.isVisible(layoutHistogramView)) {
            layoutHistogramView.setImageBitmap(ImageTools.generateHistogram(filteredImage));
        }
    }

    /*
    private void refreshImageInfo() {

        final String infoString = String.format(
                Locale.ENGLISH,"%s%d  |  %s%d",
                getResources().getString(R.string.width),
                filteredImage.getWidth(),
                getResources().getString(R.string.height),
                filteredImage.getHeight());

        layoutImageInfo.setText(infoString);
    }
     */

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
        final ScaleGestureDetector myScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
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
        layoutImageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (selectedFilter.allowScrollZoom) {
                    myScaleDetector.onTouchEvent(event);
                    myGestureDetector.onTouchEvent(event);

                }
                if (pickBool){
                    layoutImageView.setImageBitmap(originalImage);
                    if (layoutColorSeekBar.getVisibility() == View.VISIBLE) {
                        layoutImageView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                Point choosedPixel = layoutImageView.imageViewTouchPointToBmpCoordinates(new Point(event.getX(), event.getY()));
                                int newHue = layoutImageView.hueOfSelectedPixel(choosedPixel);
                                if (newHue>=0) {
                                    layoutColorSeekBar.setProgress(layoutImageView.hueOfSelectedPixel(choosedPixel));
                                }
                                return false;
                            }
                        });
                    }
                }
                else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            imageTouchDown.x = (int) event.getX();
                            imageTouchDown.y = (int) event.getY();
                            imageTouchDown.set(layoutImageView.imageViewTouchPointToBmpCoordinates(imageTouchDown));
                            layoutImageView.sanitizeBmpCoordinates(imageTouchDown);
                        }
                        case MotionEvent.ACTION_MOVE: {
                            imageTouchUp.x = (int) event.getX();
                            imageTouchUp.y = (int) event.getY();
                            imageTouchUp.set(layoutImageView.imageViewTouchPointToBmpCoordinates(imageTouchUp));
                            layoutImageView.sanitizeBmpCoordinates(imageTouchUp);
                        }
                        case MotionEvent.ACTION_UP: break;
                    }

                    previewFilter();

                }

                v.performClick();
                return true;
            }
        });

        layoutFilterMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedFilter.allowFilterMenu) {
                    if (!MainActivity.isVisible(filterMenu)) {
                        layoutFilterMenuButton.setBackgroundColor(Settings.COLOR_SELECTED);
                        filterMenu.setVisibility(View.VISIBLE);
                    } else {
                        v.setBackgroundColor(Settings.COLOR_GREY);
                        filterMenu.setVisibility(View.GONE);
                    }
                }


            }
        });

        // Adds listener for the first seek bar
        layoutSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) previewFilter();
                layoutSeekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar1Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the second seek bar
        layoutSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) previewFilter();
                layoutSeekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar2Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the color seek bar
        layoutColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) previewFilter();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the first switch
        layoutSwitch1.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (layoutSwitch1.isChecked()) {
                    layoutSwitch1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitch1.setText(selectedFilter.switch1UnitFalse);
                }
                if (inputsReady) previewFilter();
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
                    activityBitmap = filteredImage;
                    finish();
                }
            }
        });

        layoutMaskButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FiltersActivity.class);
                startActivityForResult(intent, 3);
            }
        });

        layoutPickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                pickBool=!pickBool;
                if (!pickBool){
                    pickBool=false;
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
                if (!MainActivity.isVisible(layoutHistogramView)) {
                    layoutHistogramView.setVisibility(View.VISIBLE);
                    refreshHistogram();
                } else {
                    layoutHistogramView.setVisibility(View.GONE);
                }
            }
        });
    }
}
