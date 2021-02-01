package com.shakeme2.android.shakeme2;

import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;

/**
 * Created by CHIRAG on 01-11-2017.
 */

public class Library {

    public String getName(SharedPreferences sharedPreferences)
    {
        if(!sharedPreferences.contains("Name"))
            return "John Doe";
        return sharedPreferences.getString("Name", "John Doe");
    }

    public void updateName(SharedPreferences sharedPreferences, NavigationView navigationView)
    {
        String name = getName(sharedPreferences);
        View navHeaderView= navigationView.getHeaderView(0);
        TextView nameView= (TextView) navHeaderView.findViewById(R.id.name_view);
        nameView.setText(name);

    }

    public float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
