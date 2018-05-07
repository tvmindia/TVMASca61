package com.tech.thrithvam.bakeryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class Cart extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;
    ArrayList<String[]> productsList=new ArrayList<>();
//    ArrayList<String[]> cartItems=new ArrayList<>();
    TextView totalAmount;
    Double amount=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        db=DatabaseHandler.getInstance(this);
        totalAmount =(TextView)findViewById(R.id.total_amout);
        setupList();
    }
    public void setupList(){
        productsList.clear();
        amount=0.0;
        productsList=db.GetCartItems();
        if(productsList.size()>0){
            ImageView cartBag=(ImageView)findViewById(R.id.cartBag);
            cartBag.setVisibility(View.GONE);
            TextView emptyText=(TextView)findViewById(R.id.emptyCart);
            emptyText.setVisibility(View.GONE);
            for(int i=0;i<productsList.size();i++){
                amount+=Double.parseDouble(productsList.get(i)[3])*Double.parseDouble(productsList.get(i)[4]);
            }

            CustomAdapter adapter=new CustomAdapter(Cart.this, productsList,"cart");
            ListView products=(ListView)findViewById(R.id.productListCart);
            products.setAdapter(adapter);
            products.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(Cart.this);
                    alert.setTitle(R.string.enter_quantity);
                    LinearLayout linear=new LinearLayout(Cart.this);
                    linear.setLayoutParams(new LinearLayout.LayoutParams(40, ViewGroup.LayoutParams.WRAP_CONTENT));
                    final NumberPicker quantityInput=new NumberPicker(Cart.this);
                    quantityInput.setMinValue(1);
                    quantityInput.setMaxValue(1000);
                    quantityInput.setValue(Integer.parseInt(productsList.get(position)[3]));
                    linear.setOrientation(LinearLayout.VERTICAL);
                    linear.addView(quantityInput);
                    alert.setView(linear);
                    alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                                if(quantityInput.getValue()>0)
                                    db.UpdateQuantity(productsList.get(position)[0],productsList.get(position)[1],quantityInput.getValue());
                                    setupList();
                        }
                    });
                    alert.setNegativeButton(R.string.view_product, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent=new Intent(Cart.this,ItemDetails.class);
                            intent.putExtra("ProductID", productsList.get(position)[0]);
                            intent.putExtra("from","cart");
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                        }
                    });
                    alert.setCancelable(true);
                    alert.show();
                }
            });
            totalAmount.setText(getResources().getString(R.string.total_amout, String.format(Locale.US,"%.2f",amount)));
        }
        else {
            Toast.makeText(Cart.this, R.string.no_items_in_cart, Toast.LENGTH_LONG).show();
            CardView checkoutCard=(CardView)findViewById(R.id.checkoutCard);
            checkoutCard.setVisibility(View.INVISIBLE);
            CardView productListLayout=(CardView)findViewById(R.id.productListCard);
            productListLayout.setVisibility(View.INVISIBLE);
        }
    }
    public void cancelItem(final View view){
        new AlertDialog.Builder(Cart.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                .setMessage(R.string.cancel_item_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String productID=(String) view.getTag();
                        db.RemoveFromCart(productID);
                        setupList();
                    }
                }).setNegativeButton(R.string.no, null).show();
    }
    @Override
    public void onBackPressed() {
        try {
            switch (getIntent().getExtras().getString("from")){
                case "home":
                    Intent intent = new Intent(this, Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
                case "gridofproducts":
                    Intent intentgrid = new Intent(this, GridOfProducts.class);
                    intentgrid.putExtra("CategoryCode", getIntent().getExtras().getString("categorycode"));
                    intentgrid.putExtra("Category",getIntent().getExtras().getString("categoryname"));
                    intentgrid.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentgrid);
                    finish();
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
                case "itemdetails":
                    Intent intentprod = new Intent(this, ItemDetails.class);
                    intentprod.putExtra("ProductID", getIntent().getExtras().getString("productid"));
                    intentprod.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentprod);
                    finish();
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
                /*case "chat":
                    Intent intentChat = new Intent(this, Chat.class);
                    intentChat.putExtra("productName", productName);
                    intentChat.putExtra("productID", productID);
                    intentChat.putExtra("from", "itemdetailsBack");
                    startActivity(intentChat);
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;*/
                default:
                    Intent intentHome = new Intent(this, Home.class);
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentHome);
                    finish();
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
            }
        }
        catch (Exception e){
            Intent intentHome = new Intent(this, Home.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish();
            overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
        }
    }
    public void checkout(View view){
        if (db.GetUserDetail("UserID") != null) {
            Intent intent = new Intent(Cart.this, Checkout.class);
            intent.putExtra("totalAmount",String.format(Locale.US,"%.2f",amount));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
        } else {
            Toast.makeText(Cart.this, R.string.please_login, Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(Cart.this, User.class);
            startActivity(intentUser);
            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
        }
    }
}
