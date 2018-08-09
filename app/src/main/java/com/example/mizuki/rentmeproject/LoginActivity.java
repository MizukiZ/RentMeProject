package com.example.mizuki.rentmeproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    Context context;

    Button btnLogin, btnCancel;
    EditText edtEmail, edtPassword;
    TextView noAccountLink;

    // Firebase
    // database
    FirebaseDatabase db;
    DatabaseReference users;

    //authentication
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = getApplicationContext();

        // init views
        noAccountLink = findViewById(R.id.noAccountLink);
        btnCancel = findViewById(R.id.btnCancel);
        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        // init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        // set click evnet
         // login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            private String emailInput,passwordInput;
            @Override
            public void onClick(View v) {
                //  when login button is clicked

                emailInput = edtEmail.getText().toString();
                passwordInput = edtPassword.getText().toString();

                auth.signInWithEmailAndPassword(emailInput,passwordInput).
                        addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                // successfully login

                                // redirect to home page
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //  login failed
                        Toast.makeText( context, "Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

         // no account link
        noAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when the text is clicked, jump to register page
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

    }
}
