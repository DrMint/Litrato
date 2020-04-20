package com.example.litrato.activities.tools;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.litrato.filters.AppliedFilter;
import com.example.litrato.tools.ImageTools;

import java.util.ArrayList;

/**
 * This class is used to save the user's actions on the image.
 * It is a array of AppliedFilter and gives us the ability to "rewind" to any prior state.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-20-04
 */
public class History {

    private final ArrayList<AppliedFilter> historic;

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

    /**
     * Returns the image as it was at the given state.
     * Takes an image and apply the filters successively until reaching our target.
     * The original image doesn't have to be the same used while saving those states.
     * If the image ratio is different, some filter such as Crop might not work properly.
     * @param originalImage the image to apply part of the history to.
     * @param index the state to return to. This state must be between 0 and historic.size() - 1.
     * @param context used by the AppliedFilters.
     * @return the image as it was at the state given by in parameter.
     */
    public Bitmap goUntilFilter(Bitmap originalImage, int index, Context context) {
            Bitmap copy = ImageTools.bitmapClone(originalImage);
            for (int i = 0; i <= index; i++) {
                Bitmap result = historic.get(i).apply(copy, context);
                if (result != null) copy = result;
            }
            return copy;
    }

    /**
     * Remove all states following the given state (however this given state is kept).
     * @param index the state to keep, and all states before.
     */
    public void removeUntil(int index){
        historic.subList(index + 1, historic.size()).clear();
    }
}
