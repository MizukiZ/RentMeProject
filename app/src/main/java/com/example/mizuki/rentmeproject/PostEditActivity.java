package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Helper.GeocodeHandler;
import Model.Post;
import Model.User;
import dmax.dialog.SpotsDialog;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class PostEditActivity extends AppCompatActivity {

    Button editButton;
    EditText postEditLocation,postEditTitle,postEditCost,postEditDescription;
    ImageButton editPostImageBtn;
    ImageView postImage;
    Spinner postCategory;
    Switch rentToggle;

    static final int AUTO_COMP_REQ_CODE = 1;

    android.app.AlertDialog updatingDialog;

    byte[] imgData;
    Bitmap imgBit;

    String[] categoryArray;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference postItemDbRef;

    // address latitude and longitude
    Double lat,lon;

    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        //init Views
        editButton = findViewById(R.id.btnEdit);
        postEditLocation = findViewById(R.id.editPostLocation);
        postEditTitle = findViewById(R.id.editPostTitle);
        postEditCost = findViewById(R.id.editPostPrice);
        postEditDescription = findViewById(R.id.editPostDescription);
        editPostImageBtn = findViewById(R.id.editPostCamera);
        postImage = findViewById(R.id.editUploadPhoto);
        postCategory = findViewById(R.id.editCategory_spinner);
        rentToggle = findViewById(R.id.rentToggle);

        //set category array
        categoryArray = new String[]{"Sport","Appliance","Instrument","Clothe","Tool","Ride"};

        // firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        postItemDbRef = FirebaseDatabase.getInstance().getReference().child("Post");

        // make progress dialog and show
        updatingDialog = new SpotsDialog.Builder().setContext(PostEditActivity.this)
                .setMessage("Updating")
                .build();

        // get passed data
        Intent intent = getIntent();
        HashMap<String, Object> itemData = (HashMap<String, Object>)intent.getSerializableExtra("itemObject");

        // cast object to map
        @SuppressWarnings("unchecked")
        Map<String, Double> location = (Map<String, Double>)itemData.get("location");
        lat = location.get("lat");
        lon = location.get("lon");
        // create geocode handler and get place name form lat and lon
        GeocodeHandler geocodeHandler = new GeocodeHandler(this,location.get("lat"),location.get("lon"));


        // create post object by passed data
        post  = new Post(
                itemData.get("id").toString(),
                itemData.get("title").toString(),
                itemData.get("description").toString(),
                itemData.get("image").toString(),
                location,
                itemData.get("category").toString(),
                itemData.get("user_id").toString(),
                Double.valueOf(itemData.get("price").toString()),
                (boolean)itemData.get("rented"),
                itemData.get("created_at").toString(),
                itemData.get("updated_at").toString()
        );

        // set default value for text filed
        postEditTitle.setText(post.getTitle());
        postEditCost.setText(post.getCost().toString());
        postEditLocation.setText(geocodeHandler.getPlaceName());
        postEditDescription.setText(post.getDescription());
        rentToggle.setChecked(post.isRented());

        Picasso.get()
                .load(itemData.get("image").toString())
                .resize(300,300)
                .placeholder(R.drawable.loading_placeholder)
                .into(postImage);

        // set default value of
        int indexOfCategory = ArrayUtils.indexOf(categoryArray, post.getCategory());
        postCategory.setSelection(indexOfCategory);

        // set edit button click listerner
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updatingDialog.show();

                // get each field values
                String title = postEditTitle.getText().toString();
                String description = postEditDescription.getText().toString();
                String category = postCategory.getSelectedItem().toString();
                String costField = postEditCost.getText().toString();
                String locationField = postEditLocation.getText().toString();
                boolean isRented = rentToggle.isChecked();

                if(title.isEmpty() || description.isEmpty() || costField.isEmpty() || locationField.isEmpty()){
                    updatingDialog.dismiss();
                    Toast.makeText(PostEditActivity.this, "Please fill every field", Toast.LENGTH_SHORT).show();
                    return;
                }

                Double cost = Double.parseDouble(costField);

                // create map hash for location lan and lon
                final Map<String, Double> location = new HashMap<>();
                location.put("lat",lat);
                location.put("lon",lon);

                if(imgBit != null){
                    // when image is uploaded

                    // give a unique ID for the image
                    final StorageReference ref = storageReference.child("ItemImages/" + UUID.randomUUID().toString());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imgBit.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    // data for uploading to firebase storage
                    imgData = baos.toByteArray();


                    UploadTask uploadTask = ref.putBytes(imgData);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // return the Download url of the image
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                // image url for database
                                String downloadUri = task.getResult().toString();

                                Post post = new Post(
                                        itemData.get("id").toString(),
                                        title,
                                        description,
                                        downloadUri,
                                        location,
                                        category,
                                        itemData.get("user_id").toString(), // doesn't change
                                        cost,
                                        isRented,
                                        itemData.get("created_at").toString(),
                                        ServerValue.TIMESTAMP
                                );

                                // update user data
                                postItemDbRef.child(itemData.get("id").toString()).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // success handle
                                        updatingDialog.dismiss();
                                        Toast.makeText(PostEditActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // fail handle
                                        updatingDialog.dismiss();
                                        Toast.makeText(PostEditActivity.this, "update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                            } else {
                                // handle fail
                                updatingDialog.dismiss();
                                Toast.makeText(PostEditActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else{
                    // when image is no uploaded
                    Post post = new Post(
                            itemData.get("id").toString(),
                            title,
                            description,
                            itemData.get("image") != null ? itemData.get("image").toString() : null,
                            location,
                            category,
                            itemData.get("user_id").toString(), // doesn't change
                            cost,
                            isRented,
                            itemData.get("created_at").toString(),
                            ServerValue.TIMESTAMP
                    );

                    // update user data
                    postItemDbRef.child(itemData.get("id").toString()).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // success handle
                            updatingDialog.dismiss();
                            Toast.makeText(PostEditActivity.this, "Updated", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // fail handle
                            updatingDialog.dismiss();
                            Toast.makeText(PostEditActivity.this, "post failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });

        // camera click event
        editPostImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when user click the camera image
                // open the intent for easyImage library
                EasyImage.openChooserWithGallery(PostEditActivity.this, "Choose Photo", 0);
            }
        });

        // location field click event
        postEditLocation.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(MotionEvent.ACTION_UP == event.getAction())
                    autoCompleteForm();
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case AUTO_COMP_REQ_CODE:
                // get result for the autocomplete intent
                if(resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    CharSequence location = place.getAddress().toString();

                    //set lat and lon
                    lat = place.getLatLng().latitude;
                    lon = place.getLatLng().longitude;

                    postEditLocation.setText(location);
                    break;
                }
                break;
            default:
                break;
        }

        //  get result from easy image library intent
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //error handling
                Log.e("Error", e.getStackTrace().toString());
            }

            @Override
            public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {
                //Handle the images
                // get first file from the file list
                File photo = imagesFiles.get(0);
                // convert the file to bit data
                imgBit = BitmapFactory.decodeFile(photo.getAbsolutePath());
                // set to the view
                postImage.setImageBitmap(imgBit);
            }
        });
    }

    //  call google auto complete apt
    public void autoCompleteForm(){
        try {
            Intent completeIntent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(completeIntent, AUTO_COMP_REQ_CODE);
        } catch (Exception e) {
            Log.e("Error", e.getStackTrace().toString());
        }
    }
}
