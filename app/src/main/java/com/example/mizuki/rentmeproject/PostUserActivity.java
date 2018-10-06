package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Helper.GeocodeHandler;
import Helper.ItemListAdapter;
import Model.ChatRoom;
import Model.Post;
import Model.User;

public class
PostUserActivity extends AppCompatActivity {

    TextView postUserName, postUserBio , postUserLocation;
    ImageView postUserImage;
    Button sendMessage;

    private DatabaseReference chatDB;

    ListView listView;
    SimpleAdapter itemListAdapter;

    ArrayList<HashMap<String, Object>> itemListData = new ArrayList<>();


    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user);


        Intent i = getIntent();
        // extract passed user object
        Bundle bundle = i.getExtras();
        User user = (User) bundle.getSerializable("userObject");

        // init views
        postUserName = findViewById(R.id.postUserName);
        postUserBio = findViewById(R.id.postUserBio);
        postUserLocation = findViewById(R.id.postUserLocatioin);
        postUserImage = findViewById(R.id.postUserImage);
        sendMessage = findViewById(R.id.sendMessageBtn);

        listView = findViewById(R.id.postUserItemListView);

        // firebase
        db = FirebaseDatabase.getInstance().getReference();
        chatDB = FirebaseDatabase.getInstance().getReference("Chat");


        // make instance of custom simple adapter and put data in
        itemListAdapter = new ItemListAdapter(this,
                itemListData, // data you want to use
                R.layout.simple_items_card_view, // layout template
                new String[]{"title", "price"}, // from which keys
                new int[]{R.id.itemTitle, R.id.itemPrice}); // where to put the data

        // get post item the user posted
        db.child("Post").orderByChild("user_id").equalTo(user.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                itemListData.clear();

                // loop the data to generate item list array
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Post post = snapshot.getValue(Post.class);

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("image", post.getImage());
                    data.put("description", post.getDescription());
                    data.put("id", post.getId());
                    data.put("category", post.getCategory());
                    data.put("location", post.getLocation());
                    data.put("title", post.getTitle());
                    data.put("price", post.getCost());
                    data.put("rented", post.isRented());
                    data.put("created_at", post.getCreated_at());
                    data.put("updated_at", post.getUpdated_at());
                    data.put("user_id", post.getUser_id());

                    // set both original and itemListdata for adapter
                    itemListData.add(data);
                }
                // set list view with the custom adapter
                listView.setAdapter(itemListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // cast object to map
        @SuppressWarnings("unchecked")
        Map<String, Double> location = (Map<String, Double>)user.getLocation();

        if(location != null){
            // if there is a location
            // create geocode handler and get place name form lat and lon
            GeocodeHandler geocodeHandler = new GeocodeHandler(this,location.get("lat"),location.get("lon"));

            // set location to text view
            postUserLocation.setText(geocodeHandler.getPlaceName());
        } else{
            postUserLocation.setText("Not provided");
        }


        // set post user information

        postUserName.setText(user.getUserName() != null ? user.getUserName() : "Not provided");
        postUserBio.setText(user.getBio() != null ? user.getBio() : "Not provided");

        // set post user image
        Picasso.get()
                .load(user.getImage())
                .resize(600,500)
                .placeholder(R.drawable.placeholder)
                .into(postUserImage);


        // set event list view items click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //get clicked view object
                @SuppressWarnings("unchecked")
                HashMap<String, Object> itemObject = (HashMap<String, Object>) itemListAdapter.getItem(position);

                Intent detailPageIntent = new Intent(PostUserActivity.this, ItemDetailActivity.class);
                detailPageIntent.putExtra("itemObject", itemObject);
                PostUserActivity.this.startActivity(detailPageIntent);
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                            Intent chatRoomPageIntent = new Intent(PostUserActivity.this, ChatRoomActivity.class);
                            // pass corresponding chat room id
                            chatRoomPageIntent.putExtra("chatRoomId", chatRoomId);
                            chatRoomPageIntent.putExtra("otherUserName", user.getUserName());
                            PostUserActivity.this.startActivity(chatRoomPageIntent);

                        }else {
                            // get unique chat room id
                            final String id = chatDB.push().getKey();

                            // create new chart room
                            ChatRoom chatRoom = new ChatRoom(id,currentUserId,postUserId);
                            chatDB.child(id).setValue(chatRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // success handling
                                    Toast.makeText(PostUserActivity.this, "Created new chat room", Toast.LENGTH_SHORT).show();

                                    // jump to chat room
                                    Intent chatRoomPageIntent = new Intent(PostUserActivity.this, ChatRoomActivity.class);
                                    // pass corresponding chat room id
                                    chatRoomPageIntent.putExtra("chatRoomId", id);
                                    chatRoomPageIntent.putExtra("otherUserName", user.getUserName());
                                    PostUserActivity.this.startActivity(chatRoomPageIntent);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // fail handling
                                    Toast.makeText(PostUserActivity.this, "Sorry something went wrong", Toast.LENGTH_SHORT).show();

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
    }
}
