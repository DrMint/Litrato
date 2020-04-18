package com.example.retouchephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

class Historic {

    private ArrayList<AppliedFilter> historic;

    Historic() {
        this.historic = new ArrayList<>();
    }

    /*ArrayList<AppliedFilter> getHistoric() {
        return historic;
    }

     */

    AppliedFilter get(int index) {return historic.get(index);}
    int size() {return historic.size();}

    void clear(){
        this.historic.clear();
    }

    void addFilter(AppliedFilter appFilter){
        this.historic.add(appFilter);
    }

    /*
    void addFilter(Filter filter, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp){
        AppliedFilter appFilter = new AppliedFilter(filter, maskBmp, context, colorSeekHue, seekBar, seekBar2, switch1, touchDown, touchUp);
        this.historic.add(appFilter);
    }

    Bitmap goBack(Bitmap originalImage){
        int index = this.historic.size()-1;
        Bitmap copy ;
        copy=ImageTools.bitmapClone(originalImage);
        if(index >= 0) {
            this.historic.remove(index);
            for (AppliedFilter allFilter : this.historic) {
                allFilter.apply(copy);
            }
        }
        if(index == -1){
            return copy;
        }
        return copy;
    }

     */

    Bitmap goUntilFilter(Bitmap originalImage, int index, Context context) {
            Bitmap copy = ImageTools.bitmapClone(originalImage);
            for (int i = 0; i <= index; i++) {
                Bitmap result = historic.get(i).apply(copy, context);
                if (result != null) copy = result;
            }
            return copy;
    }

    void removeUntil(int index){
        historic.subList(index + 1, historic.size()).clear();
    }
}
