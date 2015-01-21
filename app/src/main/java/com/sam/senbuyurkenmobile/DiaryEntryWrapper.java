package com.sam.senbuyurkenmobile;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by SametCokpinar on 06/01/15.
 */
public class DiaryEntryWrapper implements Serializable {

    String entry_content;
    String entry_date;
    Bitmap image;


    public String getEntry_content() {
        return entry_content;
    }

    public void setEntry_content(String entry_content) {
        this.entry_content = entry_content;
    }

    public String getEntry_date() {
        return entry_date;
    }

    public void setEntry_date(String entry_date) {
        this.entry_date = entry_date;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
