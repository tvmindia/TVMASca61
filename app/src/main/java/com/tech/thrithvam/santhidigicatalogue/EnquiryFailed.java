package com.tech.thrithvam.santhidigicatalogue;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class EnquiryFailed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enquiry_failed);

        Typeface fontType1 = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        Typeface fontType2 = Typeface.createFromAsset(getAssets(), "fonts/handwriting.ttf");



        TextView greeting=(TextView)findViewById(R.id.thank);
        greeting.setTypeface(fontType2);

        TextView contactyou=(TextView)findViewById(R.id.contactyou);
        contactyou.setTypeface(fontType2);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
                Intent i3 = new Intent(EnquiryFailed.this, EnquiryItem.class);
                startActivity(i3);
            }
        }, 4000);

    }
    @Override
    public void onBackPressed() {
//        finish();
//        productReviews.cancel(true);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_exit1, R.anim.slide_exit2);
    }
}
