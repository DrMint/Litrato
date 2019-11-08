package com.example.retouchephoto;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.view.View;
import android.content.Intent;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Button;
import android.graphics.Color;
import java.util.Arrays;
import java.io.IOException;

import android.net.Uri;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;


public class MainActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    public Bitmap original_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView img = findViewById(R.id.imageView);

        img.setOnClickListener(new View.OnClickListener() {

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


        Button b = findViewById(R.id.Originalbutton);
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ImageView img = findViewById(R.id.imageView);
                img.setImageBitmap(original_image);

                Bitmap immutableBmp = ((BitmapDrawable) img.getDrawable()).getBitmap();
                ImageView hist = findViewById(R.id.histogram);
                Bitmap histogram = Bitmap.createBitmap(255, 200, Bitmap.Config.ARGB_8888);
                histogramCalc(immutableBmp, histogram);
                hist.setImageBitmap(histogram);

            }

        });


        /*SPINNER*/

        String[] arraySpinner = new String[] {
                "", "Grey Filter", "Invert", "Invert luminosity", "Keep Reds", "Contrast+", "Test RGB HSV"
        };

        final Spinner sp = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);



        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sp.setSelection(0);

                if (original_image == null) return;

                ImageView img = findViewById(R.id.imageView);

                Bitmap immutableBmp = ((BitmapDrawable) img.getDrawable()).getBitmap();
                Bitmap mutableBitmap = immutableBmp.copy(Bitmap.Config.ARGB_8888, true);

                switch ((int) id) {
                    case 1: toGray(mutableBitmap);break;
                    case 2: invert(mutableBitmap);break;
                    case 3: invertLuminosity(mutableBitmap);break;
                    case 4: HSVFilter(mutableBitmap, 0);break;
                    case 5: contrastPlusHSV(mutableBitmap);break;
                    case 6: testHSVRGB(mutableBitmap);break;
                }


                ImageView hist = findViewById(R.id.histogram);
                Bitmap histogram = Bitmap.createBitmap(255, 200, Bitmap.Config.ARGB_8888);
                histogramCalc(immutableBmp, histogram);
                hist.setImageBitmap(histogram);

                img.setImageBitmap(mutableBitmap);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    public void histogramCalc(Bitmap bmp, Bitmap hist) {
        int[] Rvalues = new int[256];
        int[] Gvalues = new int[256];
        int[] Bvalues = new int[256];

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];

        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            Rvalues[red(pixels[i])] += 1;
            Gvalues[green(pixels[i])] += 1;
            Bvalues[blue(pixels[i])] += 1;
        }

        int max = 0;

        for (int i = 0; i < 255; i++) {
            max = Math.max(max, Rvalues[i]);
            max = Math.max(max, Gvalues[i]);
            max = Math.max(max, Bvalues[i]);
        }


        int histHeight = hist.getHeight() - 1;
        int histWidth = hist.getWidth();

        int colorR;
        int colorG;
        int colorB;

        int index;

        pixels = new int[hist.getHeight() * hist.getWidth()];

        for (int x = 0; x < histWidth; x++) {


            for (int y = 0; y < histHeight; y++) {

                index = x + ((histHeight - y) * histWidth);

                colorR = 0;
                colorG = 0;
                colorB = 0;

                if (Rvalues[x] * histHeight / max >= y) {colorR = 255;}
                if (Gvalues[x] * histHeight / max >= y) {colorG = 255;}
                if (Bvalues[x] * histHeight / max >= y) {colorB = 255;}

                pixels[index] = Color.rgb(colorR, colorG, colorB);

            }
        }

        hist.setPixels(pixels, 0, hist.getWidth(), 0, 0, hist.getWidth(), hist.getHeight());


    }

    public void toGray(Bitmap bmp) {

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            int grey = (int) (red(pixels[i]) * 0.3 + Color.green(pixels[i]) * 0.59 + Color.blue(pixels[i]) * 0.11);
            pixels[i]  = Color.rgb(grey, grey, grey);
        }
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

    }

    public void invert(Bitmap bmp) {

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            pixels[i] = (0xFFFFFF - pixels[i]) | 0xFF000000;
        }
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

    }

    public void HSVFilter(Bitmap bmp, int deg) {

        deg = 180 - deg;
        int color_margin = 50;
        int diff;
        float[] hsv = new float[3];
        float[] hsv2 = new float[3];

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            Color.colorToHSV(pixels[i], hsv);
            //rgb2hsv(pixels[i], hsv);
            diff = (int) Math.abs(((hsv[0] + deg) % 360) - 180);
            hsv[1] = Math.min(hsv[1], 1f - 1f / color_margin * diff);
            pixels[i] = Color.HSVToColor(hsv);
            //pixels[i] = hsv2rgb(hsv);
        }
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

    }

    public void invertLuminosity(Bitmap bmp) {

        invert(bmp);

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        float[] hsv = new float[3];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[0] = (180 + hsv[0]) % 360;
            pixels[i] = hsv2rgb(hsv);
        }
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }

    public void contrastPlusHSV(Bitmap bmp) {

        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        float minLuminosity = 1;
        float maxLuminosity = 0;
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        float[] hsv = new float[3];

        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            rgb2hsv(pixels[i], hsv);
            minLuminosity = Math.min(minLuminosity, hsv[2]);
            maxLuminosity = Math.max(maxLuminosity, hsv[2]);
        }

        float diff = 1 / (maxLuminosity - minLuminosity);

        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            rgb2hsv(pixels[i], hsv);
            hsv[2] = (hsv[2] - minLuminosity) * diff;
            pixels[i] = hsv2rgb(hsv);
        }

        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());

    }

    public void testHSVRGB(Bitmap bmp) {
        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        float[] hsv = new float[3];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        for (int i = 0; i < bmp.getHeight() * bmp.getWidth(); i++) {
            rgb2hsv(pixels[i], hsv);
            pixels[i] = hsv2rgb(hsv);
        }
        bmp.setPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageView imageView = findViewById(R.id.imageView);
            Bitmap mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(mBitmap);


            Bitmap immutableBmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ImageView hist = findViewById(R.id.histogram);
            Bitmap histogram = Bitmap.createBitmap(255, 200, Bitmap.Config.ARGB_8888);
            histogramCalc(immutableBmp, histogram);
            hist.setImageBitmap(histogram);


            this.original_image = mBitmap;
        }
    }

    public int hsv2rgb (float[] color) {
        float H = color[0];
        float S = color[1];
        float V = color[2];
        
        if (S < 0) {S = 0;}
        if (S > 1) {S = 1;}
        if (V < 0) {V = 0;}
        if (V > 1) {V = 1;}

        float C = V * S;
        float X = C * (1 - Math.abs((H / 60f) % 2 - 1));
        float m = V - C;

        float R = 0;
        float G = 0;
        float B = 0;

        int tmp = (int) (H / 60f);


        switch (tmp) {
            case 0: R = C; G = X; B = 0; break;
            case 1: R = X; G = C; B = 0; break;
            case 2: R = 0; G = C; B = X; break;
            case 3: R = 0; G = X; B = C; break;
            case 4: R = X; G = 0; B = C; break;
            case 5: R = C; G = 0; B = X; break;
        }

        int r = (int) ((R + m) * 255);
        int g = (int) ((G + m) * 255);
        int b = (int) ((B + m) * 255);

        return Color.rgb(r,g,b);


    }

    public void rgb2hsv (int color, float[] hsv) {


        float R = (float) red(color) / 255;
        float G = (float) green(color) / 255;
        float B = (float) blue(color) / 255;

        float minRGB = Math.min(R, Math.min(G, B));
        float maxRGB = Math.max(R, Math.max(G, B));

        float deltaRGB = maxRGB - minRGB;

        float H;
        float S;
        float V;

        if (deltaRGB == 0) {
            H = 0;
        } else if (maxRGB == R) {
            H = 60 * ((G - B) / deltaRGB % 6);
        } else if (maxRGB == G) {
            H = 60 * ((B - R) / deltaRGB + 2);
        } else {
            H = 60 * ((R - G) / deltaRGB + 4);
        }

        if (maxRGB == 0) {
            S = 0;
        } else {
            S = deltaRGB / maxRGB;
        }

        V = maxRGB;

        if (H < 0.0) {
            H += 360;
        }

        hsv[0] = H;
        hsv[1] = S;
        hsv[2] = V;

    }
}


