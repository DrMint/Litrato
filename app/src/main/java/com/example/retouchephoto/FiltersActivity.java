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
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;

    private Boolean filterOpen = false;

    private ImageViewZoomScroll layoutImageView;
    private Button      layoutButtonApply;
    private Button      cancelButton;
    private ImageView   layoutHistogram;
    private TextView    layoutImageInfo;
    private SeekBar     layoutSeekBar1;
    private SeekBar     layoutSeekBar2;
    private SeekBar     layoutColorSeekBar;
    private TextView    layoutSeekBarValue1;
    private TextView    layoutSeekBarValue2;
    private Switch      layoutSwitch1;
    private LinearLayout filterMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        // Sets all the layout shortcuts.
        layoutImageView         = new ImageViewZoomScroll((ImageView) findViewById(R.id.imageView));
        layoutButtonApply       = findViewById(R.id.applyButton);
        Button filterNameButton = findViewById(R.id.filterNameButton);
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
        setBitmap(FileInputOutput.getBitmap(getResources(), R.drawable.default_image));
        layoutImageView.setImageBitmap(filteredImage);
        layoutImageView.setMaxZoom(Settings.MAX_ZOOM_LEVEL);

        // Initialize all the different listeners.
        initializeListener();

        filterNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filterMenu.getVisibility()==View.VISIBLE){
                    closeMenu();
                }else{
                    closeMenu();
                    filterMenu.setVisibility(View.VISIBLE);
                    filterOpen = true;
                }
            }
        });
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setBitmap(FileInputOutput.getLastTakenBitmap());
    }

    private void setBitmap(Bitmap bmp) {

        // If the bmp is null, aborts
        if (bmp == null) return;

        /*// Resize the image before continuing, if necessary
        if (bmp.getHeight() > Settings.IMPORTED_BMP_SIZE || bmp.getWidth() > Settings.IMPORTED_BMP_SIZE) {
            bmp = ImageTools.resizeAsContainInARectangle(bmp, Settings.IMPORTED_BMP_SIZE);
            Snackbar sb = Snackbar.make(
                    layoutSpinner,
                    "Image resized to " + bmp.getWidth() + "px by " + bmp.getHeight() + "px",
                    Snackbar.LENGTH_SHORT);
            sb.show();
        }*/

        // Set this image as the originalImage and reset the UI
        originalImage = bmp;
        resetImage();
    }

    private void previewOrApply(boolean apply) {

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

        // Adds listener for the first seek bar
        /*layoutSeekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) previewFilter();
                Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
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
                Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
                layoutSeekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), selectedFilter.seekBar2Unit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });*/

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
        /*layoutSwitch1.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Filter selectedFilter = filters.get(layoutSpinner.getSelectedItemPosition());
                if (layoutSwitch1.isChecked()) {
                    layoutSwitch1.setText(selectedFilter.switch1UnitTrue);
                } else {
                    layoutSwitch1.setText(selectedFilter.switch1UnitFalse);
                }
                if (inputsReady) previewFilter();
            }
        });*/

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
        final ScaleGestureDetector myScaleDetector = new ScaleGestureDetector(FiltersActivity.this, new ScaleGestureDetector.OnScaleGestureListener() {
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



        // When the user clicks on the histogram, makes it collapse or bring it back up.
        layoutHistogram.setOnClickListener(new View.OnClickListener() {

            boolean collapsed = false;

            @Override
            public void onClick(View v) {

                int dimensionInDp;

                if (collapsed) {
                    // Convert px to dp
                    dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
                    layoutImageInfo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    layoutImageInfo.setVisibility(View.VISIBLE);
                } else {
                    // Convert px to dp
                    dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
                    layoutImageInfo.setVisibility(View.GONE);
                }

                collapsed = !collapsed;
                v.getLayoutParams().height = dimensionInDp;
                v.requestLayout();

                //TODO: Correct bug 0001
            }
        });

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
       layoutButtonApply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (originalImage != null) {

                    // If the spinner has no filter selected, it is a save button
                    /*if (layoutSpinner.getSelectedItemPosition() == 0) {

                        // Did it save properly?
                        if (FileInputOutput.saveImage(filteredImage, FiltersActivity.this)) {
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
                    }*/
                }
            }
        });
    }


    private void closeMenu(){
        if (filterOpen){
            filterMenu.setVisibility(View.GONE);
            this.filterOpen = false;
        }
    }
}
