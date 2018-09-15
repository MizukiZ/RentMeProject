package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import Model.User;

public class PostUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user);


        Intent i = getIntent();
        // extract passed user object
        Bundle bundle = i.getExtras();
        User user = (User) bundle.getSerializable("userObject");

        TextView userName = findViewById(R.id.postUserName);
        userName.setText(user.getUserName().toString());
    }
}
