package com.sam.senbuyurkenmobile;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by SametCokpinar on 14/01/15.
 */

public class CustomFontHelper {

    /**
     * Sets a font on a textview based on the custom com.my.package:font attribute
     * If the custom font attribute isn't found in the attributes nothing happens
     *
     * @param textview
     * @param context
     * @param attrs
     */
    public static void setCustomFont(TextView textview, Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.my_monotype_corsiva);
        String font = a.getString(R.styleable.my_monotype_corsiva_font);
        setCustomFont(textview, font, context);
        a.recycle();
    }

    /**
     * Sets a font on a textview
     *
     * @param textview
     * @param font
     * @param context
     */
    public static void setCustomFont(TextView textview, String font, Context context) {
        if (font == null) {
            return;
        }
        Typeface tf = FontCache.get(font, context);
        if (tf != null) {
            textview.setTypeface(tf);
        }
    }

}
