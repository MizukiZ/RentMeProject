package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import Helper.TimeFormat;
import Model.Post;
import Model.User;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView itemImage;
    TextView itemTitle,itemPostTime,itemDescription,itemPrice,postUserName;
    Post post;
    User postUser;

    private DatabaseReference userDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);


        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.681382, 139.766084), 15));
            }
        });


        // init views
        itemImage = findViewById(R.id.itemDetailImage);
        itemTitle = findViewById(R.id.itemDetailTitle);
        itemPostTime = findViewById(R.id.itemDetailPostDate);
        itemDescription = findViewById(R.id.itemDetailDescription);
        itemPrice = findViewById(R.id.itemDetailPrice);
        postUserName = findViewById(R.id.itemDetailUserName);

        //firebase
        userDB = FirebaseDatabase.getInstance().getReference("Users");

        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> itemHash = (HashMap<String, Object>)intent.getSerializableExtra("itemObject");

        // create post object by passed data
        post  = new Post(
                itemHash.get("id").toString(),
                itemHash.get("title").toString(),
                itemHash.get("description").toString(),
                itemHash.get("image").toString(),
                itemHash.get("location").toString(),
                itemHash.get("category").toString(),
                itemHash.get("user_id").toString(),
                Double.valueOf(itemHash.get("price").toString()),
                (boolean)itemHash.get("rented"),
                itemHash.get("created_at").toString(),
                itemHash.get("updated_at").toString()
                );

        Picasso.get()
                .load(itemHash.get("image").toString())
                .resize(500,300)
                .placeholder(R.drawable.loading_placeholder)
                .into(itemImage);

        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // get user id from post item and create user instance
                postUser = dataSnapshot.child(post.getUser_id()).getValue(User.class);
                // set post user name
                postUserName.setText(postUser.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        itemTitle.setText(post.getTitle());

      TimeFormat timeFormatHelp = new TimeFormat(post.getUpdated_at().toString());
        itemPostTime.setText(timeFormatHelp.postedOnForm());

        itemDescription.setText(post.getDescription());

        itemPrice.setText(post.getCost().toString());

    }
}
