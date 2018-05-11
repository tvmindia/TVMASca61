package com.tech.thrithvam.santhidigicatalogue;

import android.graphics.Typeface;
import android.os.Bundle;
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

    }
    @Override
    public void onBackPressed() {
//        finish();
//        productReviews.cancel(true);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_exit1, R.anim.slide_exit2);
    }
}
