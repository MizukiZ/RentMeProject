package com.example.mizuki.rentmeproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    FirebaseStorage storage;

    PopupMenu popupMenu;



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
        storage = FirebaseStorage.getInstance();


        // make instance of custom simple adapter and put data in
        itemListAdapter = new ItemListAdapter(this,
                itemListData, // data you want to use
                R.layout.simple_items_card_view, // layout template
                new String[]{"title", "price"}, // from which keys
                new int[]{R.id.itemTitle, R.id.itemPrice}); // where to put the data


        // get post item the user posted
        db.child("Post").orderByChild("user_id").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // init the itemList Data
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

        // set event list view items click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                get clicked view object
                @SuppressWarnings("unchecked")
                HashMap<String, Object> itemObject = (HashMap<String, Object>) itemListAdapter.getItem(position);


                PopupMenu popup = new PopupMenu(OwnPostItemsActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.own_post_list_menu, popup.getMenu());
                //
                popup.show();

                // set popup menu event listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String operation = item.getTitle().toString();
                        switch (operation){
                            case("EDIT"):

                                Intent editPageIntent = new Intent(OwnPostItemsActivity.this, PostEditActivity.class);
                                editPageIntent.putExtra("itemObject", itemObject);
                                OwnPostItemsActivity.this.startActivity(editPageIntent);

                                return true;
                            case("DELETE"):

                                // show confirm dialog
                                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(OwnPostItemsActivity.this);
                                deleteDialog.setMessage("Are you sure?");
                                deleteDialog.setCancelable(true);

                                deleteDialog.setPositiveButton(
                                        "Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // delete corresponding data

                                                db.child("Post").child(itemObject.get("id").toString()).removeValue();

                                                // Create a storage reference from our app
                                                StorageReference imgRef = storage.getReferenceFromUrl(itemObject.get("image").toString());

                                                // delete the corresponding image
                                                imgRef.delete();
                                            }
                                        });

                                deleteDialog.setNegativeButton(
                                        "No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
//                                           //dismiss the dialog
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert11 = deleteDialog.create();
                                alert11.show();

                                return true;
                        }
                        return true;
                    }
                });
            }
        });


    }
}
