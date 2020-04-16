package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

public class Historic {
    ArrayList<AppliedFilter> historic;

    public Historic() {
        this.historic = new ArrayList<AppliedFilter>();
    }

    public ArrayList<AppliedFilter> getHistoric() {
        return historic;
    }

    public void init(){
        historic.clear();
    }

    public void addFilter(Filter filter, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp){
        AppliedFilter appFilter = new AppliedFilter(filter,maskBmp, context,colorSeekHue,seekBar,seekBar2, switch1,touchDown,touchUp);
        this.historic.add(appFilter);
    }

    public Bitmap goBack(Bitmap originalImage){
        int index = this.historic.size()-1;
        Bitmap copy =Bitmap.createBitmap(originalImage);
        if(index>=0) {
            this.historic.remove(index);
            for (AppliedFilter allFilter : this.historic) {
                Log.i("hello",allFilter.filter.getName());
                allFilter.filter.apply(copy, allFilter.maskBmp, allFilter.context, allFilter.colorSeekHue, allFilter.seekBar, allFilter.seekBar2, allFilter.switch1, allFilter.touchDown, allFilter.touchUp);
            }
        }
        if(index==-1){
            return copy;
        }
        return copy;
    }
}
