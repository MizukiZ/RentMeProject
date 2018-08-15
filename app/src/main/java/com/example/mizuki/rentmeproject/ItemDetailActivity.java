package com.example.mizuki.rentmeproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

import Helper.TimeFormat;
import Model.Post;

public class ItemDetailActivity extends AppCompatActivity {

    ImageView itemImage;
    TextView itemTitle,itemPostTime,itemDescription,itemPrice;
    Post post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // init views
        itemImage = findViewById(R.id.itemDetailImage);
        itemTitle = findViewById(R.id.itemDetailTitle);
        itemPostTime = findViewById(R.id.itemDetailPostDate);
        itemDescription = findViewById(R.id.itemDetailDescription);
        itemPrice = findViewById(R.id.itemDetailPrice);

        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> itemHash = (HashMap<String, Object>)intent.getSerializableExtra("itemObject");

        // create post object by passed data
        post  = new Post(
                itemHash.get("id").toString(),
                itemHash.get("title").toString(),
                itemHash.get("description").toString(),
                itemHash.get("image").toString(),
                itemHash.get("location").toString(),
                itemHash.get("category").toString(),
                itemHash.get("user_id").toString(),
                Double.valueOf(itemHash.get("price").toString()),
                (boolean)itemHash.get("rented"),
                itemHash.get("created_at").toString(),
                itemHash.get("updated_at").toString()
                );

        Picasso.get()
                .load(itemHash.get("image").toString())
                .resize(500,300)
                .placeholder(R.drawable.loading_placeholder)
                .into(itemImage);


        itemTitle.setText(post.getTitle());

      TimeFormat timeFormatHelp = new TimeFormat(post.getUpdated_at().toString());
        itemPostTime.setText(timeFormatHelp.postedOnForm());

        itemDescription.setText(post.getDescription());

        itemPrice.setText(post.getCost().toString());
    }
}
