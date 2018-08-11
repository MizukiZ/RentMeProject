package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.io.File;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class PostActivity extends AppCompatActivity {

    private EditText postLocation;
    private ImageView postCamera, uploadPhoto;
    static final int AUTO_COMP_REQ_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 11;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // init views
        postLocation = findViewById(R.id.postLocation);
        postCamera = findViewById(R.id.postCamera);
        uploadPhoto = findViewById(R.id.uploadPhoto);

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
            case REQUEST_IMAGE_CAPTURE:
                // get result for taking photo intent
                if(resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Log.e("Image upload", imageBitmap.toString());
                uploadPhoto.setImageBitmap(imageBitmap);

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
               Bitmap myBitmap = BitmapFactory.decodeFile(photo.getAbsolutePath());
               // set to the view
               uploadPhoto.setImageBitmap(myBitmap);

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

}
