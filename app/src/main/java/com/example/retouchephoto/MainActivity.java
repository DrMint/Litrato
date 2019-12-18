package com.example.retouchephoto;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;

import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.graphics.Color;
import java.io.IOException;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import android.content.Context;

import com.divyanshu.colorseekbar.ColorSeekBar;


/*
    This is probably bad, but that's how the Filter class can access MainActivity's context
    I need it for RenderScript.
*/
@SuppressWarnings("StaticFieldLeak")


public class MainActivity extends AppCompatActivity {

    static Context context;
    private int PICK_IMAGE_REQUEST = 1;
    public Bitmap original_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();

        setContentView(R.layout.activity_main);

        // When the user click on imageView, asks the user to choose an image
        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        // When the user click on the reset button, put back the original image
        findViewById(R.id.Originalbutton).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // If the user didn't choose an image yet, there is no image to return to.
                if (original_image == null) return;
                resetImage();
            }

        });

        // Here are the elements shown in the spinner
        String[] arraySpinner = new String[] {
                "Choose a filter...",
                "Grey Filter",
                "Invert",
                "Invert luminosity",
                "Keep a color",
                "Colorize",
                "Change hue",
                "Linear contrast stretching",
                "Linear contrast compressing",
                "Histogram equalization",
                "Test RGB HSV"
        };
        final Spinner sp = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);


        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Handles when an item is selected in the spinner and
             * applies the corresponding filter.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Reset the spinner
                sp.setSelection(0);

                // If the user didn't choose an image yet, don't apply any filter.
                if (original_image == null) return;

                final ImageView imgViewer = findViewById(R.id.imageView);
                final Bitmap immutableBmp = ((BitmapDrawable) imgViewer.getDrawable()).getBitmap();

                final int imageHeight = immutableBmp.getHeight();
                final int imageWidth = immutableBmp.getWidth();

                // Turns the image into a pixel array
                Bitmap mutableBitmap = immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
                int[] imagePixels = new int[imageHeight * imageWidth];
                mutableBitmap.getPixels(imagePixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);

                final ColorSeekBar colorSeekBar = findViewById(R.id.colorSeekBar);
                float[] tmp = new float[3];
                Filter.rgb2hsv(colorSeekBar.getColor(), tmp);
                int colorSeekHue = (int) tmp[0];


                // Apply the filter corresponding to the selected element in the spinner
                switch ((int) id) {
                    case 1: Filter.toGrayRS(imagePixels, imageWidth, imageHeight);break;
                    case 2: Filter.invertRS(imagePixels, imageWidth, imageHeight);break;
                    case 3: Filter.invertLuminosity(imagePixels, imageWidth, imageHeight);break;
                    case 4: Filter.keepAColor(imagePixels, colorSeekHue);break;
                    case 5: Filter.colorize(imagePixels, colorSeekHue);break;
                    case 6: Filter.changeHue(imagePixels, colorSeekHue);break;
                    case 7: Filter.linearContrastStretching(imagePixels, 0f, 1f);break;
                    case 8: Filter.linearContrastStretching(imagePixels, 0.3f, 0.9f);break;
                    case 9: Filter.histogramEqualization(imagePixels);break;
                    case 10: Filter.testHSVRGB(imagePixels);break;
                }

                // Turn the pixel array back into an image
                mutableBitmap.setPixels(imagePixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
                imgViewer.setImageBitmap(mutableBitmap);

                refreshHistogram(imagePixels);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // After the user as selected an image, loads it and stores it in this.original_image
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Bitmap mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.original_image = mBitmap;
            resetImage();
        }
    }


    /**
     * Display the original image in "imageView" and refresh the histogram.
     */
    public void resetImage() {
        // Finds the imageView and makes it display original_image
        final ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(original_image);

        // Get the pixels into a pixel array and refresh the histogram.
        Bitmap mutableBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        int[] imagePixels = new int[mutableBitmap.getHeight() * mutableBitmap.getWidth()];
        mutableBitmap.getPixels(imagePixels, 0, mutableBitmap.getWidth(), 0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight());
        refreshHistogram(imagePixels);
    }

    /**
     *  Generates the histogram and displays it in "histogram"
     *  @param pixels the pixels of the image to analyse
     */
    public void refreshHistogram(int[] pixels) {

        final ImageView histogram = findViewById(R.id.histogram);
        Bitmap hist = Bitmap.createBitmap(255, 200, Bitmap.Config.ARGB_8888);


        int[] Rvalues = new int[256];
        int[] Gvalues = new int[256];
        int[] Bvalues = new int[256];

        // Stores how many occurrences of all color intensities, for each channel.
        for (int pixel : pixels) {
            Rvalues[red(pixel)] += 1;
            Gvalues[green(pixel)] += 1;
            Bvalues[blue(pixel)] += 1;
        }

        int max = 0;

        // Finds the intensity (in all three channels) with the maximum number of occurrences.
        for (int i = 0; i < 255; i++) {
            max = Math.max(max, Rvalues[i]);
            max = Math.max(max, Gvalues[i]);
            max = Math.max(max, Bvalues[i]);
        }

        // If the image is blank, return with a black histogram.
        if (max == 0) {
            histogram.setImageBitmap(hist);
            return;
        }

        int histHeight = hist.getHeight() - 1;
        int histWidth = hist.getWidth();

        int colorR;
        int colorG;
        int colorB;


        int[] histPixels = new int[hist.getHeight() * hist.getWidth()];

        for (int x = 0; x < histWidth; x++) {
            for (int y = 0; y < histHeight; y++) {

                colorR = 0;
                colorG = 0;
                colorB = 0;

                if (Rvalues[x] * histHeight / max >= y) {colorR = 255;}
                if (Gvalues[x] * histHeight / max >= y) {colorG = 255;}
                if (Bvalues[x] * histHeight / max >= y) {colorB = 255;}

                histPixels[x + ((histHeight - y) * histWidth)] = Color.rgb(colorR, colorG, colorB);

            }
        }

        hist.setPixels(histPixels, 0, hist.getWidth(), 0, 0, hist.getWidth(), hist.getHeight());
        histogram.setImageBitmap(hist);

    }

    /**
     * Probably bad, but that's how I retrieve MainActivity's context
     * that I need for RenderScript in the Filter class.
     * @return MainActivity's context
     */
    public static Context getAppContext() {
        return MainActivity.context;
    }

}


