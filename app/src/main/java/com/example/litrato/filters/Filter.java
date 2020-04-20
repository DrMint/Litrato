package com.example.litrato.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.example.litrato.R;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.tools.FileInputOutput;
import com.example.litrato.tools.ImageTools;
import com.example.litrato.tools.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * A instance of this class has many properties such as what kind of inputs (colorSeekBar and seekBars) should be available to the user.
 *
 * @author Thomas Barillot
 * @version 1.0
 * @since   2019-01-08
 */

public class Filter {

    public static final List<Filter> filters = new ArrayList<>();

    /**
     * The name displayed in the spinner.
     */
    private final String name;

    /**
     * Does this filter utilize the colorSeekBar.
     */
    public boolean colorSeekBar = false;
    public final boolean colorSeekBarAutoRefresh = true;

    /**
     * Does this filter utilize the first seekBar.
     */
    public boolean seekBar1 = false;
    public boolean seekBar1AutoRefresh = true;
    public int seekBar1Min;
    public int seekBar1Set;
    public int seekBar1Max;
    public String seekBar1Title;
    public String seekBar1Unit;

    /**
     * Does this filter utilize the second seekBar.
     */
    public boolean seekBar2 = false;
    public final boolean seekBar2AutoRefresh = true;
    public int seekBar2Min;
    public int seekBar2Set;
    public int seekBar2Max;
    public String seekBar2Title;
    public String seekBar2Unit;

    /**
     * Does this filter utilize the first switch.
     */
    public boolean switch1 = false;
    public boolean switch1AutoRefresh = true;
    public boolean switch1Default;
    public String switch1UnitFalse;
    public String switch1UnitTrue;

    /**
     * Only for generate Tools Button dynamically
     */
    private Bitmap icon;

    private FilterApplyInterface myApplyInterface;
    private FilterPreviewInterface myPreviewInterface;

    private final Category category;
    public boolean needFilterActivity = true;
    public boolean allowMasking = true;
    public boolean allowHistogram = true;
    public boolean allowScrollZoom = true;
    public final boolean allowFilterMenu = true;

    public Filter(String name, Category category) {
        this.name = name;
        this.category = category;
        if (category == Category.PRESET) needFilterActivity = false;
    }

    //Getters and Setters

    private void setSeekBar1(int seekBar1Min, int seekBar1Set, int seekBar1Max, String seekBar1Title, String seekBar1Unit) {
        this.seekBar1 = true;
        this.seekBar1Min = seekBar1Min;
        this.seekBar1Set = seekBar1Set;
        this.seekBar1Max = seekBar1Max;
        this.seekBar1Title = seekBar1Title;
        this.seekBar1Unit = seekBar1Unit;
    }

    private void setSeekBar2(int seekBar2Min, int seekBar2Set, int seekBar2Max, String seekBar2Title, String seekBar2Unit) {
        this.seekBar2 = true;
        this.seekBar2Min = seekBar2Min;
        this.seekBar2Set = seekBar2Set;
        this.seekBar2Max = seekBar2Max;
        this.seekBar2Title = seekBar2Title;
        this.seekBar2Unit = seekBar2Unit;
    }

    private void setSwitch1(boolean switch1Default, String switch1UnitFalse, String switch1UnitTrue) {
        this.switch1 = true;
        this.switch1Default = switch1Default;
        this.switch1UnitFalse = switch1UnitFalse;
        this.switch1UnitTrue = switch1UnitTrue;
    }

    private void setColorSeekBar() {this.colorSeekBar = true;}

    private void setFilterApplyFunction(final FilterApplyInterface newInterface) {this.myApplyInterface = newInterface;}
    private void setFilterPreviewFunction(final FilterPreviewInterface newInterface) {this.myPreviewInterface = newInterface;}
    private void setIcon(Bitmap bmp){this.icon = bmp;}

    public String getName() {return this.name;}
    public Bitmap getIcon(){return icon;}
    public Category getFilterCategory() {return category;}


    /**
     *  Start the correct filter function for that specific filter instance.
     *  Because RenderScript uses Bitmap as input and other filters use an array of pixel, we have to
     *  create both
     *  @param bmp the image the filter will be apply to.
     *  @param colorSeekHue the value of colorSeekBar.
     *  @param seekBar the value of seekBar1.
     *  @param seekBar2 the value of seeBar2.
     */
    public Bitmap apply(final Bitmap bmp, final Bitmap maskBmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        if (myApplyInterface != null) return myApplyInterface.apply(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        return preview(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
    }

    public Bitmap apply(final Bitmap bmp, Bitmap maskBmp, final Context context) {
        return apply(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    public Bitmap apply(final Bitmap bmp, final Context context) {
        final Bitmap maskBmp = ImageTools.bitmapClone(bmp);
        ImageTools.fillWithColor(maskBmp, Color.WHITE);
        return apply(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    public Bitmap preview(final Bitmap bmp, final Bitmap maskBmp, final Context context, final int colorSeekHue, final float seekBar, final float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        if (myPreviewInterface != null) return myPreviewInterface.preview(bmp, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        return null;
    }

    @SuppressWarnings("unused")
    public Bitmap preview(final Bitmap bmp, final Bitmap maskBmp, final Context context) {
        return preview(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }

    @SuppressWarnings("unused")
    public Bitmap preview(final Bitmap bmp, final Context context) {
        final Bitmap maskBmp = ImageTools.bitmapClone(bmp);
        ImageTools.fillWithColor(maskBmp, Color.WHITE);
        return preview(bmp, maskBmp, context, 0, seekBar1Set, seekBar2Set, switch1Default, new Point(0,0), new Point(0,0));
    }


    public static void generateFilters(Context context) {
        createPresets(context);
        createTools(context);
        createFilters(context);
        createSpecial(context);
    }

    public static Filter getFilterByName(String name) {
        for (Filter filter:filters) {
            if (filter.getName().equals(name)) return filter;
        }
        return null;
    }


    private static void createPresets(Context context){

        Filter newPresets;
        newPresets = new Filter("2 Strip", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.removeAColor(bmp, 79, 72);
                FilterFunction.removeAColor(bmp, 129, 99);
                FilterFunction.removeAColor(bmp, 294, 40);
                FilterFunction.hueShift(bmp, -15);
                return null;
            }
        });
        filters.add(newPresets);
        newPresets = new Filter("Invert", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.invert(bmp);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Bleach Bypass", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.toExtDyn(bmp, 25, 255);
                FilterFunction.saturation(bmp, 70);
                FilterFunction.brightness(bmp, 40);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Candle light", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.saturation(bmp, 40);
                FilterFunction.temperature(bmp, 58);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Crisp Warm", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.contrastBurn(bmp, 8);
                FilterFunction.temperature(bmp, 20);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Crisp Winter", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.brightness(bmp, 24);
                FilterFunction.temperature(bmp, -80);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Drop Blues", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.removeAColor(bmp, 232, 109);
                FilterFunction.removeAColor(bmp, 189, 83);
                return bmp;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Old analog", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.gaussianBlur(bmp, 2);
                FilterFunction.saturation(bmp, 0);
                FilterFunction.temperature(bmp, 100);
                Bitmap texture = FileInputOutput.getBitmap(context.getResources(), R.drawable.grunge_texture, bmp.getWidth(), bmp.getHeight());
                Bitmap texture2 = FileInputOutput.getBitmap(context.getResources(), R.drawable.white_noise, bmp.getWidth(), bmp.getHeight());
                FilterFunction.applyTexture(bmp, texture, BlendType.MULTIPLY);
                FilterFunction.applyTexture(bmp, texture2, BlendType.ADD);
                return null;
            }
        });
        filters.add(newPresets);


        newPresets = new Filter("Tension Green", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.removeAColor(bmp, 270, 108);
                FilterFunction.saturation(bmp, 70);
                FilterFunction.tint(bmp, -36);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Edgy Amber", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.contrastBurn(bmp, -10);
                FilterFunction.burnValues(bmp, -10);
                FilterFunction.saturation(bmp, 40);
                FilterFunction.temperature(bmp, 100);
                FilterFunction.temperature(bmp, 50);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Night from Day", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.noise(bmp, 20, false);
                FilterFunction.gaussianBlur(bmp, 2);
                FilterFunction.saturation(bmp, 60);
                FilterFunction.brightness(bmp, -10);
                FilterFunction.temperature(bmp, -86);
                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Late Sunset", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.gamma(bmp, -40);
                FilterFunction.saturation(bmp, 30);
                FilterFunction.tint(bmp, 29);
                FilterFunction.temperature(bmp, 50);
                FilterFunction.brightness(bmp, 10);

                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Futuristic Bleak", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.contrastBurn(bmp, -29);
                FilterFunction.saturation(bmp, 60);
                FilterFunction.tint(bmp, -10);

                return null;
            }
        });
        filters.add(newPresets);

        newPresets = new Filter("Soft Warming", Category.PRESET);
        newPresets.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.contrastBurn(bmp, -23);
                FilterFunction.brightness(bmp, 20);
                FilterFunction.saturation(bmp, 70);
                FilterFunction.tint(bmp, 10);
                FilterFunction.temperature(bmp, 7);

                return null;
            }
        });
        filters.add(newPresets);
    }




    private static void createTools(Context context){
        Filter newTools;

        newTools = new Filter(Settings.FILTER_ROTATION, Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.rotate));
        newTools.allowMasking = false;
        newTools.allowHistogram = false;
        newTools.setSeekBar1(-180, 0, 180, "Angle", "deg");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                return FilterFunction.rotate(bmp, seekBar);
            }
        });
        filters.add(newTools);

        newTools = new Filter("Crop", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.crop));
        newTools.allowMasking = false;
        newTools.allowScrollZoom = false;
        newTools.allowHistogram = false;
        newTools.setSwitch1(false, "Keep ratio", "Free ratio");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                if (switch1) ImageTools.forceRectangleRatio(bmp, touchDown, touchUp);
                ImageTools.drawRectangle(bmp, touchDown, touchUp, Color.argb(Settings.CROP_OPACITY, 255,255,255));
                ImageTools.drawRectangle(bmp, touchDown, touchUp, Color.argb(Settings.CROP_OPACITY, 0,0,0), Settings.CROP_BORDER_SIZE);
                return null;
            }
        });
        newTools.setFilterApplyFunction(new FilterApplyInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                return FilterFunction.crop(bmp, touchDown, touchUp);
            }
        });
        filters.add(newTools);

        newTools = new Filter("Flip", Category.TOOL);
        newTools.needFilterActivity = false;
        newTools.allowMasking = false;
        newTools.allowHistogram = false;
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.flip));
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.mirror(bmp);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Stickers", Category.TOOL);
        newTools.allowScrollZoom=false;
        newTools.allowMasking = false;
        newTools.allowHistogram = false;
        newTools.setSeekBar1(1,1,10,"size","unit");
        newTools.setSeekBar2(0,0,360,"rotation","deg");
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.stickers));
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.putSticker(bmp,touchDown,BitmapFactory.decodeResource(context.getResources(),R.drawable.stickers),(int)seekBar,(int)seekBar2);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Luminosity", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.luminosity));
        newTools.setSeekBar1(-100, 0, 100, "Brightness","%");
        newTools.setSeekBar2(-100, 0, 100, "Gamma", "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.brightness(bmp, seekBar);
                FilterFunction.gamma(bmp, seekBar2);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Contrast", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.contrast));
        newTools.setSeekBar1(-50, 0, 50, "Contrast","%");
        newTools.setSeekBar2(-100, 0, 100, "Offset","%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.contrastBurn(bmp, seekBar);
                FilterFunction.burnValues(bmp, seekBar2);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Sharpness", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.sharpness));
        newTools.setSeekBar1(-100, 0, 100, "Intensity","%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.sharpen(bmp, seekBar);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Auto", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.auto));
        newTools.setSwitch1(false, "Linear", "Dynamic");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                if (switch1) {
                    FilterFunction.histogramEqualization(bmp);
                } else {
                    FilterFunction.toExtDyn(bmp,0, 255);
                }
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Saturation", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.saturation));
        newTools.setSeekBar1(0, 100, 200, "Intensity", "%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.saturation(bmp, seekBar);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Add noise", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.add_noise));
        newTools.setSeekBar1(0, 0, 255, "Amount visible","");
        newTools.setSwitch1(false,"B&W Noise", "Color Noise");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.noise(bmp, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Temperature", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.temperature));
        newTools.setSeekBar1(-100, 0, 100, "Cold <---> Warm","%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.temperature(bmp, seekBar);
                return null;
            }
        });
        filters.add(newTools);

        newTools = new Filter("Tint", Category.TOOL);
        newTools.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.tint));
        newTools.setSeekBar1(-100, 0, 100, "Green <---> Magenta","%");
        newTools.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.tint(bmp, seekBar);
                return null;
            }
        });
        filters.add(newTools);

    }

    private static void createFilters(Context context) {
        Filter newFilter;

        // Filters > Color

        newFilter = new Filter("Colorize", Category.COLOR);
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(0,100,100,"Saturation","");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.colorize(bmp, colorSeekHue, seekBar, true);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Change hue", Category.COLOR);
        newFilter.setColorSeekBar();
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.changeHue(bmp, colorSeekHue);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Selective coloring", Category.COLOR);
        newFilter.setColorSeekBar();
        newFilter.setSeekBar1(1, 25, 360, "Sensibility","deg");
        newFilter.setSwitch1(false, "Keep", "Remove");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                if (switch1) {
                    FilterFunction.removeAColor(bmp, colorSeekHue, (int) seekBar);
                } else {
                    FilterFunction.keepAColor(bmp, colorSeekHue, (int) seekBar);
                }
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Hue shift", Category.COLOR);
        newFilter.setSeekBar1(-180, 0, 180, "Shift amount", "deg");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.hueShift(bmp,seekBar);
                return null;
            }
        });
        filters.add(newFilter);


        // End of Filters > Color


        // Filters > Fancy

        newFilter = new Filter("Threshold", Category.FANCY);
        newFilter.setSeekBar1(0, 128, 256, "Threshold value","");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.threshold(bmp, seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Posterize", Category.FANCY);
        newFilter.setSeekBar1(2, 10, 32, "Number of values","steps");
        newFilter.setSwitch1(true,"Color", "B&W");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.posterize(bmp, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        // End of Filters > Fancy


        // Filters > Blur

        newFilter = new Filter("Average blur", Category.BLUR);
        newFilter.setSeekBar1(1, 2, 19, "Radius","px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.averageBlur(bmp, (int) seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Gaussian blur", Category.BLUR);
        newFilter.setSeekBar1(1, 2, 25, "Radius", "px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.gaussianBlur(bmp, (int) seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Directional blur", Category.BLUR);
        newFilter.setSeekBar1(2, 2, 30, "Radius", "");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.directionalBlur(bmp, (int) seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        // End of Filters > Blur


        // Filters > Contour
        newFilter = new Filter("Laplacian", Category.CONTOUR);
        newFilter.setSeekBar1(1, 2, 14, "Sensibility","px");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.laplacian(bmp, seekBar);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sobel", Category.CONTOUR);
        newFilter.setSeekBar1(1, 2, 14, "Sensibility","px");
        newFilter.setSwitch1(false, "Horizontal", "Vertical");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.sobel(bmp, seekBar, switch1);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Sketch", Category.CONTOUR);
        newFilter.setSeekBar1(1, 4, 14, "Contour finesse","");
        newFilter.setSeekBar2(0, 20, 100, "Color washing","");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                Bitmap texture = FileInputOutput.getBitmap(context.getResources(), R.drawable.canvas_texture, bmp.getWidth(), bmp.getHeight());

                // First layer
                Bitmap bmpCopy = ImageTools.bitmapClone(bmp);
                FilterFunction.laplacian(bmp, (int) seekBar);
                FilterFunction.invert(bmp);

                // Using layer 1's luminosity and apply it to layer 2
                FilterFunction.applyTexture(bmp, bmpCopy, BlendType.LUMINOSITY, seekBar2);

                FilterFunction.applyTexture(bmp, texture);
                return null;
            }
        });
        filters.add(newFilter);

        newFilter = new Filter("Cartoon", Category.CONTOUR);
        newFilter.setSeekBar1(1, 0, 100, "Black value","px");
        newFilter.setSeekBar2(2, 4, 14, "Posterization","");
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                FilterFunction.cartoon(bmp, (int) seekBar, (int) seekBar2);
                return null;
            }
        });
        filters.add(newFilter);
    }

    private static void createSpecial(Context context) {

        Filter newFilter;
        newFilter = new Filter(Settings.FILTER_MASK_NAME, Category.SPECIAL);
        newFilter.allowMasking = false;
        newFilter.allowHistogram = false;
        newFilter.allowScrollZoom = false;
        newFilter.setSeekBar1(5,30,300, "Brush radius","px");
        newFilter.setSeekBar2(0,50,100, "View opacity","%");
        newFilter.setSwitch1(true, "Black", "White");
        newFilter.seekBar1AutoRefresh = false;
        newFilter.switch1AutoRefresh = false;
        newFilter.setFilterPreviewFunction(new FilterPreviewInterface() {
            @Override
            public Bitmap preview(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                if (switch1) {
                    ImageTools.drawCircle(maskBmp, touchUp, (int) seekBar, Color.WHITE);
                } else {
                    ImageTools.drawCircle(maskBmp, touchUp, (int) seekBar, Color.BLACK);
                }
                FilterFunction.applyTexture(bmp, maskBmp, BlendType.OVERLAY, seekBar2);
                return null;
            }
        });

        newFilter.setFilterApplyFunction(new FilterApplyInterface() {
            @Override
            public Bitmap apply(Bitmap bmp, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
                return maskBmp;
            }
        });
        filters.add(newFilter);

    }

}

