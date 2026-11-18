package com.leotv.launcher;



import android.os.Handler;

import android.os.Looper;



import org.json.JSONObject;



import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.net.HttpURLConnection;

import java.net.URL;



public class Weather {



    public interface Listener {

        /** text vaut null si la météo n'a pas pu être récupérée */

        void onResult(String text);

    }



    public static void fetch(final double lat, final double lon, final Listener listener) {

        final Handler main = new Handler(Looper.getMainLooper());

        new Thread(() -> {

            String result = null;

            try {

                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=" + lat

                        + "&longitude=" + lon + "&current=temperature_2m,weather_code");

                HttpURLConnection c = (HttpURLConnection) url.openConnection();

                c.setConnectTimeout(8000);

                c.setReadTimeout(8000);

                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));

                StringBuilder sb = new StringBuilder();

                String line;

                while ((line = r.readLine()) != null) sb.append(line);

                r.close();



                JSONObject cur = new JSONObject(sb.toString()).getJSONObject("current");

                int temp = (int) Math.round(cur.getDouble("temperature_2m"));

                int code = cur.getInt("weather_code");

                result = describe(code) + "  " + temp + "°C";

            } catch (Exception e) {

                result = null;

            }

            final String out = result;

            main.post(() -> listener.onResult(out));

        }).start();

    }



    private static String describe(int code) {

        if (code == 0) return "☀️ Ciel dégagé";

        if (code <= 2) return "🌤️ Peu nuageux";

        if (code == 3) return "☁️ Couvert";

        if (code == 45 || code == 48) return "🌫️ Brouillard";

        if (code >= 51 && code <= 57) return "🌦️ Bruine";

        if (code >= 61 && code <= 67) return "🌧️ Pluie";

        if (code >= 71 && code <= 77) return "❄️ Neige";

        if (code >= 80 && code <= 82) return "🌦️ Averses";

        if (code == 85 || code == 86) return "❄️ Averses de neige";

        if (code >= 95) return "⛈️ Orage";

        return "🌡️ Météo";

    }

}

