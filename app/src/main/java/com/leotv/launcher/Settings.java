package com.leotv.launcher;



import android.content.Context;

import android.content.SharedPreferences;



public class Settings {

    public static final String ACCENT = "accent";

    public static final String BACKGROUND = "background";

    public static final String SHAPE = "shape";

    public static final String SIZE = "size";

    public static final String SHOW_TITLE = "show_title";

    public static final String SHOW_CLOCK = "show_clock";

    public static final String SHOW_DATE = "show_date";

    public static final String SHOW_FAVORITES = "show_favorites";



    public static final String[] ACCENT_NAMES = {"Bleu", "Violet", "Vert", "Orange", "Rouge"};

    public static final int[] ACCENT_COLORS = {

            0xFF2F7BFF, 0xFF8B5CF6, 0xFF22C55E, 0xFFF59E0B, 0xFFEF4444};



    public static final String[] BG_NAMES = {"Nuit bleue", "Aurore", "Océan", "Noir"};

    // Pour chaque fond : haut1, bas1, haut2, bas2 (le fond passe doucement de 1 à 2)

    public static final int[][] BG_COLORS = {

            {0xFF0A0F1F, 0xFF1A2547, 0xFF140A2B, 0xFF0F3A5C},

            {0xFF1B0A2B, 0xFF4A1D5C, 0xFF0A2B2B, 0xFF1D5C4A},

            {0xFF021526, 0xFF03506F, 0xFF011627, 0xFF0A6C74},

            {0xFF000000, 0xFF0D0D0D, 0xFF000000, 0xFF161616}};



    public static final String[] SHAPE_NAMES = {"Arrondi", "Très arrondi", "Carré"};

    private static final int[] SHAPE_RADII_DP = {20, 36, 4};



    public static final String[] SIZE_NAMES = {"Petit", "Moyen", "Grand"};

    // largeur carte, hauteur carte, taille icône (en dp), format 16:9

    private static final int[][] SIZE_DP = {{220, 124, 60}, {280, 158, 84}, {340, 191, 110}};



    private final SharedPreferences prefs;



    public Settings(Context context) {

        prefs = context.getSharedPreferences("leotv_settings", Context.MODE_PRIVATE);

    }



    public int getInt(String key, int def) {

        return prefs.getInt(key, def);

    }



    public void setInt(String key, int value) {

        prefs.edit().putInt(key, value).apply();

    }



    public void reset() {

        prefs.edit().clear().apply();

    }



    /** 0 = affiché (par défaut), 1 = masqué */

    public boolean isShown(String key) {

        return getInt(key, 0) == 0;

    }



    private int idx(String key, int length) {

        int v = getInt(key, 0);

        return (v < 0 || v >= length) ? 0 : v;

    }



    public int accentColor() {

        return ACCENT_COLORS[idx(ACCENT, ACCENT_COLORS.length)];

    }



    public int[] bgColors() {

        return BG_COLORS[idx(BACKGROUND, BG_COLORS.length)];

    }



    public int radiusDp() {

        return SHAPE_RADII_DP[idx(SHAPE, SHAPE_RADII_DP.length)];

    }



    public int[] sizeDp() {

        return SIZE_DP[idx(SIZE, SIZE_DP.length)];

    }

}
