package com.sam.senbuyurkenmobile;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by SametCokpinar on 06/01/15.
 */
public class DiaryEntryWrapper implements Serializable {

    String entry_title;
    String entry_content;
    String entry_date;
    Bitmap image;
    Boolean hasImage = false;


    public String getEntry_title() {
        return entry_title;
    }

    public void setEntry_title(String entry_title) {
        this.entry_title = entry_title;
    }

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

    public Boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage;
    }
}
