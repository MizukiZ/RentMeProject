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
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import Model.Post;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private ImageView testImage;

    private DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // init view
        drawerLayout = findViewById(R.id.activity_main);
        navigationView = findViewById(R.id.nv);
        toolbar =  findViewById(R.id.toolbar);
        testImage = findViewById(R.id.testImage);

        // firebase
        db = FirebaseDatabase.getInstance().getReference();

        setSupportActionBar(toolbar);


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
              //  testing
//                 String imgPath = dataSnapshot.child("img").getValue().toString();
//                Picasso.get().load(imgPath).into(testImage);
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
