package com.canzhang.floatview;

import android.content.Context;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;


public class EnFloatingView extends FloatingMagnetView {


    public EnFloatingView(@NonNull Context context) {
        this(context, R.layout.en_floating_view);
    }

    public EnFloatingView(@NonNull Context context, @LayoutRes int resource) {
        super(context, null);
        inflate(context, resource, this);
    }


    public EnFloatingView(@NonNull Context context, View view) {
        super(context, null);
        this.removeAllViews();
        this.addView(view);
    }

}
