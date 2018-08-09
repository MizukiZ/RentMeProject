package com.example.mizuki.rentmeproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Model.User;

public class RegisterActivity extends AppCompatActivity {

    Context context;

    Button btnRegister, btnCancel;
    EditText edtEmail, edtPassword;

    // Firebase
     // database
    FirebaseDatabase db;
    DatabaseReference users;

     //authentication
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = getApplicationContext();

        // init views
        btnCancel = findViewById(R.id.btnCancel);
        btnRegister = findViewById(R.id.btnRegister);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        // init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        // set click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
               private String emailInput,passwordInput;


            @Override
            public void onClick(View v) {
                 emailInput = edtEmail.getText().toString();
                 passwordInput = edtPassword.getText().toString();


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
                        newUser.setEmail(emailInput);
                        newUser.setPassword(passwordInput);

                        // use user id as key
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        users.child(uid).setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // created user successfully
                                Toast.makeText( context, "Registered!!", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // error
                                Toast.makeText( context, "Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // error
                        Toast.makeText( context, "Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
