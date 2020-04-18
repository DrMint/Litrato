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
    public AppliedFilter(Filter filter,Context context){
        this.filter=filter;
        this.maskBmp =Bitmap.createBitmap(1,1, Bitmap.Config.ALPHA_8);
        this.context = context;
        this.colorSeekHue = 0;
        this.seekBar = 0;
        this.seekBar2 = 0;
        this.switch1 = false;
        this.touchDown = new Point(0,0);
        this.touchUp = new Point(0,0);
    }
}
