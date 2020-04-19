package com.example.litrato.activities.tools;

import android.widget.TextView;

import com.example.litrato.filters.Filter;

public class DisplayedFilter {

    public final TextView textView;
    public final Filter filter;

    public DisplayedFilter(TextView textView, Filter filter) {
        this.textView = textView;
        this.filter = filter;
    }

}
