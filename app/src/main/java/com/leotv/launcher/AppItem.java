package com.leotv.launcher;



import android.graphics.drawable.Drawable;



public class AppItem {

    public final String label;

    public final String pkg;

    public final Drawable icon;

    public final Drawable banner;



    public AppItem(String label, String pkg, Drawable icon, Drawable banner) {

        this.label = label;

        this.pkg = pkg;

        this.icon = icon;

        this.banner = banner;

    }

}
