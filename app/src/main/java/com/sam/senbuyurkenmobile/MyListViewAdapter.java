package com.sam.senbuyurkenmobile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by SametCokpinar on 11/01/15.
 */
public class MyListViewAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Activity activity;
    private List<DiaryEntryWrapper> data;

    public MyListViewAdapter(Activity a, List<DiaryEntryWrapper> list) {
        activity = a;
        data = list;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.diary_entry, null);

        TextView diary_entry_date = (TextView) vi.findViewById(R.id.diary_entry_date);
        TextView diary_entry_content = (TextView) vi.findViewById(R.id.diary_entry_content);
        ImageView diary_entry_image = (ImageView) vi.findViewById(R.id.diary_entry_image);

        diary_entry_date.setText(data.get(position).getEntry_date());
        diary_entry_content.setText(data.get(position).getEntry_content());
        diary_entry_image.setImageBitmap(data.get(position).getImage());
        return vi;
    }
}