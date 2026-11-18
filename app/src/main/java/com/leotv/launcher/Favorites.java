package com.leotv.launcher;



import android.content.Context;

import android.content.SharedPreferences;



import java.util.HashSet;

import java.util.Set;



public class Favorites {

    private static final String PREFS = "leotv";

    private static final String KEY = "favorites";

    private final SharedPreferences prefs;



    public Favorites(Context context) {

        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

    }



    public boolean contains(String pkg) {

        return prefs.getStringSet(KEY, new HashSet<String>()).contains(pkg);

    }



    /** Ajoute ou retire. Retourne true si l'appli vient d'être ajoutée. */

    public boolean toggle(String pkg) {

        Set<String> copy = new HashSet<>(prefs.getStringSet(KEY, new HashSet<String>()));

        boolean added;

        if (copy.contains(pkg)) {

            copy.remove(pkg);

            added = false;

        } else {

            copy.add(pkg);

            added = true;

        }

        prefs.edit().putStringSet(KEY, copy).apply();

        return added;

    }

}
