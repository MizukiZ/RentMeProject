package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import Model.ChatRoom;
import Model.Post;
import Model.User;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView itemImage,itemDetailUserImage;
    TextView itemTitle,itemPostTime,itemDescription,itemPrice,postUserName,itemDetailLocation;
    Button sendMessage;
    Post post;
    User postUser,user;


    private DatabaseReference userDB, chatDB;

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
        sendMessage = findViewById(R.id.btnSendMessage);

        //firebase
        userDB = FirebaseDatabase.getInstance().getReference("Users");
        chatDB = FirebaseDatabase.getInstance().getReference("Chat");

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
                .resize(600,400)
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
                user = new User(
                        post.getUser_id(),
                        postUser.getUserName(),
                        null, // no need
                        null, // no need
                        postUser.getImage(),
                        postUser.getBio(),
                        postUser.getLocation()
                );

                // set post user name
                postUserName.setText(postUser.getUserName());

                // user image
                Picasso.get()
                        .load(user.getImage())
                        .resize(500,500)
                        .placeholder(R.drawable.placeholder)
                        .into(itemDetailUserImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // set click event for clicking post user icon
        itemDetailUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // jump to post user page

                Intent postUserPageIntent = new Intent(ItemDetailActivity.this, PostUserActivity.class);
                Bundle userOb = new Bundle();
                userOb.putSerializable("userObject",user);
                postUserPageIntent.putExtras(userOb);
                ItemDetailActivity.this.startActivity(postUserPageIntent);
            }
        });

        // set click event for sendMessage button
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if current user and post user has chat room between them

                // get users id
                String postUserId = user.getId();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                chatDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // if chat room exists
                        boolean isExist = false;
                        String chatRoomId = null;

                        // loop the data and check if chat room exists
                        for(DataSnapshot chat : dataSnapshot.getChildren()) {

                            if((chat.child("user1Id").getValue().equals(postUserId) || chat.child("user1Id").getValue().equals(currentUserId)) &&
                                    (chat.child("user2Id").getValue().equals(postUserId) || chat.child("user2Id").getValue().equals(currentUserId))
                                    ){
                                isExist = true;
                                chatRoomId = chat.child("id").getValue().toString();
                            }
                        }

                        if(isExist){
                            // if already exists go to chat room

                            Intent chatRoomPageIntent = new Intent(ItemDetailActivity.this, ChatRoomActivity.class);
                            // pass corresponding chat room id
                            chatRoomPageIntent.putExtra("chatRoomId", chatRoomId);
                            ItemDetailActivity.this.startActivity(chatRoomPageIntent);

                        }else {
                            // get unique chat room id
                            final String id = chatDB.push().getKey();

                            // create new chart room
                            ChatRoom chatRoom = new ChatRoom(id,currentUserId,postUserId);
                            chatDB.child(id).setValue(chatRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // success handling
                                    Toast.makeText(ItemDetailActivity.this, "Created new chat room", Toast.LENGTH_SHORT).show();

                                    // jump to chat room
                                    Intent chatRoomPageIntent = new Intent(ItemDetailActivity.this, ChatRoomActivity.class);
                                    // pass corresponding chat room id
                                    chatRoomPageIntent.putExtra("chatRoomId", id);
                                    ItemDetailActivity.this.startActivity(chatRoomPageIntent);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // fail handling
                                    Toast.makeText(ItemDetailActivity.this, "Sorry something went wrong", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        itemTitle.setText(post.getTitle());

      TimeFormat timeFormatHelp = new TimeFormat(post.getUpdated_at().toString());
        itemPostTime.setText(timeFormatHelp.postedOnForm());

        itemDescription.setText(post.getDescription());

        itemPrice.setText(post.getCost().toString());

    }
}
