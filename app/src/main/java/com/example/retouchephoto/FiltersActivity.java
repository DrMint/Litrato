package com.example.retouchephoto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class FiltersActivity extends AppCompatActivity {

    static Bitmap result;

    private Bitmap originalImage;
    private Bitmap filteredImage;
    private Filter selectedFilter;

    /**
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;

    private ImageViewZoomScroll layoutImageView;
    private Button      layoutButtonApply;
    private Button      layoutCancel;
    private Button      layoutFilterName;
    private Button      layoutPickButton;
    private Button      layoutBrushButton;
    private Button      layoutHistogramButton;
    private ImageView   layoutHistogram;
    private TextView    layoutImageInfo;
    private SeekBar     layoutSeekBar1;
    private SeekBar     layoutSeekBar2;
    private SeekBar     layoutColorSeekBar;
    private TextView    layoutSeekBarValue1;
    private TextView    layoutSeekBarValue2;
    private Switch      layoutSwitch1;
    private LinearLayout filterMenu;

    private View.OnTouchListener defaultImageViewTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));
        layoutButtonApply       = findViewById(R.id.applyButton);
        layoutCancel            = findViewById(R.id.cancelButton);
        layoutFilterName        = findViewById(R.id.filterNameButton);
        layoutPickButton        = findViewById(R.id.pickButton);
        layoutBrushButton       = findViewById(R.id.brushButton);
        layoutHistogramButton   = findViewById(R.id.histogramButton);
        layoutHistogram         = findViewById(R.id.histogram);
        layoutImageInfo         = findViewById(R.id.imageInformation);
        layoutSeekBar1          = findViewById(R.id.seekBar1);
        layoutSeekBar2          = findViewById(R.id.seekBar2);
        layoutColorSeekBar      = findViewById(R.id.colorSeekBar);
        layoutSeekBarValue1     = findViewById(R.id.seekBarValue1);
        layoutSeekBarValue2     = findViewById(R.id.seekBarValue2);
        layoutSwitch1           = findViewById(R.id.switch1);
        filterMenu              = findViewById(R.id.filtersMenu);

        // Selects the default image in the resource folder and set it
        setBitmap(MainActivity.selectedBitmap);
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        selectedFilter = MainActivity.selectedFilter;
        layoutFilterName.setText(selectedFilter.getName());

        // Initialize all the different listeners.
        initializeListener();
        initializeInterface();
    }


    private void initializeInterface() {
        inputsReady = false;

        selectedFilter.init();

        // Apply the custom filterTouchListener to layoutImageView if it exists, else revert to the default one.
        View.OnTouchListener filterTouchListener = selectedFilter.getImageViewTouchListener();
        if (filterTouchListener == null) {
            layoutImageView.setOnTouchListener(defaultImageViewTouchListener);
        } else {
            layoutImageView.setOnTouchListener(filterTouchListener);
        }

        if (selectedFilter.colorSeekBar) {
            layoutColorSeekBar.setVisibility(View.VISIBLE);
        } else {
            layoutColorSeekBar.setVisibility(View.INVISIBLE);
        }
        layoutPickButton.setVisibility(layoutColorSeekBar.getVisibility());

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
        }

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
        defaultImageViewTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                myScaleDetector.onTouchEvent(event);
                myGestureDetector.onTouchEvent(event);
                v.performClick();
                return true;
            }
        };
        layoutImageView.setOnTouchListener(defaultImageViewTouchListener);


        layoutFilterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isVisible(filterMenu)) {
                    layoutFilterName.setBackgroundColor(Settings.COLOR_SELECTED);
                    filterMenu.setVisibility(View.VISIBLE);
                } else {
                    v.setBackgroundColor(Settings.COLOR_GREY);
                    filterMenu.setVisibility(View.GONE);
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
                result = null;
                finish();
            }
        });

       layoutButtonApply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (originalImage != null) {
                    result = filteredImage;
                    finish();
                }
            }
        });

        layoutBrushButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        layoutPickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        layoutHistogram.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });
    }
}
