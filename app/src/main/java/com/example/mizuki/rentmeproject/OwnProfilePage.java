package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class OwnProfilePage extends AppCompatActivity {

    Button updateBtn;
    EditText nameTxt,locationTxt,bioTxt;
    ImageView ownImg, uploadCamera;
    Switch editToggle;

    byte[] imgData;
    Bitmap imgBit;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference userDbRef;

    android.app.AlertDialog updatingDialog;
    User userModle;

    static final int AUTO_COMP_REQ_CODE = 1;

    // address latitude and longitude
    Double lat,lon;

    boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_profile_page);


        //init Views
        updateBtn = findViewById(R.id.btnUpdate);
        nameTxt = findViewById(R.id.ownName);
        locationTxt = findViewById(R.id.ownLocation);
        bioTxt = findViewById(R.id.ownBio);
        ownImg = findViewById(R.id.ownImg);
        uploadCamera = findViewById(R.id.ownImgCamera);
        editToggle = findViewById(R.id.editToggle);

        isEditMode = false;

        // set non edit mode when user opens
        editMode(isEditMode);

        // firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        userDbRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // get passed data
        Intent intent = getIntent();
        HashMap<String, Object> userData = (HashMap<String, Object>)intent.getSerializableExtra("user");

        //set event for user db reference
        userDbRef.child(userData.get("id").toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               userModle = dataSnapshot.getValue(User.class);

                // set default data into filed

                nameTxt.setText(userModle.getUserName());

                if(userModle.getBio() != null){
                    // there is bio
                    bioTxt.setText(userModle.getBio());
                }else{
                    // there is no bio
                }

                if(userModle.getImage() != null) {
                    // if there is a image


                    Picasso.get()
                            .load(userModle.getImage())
                            .resize(500, 300)
                            .placeholder(R.drawable.placeholder)
                            .into(ownImg);
                }else{

                    // if there is no image, set place holder
                    ownImg.setBackgroundResource(R.drawable.account);
                }

                if(userModle.getLocation() != null) {
                    //  if there is a locaiton

                    // cast object to map
                    @SuppressWarnings("unchecked")
                    Map<String, Double> location = (Map<String, Double>)userModle.getLocation();

                    lat = location.get("lat");
                    lon = location.get("lon");

                    GeocodeHandler geocodeHandler = new GeocodeHandler(OwnProfilePage.this,lat,lon);

                    locationTxt.setText(geocodeHandler.getPlaceName());

                }else {
                    // if there is no location

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // make progress dialog and show
        updatingDialog = new SpotsDialog.Builder().setContext(OwnProfilePage.this)
                .setMessage("Updating")
                .build();




        // set switch eventlistener
        editToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    // when switch is On
                    editMode(true);
                }else{
                    // when switch is Off
                    editMode(false);
                }
            }
        });

        // set update event click listener
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when update is clicked

                updatingDialog.show();

                // get text from each field
                String newName = nameTxt.getText().toString();
                String newBio = bioTxt.getText().toString();

                if(newName.isEmpty()){
                    updatingDialog.dismiss();
                    Toast.makeText(OwnProfilePage.this, "User name can't be blank", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create map hash for location lan and lon
                final Map<String, Double> location = new HashMap<>();
                location.put("lat", lat);
                location.put("lon", lon);

                if(imgBit != null){
                // when image is uploaded

                // give a unique ID for the image
                final StorageReference ref = storageReference.child("UserImages/" + UUID.randomUUID().toString());

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

                            User user = new User(
                                    null,
                                    newName,
                                    userData.get("email").toString(),
                                    userData.get("password").toString(),
                                    downloadUri,
                                    newBio,
                                    location
                            );

                            // update user data
                            userDbRef.child(userData.get("id").toString()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // success handle
                                    updatingDialog.dismiss();
                                    Toast.makeText(OwnProfilePage.this, "Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // fail handle
                                    updatingDialog.dismiss();
                                    Toast.makeText(OwnProfilePage.this, "update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else {
                            // handle fail
                            updatingDialog.dismiss();
                            Toast.makeText(OwnProfilePage.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }else{
                    // when image is no uploaded
                    User user = new User(
                            null,
                            newName,
                            userData.get("email").toString(),
                            userData.get("password").toString(),
                            userData.get("image") != null ? userData.get("image").toString() : null,
                            newBio,
                            location
                    );

                    // update user data
                    userDbRef.child(userData.get("id").toString()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // success handle
                            updatingDialog.dismiss();
                            Toast.makeText(OwnProfilePage.this, "Updated", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // fail handle
                            updatingDialog.dismiss();
                            Toast.makeText(OwnProfilePage.this, "post failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });

        // camera click event
        uploadCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when user click the camera image
                // open the intent for easyImage library
                EasyImage.openChooserWithGallery(OwnProfilePage.this, "Choose Photo", 0);
            }
        });

        // location field click event
        locationTxt.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(MotionEvent.ACTION_UP == event.getAction() && isEditMode)
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

                    locationTxt.setText(location);
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
                ownImg.setImageBitmap(imgBit);
            }
        });
    }

    //  call google auto complete apt
    public void autoCompleteForm(){
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, AUTO_COMP_REQ_CODE);
        } catch (Exception e) {
            Log.e("Error", e.getStackTrace().toString());
        }
    }

    public void editMode(boolean state){

        isEditMode = state;

        nameTxt.setFocusableInTouchMode(state);
        nameTxt.setFocusable(state);
        nameTxt.setCursorVisible(state);

        locationTxt.setFocusableInTouchMode(state);
        locationTxt.setFocusable(state);
        locationTxt.setCursorVisible(state);

        bioTxt.setFocusableInTouchMode(state);
        bioTxt.setFocusable(state);
        bioTxt.setCursorVisible(state);

        uploadCamera.setEnabled(state);

        updateBtn.setEnabled(state);
    }


}
