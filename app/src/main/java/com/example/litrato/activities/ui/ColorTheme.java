package com.example.litrato.activities.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.litrato.R;
import com.example.litrato.activities.MainActivity;
import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.filters.Category;
import com.example.litrato.tools.ImageTools;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.Objects;

/**
 * ColorTheme is used to change the color and style of UI elements to reflect the global style.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class ColorTheme {

    /**
     * Change the colors used by the class, depending on which global theme is used.
     * @param darkMode is it in dark mode ?
     */
    private static void setColorTheme(boolean darkMode) {
        if (darkMode) {
            COLOR_BACKGROUND = Color.rgb(0,0,0);
            COLOR_GREY = Color.rgb(40,40,40);
            COLOR_SELECTED = Color.rgb(70,70,70);
            COLOR_TEXT = Color.rgb(255,255,255);
        } else {
            COLOR_BACKGROUND = Color.rgb(255,255,255);
            COLOR_GREY = Color.rgb(215,215,215);
            COLOR_SELECTED = Color.rgb(190,190,190);
            COLOR_TEXT = Color.rgb(0,0,0);
        }
    }

    private static int COLOR_BACKGROUND;
    private static int COLOR_GREY;
    private static int COLOR_SELECTED;
    private static int COLOR_TEXT;



    public static void setColorTheme(final Context context) {
        setColorTheme(PreferenceManager.getBoolean(context, Preference.DARK_MODE));
    }

    public static void window(final Context context, final Window window) {

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(COLOR_BACKGROUND);
        window.getDecorView().setBackgroundColor(COLOR_BACKGROUND);
        window.setNavigationBarColor(COLOR_GREY);

        if (!PreferenceManager.getBoolean(context, Preference.DARK_MODE)) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public static void toolBar(final Context context, final Toolbar toolbar, final ActionBar actionBar) {

        if (!PreferenceManager.getBoolean(context, Preference.DARK_MODE)) {
            toolbar.setPopupTheme(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            toolbar.setPopupTheme(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        Objects.requireNonNull(actionBar).setTitle("");

        icon(context, toolbar.getMenu().getItem(0), R.drawable.open);
        icon(context, toolbar.getMenu().getItem(1), R.drawable.history);
        icon(context, toolbar.getMenu().getItem(2), R.drawable.save);
        icon(context, toolbar.getMenu().getItem(4), R.drawable.rotateleft);
        icon(context, toolbar.getMenu().getItem(5), R.drawable.rotateright);
        icon(context, toolbar, R.drawable.overflow);
    }

    public static void switchL(final Switch layoutSwitch) {
        layoutSwitch.setThumbTintList(thumbStates());
        layoutSwitch.setTrackTintList(trackStates());
        layoutSwitch.setTextColor(COLOR_TEXT);
        layoutSwitch.setTrackTintMode(PorterDuff.Mode.OVERLAY);
    }

    public static void seekBar(final SeekBar seekBar) {
        seekBar.setThumbTintList(thumbStates());
        //seekBar.getProgressDrawable().setColorFilter(Settings.COLOR_TEXT, PorterDuff.Mode.SRC_IN);
    }

    public static void textView(final TextView textView) {
        textView.setTextColor(COLOR_TEXT);
    }

    public static void background(final View view, boolean state) {
        if (state) {
            view.setBackgroundColor(COLOR_SELECTED);
            view.setBackgroundColor(COLOR_SELECTED);
        } else {
            view.setBackgroundColor(COLOR_GREY);
        }
    }

    public static void spinner(final Spinner spinner) {
        spinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) spinner.getSelectedView()).setTextColor(COLOR_TEXT);
            }
        });
    }

    public static void button(final Button button, boolean state) {
        textView(button);
        background(button, state);
    }

    public static void googleMap(final Context context, final GoogleMap googleMap) {
        if (PreferenceManager.getBoolean(context, Preference.DARK_MODE)) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_gmap_night));
        }
    }

    public static void icon(final Context context, final ImageView imageView, int drawable) {
        imageView.setImageDrawable(ImageTools.getThemedIcon(context, drawable));
    }

    @SuppressWarnings("WeakerAccess")
    public static void icon(final Context context, final MenuItem menuItem, int drawable) {
        menuItem.setIcon(ImageTools.getThemedIcon(context, drawable));
    }

    @SuppressWarnings("WeakerAccess")
    public static void icon(final Context context, final Toolbar toolbar, int drawable) {
        toolbar.setOverflowIcon(ImageTools.getThemedIcon(context, drawable));
    }

    public static void icon(final DisplayedFilter displayedFilter) {
        displayedFilter.textView.setTextColor(ColorTheme.COLOR_TEXT);
        if (displayedFilter.filter.getFilterCategory() == Category.TOOL) {
            Drawable drawable = ImageTools.getThemedIcon(MainActivity.getAppContext(), displayedFilter.filter.getIcon());
            displayedFilter.textView.setCompoundDrawablePadding(Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL);
            displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
        }
    }

    public static void bottomMenu() {
        BottomMenu.applyColorTheme();
    }

    private static ColorStateList thumbStates() {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        COLOR_TEXT,
                        COLOR_TEXT,
                        COLOR_TEXT
                }
        );
    }

    private static ColorStateList trackStates() {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{}
                },
                new int[]{
                        COLOR_GREY,
                        COLOR_GREY
                }
        );
    }

}
