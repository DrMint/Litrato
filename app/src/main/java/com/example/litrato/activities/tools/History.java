package com.example.litrato.activities.tools;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.litrato.filters.AppliedFilter;
import com.example.litrato.tools.ImageTools;

import java.util.ArrayList;

public class History {

    private ArrayList<AppliedFilter> historic;

    public History() {
        this.historic = new ArrayList<>();
    }
    public AppliedFilter get(int index) {return historic.get(index);}
    public int size() {return historic.size();}
    public void clear(){
        this.historic.clear();
    }
    public void addFilter(AppliedFilter appFilter){
        this.historic.add(appFilter);
    }

    public Bitmap goUntilFilter(Bitmap originalImage, int index, Context context) {
            Bitmap copy = ImageTools.bitmapClone(originalImage);
            for (int i = 0; i <= index; i++) {
                Bitmap result = historic.get(i).apply(copy, context);
                if (result != null) copy = result;
            }
            return copy;
    }

    public void removeUntil(int index){
        historic.subList(index + 1, historic.size()).clear();
    }
}
