package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ChatRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        TextView roomId = findViewById(R.id.roomId);

        Intent i = getIntent();
        String chatRoomId = i.getStringExtra("chatRoomId");

        roomId.setText(chatRoomId);
    }
}
