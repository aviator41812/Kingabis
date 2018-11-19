package com.example.brettmacdonald.kingabis;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MenuListAdapter extends ArrayAdapter<Item> {

        private static final String TAG = "MenuListAdapter";

        private Context mContext;
        private int mResource;

        /**
         * Default constructor for the MenuListAdapter
         * @param context
         * @param resource
         * @param objects
         */
        public MenuListAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
            mContext = context;
            mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //get the persons information
            String name = getItem(position).getName();
            String price = getItem(position).getPrice();
            String type = getItem(position).getType();
            String count = getItem(position).getCount();

            //Create the person object with the information
            Item item = new Item(name,price,type,count);

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            TextView tvname = (TextView) convertView.findViewById(R.id.textView1);
            TextView tvprice = (TextView) convertView.findViewById(R.id.textView2);
            TextView tvtype = (TextView) convertView.findViewById(R.id.textView3);

            tvname.setText(item.getName());
            tvprice.setText(item.getPrice());
            tvtype.setText(item.getType());

            return convertView;
        }
    }



