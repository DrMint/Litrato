package com.example.litrato.activities.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.example.litrato.activities.MainActivity;
import com.example.litrato.activities.tools.Preference;
import com.example.litrato.activities.tools.PreferenceManager;
import com.example.litrato.activities.tools.Settings;
import com.example.litrato.filters.Category;
import com.example.litrato.filters.Filter;
import com.example.litrato.tools.ImageTools;

import java.util.ArrayList;
import java.util.List;

public class BottomMenu {

    /**
     * A list of all filters, associated with their corresponding TextView (their visual
     * representation on the UI).
     */
    private static final List<DisplayedFilter> displayedFilters = new ArrayList<>();
    private static final List<BottomMenu> bottomMenus = new ArrayList<>();

    public static Typeface submenuSelected;
    public static Typeface submenuUnselected;
    public static Bitmap currentImage;

    private boolean needRefreshMiniature;
    private final Category category;
    private final ViewGroup bar;
    private final ViewGroup container;
    private final Button button;
    private final BottomMenu parent;

    private ViewGroup   toolsLineOne;
    private ViewGroup   toolsLineTwo;
    private ViewGroup   toolsLineThree;

    private int numberOfTools;

    @SuppressWarnings("WeakerAccess")
    public BottomMenu(final Button button, final ViewGroup bar, final ViewGroup container, final Category category, final BottomMenu parent) {
        this.needRefreshMiniature = true;
        this.category = category;
        this.bar = bar;
        this.container = container;
        this.button = button;
        this.parent = parent;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = ViewTools.isVisible(bar);
                closeOtherMenus();
                if (parent != null || !visible) {
                    open();
                } else {
                    close();
                }
            }
        });

        // We leave those because if no onClickListener is set, there are permeable to touch events.
        // That means that clicking on the their background will trigger an event to the object behind.
        bar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});

        bottomMenus.add(this);

    }

    public BottomMenu(final Button button, final ViewGroup bar, final Category category, final BottomMenu parent) {
        this(button, bar, bar, category, parent);
    }

    public BottomMenu(final Button button, final ViewGroup bar, final ViewGroup container, final Category category) {
        this(button, bar, container, category, null);
    }

    public BottomMenu(final Button button, final ViewGroup bar, final Category category) {
        this(button, bar, bar, category, null);
    }

    public void setToolsRows(ViewGroup toolsLineOne, ViewGroup toolsLineTwo, ViewGroup toolsLineThree) {
        this.toolsLineOne = toolsLineOne;
        this.toolsLineTwo = toolsLineTwo;
        this.toolsLineThree = toolsLineThree;
    }

    /**
     * Refreshes/Generates the miniatures of the currently opened.
     * If no menu is opened, no image is generated.
     */
    private void generateMiniatureForOpenedMenu() {
        if (ViewTools.isVisible(bar) && needRefreshMiniature) {
            generateMiniatures(category);
            needRefreshMiniature = false;
        }
    }

    private void generateMiniatures(Category onlyThisCategory) {

        Bitmap resizedMiniature = ImageTools.toSquare(
                currentImage,
                PreferenceManager.getInt(MainActivity.getAppContext(), Preference.MINIATURE_BMP_SIZE)
        );

        for (DisplayedFilter displayedFilter:displayedFilters) {

            // Only generate the miniature if the displayedFilter of this category
            if (displayedFilter.filter.getFilterCategory() == onlyThisCategory) {

                Drawable drawable;
                if(onlyThisCategory != Category.TOOL) {
                    Bitmap filteredMiniature =  ImageTools.bitmapClone(resizedMiniature);

                    // Apply the filter to the miniature
                    Bitmap result = displayedFilter.filter.apply(filteredMiniature, MainActivity.getAppContext());
                    if (result != null) filteredMiniature = result;

                    // Add the image on top of the text
                    drawable = new BitmapDrawable(MainActivity.getAppContext().getResources(), filteredMiniature);
                    drawable.setBounds(0, 0, Settings.MINIATURE_DISPLAYED_SIZE, Settings.MINIATURE_DISPLAYED_SIZE);

                    displayedFilter.textView.setCompoundDrawablePadding(25);
                    displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
                }
            }
        }


    }

    static private TextView generateATextView(Filter filter, Context context) {
        TextView textView;
        textView = new TextView(context);
        textView.setClickable(true);
        textView.setText(filter.getName());
        textView.setAllCaps(true);
        textView.setMaxLines(2);
        textView.setHorizontallyScrolling(false);
        textView.setTextColor(Settings.COLOR_TEXT);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setBackgroundColor(Color.TRANSPARENT);

        if (filter.getFilterCategory() == Category.TOOL) {

            textView.setMaxWidth(Settings.TOOL_DISPLAYED_SIZE);
            textView.setHeight((int) (Settings.TOOL_DISPLAYED_SIZE * 1.8));
            TableRow.LayoutParams params = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT,4);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);

        } else {

            textView.setMaxWidth(Settings.MINIATURE_DISPLAYED_SIZE);
            textView.setHeight((int) (Settings.MINIATURE_DISPLAYED_SIZE * 1.4));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);

        }
        return textView;
    }

    private void closeOtherMenus() {
        for (BottomMenu menu:bottomMenus) {
            if (menu != this && menu != this.parent) {
                menu.close();
            }
        }
    }

    private void close() {
        button.setTypeface(submenuUnselected);
        if (parent == null) button.setBackgroundColor(Settings.COLOR_GREY);
        bar.setVisibility(View.GONE);
    }

    private void open() {
        this.button.setBackgroundColor(Settings.COLOR_SELECTED);
        button.setTypeface(submenuSelected);
        bar.setVisibility(View.VISIBLE);

        for (BottomMenu menu:bottomMenus) {
            if (menu.parent == this) {
                menu.open();
                break;
            }
        }

        generateMiniatureForOpenedMenu();
    }

    static public void initializeMenus(){

        TextView textView;

        for (BottomMenu bottomMenu:bottomMenus) {
            for (final Filter currentFilter:Filter.filters) {
                if (bottomMenu.category == currentFilter.getFilterCategory()) {

                    textView = BottomMenu.generateATextView(currentFilter, MainActivity.getAppContext());
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.subActivityFilter = currentFilter;
                            MainActivity.getMenuItemListener().onClick(v);
                        }
                    });

                    if (currentFilter.getFilterCategory() == Category.TOOL) {
                        switch (bottomMenu.numberOfTools / 4) {
                            case 0: bottomMenu.toolsLineOne.addView(textView); break;
                            case 1: bottomMenu.toolsLineTwo.addView(textView); break;
                            case 2: bottomMenu.toolsLineThree.addView(textView); break;
                        }
                        bottomMenu.numberOfTools++;
                    } else {
                        bottomMenu.container.addView(textView);
                    }

                    displayedFilters.add(new DisplayedFilter(textView, currentFilter));
                }
            }
        }
    }

    static public void closeMenus(){
        for (BottomMenu bottomMenu:bottomMenus) {
            if (bottomMenu.parent == null) {
                bottomMenu.button.setTypeface(submenuUnselected);
                bottomMenu.button.setBackgroundColor(Settings.COLOR_GREY);
                bottomMenu.bar.setVisibility(View.GONE);
            }
        }
    }

    static public void invalidateMiniatures() {
        for (BottomMenu bottomMenu:bottomMenus) {
            bottomMenu.needRefreshMiniature = true;
            bottomMenu.generateMiniatureForOpenedMenu();
        }
    }

    static public void applyColorTheme() {
        for (BottomMenu bottomMenu:bottomMenus) {
            bottomMenu.button.setTypeface(submenuUnselected);
            bottomMenu.button.setTextColor(Settings.COLOR_TEXT);
            if (bottomMenu.parent == null) bottomMenu.button.setBackgroundColor(Settings.COLOR_GREY);
            bottomMenu.bar.setBackgroundColor(Settings.COLOR_SELECTED);
        }

        for (DisplayedFilter displayedFilter:displayedFilters) {
            displayedFilter.textView.setTextColor(Settings.COLOR_TEXT);
            if (displayedFilter.filter.getFilterCategory() == Category.TOOL) {
                Drawable drawable = ImageTools.getThemedIcon(MainActivity.getAppContext(), displayedFilter.filter.getIcon());
                displayedFilter.textView.setCompoundDrawablePadding(25);
                displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
            }
        }
    }
}
