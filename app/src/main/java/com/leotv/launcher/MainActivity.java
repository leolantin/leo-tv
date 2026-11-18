package com.leotv.launcher;



import android.animation.ArgbEvaluator;

import android.animation.ValueAnimator;

import android.app.Activity;

import android.content.Intent;

import android.content.pm.PackageManager;

import android.content.pm.ResolveInfo;

import android.graphics.Color;

import android.graphics.Typeface;

import android.graphics.drawable.GradientDrawable;

import android.os.Bundle;

import android.os.Handler;

import android.os.Looper;

import android.text.TextUtils;

import android.view.Gravity;

import android.view.View;

import android.view.ViewGroup;

import android.widget.FrameLayout;

import android.widget.HorizontalScrollView;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.widget.ScrollView;

import android.widget.TextView;

import android.widget.Toast;



import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Collections;

import java.util.Date;

import java.util.HashSet;

import java.util.List;

import java.util.Locale;

import java.util.Set;



public class MainActivity extends Activity {



    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;

    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;



    private static final int BLUE = Color.parseColor("#2F7BFF");

    private static final int CARD = Color.parseColor("#1C2236");

    private static final int CARD_FOCUS = Color.parseColor("#26304D");

    private static final int TEXT_SOFT = Color.parseColor("#AAB4D4");



    private static final int BG1_TOP = Color.parseColor("#0A0F1F");

    private static final int BG1_BOTTOM = Color.parseColor("#1A2547");

    private static final int BG2_TOP = Color.parseColor("#140A2B");

    private static final int BG2_BOTTOM = Color.parseColor("#0F3A5C");



    private float density;

    private Favorites favorites;

    private List<AppItem> apps = new ArrayList<>();



    private LinearLayout favRow;

    private LinearLayout allRow;

    private HorizontalScrollView favScroller;

    private TextView favHint;

    private TextView clockView;

    private TextView dateView;



    private GradientDrawable background;

    private ValueAnimator bgAnimator;



    private String lastPkg = null;

    private boolean lastInFav = false;



    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable clockTick = new Runnable() {

        @Override

        public void run() {

            updateClock();

            handler.postDelayed(this, 10000);

        }

    };



    private int dp(int value) {

        return Math.round(value * density);

    }



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        density = getResources().getDisplayMetrics().density;

        favorites = new Favorites(this);

        setContentView(buildUi());

    }



    @Override

    protected void onResume() {

        super.onResume();

        apps = loadApps();

        rebuildRows();

        handler.removeCallbacks(clockTick);

        handler.post(clockTick);

        bgAnimator.start();

    }



    @Override

    protected void onPause() {

        super.onPause();

        handler.removeCallbacks(clockTick);

        bgAnimator.cancel();

    }



    // ---------- Construction de l'interface ----------



    private View buildUi() {

        FrameLayout root = new FrameLayout(this);

        background = new GradientDrawable(

                GradientDrawable.Orientation.TOP_BOTTOM,

                new int[]{BG1_TOP, BG1_BOTTOM});

        root.setBackground(background);

        setupBackgroundAnimation();



        LinearLayout page = new LinearLayout(this);

        page.setOrientation(LinearLayout.VERTICAL);

        page.setPadding(dp(56), dp(32), dp(56), 0);



        // En-tête : titre à gauche, horloge et date à droite

        FrameLayout header = new FrameLayout(this);



        TextView title = new TextView(this);

        title.setText("LEO TV");

        title.setTextColor(Color.WHITE);

        title.setTextSize(34);

        title.setTypeface(Typeface.DEFAULT_BOLD);

        title.setPadding(dp(16), 0, 0, 0);

        header.addView(title, new FrameLayout.LayoutParams(

                WRAP, WRAP, Gravity.START | Gravity.CENTER_VERTICAL));



        LinearLayout clockBox = new LinearLayout(this);

        clockBox.setOrientation(LinearLayout.VERTICAL);

        clockBox.setGravity(Gravity.END);



        clockView = new TextView(this);

        clockView.setTextColor(Color.WHITE);

        clockView.setTextSize(34);

        clockView.setTypeface(Typeface.DEFAULT_BOLD);

        clockBox.addView(clockView);



        dateView = new TextView(this);

        dateView.setTextColor(TEXT_SOFT);

        dateView.setTextSize(16);

        clockBox.addView(dateView);



        header.addView(clockBox, new FrameLayout.LayoutParams(

                WRAP, WRAP, Gravity.END | Gravity.CENTER_VERTICAL));

        page.addView(header, new LinearLayout.LayoutParams(MATCH, WRAP));



        // Contenu défilant verticalement

        ScrollView scroll = new ScrollView(this);

        scroll.setVerticalScrollBarEnabled(false);

        scroll.setClipChildren(false);

        scroll.setClipToPadding(false);



        LinearLayout content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);

        content.setClipChildren(false);

        content.setPadding(0, dp(16), 0, dp(48));



        content.addView(sectionTitle("Favoris"));



        favHint = new TextView(this);

        favHint.setText("Aucun favori. Maintiens OK appuyé sur une application pour l'ajouter ici.");

        favHint.setTextColor(TEXT_SOFT);

        favHint.setTextSize(16);

        favHint.setPadding(dp(16), dp(12), 0, dp(12));

        content.addView(favHint);



        favRow = new LinearLayout(this);

        favScroller = makeRowScroller(favRow);

        content.addView(favScroller);



        content.addView(sectionTitle("Toutes les applications"));



        allRow = new LinearLayout(this);

        content.addView(makeRowScroller(allRow));



        scroll.addView(content);

        page.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));



        root.addView(page, new FrameLayout.LayoutParams(MATCH, MATCH));

        return root;

    }



    private void setupBackgroundAnimation() {

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        bgAnimator = ValueAnimator.ofFloat(0f, 1f);

        bgAnimator.setDuration(12000);

        bgAnimator.setRepeatCount(ValueAnimator.INFINITE);

        bgAnimator.setRepeatMode(ValueAnimator.REVERSE);

        bgAnimator.addUpdateListener(a -> {

            float f = (Float) a.getAnimatedValue();

            int top = (Integer) evaluator.evaluate(f, BG1_TOP, BG2_TOP);

            int bottom = (Integer) evaluator.evaluate(f, BG1_BOTTOM, BG2_BOTTOM);

            background.setColors(new int[]{top, bottom});

        });

    }



    private TextView sectionTitle(String text) {

        TextView t = new TextView(this);

        t.setText(text);

        t.setTextColor(Color.WHITE);

        t.setTextSize(22);

        t.setTypeface(Typeface.DEFAULT_BOLD);

        t.setPadding(dp(16), dp(16), 0, 0);

        return t;

    }



    private HorizontalScrollView makeRowScroller(LinearLayout row) {

        row.setOrientation(LinearLayout.HORIZONTAL);

        row.setClipChildren(false);



        HorizontalScrollView hs = new HorizontalScrollView(this);

        hs.setHorizontalScrollBarEnabled(false);

        hs.setClipChildren(false);

        hs.setClipToPadding(false);

        hs.setPadding(dp(16), dp(20), dp(16), dp(20));

        hs.addView(row, new FrameLayout.LayoutParams(WRAP, WRAP));

        return hs;

    }



    // ---------- Données ----------



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



    private void rebuildRows() {

        favRow.removeAllViews();

        allRow.removeAllViews();



        View toFocus = null;

        View firstFav = null;

        View firstAll = null;

        int favCount = 0;



        for (AppItem app : apps) {

            if (favorites.contains(app.pkg)) {

                View c = makeCard(app, true);

                favRow.addView(c, cardParams());

                favCount++;

                if (firstFav == null) firstFav = c;

                if (lastInFav && app.pkg.equals(lastPkg)) toFocus = c;

            }

        }

        for (AppItem app : apps) {

            View c = makeCard(app, false);

            allRow.addView(c, cardParams());

            if (firstAll == null) firstAll = c;

            if (!lastInFav && app.pkg.equals(lastPkg)) toFocus = c;

        }



        favHint.setVisibility(favCount == 0 ? View.VISIBLE : View.GONE);

        favScroller.setVisibility(favCount == 0 ? View.GONE : View.VISIBLE);



        if (toFocus == null) toFocus = (firstFav != null) ? firstFav : firstAll;

        if (toFocus != null) toFocus.requestFocus();

    }



    // ---------- Cartes ----------



    private LinearLayout.LayoutParams cardParams() {

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(190), dp(180));

        int m = dp(10);

        lp.setMargins(m, m, m, m);

        return lp;

    }



    private View makeCard(final AppItem app, final boolean inFavRow) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setGravity(Gravity.CENTER);

        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        card.setFocusable(true);

        card.setClickable(true);

        card.setBackground(cardBackground(false));



        ImageView icon = new ImageView(this);

        icon.setImageDrawable(app.icon);

        card.addView(icon, new LinearLayout.LayoutParams(dp(104), dp(104)));



        TextView label = new TextView(this);

        boolean isFav = favorites.contains(app.pkg);

        label.setText((isFav && !inFavRow ? "★ " : "") + app.label);

        label.setTextColor(Color.WHITE);

        label.setTextSize(15);

        label.setMaxLines(1);

        label.setEllipsize(TextUtils.TruncateAt.END);

        label.setGravity(Gravity.CENTER);

        label.setPadding(0, dp(10), 0, 0);

        card.addView(label, new LinearLayout.LayoutParams(MATCH, WRAP));



        card.setOnClickListener(v -> launch(app.pkg));



        card.setOnLongClickListener(v -> {

            boolean added = favorites.toggle(app.pkg);

            lastPkg = app.pkg;

            lastInFav = false;

            Toast.makeText(this,

                    added ? "Ajouté aux favoris" : "Retiré des favoris",

                    Toast.LENGTH_SHORT).show();

            rebuildRows();

            return true;

        });



        card.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus) {

                lastPkg = app.pkg;

                lastInFav = inFavRow;

            }

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



    // ---------- Horloge ----------



    private void updateClock() {

        Date now = new Date();

        clockView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));

        String d = new SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now);

        if (!d.isEmpty()) d = d.substring(0, 1).toUpperCase() + d.substring(1);

        dateView.setText(d);

    }

}

