package com.example.mizuki.rentmeproject;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import Helper.ItemListAdapter;
import Model.Post;
import Model.User;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ListView listView;

    private SearchView searchView;
    private MenuItem searchItem;

    private Button sportBtn, appilianceBtn, instrumentBtn, clotheBtn, toolBtn, rideBtn,resetBtn;

    android.app.AlertDialog searchDialog;

    private DatabaseReference db;
    User currentUser;

    ValueEventListener updateEventListener;

    private String userId;


    ArrayList<HashMap<String,Object>> itemListData = new ArrayList<>();
    SimpleAdapter itemListAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        if(currentUser != null){
        Log.d("Userdata", currentUser.getEmail());}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // if no user is logged in, go back to login page
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        } else {
            // if there is a current user get user ID
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


            // init view
            drawerLayout = findViewById(R.id.activity_main);
            navigationView = findViewById(R.id.nv);
            toolbar = findViewById(R.id.toolbar);

            sportBtn = findViewById(R.id.sportsBtn);
            appilianceBtn = findViewById(R.id.appilianceBtn);
            instrumentBtn = findViewById(R.id.insutrumentBtn);
            clotheBtn = findViewById(R.id.clotheBtn);
            toolBtn = findViewById(R.id.toolBtn);
            rideBtn = findViewById(R.id.rideBtn);
            resetBtn = findViewById(R.id.resetBtn);

            listView = findViewById(R.id.listview1);

            // firebase
            db = FirebaseDatabase.getInstance().getReference();

            // make instance of custom simple adapter and put data in
            itemListAdapter = new ItemListAdapter(this,
                    itemListData, // data you want to use
                    R.layout.items_card_view, // layout template
                    new String[]{"title", "price"}, // from which keys
                    new int[]{R.id.itemTitle, R.id.itemPrice}); // where to put the data

            // set toolbar as actionbar(converting)
            setSupportActionBar(toolbar);

            // set drawerToggle in toolbar
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            actionBarDrawerToggle.syncState();

            // make progress dialog and show
            searchDialog = new SpotsDialog.Builder().setContext(HomeActivity.this)
                    .setMessage("Searching")
                    .build();

            searchDialog.show();



            // set update event listener
            updateEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){

                        // no result message
                        Toast.makeText(HomeActivity.this, "No result found", Toast.LENGTH_SHORT).show();
                    }
                    String resultCount = String.valueOf(dataSnapshot.getChildrenCount());
                    Toast.makeText(HomeActivity.this, resultCount + " result found", Toast.LENGTH_SHORT).show();

                    updateListVIew(dataSnapshot);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            };

            // set event list view items click listener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    //get clicked view object
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> itemObject = (HashMap<String, Object>) itemListAdapter.getItem(position);

                    Intent detailPageIntent = new Intent(HomeActivity.this, ItemDetailActivity.class);
                    detailPageIntent.putExtra("itemObject", itemObject);
                    HomeActivity.this.startActivity(detailPageIntent);
                }
            });

            // click event for category buttons

            sportBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                 categoryFilter("Sport");
                }
            });

            appilianceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("Appliance");
                }
            });

            instrumentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("Instrument");
                }
            });

            clotheBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("Clothe");
                }
            });

            toolBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("Tool");
                }
            });

            rideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("Ride");
                }
            });

            resetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("All");
                }
            });




            // click event for the navigation items
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override

                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    switch (id) {
                        case R.id.itemPost:
                            //  redirect to new post page
                            startActivity(new Intent(HomeActivity.this, PostActivity.class));
                            return true;

                        case R.id.itemAccount:
                            Toast.makeText(HomeActivity.this, "Account", Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.itemMessageBox:
                            Toast.makeText(HomeActivity.this, "MessageBox", Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.itemPostbox:
                            Toast.makeText(HomeActivity.this, "Postbox", Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.itemHelp:
                            Toast.makeText(HomeActivity.this, "Help", Toast.LENGTH_SHORT).show();
                            return true;

                        case R.id.itemLogout:
//                                 when logout is clicked sign out form firebase auth
                            FirebaseAuth.getInstance().signOut();
                            // redirect to login page
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            finish();
                            return true;

                        default:
                            return true;
                    }
                }
            });

            // Attach a listener to read the user data
            db.child("Users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // get user data by user id and store it into object
                    currentUser = dataSnapshot.child(userId).getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // set all of them as default
            categoryFilter("All");

        }

        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){

            if (actionBarDrawerToggle.onOptionsItemSelected(item))
                return true;

            return super.onOptionsItemSelected(item);
        }

        // Menu init
        @Override
        public boolean onCreateOptionsMenu (Menu menu){

            // get searchview menu file and set to menu
            getMenuInflater().inflate(R.menu.searchview_menu, menu);

            // activate search item
            searchItem = menu.findItem(R.id.itemSearch);
            searchView = (SearchView) searchItem.getActionView();

            // set hint text
            searchView.setQueryHint("Search items");


            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchDialog.show();

                    // when user submit the text
                    wordSearch(query);

                    // make empty and close the search field
                    searchView.clearFocus();
                    searchItem.collapseActionView();

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // when user inputs text
                    return false;
                }
            });

            return true;
        }

        public void updateListVIew(DataSnapshot dataSnapshot){

            // reset the list
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

                itemListData.add(data);
            }
            // set list view with the custom adapter
            listView.setAdapter(itemListAdapter);


            searchDialog.dismiss();
        }

        public void categoryFilter(String category){

            // empty search field
            if(searchView != null) {
                // make empty and close the search field
                searchView.clearFocus();
                searchItem.collapseActionView();
            }

            if(category.equals("All")){

                // remove previous listener
                db.child("Post").removeEventListener(updateEventListener);
                db.child("Post").removeEventListener(updateEventListener);
                db.child("Post").removeEventListener(updateEventListener);

                // when category all
                db.child("Post").addValueEventListener(updateEventListener);

            }else {

                // remove previous listener
                db.child("Post").removeEventListener(updateEventListener);
                db.child("Post").removeEventListener(updateEventListener);
                db.child("Post").removeEventListener(updateEventListener);

                // when something but All
                db.child("Post").orderByChild("category").equalTo(category).addValueEventListener(updateEventListener);

            }
        }

        public void wordSearch(String query){
           // remove previous listener
            db.child("Post").removeEventListener(updateEventListener);
            db.child("Post").removeEventListener(updateEventListener);
            db.child("Post").removeEventListener(updateEventListener);


            db.child("Post").orderByChild("title").startAt(query).endAt(query + "\uf8ff")
                    .addValueEventListener(updateEventListener);

        }




}
