package com.petronas.fof.spot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.petronas.fof.spot.R;
import com.petronas.fof.spot.models.TimeBeans;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<TimeBeans> {

    private List<TimeBeans> objects;
    private Context context;

    public CustomArrayAdapter(Context context, int resourceId,
                              List<TimeBeans> objects) {
        super(context, resourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=(LayoutInflater) context.getSystemService(  Context.LAYOUT_INFLATER_SERVICE );
        View row=inflater.inflate(R.layout.item_spinner, parent, false);
        TextView label=(TextView)row.findViewById(R.id.title);
        label.setText(objects.get(position).getValue());

        return row;
    }

}