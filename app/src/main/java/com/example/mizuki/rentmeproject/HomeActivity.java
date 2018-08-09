package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // inti views
        btnLogout = findViewById(R.id.btnLogout);

        // set click event
        btnLogout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                // sign out from firebase auth
                FirebaseAuth.getInstance().signOut();

              // redirect to login page
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
        });
    }


}
