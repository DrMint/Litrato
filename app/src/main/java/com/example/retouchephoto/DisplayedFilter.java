package com.example.retouchephoto;

import android.widget.TextView;

class DisplayedFilter {

    final TextView textView;
    final Filter filter;

    DisplayedFilter(TextView textView, Filter filter) {
        this.textView = textView;
        this.filter = filter;
    }

}
