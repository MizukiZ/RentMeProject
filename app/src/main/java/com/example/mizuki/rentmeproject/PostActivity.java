package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.UUID;

import Model.Post;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class PostActivity extends AppCompatActivity {

    EditText postLocation,postTitle,postCost,postDescription;
    ImageView postCamera, uploadPhoto;
    Button btnSubmit;
    Spinner postCategory;

    static final int AUTO_COMP_REQ_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 11;

    byte[] imgData;
    Bitmap imgBit;


    //Firebase
     FirebaseStorage storage;
     StorageReference storageReference;
     DatabaseReference db;
     DatabaseReference postRef;

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
        postLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompleteForm();
            }
        });

        // post click event
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        final Double cost = Double.parseDouble(postCost.getText().toString());
        final String location = postLocation.getText().toString();
        final String userId = "test";

        // give a unique ID for the image
        final StorageReference ref = storageReference.child("ItemImages/"+ UUID.randomUUID().toString());


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
                            cost
                    );

                    postRef.child(postId).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // success handle
                            Toast.makeText(PostActivity.this, "posted!!", Toast.LENGTH_SHORT).show();

                            // redirect to Main
                            startActivity(new Intent(PostActivity.this, HomeActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // fail
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
