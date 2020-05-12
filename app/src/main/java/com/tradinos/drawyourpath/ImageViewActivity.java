package com.tradinos.drawyourpath;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tradinos.drawyourpath.Utils.TouchImageView;

import androidx.fragment.app.FragmentActivity;

public class ImageViewActivity extends FragmentActivity implements View.OnClickListener {

    ImageButton close_button;
    TouchImageView imageView;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        setFinishOnTouchOutside(true);
        assignUIReferences();
        assignActions();
        getData();
    }

    protected void getData() {
        imageUrl=getIntent().getStringExtra("image_url");
        showData();

    }


    protected void showData() {

        try {
            Picasso.get()
                    .load(imageUrl)
                    .error(R.drawable.defult_image)
                    .placeholder(R.drawable.defult_image)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(imageView);
        } catch (Exception | OutOfMemoryError e) {
            imageView.setVisibility(View.GONE);
        }

    }

    public void assignUIReferences() {
        close_button = (ImageButton) findViewById(R.id.close_button);
        imageView=(TouchImageView) findViewById(R.id.image_view);
    }

    protected void assignActions() {

        close_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_button:
                finish();
                break;
        }
    }

}
