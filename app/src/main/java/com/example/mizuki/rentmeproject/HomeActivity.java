package com.example.mizuki.rentmeproject;

import android.content.ClipData;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private DatabaseReference db;


    ArrayList<HashMap<String,String>> itemListData = new ArrayList<>();
    SimpleAdapter itemListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        // init view
        drawerLayout = findViewById(R.id.activity_main);
        navigationView = findViewById(R.id.nv);
        toolbar =  findViewById(R.id.toolbar);


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
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.syncState();


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
                            Toast.makeText(HomeActivity.this, "Account",Toast.LENGTH_SHORT).show();
                            return true;

                    case R.id.itemMessageBox:
                        Toast.makeText(HomeActivity.this, "MessageBox",Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.itemPostbox:
                        Toast.makeText(HomeActivity.this, "Postbox",Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.itemHelp:
                        Toast.makeText(HomeActivity.this, "Help",Toast.LENGTH_SHORT).show();
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

        // Attach a listener to read the data
        db.child("Post").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // reset the list
                itemListData.clear();

                // loop the data to generate item list array
              for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                  Post post = snapshot.getValue(Post.class);

                  HashMap<String, String> data = new HashMap<>();
                  data.put("image", post.getImage());
                  data.put("title", post.getTitle());
                  data.put("price", post.getCost().toString());
                  itemListData.add(data);
              }
               // set list view with the custom adapter
                ListView listView = findViewById(R.id.listview1);
                listView.setAdapter(itemListAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    // Menu init
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // get searchview menu file and set to menu
        getMenuInflater().inflate(R.menu.searchview_menu, menu);

        // activate search item
        MenuItem searchItem = menu.findItem(R.id.itemSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // set hint text
        searchView.setQueryHint("Search items");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // when user submit the text
                Toast.makeText(HomeActivity.this, query,Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // when user inputs text
                return false;
            }
        });

        return true;
    }


}
