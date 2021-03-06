package com.example.mizuki.rentmeproject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.ItemFilterHandler;
import Helper.ItemListAdapter;
import Helper.filterChip;
import Model.Post;
import Model.User;
import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ListView listView;
    private String userId;
    private EditText distanceInput;
    private int inputDistance;

    private SearchView searchView;
    private MenuItem searchItem;

    private Button sportBtn, appilianceBtn, instrumentBtn, clotheBtn, toolBtn, rideBtn, resetBtn, nearByBtn, homePostBtn;

    android.app.AlertDialog searchDialog;

    private DatabaseReference db;
    User currentUser;

    final int GPS_PERMISSION_CODE = 44;
    boolean gpsPermission = false;

    boolean defaultData;

    private Double currentLat, currentLon;


    ValueEventListener updateEventListener;

    private FusedLocationProviderClient locationClient;

    List<Chip> chipList;
    ChipView chipDefault;

    String searchQuery;

    HashMap<String, String> filterCriteria;


    ArrayList<HashMap<String, Object>> originalItemListData = new ArrayList<>();
    ArrayList<HashMap<String, Object>> itemListData = new ArrayList<>();
    SimpleAdapter itemListAdapter;


    // for setting custom fonts(using library called Callingraphy)
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
            nearByBtn = findViewById(R.id.nearByBtn);
            homePostBtn = findViewById(R.id.homePostBtn);
            chipList = new ArrayList<>();
            chipDefault = findViewById(R.id.chipview);

            // set initial criteria
            filterCriteria = new HashMap<String, String>();
            filterCriteria.put("category", "all");
            filterCriteria.put("distance", "all");
            filterCriteria.put("word", "all");

            defaultData = true;

            listView = findViewById(R.id.itemListView);

            // firebase
            db = FirebaseDatabase.getInstance().getReference();


            // location service
            locationClient = LocationServices.getFusedLocationProviderClient(this);


            // make instance of custom simple adapter and put data in
            itemListAdapter = new ItemListAdapter(this,
                    itemListData, // data you want to use
                    R.layout.items_card_view, // layout template
                    new String[]{"title", "price"}, // from which keys
                    new int[]{R.id.itemTitle, R.id.itemPrice}); // where to put the data

            // set toolbar as actionbar(converting)
            setSupportActionBar(toolbar);

            // remove the title form manifest lable
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setTitle("");
            toolbar.setSubtitle("");
            // ask permission for the GPS use
            requestPermission();


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
                    if (!dataSnapshot.exists()) {
                        // no result message
                        Toast.makeText(HomeActivity.this, "No result found", Toast.LENGTH_SHORT).show();

                    } else {
                        String resultCount = String.valueOf(dataSnapshot.getChildrenCount());
                        Toast.makeText(HomeActivity.this, resultCount + " result found", Toast.LENGTH_SHORT).show();
                    }

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
                    categoryFilter("Sport");
                }
            });

            appilianceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryFilter("Appliance");
                }
            });

            instrumentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryFilter("Instrument");
                }
            });

            clotheBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryFilter("Clothe");
                }
            });

            toolBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryFilter("Tool");
                }
            });

            rideBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryFilter("Ride");
                }
            });

            nearByBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Create Distance filter dialog
                    final View cdialog = getLayoutInflater().inflate(R.layout.nearby_dialog, null);
                    AlertDialog.Builder distanceDialog = new AlertDialog.Builder(HomeActivity.this);
                    distanceDialog.setTitle("Distance Filter(km)");
                    distanceDialog.setView(cdialog);

                    // set positive button event
                    distanceDialog.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            distanceInput = cdialog.findViewById(R.id.distanceInput);
                            inputDistance = Integer.valueOf(distanceInput.getText().toString());
                            detectGPS();
                        }
                    });

                    distanceDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    distanceDialog.create();

                    distanceDialog.show();
                }
            });

            resetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDialog.show();
                    categoryFilter("init");

                    filterCriteria.put("category", "all");
                    filterCriteria.put("distance", "all");
                    filterCriteria.put("word", "all");

                    addFilterChip(4, "reset");
                }
            });

            homePostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  redirect to new post page
                    startActivity(new Intent(HomeActivity.this, PostActivity.class));
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

                            //  redirect to post page
                            Intent ownProfilePageIntent = new Intent(HomeActivity.this, OwnProfilePage.class);

                            // set user id
                            currentUser.setId(userId);
                            ownProfilePageIntent.putExtra("user", currentUser.toHashData());
                            HomeActivity.this.startActivity(ownProfilePageIntent);
                            return true;


                        case R.id.itemMessageBox:
                            startActivity(new Intent(HomeActivity.this, MessageBoxActivity.class));
                            return true;

                        case R.id.itemPostbox:
                            //  redirect to new post page
                            startActivity(new Intent(HomeActivity.this, OwnPostItemsActivity.class));
                            return true;

                        case R.id.itemHelp:
                            //  redirect to help page
                            startActivity(new Intent(HomeActivity.this, HelpActivity.class));
                            return true;

                        case R.id.itemLogout:
//                                 when logout is clicked show dialog

                            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(HomeActivity.this);
                            logoutDialog.setMessage("Do you want to logout?");
                            logoutDialog.setCancelable(true);

                            logoutDialog.setPositiveButton(
                                    "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // yes is clicked signout from firebase

                                            FirebaseAuth.getInstance().signOut();
                                            // redirect to login page
                                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    });

                            logoutDialog.setNegativeButton(
                                    "No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

//                                           //dismiss the dialog
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = logoutDialog.create();
                            alert11.show();

                            return true;

                        default:
                            return true;
                    }
                }
            });

            // Attach a listener to read the user data
            db.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
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
            categoryFilter("init");
            addFilterChip(4, "reset");
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    // Menu init
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // get searchview menu file and set to menu
        getMenuInflater().inflate(R.menu.searchview_menu, menu);

        // activate search item
        searchItem = menu.findItem(R.id.itemSearch);
        searchView = (SearchView) searchItem.getActionView();

        // set hint text
        searchView.setQueryHint("What do you want to rent?");


        // search on click event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchQuery = query;

                // when user submit the text
                wordSearch(searchQuery);

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

    public void updateListVIew(DataSnapshot dataSnapshot) {

        // reset the both list
        originalItemListData.clear();
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
            originalItemListData.add(data);
            itemListData.add(data);
        }
        // set list view with the custom adapter
        listView.setAdapter(itemListAdapter);

        // dismiss dialog
        searchDialog.dismiss();
    }

    public void categoryFilter(String category) {


        // empty search field
        if (searchView != null) {
            // make empty and close the search field
            searchView.clearFocus();
            searchItem.collapseActionView();
        }

        if (category.equals("init")) {

            // when category init get all data
            db.child("Post").addListenerForSingleValueEvent(updateEventListener);

        } else {

            //set new criteria for category
            filterCriteria.put("category", category);

            combineFilterCriteria();

        }
    }

    public void wordSearch(String query) {

        //set new criteria for word
        filterCriteria.put("word", query);

        combineFilterCriteria();
    }

    public void detectGPS() {

        if (gpsPermission) {
            final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // gps is off
                Toast.makeText(this, "Please turn on GPS to use this service", Toast.LENGTH_SHORT).show();

                String locationProviders = Settings.Secure.getString(getContentResolver(), LocationManager.PROVIDERS_CHANGED_ACTION);
                if (locationProviders == null || locationProviders.equals("")) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            } else {
                // gps is ON
                // call get location func
                getCurrentLocation();
            }
        } else {
            // no permission handling
            Toast.makeText(this, "Please accept GPS permission", Toast.LENGTH_SHORT).show();

        }
    }

    public void getCurrentLocation() {

        // validation
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location service", "No Permission");
            return;
        }

        // get current location
        locationClient.getLastLocation().addOnSuccessListener(HomeActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Toast.makeText(HomeActivity.this, "Sorry could't get your location, try again.", Toast.LENGTH_SHORT).show();
                } else {

                    // set current latitude and longitude
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();

                    filterCriteria.put("distance", String.valueOf(inputDistance));

                    combineFilterCriteria();

                }
            }
        });

    }

    public void combineFilterCriteria() {

        //reset all chips
        addFilterChip(4, "reset");

        boolean notFirstOne = false;


        // add filer chip
        for (Object key : filterCriteria.keySet()) {
            String value = filterCriteria.get(key);

            if(!value.equals("all")){
                int index;
                if(key.equals("category")){
                    index = 1;
                }else if(key.equals("distance")){
                    index = 2;
                }else {
                    index = 3;
                }

                addFilterChip(index,filterCriteria.get(key));
            }

        }


        ArrayList<HashMap<String, Object>> filterResult = new ArrayList<>();


        // get filter helper instance with new originalItemListData
        ItemFilterHandler filterHelp = new ItemFilterHandler(originalItemListData);

        if (!filterCriteria.get("category").equals("all")) {

            notFirstOne = true;

            // there is a category filter
            filterResult = (ArrayList<HashMap<String, Object>>) filterHelp.categoryFilter(filterCriteria.get("category").toString());
        }

        if (!filterCriteria.get("distance").equals("all")) {

            if (notFirstOne) {
                //if not first filter, combine with previous result

                // get filter helper instance with new itemlistData
                ItemFilterHandler filterHelp2 = new ItemFilterHandler(filterResult);
                // there is a distance filter
                filterResult = (ArrayList<HashMap<String, Object>>) filterHelp2.nearByFilter(
                        currentLat,
                        currentLon,
                        Integer.valueOf(filterCriteria.get("distance").toString()));

            } else {
                notFirstOne = true;

                // this is first filter so use original
                filterResult = (ArrayList<HashMap<String, Object>>) filterHelp.nearByFilter(
                        currentLat,
                        currentLon,
                        Integer.valueOf(filterCriteria.get("distance").toString()));
            }

        }

        if (!filterCriteria.get("word").equals("all")) {

            if (notFirstOne) {
                //if not first filter, combine with previous result

                // get filter helper instance with new itemlistData
                ItemFilterHandler filterHelp3 = new ItemFilterHandler(filterResult);
                // there is a distance filter
                filterResult = (ArrayList<HashMap<String, Object>>) filterHelp3.wordFilter(searchQuery);

            } else {

                // this is first filter so use original
                filterResult = (ArrayList<HashMap<String, Object>>) filterHelp.wordFilter(searchQuery);
            }
        }

        itemListData.clear();
        itemListData.addAll(filterResult);

        int resultCount = itemListData.toArray().length;


        // set list view with the custom adapter
        listView.setAdapter(itemListAdapter);

        Toast.makeText(HomeActivity.this, resultCount + " result found", Toast.LENGTH_SHORT).show();

    }


    public void addFilterChip(int type, String title) {


        // reset all
        if (title.equals("reset")) {

            defaultData = true;

            // clear chip list array and set
            chipList.clear();

            chipList.add(new filterChip("All"));
            chipDefault.setChipList(chipList);
            return;
        }


        if (type == 1) {

            if (defaultData) {
                chipList.clear();
            }

            defaultData = false;
            // category chips
            String categoryTitle = "Category: " + title;

            // add new chip to the chip list array
            chipList.add(new filterChip(categoryTitle));
            chipDefault.setChipList(chipList);
        } else if (type == 2) {

            if (defaultData) {
                chipList.clear();
            }

            defaultData = false;

            // distance chips
            String categoryTitle = "Distance: " + title + "km";

            // add new chip to the chip list array
            chipList.add(new filterChip(categoryTitle));
            chipDefault.setChipList(chipList);
        } else if (type == 3) {

            if (defaultData) {
                chipList.clear();
            }

            defaultData = false;


            // word chips
            String categoryTitle = "Free word: " + title;

            // add new chip to the chip list array
            chipList.add(new filterChip(categoryTitle));
            chipDefault.setChipList(chipList);
        }
    }

    // request permission to users
    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, GPS_PERMISSION_CODE);
    }

    // get result from request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GPS_PERMISSION_CODE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted

                    // set the variable true
                    gpsPermission = true;

                } else {

                    // permission denied, boo!
                    gpsPermission = false;
                }
                return;
            }
        }
    }
}
