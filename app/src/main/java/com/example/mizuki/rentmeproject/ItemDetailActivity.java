package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView itemImage;
    TextView itemTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // init views
        itemImage = findViewById(R.id.itemDetailImage);
        itemTitle = findViewById(R.id.itemDetailTitle);

        Intent intent = getIntent();
        HashMap<String, String> itemHashMap = (HashMap<String, String>)intent.getSerializableExtra("itemObject");
        Picasso.get()
                .load(itemHashMap.get("image"))
                .resize(500,300)
                .placeholder(R.drawable.loading_placeholder)
                .into(itemImage);

        itemTitle.setText(itemHashMap.get("title"));
    }
}
