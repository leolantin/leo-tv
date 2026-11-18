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



    private static final int CARD = Color.parseColor("#1C2236");

    private static final int CARD_FOCUS = Color.parseColor("#26304D");

    private static final int TEXT_SOFT = Color.parseColor("#AAB4D4");



    // Le splash ne s'affiche qu'une fois par lancement de l'appli

    private static boolean splashShown = false;



    private float density;

    private Favorites favorites;

    private Settings settings;

    private List<AppItem> apps = new ArrayList<>();



    // Valeurs lues depuis les paramètres

    private int accent;

    private int bgTop1, bgBottom1, bgTop2, bgBottom2;

    private int radius, cardW, cardH, iconSize;



    private TextView titleView;

    private TextView favTitle;

    private LinearLayout favRow;

    private LinearLayout allRow;

    private HorizontalScrollView favScroller;

    private TextView favHint;

    private TextView clockView;

    private TextView dateView;

    private TextView weatherView;



    private GradientDrawable background;

    private ValueAnimator bgAnimator;



    private String lastPkg = null;

    private boolean lastInFav = false;



    private long weatherTime = 0;

    private String weatherCity = "";

    private String weatherText = null;



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

        settings = new Settings(this);

        setContentView(buildUi());

    }



    @Override

    protected void onResume() {

        super.onResume();

        apps = loadApps();

        applySettings();

        rebuildRows();

        refreshWeather();

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



    // ---------- Paramètres ----------



    private void applySettings() {

        accent = settings.accentColor();

        int[] bg = settings.bgColors();

        bgTop1 = bg[0];

        bgBottom1 = bg[1];

        bgTop2 = bg[2];

        bgBottom2 = bg[3];

        background.setColors(new int[]{bgTop1, bgBottom1});



        radius = dp(settings.radiusDp());

        int[] s = settings.sizeDp();

        cardW = dp(s[0]);

        cardH = dp(s[1]);

        iconSize = dp(s[2]);



        titleView.setTextColor(accent);

        titleView.setVisibility(settings.isShown(Settings.SHOW_TITLE) ? View.VISIBLE : View.GONE);

        clockView.setVisibility(settings.isShown(Settings.SHOW_CLOCK) ? View.VISIBLE : View.GONE);

        dateView.setVisibility(settings.isShown(Settings.SHOW_DATE) ? View.VISIBLE : View.GONE);

        weatherView.setVisibility(settings.isShown(Settings.SHOW_WEATHER) ? View.VISIBLE : View.GONE);

    }



    // ---------- Construction de l'interface ----------



    private View buildUi() {

        final FrameLayout root = new FrameLayout(this);

        background = new GradientDrawable(

                GradientDrawable.Orientation.TOP_BOTTOM,

                new int[]{Color.BLACK, Color.BLACK});

        root.setBackground(background);

        setupBackgroundAnimation();



        LinearLayout page = new LinearLayout(this);

        page.setOrientation(LinearLayout.VERTICAL);

        page.setPadding(dp(56), dp(32), dp(56), 0);



        FrameLayout header = new FrameLayout(this);



        titleView = new TextView(this);

        titleView.setText("LEO TV");

        titleView.setTextColor(Color.WHITE);

        titleView.setTextSize(34);

        titleView.setTypeface(Typeface.DEFAULT_BOLD);

        titleView.setPadding(dp(16), 0, 0, 0);

        header.addView(titleView, new FrameLayout.LayoutParams(

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



        weatherView = new TextView(this);

        weatherView.setTextColor(TEXT_SOFT);

        weatherView.setTextSize(16);

        weatherView.setPadding(0, dp(2), 0, 0);

        clockBox.addView(weatherView);



        header.addView(clockBox, new FrameLayout.LayoutParams(

                WRAP, WRAP, Gravity.END | Gravity.CENTER_VERTICAL));

        page.addView(header, new LinearLayout.LayoutParams(MATCH, WRAP));



        ScrollView scroll = new ScrollView(this);

        scroll.setVerticalScrollBarEnabled(false);

        scroll.setClipChildren(false);

        scroll.setClipToPadding(false);



        LinearLayout content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);

        content.setClipChildren(false);

        content.setPadding(0, dp(16), 0, dp(48));



        favTitle = sectionTitle("Favoris");

        content.addView(favTitle);



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



        if (!splashShown) {

            splashShown = true;

            addSplash(root);

        }

        return root;

    }



    private void addSplash(final FrameLayout root) {

        final FrameLayout splash = new FrameLayout(this);

        splash.setBackgroundColor(Color.parseColor("#0A0F1F"));



        TextView logo = new TextView(this);

        logo.setText("LEO TV");

        logo.setTextColor(Color.WHITE);

        logo.setTextSize(64);

        logo.setTypeface(Typeface.DEFAULT_BOLD);

        logo.setLetterSpacing(0.15f);

        splash.addView(logo, new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER));



        root.addView(splash, new FrameLayout.LayoutParams(MATCH, MATCH));



        handler.postDelayed(() -> splash.animate()

                .alpha(0f)

                .setDuration(600)

                .withEndAction(() -> root.removeView(splash))

                .start(), 1500);

    }



    private void setupBackgroundAnimation() {

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        bgAnimator = ValueAnimator.ofFloat(0f, 1f);

        bgAnimator.setDuration(12000);

        bgAnimator.setRepeatCount(ValueAnimator.INFINITE);

        bgAnimator.setRepeatMode(ValueAnimator.REVERSE);

        bgAnimator.addUpdateListener(a -> {

            float f = (Float) a.getAnimatedValue();

            int top = (Integer) evaluator.evaluate(f, bgTop1, bgTop2);

            int bottom = (Integer) evaluator.evaluate(f, bgBottom1, bgBottom2);

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

                list.add(new AppItem(

                        ri.loadLabel(pm).toString(),

                        pkg,

                        ri.loadIcon(pm),

                        ri.activityInfo.loadBanner(pm)));

            }

        }

        Collections.sort(list, (a, b) -> a.label.compareToIgnoreCase(b.label));

        return list;

    }



    private void rebuildRows() {

        favRow.removeAllViews();

        allRow.removeAllViews();



        boolean showFav = settings.isShown(Settings.SHOW_FAVORITES);



        View toFocus = null;

        View firstFav = null;

        View firstApp = null;

        int favCount = 0;



        if (showFav) {

            for (AppItem app : apps) {

                if (favorites.contains(app.pkg)) {

                    View c = makeCard(app, true);

                    favRow.addView(c, cardParams());

                    favCount++;

                    if (firstFav == null) firstFav = c;

                    if (lastInFav && app.pkg.equals(lastPkg)) toFocus = c;

                }

            }

        }



        View settingsCard = makeSettingsCard();

        allRow.addView(settingsCard, cardParams());



        for (AppItem app : apps) {

            View c = makeCard(app, false);

            allRow.addView(c, cardParams());

            if (firstApp == null) firstApp = c;

            if ((!lastInFav || !showFav) && app.pkg.equals(lastPkg)) toFocus = c;

        }



        favTitle.setVisibility(showFav ? View.VISIBLE : View.GONE);

        favHint.setVisibility(showFav && favCount == 0 ? View.VISIBLE : View.GONE);

        favScroller.setVisibility(showFav && favCount > 0 ? View.VISIBLE : View.GONE);



        if (toFocus == null) {

            if (firstFav != null) toFocus = firstFav;

            else if (firstApp != null) toFocus = firstApp;

            else toFocus = settingsCard;

        }

        toFocus.requestFocus();

    }



    // ---------- Météo ----------



    private void refreshWeather() {

        if (!settings.isShown(Settings.SHOW_WEATHER)) return;



        final String city = settings.cityName();

        long now = System.currentTimeMillis();



        // Pas de nouvelle requête si la météo date de moins de 10 minutes

        if (city.equals(weatherCity) && weatherText != null && now - weatherTime < 600000) {

            weatherView.setText(city + "  ·  " + weatherText);

            return;

        }



        weatherCity = city;

        weatherText = null;

        weatherView.setText(city);



        Weather.fetch(settings.cityLat(), settings.cityLon(), text -> {

            if (text == null) {

                weatherView.setText(city + "  ·  météo indisponible");

                return;

            }

            weatherText = text;

            weatherTime = System.currentTimeMillis();

            weatherView.setText(city + "  ·  " + text);

        });

    }



    // ---------- Cartes ----------



    private LinearLayout.LayoutParams cardParams() {

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardW, cardH);

        int m = dp(10);

        lp.setMargins(m, m, m, m);

        return lp;

    }



    private LinearLayout baseCard() {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);

        card.setGravity(Gravity.CENTER);

        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        card.setFocusable(true);

        card.setClickable(true);

        card.setBackground(cardBackground(false));

        return card;

    }



    private TextView cardLabel(String text) {

        TextView label = new TextView(this);

        label.setText(text);

        label.setTextColor(Color.WHITE);

        label.setTextSize(15);

        label.setMaxLines(1);

        label.setEllipsize(TextUtils.TruncateAt.END);

        label.setGravity(Gravity.CENTER);

        label.setPadding(0, dp(10), 0, 0);

        return label;

    }



    private void addFocusEffect(LinearLayout card, final AppItem app, final boolean inFavRow) {

        card.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus && app != null) {

                lastPkg = app.pkg;

                lastInFav = inFavRow;

            }

            float scale = hasFocus ? 1.12f : 1f;

            v.animate().scaleX(scale).scaleY(scale).setDuration(150).start();

            v.setBackground(cardBackground(hasFocus));

            v.setForeground(hasFocus ? focusRing() : null);

            v.setElevation(hasFocus ? dp(12) : 0);

        });

    }



    private View makeSettingsCard() {

        LinearLayout card = baseCard();



        ImageView icon = new ImageView(this);

        icon.setImageResource(android.R.drawable.ic_menu_preferences);

        icon.setColorFilter(Color.WHITE);

        icon.setPadding(dp(10), dp(10), dp(10), dp(10));

        card.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));

        card.addView(cardLabel("Paramètres"), new LinearLayout.LayoutParams(MATCH, WRAP));



        card.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        addFocusEffect(card, null, false);

        return card;

    }



    private View makeCard(final AppItem app, final boolean inFavRow) {

        LinearLayout card = baseCard();



        if (app.banner != null) {

            // Style Apple TV : l'image de l'appli remplit toute la carte

            card.setPadding(0, 0, 0, 0);

            card.setClipToOutline(true);



            ImageView banner = new ImageView(this);

            banner.setImageDrawable(app.banner);

            banner.setScaleType(ImageView.ScaleType.CENTER_CROP);

            card.addView(banner, new LinearLayout.LayoutParams(MATCH, MATCH));

        } else {

            // Pas de bannière : icône et nom

            ImageView icon = new ImageView(this);

            icon.setImageDrawable(app.icon);

            card.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));



            boolean isFav = favorites.contains(app.pkg);

            String text = (isFav && !inFavRow ? "★ " : "") + app.label;

            card.addView(cardLabel(text), new LinearLayout.LayoutParams(MATCH, WRAP));

        }



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



        addFocusEffect(card, app, inFavRow);

        return card;

    }



    private GradientDrawable cardBackground(boolean focused) {

        GradientDrawable d = new GradientDrawable();

        d.setColor(focused ? CARD_FOCUS : CARD);

        d.setCornerRadius(radius);

        return d;

    }



    private GradientDrawable focusRing() {

        GradientDrawable d = new GradientDrawable();

        d.setColor(Color.TRANSPARENT);

        d.setCornerRadius(radius);

        d.setStroke(dp(4), accent);

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
