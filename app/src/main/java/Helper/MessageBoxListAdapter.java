package Helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.mizuki.rentmeproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import Model.User;

public class MessageBoxListAdapter extends SimpleAdapter {

    private DatabaseReference userDB, chatDB, messageDB;
    String imageUrl;
    String currentUserId;

    public MessageBoxListAdapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
    }

    public View getView(int position, View convertView, ViewGroup parent){

        // here you let SimpleAdapter built the view normally.
        View v = super.getView(position, convertView, parent);

        // get reference
        ImageView img = (ImageView) v.getTag();
        if(img == null){
            img = v.findViewById(R.id.userIcon);
            v.setTag(img);
        }

        TextView lastMessageBody = v.findViewById(R.id.lastMessage);
        if(lastMessageBody == null){
            lastMessageBody = v.findViewById(R.id.lastMessage);
            v.setTag(lastMessageBody);
        }

        TextView userNameView = v.findViewById(R.id.messageUserName);
        if(userNameView == null){
            userNameView = v.findViewById(R.id.messageUserName);
            v.setTag(userNameView);
        }

        TextView messageTime = v.findViewById(R.id.timeStamp);
        if(messageTime == null){
            messageTime = v.findViewById(R.id.timeStamp);
            v.setTag(messageTime);
        }

        String imageUrl = ((Map<String, String>)getItem(position)).get("userImage");
        String userName = ((Map<String, String>)getItem(position)).get("userName");
        String lastMsg = ((Map<String, String>)getItem(position)).get("lastMessage");
        String msgTime = ((Map<String, String>)getItem(position)).get("messageTime");



        // set values
        userNameView.setText(userName);
        lastMessageBody.setText(lastMsg);

        if(!msgTime.isEmpty()){
            TimeFormat timeFormat = new TimeFormat(msgTime);
            messageTime.setText(timeFormat.messageForm());
        }else{
            messageTime.setText("");
        }


        if(imageUrl != null){
            // Picasso setting
            Picasso.get()
                    .load(imageUrl)
                    .resize(100,100)
                    .centerCrop()
                    .placeholder(R.drawable.loading_placeholder)
                    .into(img);
        }else{
            img.setImageResource(R.drawable.account);
        }



        // return the view
        return v;
    }
}