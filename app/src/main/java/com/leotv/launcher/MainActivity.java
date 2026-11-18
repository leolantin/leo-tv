package com.leotv.launcher;



import android.app.Activity;

import android.graphics.Color;

import android.os.Bundle;

import android.view.Gravity;

import android.widget.TextView;



public class MainActivity extends Activity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TextView t = new TextView(this);

        t.setText("LEO TV");

        t.setTextSize(64);

        t.setTextColor(Color.WHITE);

        t.setGravity(Gravity.CENTER);

        t.setBackgroundColor(Color.parseColor("#0A0F1F"));

        setContentView(t);

    }

}

