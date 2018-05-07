package com.tech.thrithvam.bakeryapp;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewer extends AppCompatActivity {
    PhotoView photoView;
    Bundle extras;
    boolean isDetailsVisible=true;
    ObjectAnimator Anim1;
    RelativeLayout imgDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        extras=getIntent().getExtras();
        imgDetails =(RelativeLayout)findViewById(R.id.imgDetails);
        TextView imagename=(TextView)findViewById(R.id.imgName);
        imagename.setText(extras.getString("ProductName"));
        Anim1 = ObjectAnimator.ofFloat(imgDetails, "y", 1500);
        Anim1.setDuration(300);
        extras=getIntent().getExtras();
        photoView=(PhotoView)findViewById(R.id.punchAttachView);
        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {

                if (isDetailsVisible) {
                    Anim1.start();
                    isDetailsVisible=false;
                } else {
                    Anim1.reverse();
                    isDetailsVisible=true;
                }
            }
        });
        if (isOnline()){

        Glide.with(ImageViewer.this)
                .load(extras.getString("Imageurl"))
                        .into(photoView);
        }
        else {
        Toast.makeText(ImageViewer.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
        finish();
    }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    //for rotating the image
    public void rotateImage(View view){
        photoView.setRotationBy(90f);
    }
}
