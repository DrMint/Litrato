package com.example.litrato.activities.ui;

import android.widget.TextView;

import com.example.litrato.filters.Filter;

class DisplayedFilter {

    public final TextView textView;
    public final Filter filter;

    public DisplayedFilter(TextView textView, Filter filter) {
        this.textView = textView;
        this.filter = filter;
    }

}
