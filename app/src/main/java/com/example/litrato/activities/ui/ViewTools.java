package com.example.litrato.activities.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

public class ViewTools {

    public static boolean isVisible(View view) {
        return (view.getVisibility() == View.VISIBLE);
    }

    public static int convertDpToPixel(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int convertSpToPixel(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }


    }
