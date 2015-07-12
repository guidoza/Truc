package com.game.guillermo.truc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author alessandro.balocco
 */
public class SimpleAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public SimpleAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.simple_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) view.findViewById(R.id.text_view);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        switch (position) {
            case 0:
                viewHolder.textView.setText('"'+"Voy a ti"+'"');
                viewHolder.imageView.setImageResource(R.drawable.common_signin_btn_icon_dark);
                break;
            case 1:
                viewHolder.textView.setText('"'+"Ven a mi"+'"');
                viewHolder.imageView.setImageResource(R.drawable.common_signin_btn_icon_dark);
                break;
            case 2:
                viewHolder.textView.setText(('"'+"Dicen que al basto se le envida"+'"'));
                viewHolder.imageView.setImageResource(R.drawable.common_signin_btn_icon_dark);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
