package Helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.mizuki.rentmeproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class MessageListAdapter extends SimpleAdapter {


    // constructor
    public MessageListAdapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
    }

    public View getView(int position, View convertView, ViewGroup parent){

        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        // get reference
        TextView messageTime = (TextView) v.getTag();
        if(messageTime == null){
            messageTime = v.findViewById(R.id.messageTime);
            v.setTag(messageTime);
        }

        LinearLayout bubbleLayout = v.findViewById(R.id.bubbleLayout);
        LinearLayout bubble = v.findViewById(R.id.bubble);


        // get message time stamp as string
        String timeStamp = String.valueOf(((Map<String, Long>)getItem(position)).get("created_at"));
        // use helper to format time
        TimeFormat timeFormat = new TimeFormat(timeStamp);
       // set text to time filed
        messageTime.setText(timeFormat.messageForm());

        // current user id
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderId = ((Map<String, String>)getItem(position)).get("senderId");

        // check if current user id is same as message sender Id
        if(currentUserId.equals(senderId)){
            // put message on the right (own message)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.RIGHT;
            bubbleLayout.setLayoutParams(params);
            bubble.setBackgroundResource(R.drawable.chat_bubble_blue);
        }else{
            // put message on the right (own message)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.LEFT;
            bubbleLayout.setLayoutParams(params);
            bubble.setBackgroundResource(R.drawable.chat_bubble_green);
        }

        // return the view
        return v;
    }
}
