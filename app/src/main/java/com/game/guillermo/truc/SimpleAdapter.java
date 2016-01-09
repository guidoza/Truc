package com.game.guillermo.truc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import static com.game.guillermo.truc.R.drawable.common_google_signin_btn_icon_dark;

/**
 * @author alessandro.balocco
 */
public class SimpleAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private Context context;

    public SimpleAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 15;
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
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.voy_a_ti)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 1:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.ven_a_mi)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 2:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.basto_envida)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 3:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.paliza)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 4:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.farol)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 5:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.atreves)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 6:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.bien_jugado)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 7:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.espera_y_veras)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 8:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.sobrado)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 9:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.fantasmilla)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 10:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.bo)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 11:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.cargadito)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 12:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.nada_que_hacer)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 13:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.rival_digno)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
            case 14:
                viewHolder.textView.setText('"'+context.getResources().getString(R.string.sabes_hacer)+'"');
                viewHolder.imageView.setImageResource(common_google_signin_btn_icon_dark);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}
