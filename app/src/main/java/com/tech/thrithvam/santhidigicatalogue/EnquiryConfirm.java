package com.tech.thrithvam.santhidigicatalogue;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class EnquiryConfirm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enquiry_confirm);

        Typeface fontType1 = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        Typeface fontType2 = Typeface.createFromAsset(getAssets(), "fonts/handwriting.ttf");



        TextView greeting=(TextView)findViewById(R.id.thank);
        greeting.setTypeface(fontType2);

        TextView contactyou=(TextView)findViewById(R.id.contactyou);
        contactyou.setTypeface(fontType2);

    }
    @Override
    public void onBackPressed() {

//        super.onBackPressed();
//        return;
        finish();
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
//        productReviews.cancel(true);
////        super.onBackPressed();
//        overridePendingTransition(0,0);
    }
}
