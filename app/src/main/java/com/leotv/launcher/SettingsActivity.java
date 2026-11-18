package com.leotv.launcher;



import android.app.Activity;

import android.graphics.Color;

import android.graphics.Typeface;

import android.graphics.drawable.GradientDrawable;

import android.os.Bundle;

import android.view.Gravity;

import android.view.KeyEvent;

import android.view.View;

import android.view.ViewGroup;

import android.widget.LinearLayout;

import android.widget.ScrollView;

import android.widget.TextView;



public class SettingsActivity extends Activity {



    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;

    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private static final int CARD = Color.parseColor("#1C2236");

    private static final int CARD_FOCUS = Color.parseColor("#26304D");

    private static final int TEXT_SOFT = Color.parseColor("#AAB4D4");

    private static final String[] SHOWN = {"Affiché", "Masqué"};



    private float density;

    private Settings settings;

    private int accent;

    private LinearLayout content;

    private View firstRow;



    private int dp(int value) {

        return Math.round(value * density);

    }



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        density = getResources().getDisplayMetrics().density;

        settings = new Settings(this);

        accent = settings.accentColor();

        setContentView(buildUi());

        if (firstRow != null) firstRow.requestFocus();

    }



    private View buildUi() {

        int[] bg = settings.bgColors();

        ScrollView scroll = new ScrollView(this);

        scroll.setBackground(new GradientDrawable(

                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{bg[0], bg[1]}));

        scroll.setVerticalScrollBarEnabled(false);



        content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);

        content.setPadding(dp(96), dp(40), dp(96), dp(48));

        scroll.addView(content);



        TextView title = new TextView(this);

        title.setText("Paramètres");

        title.setTextColor(accent);

        title.setTextSize(34);

        title.setTypeface(Typeface.DEFAULT_BOLD);

        content.addView(title);



        addSection("Apparence");

        addChoice("Couleur", Settings.ACCENT, Settings.ACCENT_NAMES);

        addChoice("Fond d'écran", Settings.BACKGROUND, Settings.BG_NAMES);



        addSection("Cartes");

        addChoice("Forme", Settings.SHAPE, Settings.SHAPE_NAMES);

        addChoice("Taille", Settings.SIZE, Settings.SIZE_NAMES);



        addSection("Écran d'accueil");

        addChoice("Titre LEO TV", Settings.SHOW_TITLE, SHOWN);

        addChoice("Horloge", Settings.SHOW_CLOCK, SHOWN);

        addChoice("Date", Settings.SHOW_DATE, SHOWN);

        addChoice("Météo", Settings.SHOW_WEATHER, SHOWN);

        addChoice("Ville de la météo", Settings.CITY, Settings.CITY_NAMES);

        addChoice("Favoris", Settings.SHOW_FAVORITES, SHOWN);



        addSection("Autre");

        addResetRow();



        TextView hint = new TextView(this);

        hint.setText("Flèches gauche/droite : changer la valeur   •   Retour : revenir à l'accueil");

        hint.setTextColor(TEXT_SOFT);

        hint.setTextSize(15);

        hint.setPadding(0, dp(28), 0, 0);

        content.addView(hint);



        return scroll;

    }



    private void addSection(String text) {

        TextView t = new TextView(this);

        t.setText(text);

        t.setTextColor(TEXT_SOFT);

        t.setTextSize(18);

        t.setTypeface(Typeface.DEFAULT_BOLD);

        t.setPadding(dp(8), dp(28), 0, dp(6));

        content.addView(t);

    }



    private GradientDrawable rowBackground(boolean focused) {

        GradientDrawable d = new GradientDrawable();

        d.setColor(focused ? CARD_FOCUS : CARD);

        d.setCornerRadius(dp(14));

        if (focused) d.setStroke(dp(3), accent);

        return d;

    }



    private void addChoice(String label, final String key, final String[] names) {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);

        row.setGravity(Gravity.CENTER_VERTICAL);

        row.setPadding(dp(24), dp(18), dp(24), dp(18));

        row.setFocusable(true);

        row.setClickable(true);

        row.setBackground(rowBackground(false));



        TextView name = new TextView(this);

        name.setText(label);

        name.setTextColor(Color.WHITE);

        name.setTextSize(20);

        row.addView(name, new LinearLayout.LayoutParams(0, WRAP, 1f));



        final TextView value = new TextView(this);

        value.setTextColor(Color.WHITE);

        value.setTextSize(20);

        value.setTypeface(Typeface.DEFAULT_BOLD);

        showValue(value, key, names);

        row.addView(value);



        row.setOnClickListener(v -> step(key, names, value, 1));

        row.setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

                step(key, names, value, 1);

                return true;

            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                step(key, names, value, -1);

                return true;

            }

            return false;

        });

        row.setOnFocusChangeListener((v, hasFocus) -> v.setBackground(rowBackground(hasFocus)));



        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);

        lp.setMargins(0, dp(6), 0, dp(6));

        content.addView(row, lp);

        if (firstRow == null) firstRow = row;

    }



    private void showValue(TextView value, String key, String[] names) {

        int i = settings.getInt(key, 0);

        if (i < 0 || i >= names.length) i = 0;

        value.setText("◀   " + names[i] + "   ▶");

    }



    private void step(String key, String[] names, TextView value, int delta) {

        int i = settings.getInt(key, 0);

        if (i < 0 || i >= names.length) i = 0;

        i = (i + delta + names.length) % names.length;

        settings.setInt(key, i);

        showValue(value, key, names);

    }



    private void addResetRow() {

        TextView row = new TextView(this);

        row.setText("Réinitialiser tous les réglages");

        row.setTextColor(Color.WHITE);

        row.setTextSize(20);

        row.setPadding(dp(24), dp(18), dp(24), dp(18));

        row.setFocusable(true);

        row.setClickable(true);

        row.setBackground(rowBackground(false));

        row.setOnClickListener(v -> {

            settings.reset();

            recreate();

        });

        row.setOnFocusChangeListener((v, hasFocus) -> v.setBackground(rowBackground(hasFocus)));



        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);

        lp.setMargins(0, dp(6), 0, dp(6));

        content.addView(row, lp);

    }

}

