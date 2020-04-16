package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;

public class AppliedFilter {
    Filter filter;
    Bitmap maskBmp;
    Context context;
    int colorSeekHue;
    float seekBar;
    float seekBar2;
    boolean switch1;
    Point touchDown;
    Point touchUp;

    public AppliedFilter(Filter filter, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp) {
        this.filter = filter;
        this.maskBmp = maskBmp;
        this.context = context;
        this.colorSeekHue = colorSeekHue;
        this.seekBar = seekBar;
        this.seekBar2 = seekBar2;
        this.switch1 = switch1;
        this.touchDown = touchDown;
        this.touchUp = touchUp;
    }
}
