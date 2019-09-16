package com.jvvas.incomesoutcomes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class ImageActivity extends AppCompatActivity {

    private ImageView imagePhoto ;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        Intent incomingDateIntentInfo = getIntent();

        String url = incomingDateIntentInfo.getStringExtra("url");
        imagePhoto = (ImageView) findViewById(R.id.imageView_DownloadPhoto);

        if(url != null){
            System.out.println("-------------------------------------------------- " + url);

            Picasso.with(ImageActivity.this)
                    .load(url)
                    .into(imagePhoto);
        }
    }
}
