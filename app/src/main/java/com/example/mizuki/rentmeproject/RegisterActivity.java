package com.example.mizuki.rentmeproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Model.User;
import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    Context context;

    TextView alreadyAccountLink;
    Button btnRegister;
    EditText edtName,edtEmail, edtPassword;

    // Firebase
     // database
    FirebaseDatabase db;
    DatabaseReference users;

     //authentication
    FirebaseAuth auth;

    android.app.AlertDialog registerDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = getApplicationContext();

        // init views
        alreadyAccountLink = findViewById(R.id.alreadyAccountLink);
        btnRegister = findViewById(R.id.btnRegister);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtName = findViewById(R.id.edtUserName);

        // init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        // set click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
               private String emailInput,passwordInput,userNameInput;

            @Override
            public void onClick(View v) {

                userNameInput = edtName.getText().toString();
                emailInput = edtEmail.getText().toString();
                passwordInput = edtPassword.getText().toString();

                if(userNameInput == null || userNameInput.isEmpty()){
                    // if user name field is empty
                    Toast.makeText( context, "User name field is empty", Toast.LENGTH_LONG).show();

                } else if(emailInput == null || emailInput.isEmpty()){
                    // if email field is empty
                    Toast.makeText( context, "Email field is empty", Toast.LENGTH_LONG).show();

                } else if(passwordInput == null || passwordInput.isEmpty()){
                    // if password field is empty
                    Toast.makeText( context, "Password field is empty", Toast.LENGTH_LONG).show();
                }else{

                    // create registering progress dialog
                    registerDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this)
                            .setMessage("Registering")
                            .build();

                    registerDialog.show();

                    register();
                }


    }

    public void register(){

        //  register process
        auth.createUserWithEmailAndPassword(
                emailInput,
                passwordInput
        ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //  save data to database

                // create User class
                User newUser = new User();
                newUser.setUserName(userNameInput);
                newUser.setEmail(emailInput);
                newUser.setPassword(passwordInput);

                // use user id as key
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                users.child(uid).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //dismiss dialog
                        registerDialog.dismiss();

                        // created user successfully
                        Toast.makeText( context, "Successfully registered", Toast.LENGTH_LONG).show();
                        // after the registration login
                        startActivity(new Intent(context, HomeActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //dismiss dialog
                        registerDialog.dismiss();

                        // error
                        Toast.makeText( context, "Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //dismiss dialog
                registerDialog.dismiss();

                // error
                Toast.makeText( context, "Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
        });

        // already have an account link
        alreadyAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when the text is clicked, jump to login page
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

}
