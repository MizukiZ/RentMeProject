package Helper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.mizuki.rentmeproject.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class ItemListAdapter extends SimpleAdapter {

    public ItemListAdapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
    }

    public View getView(int position, View convertView, ViewGroup parent){

        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        // get reference
        ImageView img = (ImageView) v.getTag();
        if(img == null){
            img = v.findViewById(R.id.itemImg);
            v.setTag(img);
        }

        TextView rentedText = v.findViewById(R.id.itemRented);
        if(rentedText == null){
            rentedText = v.findViewById(R.id.itemRented);
            v.setTag(rentedText);
        }

        // get the rented attribute and check the boolean value
        @SuppressWarnings("unchecked")
        Boolean isRented = ((Map<String, Boolean>)getItem(position)).get("rented");

        if(isRented) {
            // if the item is rented
            rentedText.setText("Rented");
            // set opacity for the rented item
            img.setAlpha(0.4f);
        }else{
            // if the item is not rented
            rentedText.setText(null);
            img.setAlpha(1.0f);
        }



        // get the url from the data the value of key "image" to the `Map`
        @SuppressWarnings("unchecked")
        String url = ((Map<String, String>)getItem(position)).get("image");

        // Picasso setting
        Picasso.get()
                .load(url)
                .resize(500,500)
                .centerCrop()
                .placeholder(R.drawable.loading_placeholder)
                .into(img);

        // return the view
        return v;
    }
}