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

    public List<DiaryEntryWrapper> getData() {
        return data;
    }

    public void setData(List<DiaryEntryWrapper> data) {
        this.data = data;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;

        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            vi = inflater.inflate(R.layout.diary_entry, parent, false);

            holder.diary_entry_title = (TextView) vi.findViewById(R.id.diary_entry_title);
            holder.diary_entry_date = (TextView) vi.findViewById(R.id.diary_entry_date);
            holder.diary_entry_image = (ImageView) vi.findViewById(R.id.diary_entry_image);
            holder.diary_entry_content = (TextView) vi.findViewById(R.id.diary_entry_content);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        final DiaryEntryWrapper dew = data.get(position);

        if (dew != null) {
            holder.diary_entry_title.setText(dew.getEntry_title());
            holder.diary_entry_date.setText(dew.getEntry_date());
            if (!dew.getHasImage()) {
                holder.diary_entry_image.setImageBitmap(null);
                holder.diary_entry_image.setVisibility(View.GONE);
            } else {
                holder.diary_entry_image.setImageBitmap(dew.getImage());
                holder.diary_entry_image.setVisibility(View.VISIBLE);
            }
            holder.diary_entry_content.setText(dew.getEntry_content());
        }

        ImageView deleteButton = (ImageView) vi.findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DiaryPageActivity fragment_1 = (DiaryPageActivity) activity.getFragmentManager().findFragmentByTag("fragment_1");
                fragment_1.deleteProcess(dew.getId(), position);
            }
        });

        return vi;
    }


    public class ViewHolder {
        TextView diary_entry_title;
        TextView diary_entry_date;
        ImageView diary_entry_image;
        TextView diary_entry_content;
    }
}