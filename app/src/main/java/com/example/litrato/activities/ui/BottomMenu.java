package com.example.litrato.activities.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.example.litrato.activities.FiltersActivity;
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
     * A list of all filters, associated with their corresponding TextView (their visual
     * representation on the UI).
     */
    private final List<Bitmap> bitmaps = new ArrayList<>();

    /**
     * A list of all create bottomMenus.
     */
    //private List<BottomMenu> bottomMenus;

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

    private final MenuType type;

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
    public final BottomMenu parent;

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
    public BottomMenu(final Button button, final ViewGroup bar, final ViewGroup container, final Category category, final MenuType type, final BottomMenu parent) {
        this.needRefreshMiniature = true;
        this.category = category;
        this.bar = bar;
        this.container = container;
        this.button = button;
        this.parent = parent;
        this.type = type;

        if (type != MenuType.BITMAPS) {
            button.setOnClickListener(MainActivity.getMenuButtonListener());
        }

        // We leave those because if no onClickListener is set, there are permeable to touch events.
        // That means that clicking on the their background will trigger an event to the object behind.
        bar.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {}});
    }

    public BottomMenu(final Button button, final ViewGroup bar, final Category category, final MenuType type, final BottomMenu parent) {
        this(button, bar, bar, category, type, parent);
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
     * @param textView the textView to initialize
     */
     private void initializeTextView(final TextView textView) {
        textView.setClickable(true);
        textView.setAllCaps(true);
        ColorTheme.textView(textView);
        textView.setTextSize(Settings.MINIATURE_AND_TOOL_TEXT_SIZE_SP);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setBackgroundColor(Color.TRANSPARENT);

        if (this.type == MenuType.TOOLS) {
            textView.setMaxWidth(Settings.TOOL_DISPLAYED_SIZE);
            textView.setHeight(Settings.TOOL_DISPLAYED_SIZE + Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL + Settings.MINIATURE_AND_TOOL_TEXT_SIZE * 3);
            TableRow.LayoutParams params = new TableRow.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT,4);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);
        } else if (this.type == MenuType.MINIATURE) {
            textView.setMaxLines(2);
            textView.setHorizontallyScrolling(false);
            textView.setMaxWidth(Settings.MINIATURE_DISPLAYED_SIZE);
            textView.setHeight(Settings.MINIATURE_DISPLAYED_SIZE + Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL + Settings.MINIATURE_AND_TOOL_TEXT_SIZE * 3);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2,Settings.ITEMS_MARGIN_IN_MENU,Settings.ITEMS_MARGIN_IN_MENU * 2);
            textView.setLayoutParams(params);
        } else if (this.type == MenuType.BITMAPS) {
            textView.setMaxWidth(Settings.STICKERS_DISPLAYED_SIZE);
            textView.setHeight(Settings.STICKERS_DISPLAYED_SIZE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,5,5,5);
            textView.setLayoutParams(params);
        }
    }

    /**
     * Close itself.
     */
    public void close() {
        button.setTypeface(unselected);
        ColorTheme.button(button, parent != null);
        bar.setVisibility(View.GONE);
    }

    /**
     * Open itself.
     */
    public void open() {
        ColorTheme.button(this.button, true);
        button.setTypeface(selected);
        bar.setVisibility(View.VISIBLE);

        if (this.parent != null) parent.open();
        generateMiniatureForOpenedMenu();
    }

    @SuppressWarnings("WeakerAccess")
    public void initialize(List<?> list) {
        TextView textView;

        if (this.type != MenuType.BITMAPS) {
            Filter currentFilter;
            for (int i = 0; i < list.size(); i++) {
                currentFilter = (Filter) list.get(i);
                if (this.category == currentFilter.getFilterCategory()) {

                    textView = new TextView(MainActivity.getAppContext());
                    initializeTextView(textView);
                    textView.setText(currentFilter.getName());

                    if (currentFilter.getFilterCategory() == Category.TOOL) {
                        switch (this.numberOfTools / 4) {
                            case 0: this.toolsLineOne.addView(textView); break;
                            case 1: this.toolsLineTwo.addView(textView); break;
                            case 2: this.toolsLineThree.addView(textView); break;
                        }
                        this.numberOfTools++;
                    } else {
                        this.container.addView(textView);
                    }

                    final Filter selectedFilter = currentFilter;
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainActivity.subActivityFilter = selectedFilter;
                            MainActivity.getMenuItemListener().onClick(v);
                        }
                    });

                    displayedFilters.add(new DisplayedFilter(textView, currentFilter));
                }
            }

        } else {

            Bitmap currentBitmap;
            for (int i = 0; i < list.size(); i++) {
                currentBitmap = (Bitmap) list.get(i);

                textView = new TextView(MainActivity.getAppContext());
                initializeTextView(textView);

                final int bitmapIndex = i;

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FiltersActivity.selectedMenuItem = bitmapIndex;
                        FiltersActivity.getMenuItemListener().onClick(v);
                    }
                });

                Drawable drawable = new BitmapDrawable(MainActivity.getAppContext().getResources(), currentBitmap);
                drawable.setBounds(0, 0, Settings.STICKERS_DISPLAYED_SIZE, Settings.STICKERS_DISPLAYED_SIZE);

                textView.setCompoundDrawablePadding(Settings.PADDING_BETWEEN_MINIATURE_AND_LABEL);
                textView.setCompoundDrawables(null, drawable,null,null);

                switch (this.numberOfTools % 3) {
                    case 0:
                        this.toolsLineOne.addView(textView);
                        break;
                    case 1:
                        this.toolsLineTwo.addView(textView);
                        break;
                    case 2:
                        this.toolsLineThree.addView(textView);
                        break;
                }
                this.numberOfTools++;

                bitmaps.add(currentBitmap);
            }
        }
    }

    /**
     * Close all menus
     */
    public static void closeMenus(List<BottomMenu> bottomMenus){
        for (BottomMenu bottomMenu:bottomMenus) {
            if (bottomMenu.type != MenuType.BITMAPS) {
                bottomMenu.close();
            }
        }
    }

    /**
     * Invalidates all miniatures, which mean the image has changed.
     */
    public static void invalidateMiniatures(List<BottomMenu> bottomMenus) {
        for (BottomMenu bottomMenu:bottomMenus) {
            if (bottomMenu.type != MenuType.BITMAPS) {
                bottomMenu.needRefreshMiniature = true;
                bottomMenu.generateMiniatureForOpenedMenu();
            }
        }
    }

    /**
     * Apply the proper color for the UI element, in accordance with the global theme.
     */
    public static void applyColorTheme(List<BottomMenu> bottomMenus) {
        for (BottomMenu bottomMenu:bottomMenus) {
            ColorTheme.button(bottomMenu.button, bottomMenu.parent != null);
            bottomMenu.button.setTypeface(unselected);
            ColorTheme.background(bottomMenu.bar, true);
        }
        for (DisplayedFilter displayedFilter:displayedFilters) {
            ColorTheme.icon(displayedFilter);
        }
    }

    public static BottomMenu getMenuByItsButton(List<BottomMenu> bottomMenus, Button button) {
        for (BottomMenu bottomMenu:bottomMenus) {
            if (bottomMenu.button == button) {
                return bottomMenu;
            }
        }
        return null;
    }

    public boolean isOpened() {
        return ViewTools.isVisible(this.bar);
    }
}
