package com.amti.vela.bluetoothlegatt;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<ClipData.Item> {

    public CustomListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomListAdapter(Context context, int resource, List<ClipData.Item> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.custom_listview_item, null);
        }

        ClipData.Item p = getItem(position);

        if (p != null) {
            TextView text = (TextView)v.findViewById(R.id.list_item_text);
            TextView subtext = (TextView)v.findViewById(R.id.list_item_subtext);

            //split text and subtext by the newline character
            String textSplit[] = p.getText().toString().split("\n");

            if (text != null) {
                text.setText(textSplit[0]);
            }

            if (subtext != null) {
                subtext.setText(textSplit[1]);
            }
        }

        return v;
    }

}
