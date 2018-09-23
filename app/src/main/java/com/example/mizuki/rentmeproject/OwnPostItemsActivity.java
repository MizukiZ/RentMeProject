package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import Helper.ItemListAdapter;
import Model.Post;

public class OwnPostItemsActivity extends AppCompatActivity {

    ListView listView;
    SimpleAdapter itemListAdapter;

    ArrayList<HashMap<String, Object>> itemListData = new ArrayList<>();

    private DatabaseReference db;
    FirebaseUser currentFirebaseUser;
    String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_post_items);

        listView = findViewById(R.id.ownItemsListView);

        // firebase
        db = FirebaseDatabase.getInstance().getReference();
        // get current user object and get user id
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        userID = currentFirebaseUser.getUid();



        // make instance of custom simple adapter and put data in
        itemListAdapter = new ItemListAdapter(this,
                itemListData, // data you want to use
                R.layout.simple_items_card_view, // layout template
                new String[]{"title", "price"}, // from which keys
                new int[]{R.id.itemTitle, R.id.itemPrice}); // where to put the data


        // get post item the user posted
        db.child("Post").orderByChild("user_id").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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

        // set event list view items click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //get clicked view object
                @SuppressWarnings("unchecked")
                HashMap<String, Object> itemObject = (HashMap<String, Object>) itemListAdapter.getItem(position);

                Intent detailPageIntent = new Intent(OwnPostItemsActivity.this, ItemDetailActivity.class);
                detailPageIntent.putExtra("itemObject", itemObject);
                OwnPostItemsActivity.this.startActivity(detailPageIntent);
            }
        });
    }
}
