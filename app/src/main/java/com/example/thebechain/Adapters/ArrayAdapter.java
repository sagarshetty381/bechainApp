package com.example.thebechain.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.thebechain.Cards;
import com.example.thebechain.R;

import java.util.List;

public class ArrayAdapter extends android.widget.ArrayAdapter {
    Context context;
    TextView name;
    ImageView image_view;
    Cards card_item;

    public ImageView getImage_view() {
        return image_view;
    }

    public ArrayAdapter(Context context, int resourceId, List<Cards> items) {
        super(context, resourceId, items);
    }

    public TextView getName() {
        return name;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        card_item = (Cards) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        name = convertView.findViewById(R.id.helloText);
        image_view = convertView.findViewById(R.id.imageOfProf);

        name.setText((card_item.getName() + ", " + card_item.getAge()));
        switch (card_item.getProfileImmageUrl()){
            case "default":
                Glide.with(convertView.getContext()).load(R.mipmap.ic_launcher).into(image_view);
                break;
            default:
                Glide.with(convertView.getContext()).load(card_item.getProfileImmageUrl()).into(image_view);
                break;
        }

        return convertView;
    }

}
