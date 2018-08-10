package com.example.mizuki.rentmeproject;

import android.content.ClipData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // init view
        drawerLayout = findViewById(R.id.activity_main);
        navigationView = findViewById(R.id.nv);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }


}
