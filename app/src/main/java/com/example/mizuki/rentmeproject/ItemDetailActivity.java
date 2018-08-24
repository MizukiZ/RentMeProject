package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.GeocodeHandler;
import Helper.TimeFormat;
import Model.Post;
import Model.User;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView itemImage,itemDetailUserImage;
    TextView itemTitle,itemPostTime,itemDescription,itemPrice,postUserName,itemDetailLocation;
    Post post;
    User postUser;

    private DatabaseReference userDB;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // init views
        itemImage = findViewById(R.id.itemDetailImage);
        itemTitle = findViewById(R.id.itemDetailTitle);
        itemPostTime = findViewById(R.id.itemDetailPostDate);
        itemDescription = findViewById(R.id.itemDetailDescription);
        itemPrice = findViewById(R.id.itemDetailPrice);
        itemDetailLocation = findViewById(R.id.itemDetailLocation);
        postUserName = findViewById(R.id.itemDetailUserName);
        itemDetailUserImage = findViewById(R.id.itemDetailUserImage);

        //firebase
        userDB = FirebaseDatabase.getInstance().getReference("Users");

        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> itemHash = (HashMap<String, Object>)intent.getSerializableExtra("itemObject");

        // cast object to map
        @SuppressWarnings("unchecked")
        Map<String, Double> location = (Map<String, Double>)itemHash.get("location");

        // create post object by passed data
        post  = new Post(
                itemHash.get("id").toString(),
                itemHash.get("title").toString(),
                itemHash.get("description").toString(),
                itemHash.get("image").toString(),
                location,
                itemHash.get("category").toString(),
                itemHash.get("user_id").toString(),
                Double.valueOf(itemHash.get("price").toString()),
                (boolean)itemHash.get("rented"),
                itemHash.get("created_at").toString(),
                itemHash.get("updated_at").toString()
                );

        // create geocode handler and get place name form lat and lon
        GeocodeHandler geocodeHandler = new GeocodeHandler(this,location.get("lat"),location.get("lon"));

            itemDetailLocation.setText(geocodeHandler.getPlaceName());



        Picasso.get()
                .load(itemHash.get("image").toString())
                .resize(500,300)
                .placeholder(R.drawable.loading_placeholder)
                .into(itemImage);


        // create google map and set to the fragment view
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                LatLng itemLocation = new LatLng(
                        post.getLocation().get("lat"),
                        post.getLocation().get("lon"));

                googleMap.addMarker(new MarkerOptions().position(itemLocation)
                        .title("Item's location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemLocation,15.0f));
            }
        });

        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // get user id from post item and create user instance
                postUser = dataSnapshot.child(post.getUser_id()).getValue(User.class);

                // create user instance
                User user = new User(
                        null,
                        postUser.getUserName().toString(),
                        null,
                        null,
                        postUser.getImage() != null ? postUser.getImage().toString() : null,
                        postUser.getBio().toString(),
                        postUser.getLocation()
                );

                // set post user name
                postUserName.setText(postUser.getUserName());

                Picasso.get()
                        .load(user.getImage())
                        .resize(500,300)
                        .placeholder(R.drawable.account)
                        .into(itemDetailUserImage);
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
