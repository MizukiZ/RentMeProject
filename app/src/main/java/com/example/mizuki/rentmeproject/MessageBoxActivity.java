package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Helper.ItemListAdapter;
import Helper.MessageBoxListAdapter;
import Model.ChatRoom;
import Model.Message;
import Model.User;

public class MessageBoxActivity extends AppCompatActivity {

    private DatabaseReference db;
    ListView listView;
    SimpleAdapter messageListAdapter;
    String currentUserId;
    String otherUserId;

    User otherUser;

    ArrayList<Message> messageModelList = new ArrayList<>();
    ArrayList<HashMap<String, Object>> chatListData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_box);

        listView = findViewById(R.id.messageBoxListView);

        // firebase
        db = FirebaseDatabase.getInstance().getReference();

        // current user id
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // make instance of custom simple adapter and put data in
        messageListAdapter = new MessageBoxListAdapter(this,
                chatListData, // data you want to use
                R.layout.message_box_view, // layout template
                new String[]{"user2Id"}, // from which keys
                new int[]{R.id.lastMessage}); // where to put the data

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // init the itemList Data
                chatListData.clear();

                // loop the data to generate item list array
                for (DataSnapshot snapshot : dataSnapshot.child("Chat").getChildren()) {

                    // init list
                    messageModelList.clear();

                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);

                    // choose only current user related chat rooms
                    if(chatRoom.getUser1Id().equals(currentUserId) || chatRoom.getUser2Id().equals(currentUserId)){

                        // get other user id
                        otherUserId = chatRoom.getUser1Id().equals(currentUserId) ? chatRoom.getUser2Id() : chatRoom.getUser1Id();
                        // set user data to Model
                        otherUser = dataSnapshot.child("Users").child(otherUserId).getValue(User.class);

                        for(DataSnapshot msg : dataSnapshot.child("Message").getChildren()){
                            Message message = msg.getValue(Message.class);

                            // choose only the corresponding message
                            if(message.getChatRoomId().equals(chatRoom.getId())){
                                messageModelList.add(message);
                            }

                        }



                        String lastMessage = !messageModelList.isEmpty() ? messageModelList.get(messageModelList.size()-1).getBody() : "";
                        String lastMessageTime = !messageModelList.isEmpty() ? messageModelList.get(messageModelList.size()-1).getCreated_at().toString() : "";
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("id", chatRoom.getId());
                        data.put("user1Id", chatRoom.getUser1Id());
                        data.put("user2Id", chatRoom.getUser2Id());
                        data.put("userImage", otherUser.getImage());
                        data.put("userName", otherUser.getUserName());
                        data.put("lastMessage", lastMessage);
                        data.put("chatRoomId", chatRoom.getId());
                        data.put("messageTime", lastMessageTime);

                        // set both original and chatListdata for adapter
                        chatListData.add(data);
                    }

                }
//                 set list view with the custom adapter
                Log.d("chatListData", chatListData.toString());
                listView.setAdapter(messageListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // set event list view items click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //get clicked view object
                @SuppressWarnings("unchecked")
                HashMap<String, Object> messageAPObject = (HashMap<String, Object>) messageListAdapter.getItem(position);

                Intent chatRoomPageIntent = new Intent(MessageBoxActivity.this, ChatRoomActivity.class);
                // pass corresponding chat room id
                chatRoomPageIntent.putExtra("chatRoomId", messageAPObject.get("chatRoomId").toString());
                chatRoomPageIntent.putExtra("otherUserName", messageAPObject.get("userName").toString());
                MessageBoxActivity.this.startActivity(chatRoomPageIntent);
            }
        });
    }

}
