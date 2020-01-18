package com.example.retouchephoto;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.TextView;

import com.divyanshu.colorseekbar.ColorSeekBar;
import com.google.android.material.snackbar.Snackbar;

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

    private final int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private PackageManager pm;

    /**
     * This is the image as it was before applying any filter.
     */
    private Bitmap originalImage;

    /**
     * This is the image as it was after the last apply button click.
     * This is the image filter are applied to.
     */
    private Bitmap beforeLastFilterImage;

    /**
     * This is beforeLastFilterImage's pixels
     */
    private String seekBar1ValueUnit = "";

    /**
     * This is beforeLastFilterImage's pixels
     */
    private String seekBar2ValueUnit = "";

    /**
     * A boolean to avoid applying filter because the listener have been triggered when modifying
     * the seeks bars minimum, progress, or maximum value.
     */
    private boolean inputsReady = false;

    /**
     * A list of all filters. The order is the same as shown by the spinner.
     */
    private final List<Filter> filters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        pm = getApplicationContext().getPackageManager();

        // Adds listener for the first seek bar
        final SeekBar seekBar = findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
                final TextView seekBarValue = findViewById(R.id.seekBarValue1);
                seekBarValue.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), seekBar1ValueUnit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the second seek bar
        final SeekBar seekBar2 = findViewById(R.id.seekBar2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (inputsReady) applyCorrectFilter();
                final TextView seekBarValue = findViewById(R.id.seekBarValue2);
                seekBarValue.setText(String.format(Locale.ENGLISH,"%d%s", seekBar.getProgress(), seekBar2ValueUnit));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Adds listener for the color seek bar
        final ColorSeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i) {
                if (inputsReady) applyCorrectFilter();
            }
        });

        // Selects the default image in the resource folder.
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        loadBitmap(mBitmap);

        // When the user clicks on imageView, asks the user to choose an image
        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Makes sure the phone has a camera module.
                if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    final CharSequence[] items = {"Take Photo", "Choose from Library"};
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Select a photo...");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if (items[item].equals("Take Photo")) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
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

        // When the user clicks on the reset button, puts back the original image
        findViewById(R.id.originalButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // If the user didn't choose an image yet, there is no image to return to.
                if (originalImage == null) return;
                v.setEnabled(false);
                resetImage();

                /* Puts the spinner back to the default position */
                final Spinner sp = findViewById(R.id.spinner);
                sp.setSelection(0);
            }

        });


        // Creates the filters


        Filter newFilter = new Filter("Select a filter...");
        filters.add(newFilter);

        newFilter = new Filter("Brightness");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.brightnessRS(bmp, context, seekBar * 2.55f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Saturation");
        newFilter.setSeekBar1(0, 100, 200, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.saturationRS(bmp, context,seekBar / 100f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Temperature");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.temperatureRS(bmp, context, seekBar / 10f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Tint");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.tintRS(bmp, context, seekBar / 10f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sharpening");
        newFilter.setSeekBar1(-100, 0, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.sharpenRS(bmp, context, seekBar / 200f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Colorize");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(0, 100, 100, "%");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.colorize(bmp, colorSeekHue, seekBar / 100f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Change hue");
        newFilter.setColorSeekBar();
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.changeHue(bmp, colorSeekHue);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Hue shift");
        newFilter.setSeekBar1(-180, 0, 180, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.hueShift(bmp, (int) seekBar);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Invert");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.invertRS(bmp, context);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Keep a color");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 50, 360, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.keepOrRemoveAColor(bmp, colorSeekHue, (int) seekBar, true);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Remove a color");
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 50, 360, "deg");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.keepOrRemoveAColor(bmp, colorSeekHue, (int) seekBar, false);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Posterize");
        newFilter.setSeekBar1(2, 10, 32, "steps");
        newFilter.setSeekBar2(0, 0, 1, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.posterizeRS(bmp, context, (int) seekBar, seekBar2 > 0);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Threshold");
        newFilter.setSeekBar1(0, 128, 256, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.thresholdRS(bmp, context, seekBar / 256f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Add noise");
        newFilter.setSeekBar1(0, 0, 255, "");
        newFilter.setSeekBar2(0, 0, 1, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.noiseRS(bmp, context, (int) seekBar, seekBar2 > 0);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Linear contrast stretching");
        newFilter.setSeekBar1(0, 0, 255, "");
        newFilter.setSeekBar2(0, 255, 255, "");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.linearContrastStretching(bmp, seekBar / 255f, seekBar2 / 255f);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Histogram equalization");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.histogramEqualization(bmp);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Average blur");
        newFilter.setSeekBar1(1, 2, 19, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.averageBlur(bmp, (int) seekBar);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Gaussian blur");
        newFilter.setSeekBar1(1, 2, 50, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.gaussianBlur(bmp, (int) seekBar, true);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Gaussian blur RS");
        newFilter.setSeekBar1(1, 2, 25, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.gaussianRS(bmp, context, seekBar);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Laplacian");
        newFilter.setSeekBar1(1, 2, 20, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.laplacienEdgeDetection(bmp, (int) seekBar);
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Laplacian RS");
        newFilter.setSeekBar1(0, 2, 14, "px");
        newFilter.setFilterFunction(new FilterInterface() {
            @Override
            public void apply(Bitmap bmp, Context context, int colorSeekHue, float seekBar, float seekBar2) {
                FilterFunctions.laplacianRS(bmp, context, seekBar);
            }
        });
        filters.add(newFilter);


        // Adds all filter names in a array that will be used by the spinner
        String[] arraySpinner = new String[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            arraySpinner[i] = filters.get(i).getName();
        }

        final Spinner sp = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Handles when an item is selected in the spinner.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // This values is used to avoid applying filters while the seek bar are modified.
                // Changing the seek bar minimum, progress or maximum values would normally call the
                // seek bar listener, which would apply the filter 3 time for no reason.
                inputsReady = false;

                final ColorSeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
                final SeekBar seekBar1 = findViewById(R.id.seekBar1);
                final SeekBar seekBar2 = findViewById(R.id.seekBar2);
                final TextView seekBarValue1 = findViewById(R.id.seekBarValue1);
                final TextView seekBarValue2 = findViewById(R.id.seekBarValue2);
                final Button applyButton = findViewById(R.id.applyButton);
                final Button originalButton = findViewById(R.id.originalButton);

                Filter selectedFilter = filters.get(position);

                if (position != 0) {
                    applyButton.setText(getResources().getString(R.string.applyButtonString));
                    applyButton.setEnabled(true);
                    originalButton.setEnabled(true);
                }


                if (selectedFilter.colorSeekBar) {
                    colorSeekBar.setVisibility(View.VISIBLE);
                } else {
                    colorSeekBar.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.seekBar1) {
                    seekBar1.setVisibility(View.VISIBLE);
                    seekBar1.setMin(selectedFilter.seekBar1Min);
                    seekBar1.setMax(selectedFilter.seekBar1Max);
                    seekBar1.setProgress(selectedFilter.seekBar1Set);
                    seekBar1ValueUnit = selectedFilter.seekBar1Unit;
                } else {
                    seekBar1.setVisibility(View.INVISIBLE);
                }

                if (selectedFilter.seekBar2) {
                    seekBar2.setVisibility(View.VISIBLE);
                    seekBar2.setMin(selectedFilter.seekBar2Min);
                    seekBar2.setMax(selectedFilter.seekBar2Max);
                    seekBar2.setProgress(selectedFilter.seekBar2Set);
                    seekBar2ValueUnit = selectedFilter.seekBar2Unit;
                } else {
                    seekBar2.setVisibility(View.INVISIBLE);
                }

                // Only shows the seekBarValues when the seekBars are visible.
                seekBarValue1.setVisibility(seekBar1.getVisibility());
                seekBarValue2.setVisibility(seekBar2.getVisibility());
                seekBarValue1.setText(String.format(Locale.ENGLISH,"%d%s", seekBar1.getProgress(), seekBar1ValueUnit));
                seekBarValue2.setText(String.format(Locale.ENGLISH,"%d%s", seekBar2.getProgress(), seekBar2ValueUnit));

                // Finds the imageView and makes it display beforeLastFilterImage
                if (originalImage != null) {
                    final ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(beforeLastFilterImage);
                    applyCorrectFilter();
                }

                // The seek bars listener can be triggered again.
                inputsReady = true;

            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // When the user click on the apply button, apply the selected filter in the spinner
        findViewById(R.id.applyButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (originalImage != null) {
                    final Spinner sp = findViewById(R.id.spinner);

                    // If the spinner has no filter selected, it is a save button
                    if (sp.getSelectedItemPosition() == 0) {

                        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name) + "/";
                        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
                        Snackbar sb = Snackbar.make(v, getString(R.string.savingMessage), Snackbar.LENGTH_SHORT);

                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        try {
                            File dir = new File(fullPath);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }

                            OutputStream fOut;
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            File file = new File(fullPath, timeStamp + ".jpg");
                            file.createNewFile();
                            fOut = new FileOutputStream(file);

                            // 100 means no compression, the lower you go, the stronger the compression
                            final ImageView imageView = findViewById(R.id.imageView);
                            Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                            fOut.flush();
                            fOut.close();

                            // This asks the MediaStore to add this image to the gallery. This seems to duplicate the file in the Picture folder.
                            //MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                            v.setEnabled(false);
                            sb.show();

                        } catch (Exception e) {
                            Log.e("saveToExternalStorage()", e.getMessage());
                        }

                    // Else it is an apply button
                    } else {
                        // Finds the imageView and makes it display original_image
                        final ImageView imageView = findViewById(R.id.imageView);
                        Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        beforeLastFilterImage = bmp.copy(bmp.getConfig(), true);

                        /* Put the spinner back to the default position */
                        sp.setSelection(0);
                        ((Button)v).setText(getResources().getString(R.string.saveButtonString));
                    }
                }
            }
        });
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
                final Spinner sp = findViewById(R.id.spinner);
                sp.setSelection(0);

                loadBitmap(mBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap mBitmap;
            if (extras != null) {
                mBitmap = (Bitmap) extras.get("data");
                if (mBitmap != null) {
                    loadBitmap(mBitmap);
                }
            }

        }

    }


    /**
     * Function called when a new image is loaded by the program.
     * @param bmp the image to load
     */
    private void loadBitmap(final Bitmap bmp) {
        // Store this image in originalImage, generate all the useful values
        this.originalImage = bmp;
        this.beforeLastFilterImage = bmp.copy(bmp.getConfig(), true);

        // Display the image characteristics on imageInformation
        final TextView imageInfo = findViewById(R.id.imageInformation);
        final String infoString = String.format(Locale.ENGLISH,"%s%d  |  %s%d", getResources().getString(R.string.width), bmp.getWidth(), getResources().getString(R.string.height), bmp.getHeight());
        imageInfo.setText(infoString);

        // Apply filter which also refresh the imageViewer and histogram
        applyCorrectFilter();
    }

    /**
     * Applies whichever filter is selected in the spinner, with the appropriate parameters from the
     * seek bars and color bar. Refreshes the histogram and imageViewer after.
     */
    private void applyCorrectFilter() {

        final Spinner sp = findViewById(R.id.spinner);
        // If the spinner has yet to be initialize, aborts.
        if (sp.getSelectedItemPosition() == -1) return;

        final ColorSeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
        final SeekBar seekBar = findViewById(R.id.seekBar1);
        final SeekBar seekBar2 = findViewById(R.id.seekBar2);

        // Stores the color from the color seek bar make sure the value is between [0; 359]
        int colorSeekHue = ColorTools.rgb2h(colorSeekBar.getColor());
        if (colorSeekHue < 0) colorSeekHue = 0;
        if (colorSeekHue >= 360) colorSeekHue = 0;

        // Otherwise, applies the filter selected in the spinner.
        Bitmap bmpCopy = beforeLastFilterImage.copy(beforeLastFilterImage.getConfig(), true);
        filters.get(sp.getSelectedItemPosition()).apply(bmpCopy, getApplicationContext(), colorSeekHue, seekBar.getProgress(), seekBar2.getProgress());

        // Refresh the image viewer and the histogram.
        refreshImageView(bmpCopy);
        refreshHistogram(bmpCopy);

    }

    /**
     * Takes a bitmap and displays it on imageView.
     * @param bmp the image to display
     */
    private void refreshImageView(final Bitmap bmp) {
        final ImageView imgViewer = findViewById(R.id.imageView);
        imgViewer.setImageBitmap(bmp);
    }

    /**
     * Display the original image in "imageView" and refresh the histogram.
     */
    private void resetImage() {
        // Finds the imageView and makes it display original_image
        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(originalImage);
        beforeLastFilterImage = originalImage.copy(originalImage.getConfig(), true);

        // Get the pixels into a pixel array and refresh the histogram.
        refreshHistogram(beforeLastFilterImage);
    }


    /**
     *  Generates the histogram and displays it in "histogram"
     *  @param bmp the image to analyse
     */
    private void refreshHistogram(Bitmap bmp) {

        int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

        int[] Rvalues = new int[256];
        int[] Gvalues = new int[256];
        int[] Bvalues = new int[256];

        // Stores how many occurrences of all color intensities, for each channel.
        for (int pixel : pixels) {
            Rvalues[(pixel >> 16) & 0x000000FF] += 1;
            Gvalues[(pixel >>8 ) & 0x000000FF] += 1;
            Bvalues[(pixel) & 0x000000FF] += 1;
        }

        int max = 0;

        // Finds the intensity (in all three channels) with the maximum number of occurrences.
        for (int i = 0; i < 256; i++) {
            max = Math.max(max, Rvalues[i]);
            max = Math.max(max, Gvalues[i]);
            max = Math.max(max, Bvalues[i]);
        }

        Bitmap hist = Bitmap.createBitmap(256, 200, Bitmap.Config.ARGB_8888);
        int histHeight = hist.getHeight() - 1;
        int histWidth = hist.getWidth();

        int[] histPixels = new int[hist.getHeight() * hist.getWidth()];

        // If the image is blank, return with a black histogram.
        if (max == 0) {
            for (int x = 0; x < histWidth; x++) {
                for (int y = 0; y < histHeight; y++) {
                    histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(0, 0, 0);
                }
            }

        } else {

            int colorR;
            int colorG;
            int colorB;

            for (int x = 0; x < histWidth; x++) {
                for (int y = 0; y < histHeight; y++) {

                    colorR = 0;
                    colorG = 0;
                    colorB = 0;

                    if (Math.sqrt(Rvalues[x] * histHeight / max) * 14 >= y) {colorR = 255;}
                    if (Math.sqrt(Gvalues[x] * histHeight / max) * 14 >= y) {colorG = 255;}
                    if (Math.sqrt(Bvalues[x] * histHeight / max) * 14 >= y) {colorB = 255;}

                    histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(colorR, colorG, colorB);

                }
            }
        }

        hist.setPixels(histPixels, 0, hist.getWidth(), 0, 0, hist.getWidth(), hist.getHeight());
        final ImageView histogram = findViewById(R.id.histogram);
        histogram.setImageBitmap(hist);

    }
}


