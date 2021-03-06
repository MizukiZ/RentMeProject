package com.example.mizuki.rentmeproject;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Model.Post;
import dmax.dialog.SpotsDialog;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class PostActivity extends AppCompatActivity {

    // address latitude and longitude
    Double lat,lon;

    EditText postLocation,postTitle,postCost,postDescription;
    ImageView postCamera, uploadPhoto;
    Button btnSubmit;
    Spinner postCategory;

    android.app.AlertDialog postingDialog;

    static final int AUTO_COMP_REQ_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 11;

    byte[] imgData;
    Bitmap imgBit;


    //Firebase
     FirebaseStorage storage;
     StorageReference storageReference;
     DatabaseReference db;
     DatabaseReference postRef;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // init views
        postTitle = findViewById(R.id.postTitle);
        postCost = findViewById(R.id.postPrice);
        postDescription = findViewById(R.id.postDescription);
        postLocation = findViewById(R.id.postLocation);
        postCamera = findViewById(R.id.postCamera);
        uploadPhoto = findViewById(R.id.uploadPhoto);
        btnSubmit = findViewById(R.id.btnPost);
        postCategory = findViewById(R.id.category_spinner);

        // firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        db = FirebaseDatabase.getInstance().getReference();
        postRef = db.child("Post");


        // camera click event
        postCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when user click the camera image
                 // open the intent for easyImage library
                EasyImage.openChooserWithGallery(PostActivity.this, "Choose Photo", 0);
            }
        });


         // location field click event
        postLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction())
                    autoCompleteForm();
                return false;
            }
        });

        // post click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // make progress dialog and show
                postingDialog = new SpotsDialog.Builder().setContext(PostActivity.this)
                        .setMessage("Posting")
                        .build();

                postingDialog.show();

                // when user click post button
                submit();
            }
        });

    }

    // get data form autocomplete form
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

                    postLocation.setText(location);
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
               uploadPhoto.setImageBitmap(imgBit);
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

    // submit function
    public void submit(){
        // get unique post id
        final String postId = postRef.push().getKey();

        // get each value form the form
        final String title = postTitle.getText().toString();
        final String category = postCategory.getSelectedItem().toString();
        final String description = postDescription.getText().toString();
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // create map hash for location lan and lon
        final Map<String, Double> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lon",lon);

        // give a unique ID for the image
        final StorageReference ref = storageReference.child("ItemImages/"+ UUID.randomUUID().toString());

        // empty field detection
        if(imgBit == null){
            // no image uploaded
            // handle fail
            postingDialog.dismiss();
            Toast.makeText(PostActivity.this, "Please upload a image", Toast.LENGTH_SHORT).show();
        } else if(
                title == null || title.isEmpty() ||
                        category == null || category.isEmpty() ||
                        description == null || description.isEmpty() ||
                        TextUtils.isEmpty(postCost.getText().toString()) ||
                        location == null || location.isEmpty()
                ){
            // if there is a empty field
            postingDialog.dismiss();
            Toast.makeText(PostActivity.this, "Please fill ALL the field", Toast.LENGTH_SHORT).show();
        }else {
            // if every field is filled
            final Double cost = Double.parseDouble(postCost.getText().toString());



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



                        // create new post object to ues user class
                        Post post = new Post(
                                postId,
                                title,
                                description,
                                downloadUri,
                                location,
                                category,
                                userId,
                                cost,
                                false,
                                ServerValue.TIMESTAMP,
                                ServerValue.TIMESTAMP
                        );

                        postRef.child(postId).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // success handle

                                        // dismiss dialog
                                        postingDialog.dismiss();

                                        Toast.makeText(PostActivity.this, "posted!!", Toast.LENGTH_SHORT).show();

                                        // redirect to Main
                                        startActivity(new Intent(PostActivity.this, HomeActivity.class));
                                        finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // fail

                                // dismiss dialog
                                postingDialog.dismiss();

                                Toast.makeText(PostActivity.this, "post failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    } else {
                        // handle fail
                        Toast.makeText(PostActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


}
