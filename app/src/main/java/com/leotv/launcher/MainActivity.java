package com.leotv.launcher;



import android.app.Activity;

import android.content.Intent;

import android.content.pm.PackageManager;

import android.content.pm.ResolveInfo;

import android.graphics.Color;

import android.graphics.Typeface;

import android.graphics.drawable.Drawable;

import android.graphics.drawable.GradientDrawable;

import android.os.Bundle;

import android.text.TextUtils;

import android.view.Gravity;

import android.view.View;

import android.widget.FrameLayout;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.widget.ScrollView;

import android.widget.TextView;



import java.util.ArrayList;

import java.util.Collections;

import java.util.HashSet;

import java.util.List;

import java.util.Set;



public class MainActivity extends Activity {



    private static final int BLUE = Color.parseColor("#2F7BFF");

    private static final int CARD = Color.parseColor("#1C2236");

    private static final int CARD_FOCUS = Color.parseColor("#26304D");

    private static final int BG_TOP = Color.parseColor("#0A0F1F");

    private static final int BG_BOTTOM = Color.parseColor("#1A2547");



    private float density;



    private static class AppItem {

        final String label;

        final String pkg;

        final Drawable icon;



        AppItem(String label, String pkg, Drawable icon) {

            this.label = label;

            this.pkg = pkg;

            this.icon = icon;

        }

    }



    private int dp(int value) {

        return Math.round(value * density);

    }



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        density = getResources().getDisplayMetrics().density;



        FrameLayout root = new FrameLayout(this);

        root.setBackground(new GradientDrawable(

                GradientDrawable.Orientation.TOP_BOTTOM,

                new int[]{BG_TOP, BG_BOTTOM}));



        LinearLayout page = new LinearLayout(this);

        page.setOrientation(LinearLayout.VERTICAL);

        page.setPadding(dp(56), dp(40), dp(56), 0);



        TextView title = new TextView(this);

        title.setText("LEO TV");

        title.setTextColor(Color.WHITE);

        title.setTextSize(34);

        title.setTypeface(Typeface.DEFAULT_BOLD);

        title.setPadding(dp(16), 0, 0, dp(24));

        page.addView(title);



        ScrollView scroll = new ScrollView(this);

        scroll.setClipChildren(false);

        scroll.setClipToPadding(false);

        scroll.setVerticalScrollBarEnabled(false);



        LinearLayout grid = new LinearLayout(this);

        grid.setOrientation(LinearLayout.VERTICAL);

        grid.setClipChildren(false);

        grid.setPadding(0, dp(16), 0, dp(40));

        scroll.addView(grid);

        page.addView(scroll);

        root.addView(page);

        setContentView(root);



        List<AppItem> apps = loadApps();



        int cardW = dp(180);

        int cardH = dp(160);

        int margin = dp(12);

        int available = getResources().getDisplayMetrics().widthPixels - dp(112) - dp(32);

        int cols = Math.max(1, available / (cardW + 2 * margin));



        View firstCard = null;

        LinearLayout row = null;

        for (int i = 0; i < apps.size(); i++) {

            if (i % cols == 0) {

                row = new LinearLayout(this);

                row.setOrientation(LinearLayout.HORIZONTAL);

                row.setClipChildren(false);

                grid.addView(row);

            }

            View card = makeCard(apps.get(i));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, cardH);

            lp.setMargins(margin, margin, margin, margin);

            row.addView(card, lp);

            if (firstCard == null) firstCard = card;

        }



        if (firstCard != null) firstCard.requestFocus();

    }



    private List<AppItem> loadApps() {

        PackageManager pm = getPackageManager();

        Set<String> seen = new HashSet<>();

        List<AppItem> list = new ArrayList<>();

        String[] categories = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};



        for (String category : categories) {

            Intent intent = new Intent(Intent.ACTION_MAIN);

            intent.addCategory(category);

            for (ResolveInfo ri : pm.queryIntentActivities(intent, 0)) {

                String pkg = ri.activityInfo.packageName;

                if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;

                list.add(new AppItem(ri.loadLabel(pm).toString(), pkg, ri.loadIcon(pm)));

            }

        }

        Collections.sort(list, (a, b) -> a.label.compareToIgnoreCase(b.label));

        return list;

    }



    private View makeCard(final AppItem app) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setGravity(Gravity.CENTER);

        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        card.setFocusable(true);

        card.setClickable(true);

        card.setBackground(cardBackground(false));



        ImageView icon = new ImageView(this);

        icon.setImageDrawable(app.icon);

        card.addView(icon, new LinearLayout.LayoutParams(dp(96), dp(96)));



        TextView label = new TextView(this);

        label.setText(app.label);

        label.setTextColor(Color.WHITE);

        label.setTextSize(15);

        label.setMaxLines(1);

        label.setEllipsize(TextUtils.TruncateAt.END);

        label.setGravity(Gravity.CENTER);

        label.setPadding(0, dp(10), 0, 0);

        card.addView(label);



        card.setOnClickListener(v -> launch(app.pkg));

        card.setOnFocusChangeListener((v, hasFocus) -> {

            float scale = hasFocus ? 1.12f : 1f;

            v.animate().scaleX(scale).scaleY(scale).setDuration(150).start();

            v.setBackground(cardBackground(hasFocus));

            v.setElevation(hasFocus ? dp(12) : 0);

        });

        return card;

    }



    private GradientDrawable cardBackground(boolean focused) {

        GradientDrawable d = new GradientDrawable();

        d.setColor(focused ? CARD_FOCUS : CARD);

        d.setCornerRadius(dp(20));

        if (focused) d.setStroke(dp(4), BLUE);

        return d;

    }



    private void launch(String pkg) {

        PackageManager pm = getPackageManager();

        Intent intent = pm.getLeanbackLaunchIntentForPackage(pkg);

        if (intent == null) intent = pm.getLaunchIntentForPackage(pkg);

        if (intent != null) startActivity(intent);

    }

}
