package com.example.retouchephoto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

public class FiltersActivity extends AppCompatActivity {

    static Filter subActivityFilter;
    static Bitmap subActivityBitmap;
    static Bitmap activityBitmap;

    private Bitmap originalImage;
    private Bitmap filteredImage;
    private Bitmap maskBmp;
    private Bitmap originalImageMasked;

    private Filter selectedFilter;

    private final int GET_MASK_IMAGE = 4;

    /**
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;
    private boolean pickBool = false;
    private boolean shouldUseMask = false;

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

    private Point imageTouchDown;
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
        filterMenu              = findViewById(R.id.filtersMenu);


        if (Objects.requireNonNull(getCallingActivity()).getClassName().equals(MainActivity.class.getName())) {

            setBitmap(MainActivity.subActivityBitmap);
            selectedFilter = MainActivity.subActivityFilter;

        } else if (getCallingActivity().getClassName().equals(FiltersActivity.class.getName())) {

            setBitmap(FiltersActivity.subActivityBitmap);
            selectedFilter = FiltersActivity.subActivityFilter;

        }

        layoutFilterMenuButton.setText(selectedFilter.getName());

        // Selects the default image in the resource folder and set it
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        layoutHistogramView.setVisibility(View.GONE);

        // Initialize all the different listeners, the interface and the masks
        initializeListener();
        initializeInterface();
        generateMasks();

        previewFilter();
    }

    private void generateMasks(Bitmap bmp) {
        maskBmp = bmp;

        Bitmap invertedMaskBmp = ImageTools.bitmapClone(maskBmp);
        FilterFunction.invert(invertedMaskBmp, getApplicationContext());

        originalImageMasked = ImageTools.bitmapClone(originalImage);
        FilterFunction.applyTexture(originalImageMasked, invertedMaskBmp, getApplicationContext(), BlendType.MULTIPLY);
    }

    private void generateMasks() {
        Bitmap bmp = ImageTools.bitmapClone(originalImage);
        ImageTools.fillWithColor(bmp, Color.BLACK);
        generateMasks(bmp);
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
            FilterFunction.applyTexture(filteredImage, maskBmp,getApplicationContext(),BlendType.MULTIPLY);
            FilterFunction.applyTexture(filteredImage, originalImageMasked, getApplicationContext(), BlendType.ADD);
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

        final View.OnTouchListener defaultImageViewListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (selectedFilter.allowScrollZoom) {
                    myScaleDetector.onTouchEvent(event);
                    myGestureDetector.onTouchEvent(event);

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
                if (inputsReady && selectedFilter.seekBar1AutoRefresh) previewFilter();
                layoutSeekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar1Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the second seek bar
        layoutSeekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady && selectedFilter.seekBar2AutoRefresh) previewFilter();
                layoutSeekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar2Unit));
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
                if (layoutSwitch1.isChecked()) {
                    layoutSwitch1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitch1.setText(selectedFilter.switch1UnitFalse);
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
                    activityBitmap = filteredImage;
                    finish();
                }
            }
        });

        layoutMaskButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Filter maskFilter;
                maskFilter = new Filter("Create Mask");
                maskFilter.allowMasking = false;
                maskFilter.allowHistogram = false;
                maskFilter.allowScrollZoom = false;
                maskFilter.setSeekBar1(5,30,300, "px");
                maskFilter.setSeekBar2(0,50,100, "%");
                maskFilter.setSwitch1(true, "Black", "White");
                maskFilter.seekBar1AutoRefresh = false;
                maskFilter.seekBar2AutoRefresh = false;
                maskFilter.switch1AutoRefresh = false;
                maskFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
                    @Override
                    public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                        if (switch1) {
                            ImageTools.drawCircle(maskBmp, touchUp, (int) seekBar, Color.WHITE);
                        } else {
                            ImageTools.drawCircle(maskBmp, touchUp, (int) seekBar, Color.BLACK);
                        }
                        FilterFunction.applyTexture(bmp, maskBmp, context, BlendType.OVERLAY, seekBar2 / 100f);
                        return null;
                    }
                });

                maskFilter.setFilterApplyFunction(new FilterApplyInterface() {
                    @Override
                    public Bitmap apply(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                        return maskBmp;
                    }
                });

                subActivityFilter = maskFilter;
                subActivityBitmap = originalImage;
                Intent intent = new Intent(getApplicationContext(), FiltersActivity.class);
                startActivityForResult(intent, GET_MASK_IMAGE);
            }
        });


        final View.OnTouchListener pickTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE: {
                        Point chosenPixel = layoutImageView.imageViewTouchPointToBmpCoordinates(new Point(event.getX(), event.getY()));
                        int newHue = ImageTools.getHueFromColor(layoutImageView.getPixelAt(chosenPixel));
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
                pickBool = !pickBool;
                if (pickBool) {
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
                if (!MainActivity.isVisible(layoutHistogramView)) {
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
