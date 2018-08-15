package Helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.example.mizuki.rentmeproject.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class ItemListAdapter extends SimpleAdapter {

    public ItemListAdapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
    }

    public View getView(int position, View convertView, ViewGroup parent){

        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        // Then we get reference for Picasso
        ImageView img = (ImageView) v.getTag();
        if(img == null){
            img = v.findViewById(R.id.itemImg);
            v.setTag(img);
        }

        // get the url from the data the value of key "image" to the `Map`
        @SuppressWarnings("unchecked")
        String url = ((Map<String, String>)getItem(position)).get("image");

        // Picasso setting
        Picasso.get()
                .load(url)
                .resize(150,150)
                .centerCrop()
                .placeholder(R.drawable.loading_placeholder)
                .into(img);

        // return the view
        return v;
    }
}
