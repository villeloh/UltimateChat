package com.ville.ultimatechatclient;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/*
    A class to properly style a goddamn Spinner (took me long enough to find something that even mildly works).
    @author Ville Lohkovuori
    10/2017
 */

public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Context context;
    private String[] arr;

    public CustomSpinnerAdapter(Context context, String[] arr) {

        this.arr=arr;
        this.context = context;
    }

    // for the main Spinner view
    @Override
    public View getView(int position, View view, ViewGroup viewgroup) {

        TextView txt = new TextView(this.context);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(14);
        txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
        txt.setText(arr[position]);
        txt.setTextColor(Color.WHITE);
        return txt;
    } // end getView()

    // for the drop-down views
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        TextView txt = new TextView(this.context);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(16);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(arr[position]);
        txt.setTextColor(Color.WHITE);
        txt.setBackgroundResource(R.drawable.spinner_background);
        return txt;
    } // end getDropDownView()

    // needed to make it work...
    @Override
    public int getCount()
    {
        return arr.length;
    }

    @Override
    public Object getItem(int i)
    {
        return arr[i];
    }

    @Override
    public long getItemId(int i)
    {
        return (long)i;
    }

} // end class