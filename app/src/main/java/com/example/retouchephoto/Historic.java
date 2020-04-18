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
        this.historic.removeAll(this.historic);
    }

    public void addFilter(AppliedFilter appFilter){
        this.historic.add(appFilter);
    }
    public void addFilter(Filter filter, Bitmap maskBmp, Context context, int colorSeekHue, float seekBar, float seekBar2, boolean switch1, Point touchDown, Point touchUp){
        AppliedFilter appFilter = new AppliedFilter(filter,maskBmp, context,colorSeekHue,seekBar,seekBar2, switch1,touchDown,touchUp);
        this.historic.add(appFilter);
    }
/*unused*/
    public Bitmap goBack(Bitmap originalImage){
        int index = this.historic.size()-1;
        Bitmap copy ;
        copy=ImageTools.bitmapClone(originalImage);
        if(index>=0) {
            this.historic.remove(index);
            for (AppliedFilter allFilter : this.historic) {
                allFilter.filter.apply(copy, allFilter.maskBmp, allFilter.context, allFilter.colorSeekHue, allFilter.seekBar, allFilter.seekBar2, allFilter.switch1, allFilter.touchDown, allFilter.touchUp);
            }
        }
        if(index==-1){
            return copy;
        }
        return copy;
    }

    public Bitmap goUntilFilter(Bitmap originalImage,int index) {
            Bitmap copy = ImageTools.bitmapClone(originalImage);
            if(index==0){
                return copy;
            }
            if(index!=0) {
                for (int i = 0; i < index; i++) {
                    AppliedFilter allFilter = this.historic.get(i);
                    allFilter.filter.apply(copy, allFilter.maskBmp, allFilter.context, allFilter.colorSeekHue, allFilter.seekBar, allFilter.seekBar2, allFilter.switch1, allFilter.touchDown, allFilter.touchUp);
                }
            }
            return copy;
    }

    public void removeUntil(int index){
        for (int i =1; i <=this.historic.size()-index; i++) {
            this.historic.remove(this.historic.size() - i);
        }
    }
}
