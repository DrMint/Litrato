package com.example.litrato.activities.ui;

import android.widget.TextView;

import com.example.litrato.filters.Filter;

/**
 * A displayed filter is a filter along its visual representation.
 *
 * @author Thomas Barillot, Rodin Duhayon, Alex Fournier, Marion de Oliveira
 * @version 1.0
 * @since   2020-31-01
 */
class DisplayedFilter {

    public final TextView textView;
    public final Filter filter;

    public DisplayedFilter(TextView textView, Filter filter) {
        this.textView = textView;
        this.filter = filter;
    }

}
