package com.example.mizuki.rentmeproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    Context context;

    Button btnLogin, btnCancel;
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
        setContentView(R.layout.activity_login);

        context = getApplicationContext();

        // init views
        btnCancel = findViewById(R.id.btnCancel);
        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        // init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

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
                                Log.d("Login test", "Login!!");
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

    }
}
