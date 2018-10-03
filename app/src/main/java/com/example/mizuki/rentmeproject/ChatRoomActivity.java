package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import Helper.ItemListAdapter;
import Helper.MessageListAdapter;
import Model.ChatRoom;
import Model.Message;

public class ChatRoomActivity extends AppCompatActivity {

    ImageButton sendBtn;
    EditText messageBody;
    ChatRoom currentChatRoom;
    TextView otherUserNameView;
    ScrollView scroll;
    String currentUserId,otherUserName;
    ListView listView;
    SimpleAdapter messageListAdapter;

    ArrayList<HashMap<String, Object>> messageList = new ArrayList<>();

    private DatabaseReference messageDB,chatDB,userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


        // init views
        sendBtn = findViewById(R.id.chatSendBtn);
        messageBody = findViewById(R.id.chatBodyText);
        scroll = findViewById(R.id.chatScroll);
        listView = findViewById(R.id.messageListView);
        otherUserNameView = findViewById(R.id.otherUserName);

        // current  user id
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent i = getIntent();
        String chatRoomId = i.getStringExtra("chatRoomId");
        otherUserName = i.getStringExtra("otherUserName");

        otherUserNameView.setText(otherUserName);

        messageDB = FirebaseDatabase.getInstance().getReference("Message");
        chatDB = FirebaseDatabase.getInstance().getReference("Chat");
        userDB = FirebaseDatabase.getInstance().getReference("Users");

        // make instance of custom simple adapter and put data in
        messageListAdapter = new MessageListAdapter(this,
                messageList, // data you want to use
                R.layout.message_list_view, // layout template
                new String[]{"body"}, // from which keys
                new int[]{R.id.messageBody}); // where to put the data

        // get rid of focus
        messageBody.clearFocus();


        // set event for chat database
        chatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // create ChatRoom instance
                currentChatRoom = dataSnapshot.child(chatRoomId).getValue(ChatRoom.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // set event for message database
        messageDB.orderByChild("chatRoomId").equalTo(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // init the itemList Data
                messageList.clear();

                // loop the data to generate item list array
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Message message = snapshot.getValue(Message.class);

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("id", message.getId());
                    data.put("senderId", message.getSenderId());
                    data.put("recieverId", message.getRecieverId());
                    data.put("chatRoomId", message.getChatRoomId());
                    data.put("body", message.getBody());
                    data.put("created_at", message.getCreated_at());

                    // set both original and itemListdata for adapter
                    messageList.add(data);
                }

                listView.setAdapter(messageListAdapter);

                // move to buttom

                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        // set click event for send button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageBody.getText().toString();

                if(messageText.isEmpty()){
                    Toast.makeText(ChatRoomActivity.this, "The message box is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // get unique id for messageDB
                final String id = messageDB.push().getKey();

                Message messageObj = new Message(
                        id,
                        currentUserId,
                        currentChatRoom.getUser1Id().equals(currentUserId) ? currentChatRoom.getUser2Id() : currentChatRoom.getUser2Id(),
                        currentChatRoom.getId(),
                        messageText,
                        ServerValue.TIMESTAMP
                        );

                messageDB.child(id).setValue(messageObj);

                // empty the text edit field
                messageBody.setText(null);
                messageBody.clearFocus();
            }
        });
    }
}
