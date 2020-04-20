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

/**
 * Those menus are used in the bottom part of the UI, in the MainActivity.
 * It can be the main menus, such as Filters, Tools, Presets, or the submenus in Filters.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
public class BottomMenu {

    /**
     * A list of all filters, associated with their corresponding TextView (their visual
     * representation on the UI).
     */
    private static final List<DisplayedFilter> displayedFilters = new ArrayList<>();

    /**
     * A list of all create bottomMenus.
     */
    private static final List<BottomMenu> bottomMenus = new ArrayList<>();

    public static Typeface selected;
    public static Typeface unselected;

    /**
     * The image used for the miniatures.
     */
    public static Bitmap currentImage;

    /**
     * The miniatures are only calculated once when the image is changed.
     * Also, they are only calculated when the menu is opened.
     */
    private boolean needRefreshMiniature;

    /**
     * Each menu displays the filter of one category.
     * This category can be null if no filter should be displayed, or if its a menu containing
     * submenus.
     */
    private final Category category;

    /**
     * The layout element that is turned visible when the menu is opened.
     */
    private final ViewGroup bar;

    /**
     * The layout element which contain the TextView for each filter of that category.
     */
    private final ViewGroup container;

    /**
     * The button that is used to open or close the menu.
     * If this menu is a submenu, clicking on the button while the menu is opened won't close
     * the menu as it would lead to an empty parent menu.
     */
    private final Button button;

    /**
     * Is null by default. Used to set which menu is the parent.
     */
    private final BottomMenu parent;

    private ViewGroup   toolsLineOne;
    private ViewGroup   toolsLineTwo;
    private ViewGroup   toolsLineThree;

    /**
     * Used to display the tools.
     * There is only 4 tools by row in the gridLayout,
     * and this value is used to return to a new row.
     */
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

    /**
     * Right now this is the way we decided to used to save those element.
     * Maybe it is possible to retrieve those from the tool's bar itself.
     * @param toolsLineOne the first line
     * @param toolsLineTwo the second line
     * @param toolsLineThree the third line
     */
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

    /**
     * Refreshes/Generates the miniatures of filter of one category.
     * As each menu are one category, it is the same as refreshing all the filters from a menu.
     * @param onlyThisCategory the category of filter's miniature to refresh.
     */
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

                    displayedFilter.textView.setCompoundDrawablePadding(Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL);
                    displayedFilter.textView.setCompoundDrawables(null, drawable,null,null);
                }
            }
        }


    }

    /**
     * Generate the visual representation of a filter.
     * It can be a miniature with a title for the Filters and Preset,
     * or a icon and title for the Tools. Also creates the listener for it.
     * @param filter the filter from which the TextView is generated.
     * @param context the context to use
     * @return a TextView
     */
    static private TextView generateATextView(final Filter filter, Context context) {
        TextView textView;
        textView = new TextView(context);
        textView.setClickable(true);
        textView.setText(filter.getName());
        textView.setAllCaps(true);
        ColorTheme.textView(textView);
        textView.setTextSize(Settings.MINIATURE_AND_TOOL_TEXT_SIZE_SP);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setBackgroundColor(Color.TRANSPARENT);

        if (filter.getFilterCategory() == Category.TOOL) {
            textView.setMaxWidth(Settings.TOOL_DISPLAYED_SIZE);
            textView.setHeight(Settings.TOOL_DISPLAYED_SIZE + Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL + Settings.MINIATURE_AND_TOOL_TEXT_SIZE * 3);
            TableRow.LayoutParams params = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT,4);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);
        } else {
            textView.setMaxLines(2);
            textView.setHorizontallyScrolling(false);
            textView.setMaxWidth(Settings.MINIATURE_DISPLAYED_SIZE);
            textView.setHeight(Settings.MINIATURE_DISPLAYED_SIZE + Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL + Settings.MINIATURE_AND_TOOL_TEXT_SIZE * 3);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);

        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.subActivityFilter = filter;
                MainActivity.getMenuItemListener().onClick(v);
            }
        });

        return textView;
    }

    /**
     * Close all menus except itself.
     * If it is a submenu, its parent will also be kept open.
     */
    private void closeOtherMenus() {
        for (BottomMenu menu:bottomMenus) {
            if (menu != this && menu != this.parent) {
                menu.close();
            }
        }
    }

    /**
     * Close itself.
     */
    private void close() {
        button.setTypeface(unselected);
        ColorTheme.button(button, parent != null);
        bar.setVisibility(View.GONE);
    }

    /**
     * Open itself.
     */
    private void open() {
        ColorTheme.button(this.button, true);
        button.setTypeface(selected);
        bar.setVisibility(View.VISIBLE);

        for (BottomMenu menu:bottomMenus) {
            if (menu.parent == this) {
                menu.open();
                break;
            }
        }

        generateMiniatureForOpenedMenu();
    }

    /**
     * Has to be called before using the menus to populate them.
     */
    static public void initializeMenus(){

        TextView textView;

        for (BottomMenu bottomMenu:bottomMenus) {
            for (final Filter currentFilter:Filter.filters) {
                if (bottomMenu.category == currentFilter.getFilterCategory()) {

                    textView = BottomMenu.generateATextView(currentFilter, MainActivity.getAppContext());

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

    /**
     * Close all menus
     */
    static public void closeMenus(){
        for (BottomMenu bottomMenu:bottomMenus) {
            if (bottomMenu.parent == null) {
                bottomMenu.button.setTypeface(unselected);
                ColorTheme.button(bottomMenu.button, false);
                bottomMenu.bar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Invalidates all miniatures, which mean the image has changed.
     */
    static public void invalidateMiniatures() {
        for (BottomMenu bottomMenu:bottomMenus) {
            bottomMenu.needRefreshMiniature = true;
            bottomMenu.generateMiniatureForOpenedMenu();
        }
    }

    /**
     * Apply the proper color for the UI element, in accordance with the global theme.
     */
    static void applyColorTheme() {
        for (BottomMenu bottomMenu:bottomMenus) {
            ColorTheme.button(bottomMenu.button, bottomMenu.parent != null);
            bottomMenu.button.setTypeface(unselected);
            ColorTheme.background(bottomMenu.bar, true);
        }
        for (DisplayedFilter displayedFilter:displayedFilters) {
            ColorTheme.icon(displayedFilter);
        }
    }
}
