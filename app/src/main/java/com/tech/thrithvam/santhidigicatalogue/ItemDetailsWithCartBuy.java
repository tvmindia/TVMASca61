package com.tech.thrithvam.santhidigicatalogue;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemDetailsWithCartBuy extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;
    ImageView favorite;
    ImageView share;
    ImageView review;
    ImageView chat;
    Boolean isFav=false;
    Integer favCount=0;
    TextView favCountString;
    SliderLayout itemImages;
    LayoutInflater inflater;
    TextView description;
    TextView productNo;
    Integer productNoInt;
    TextView productNameView;
    TextView viewDesigner;
//    TextView price;
    TextView actualPrice;
    ImageView offer;
//    TextView stock;
    String productID;
    String productName;
    Bundle extras;
    Boolean relatedItemsLoaded=false;
    ListView sideBar;
    ArrayList<String> categoryList;
    Dictionary<String,String> categoryCode=new Hashtable<>();
    ArrayAdapter categoryAdapter;
    CardView itemDetailsCard;
    CardView relatedItemsCard;
    AsyncTask productDetails,productImages,relatedProducts,getProductTypes;
    TextView cart_count;
    String purchaseButton;
    String productImage;
    String priceStringGlobal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details_with_cart_buy);
        db=DatabaseHandler.getInstance(this);
        extras = getIntent().getExtras();
        productID = extras.getString("ProductID");
        getSupportActionBar().setElevation(0);

        inflater = (LayoutInflater) ItemDetailsWithCartBuy.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Typeface fontType1 = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        description = (TextView) findViewById(R.id.description);
        description.setTypeface(fontType1);
        productNo=(TextView)findViewById(R.id.productNo);
        productNameView=(TextView)findViewById(R.id.productName);
        itemImages = (SliderLayout) findViewById(R.id.itemImages);
        viewDesigner = (TextView) findViewById(R.id.view_designer);

//        price = (TextView) findViewById(R.id.price);
        actualPrice = (TextView) findViewById(R.id.actualPrice);
        offer = (ImageView) findViewById(R.id.offer);
//        stock = (TextView) findViewById(R.id.stock);


        //-----------Add to favorite and Sharing and review--------------------------
        favorite = (ImageView) findViewById(R.id.fav);
        favCountString = (TextView) findViewById(R.id.favCount);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()){
                    if (db.GetUserDetail("UserID") != null) {
                        new AddOrRemoveFromFavorite().execute();
                    } else {
                        Toast.makeText(ItemDetailsWithCartBuy.this, R.string.please_login, Toast.LENGTH_LONG).show();
                        Intent intentUser = new Intent(ItemDetailsWithCartBuy.this, User.class);
                        startActivity(intentUser);
                        finish();
                        overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                    }
                }
                else {
                    Toast.makeText(ItemDetailsWithCartBuy.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                }
            }
        });

        share = (ImageView) findViewById(R.id.share);

//        review = (ImageView) findViewById(R.id.review);
//        review.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intentReview = new Intent(ItemDetails.this, ProductReviews.class);
//                intentReview.putExtra("productName", productName);
//                intentReview.putExtra("productID", productID);
//                startActivity(intentReview);
//                overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//            }
//        });

//        chat = (ImageView) findViewById(R.id.chat);
//        chat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (db.GetUserDetail("UserID") != null) {
//                    Intent intentChat = new Intent(ItemDetails.this, Chat.class);
//                    intentChat.putExtra("productName", productName);
//                    intentChat.putExtra("productID", productID);
//                    if(getIntent().getExtras().getString("from").equals("chat")){   //Whether the product details is came from chat
//                        intentChat.putExtra("from", "itemdetailsBack");
//                        startActivity(intentChat);
//                        finish();
//                        overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                    }
//                    else {
//                        intentChat.putExtra("from", "itemdetails");
//                        startActivity(intentChat);
//                        overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                    }
//                } else {
//                    Toast.makeText(ItemDetails.this, R.string.please_login, Toast.LENGTH_LONG).show();
//                    Intent intentUser = new Intent(ItemDetails.this, User.class);
//                    startActivity(intentUser);
//                    overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                }
//
//            }
//        });

        final ScrollView scrollView=(ScrollView)findViewById(R.id.itemDetailsScroll);
        itemDetailsCard=(CardView)findViewById(R.id.itemDetailsCard);
        relatedItemsCard=(CardView)findViewById(R.id.relatedItemsCard);
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(null);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if(!relatedItemsLoaded){
                    int diff = itemDetailsCard.getBottom()-(scrollView.getHeight()+scrollView.getScrollY());
                    if( diff <= 0 )
                    {
                        relatedProducts=new RelatedProducts().execute();
                        relatedItemsLoaded=true;
                    } // super.onScrollChanged(l, t, oldl, oldt);
                }
            }
        });
        if (isOnline()) {
            productDetails=new ProductDetails().execute();
            productImages=new ProductImages().execute();
        } else {
            Toast.makeText(ItemDetailsWithCartBuy.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();
        }
        loadCategories();
        //Added to cart or not-------------
        ArrayList<String[]> productsListOnCart=db.GetCartItems();
        if(productsListOnCart.size()>0){
            for(int i=0;i<productsListOnCart.size();i++){
                if(productID.equals(productsListOnCart.get(i)[0])){
                    itemInCart();
                }
            }
        }
    }
    //---------------------------Categories loading------------------------------------
    public void loadCategories(){
        categoryList=new ArrayList<>();
        sideBar = (ListView) findViewById(R.id.drawer);
        ArrayList<String []> cats = db.GetCategories();
        if (cats.size()>0)
        {
            for (int i = 0; i < cats.size(); i++) {
                categoryList.add(cats.get(i)[1]);
                categoryCode.put(cats.get(i)[1], cats.get(i)[0]);
            }
            //Links other than category
            categoryList.add("");
            categoryList.add(getResources().getString(R.string.trending));
            categoryCode.put(getResources().getString(R.string.trending),"trends");
            categoryList.add(getResources().getString(R.string.my_favorites));
            categoryCode.put(getResources().getString(R.string.my_favorites),"myfav");
//            categoryList.add(getResources().getString(R.string.my_orders_sidebar));
            categoryList.add(getResources().getString(R.string.boutique_details));
            categoryAdapter = new ArrayAdapter<>(ItemDetailsWithCartBuy.this, R.layout.side_bar_item, categoryList);
            sideBar.setAdapter(categoryAdapter);
            sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!categoryList.get(position).equals("")) {
//                        if (categoryList.get(position).equals(getResources().getString(R.string.my_orders_sidebar))) {
//                            Intent orderIntent = new Intent(ItemDetails.this, OrderStatus.class);
//                            startActivity(orderIntent);
//                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                        }
                        if (categoryList.get(position).equals(getResources().getString(R.string.boutique_details))) {
                            Intent boutiqueIntent = new Intent(ItemDetailsWithCartBuy.this, BoutiqueDetails.class);
                            startActivity(boutiqueIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else {
                            Intent categoryIntent = new Intent(ItemDetailsWithCartBuy.this, GridOfProducts.class);
                            categoryIntent.putExtra("CategoryCode", categoryCode.get(categoryList.get(position)));
                            categoryIntent.putExtra("Category", categoryList.get(position).replace("\uD83D\uDC49\t", ""));
                            startActivity(categoryIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
                        RelativeLayout drawer = (RelativeLayout) findViewById(R.id.rightDrawer);
                        drawerLayout.closeDrawer(drawer);
                    }
                }
            });
            AVLoadingIndicatorView avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.catItemsLoading);
            avLoadingIndicatorView.setVisibility(View.GONE);
        }
    }
//    public void addtoCart(View view){
//        purchaseButton="addtocart";
//        getProductTypes=new GetProductTypes().execute();
//    }
//    public void buyNow(View view){
//        purchaseButton="buynow";
//        getProductTypes=new GetProductTypes().execute();
//    }

    //Item is already there in cart
    public void itemInCart(){
        //  Button addToCart=(Button)findViewById(R.id.addToCart);
        //   addToCart.setVisibility(View.INVISIBLE);
//        Button buyNow=(Button)findViewById(R.id.buyNow);
//        buyNow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intentCart = new Intent(ItemDetails.this, Cart.class);
//                startActivity(intentCart);
//                intentCart.putExtra("productid",productID);
//                intentCart.putExtra("from","itemdetails");
//                //Toast.makeText(ItemDetails.this, getResources().getString(R.string.already_added_to_cart,productName), Toast.LENGTH_LONG).show();
//                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
//            }
//        });
    }
    public void updateCartIcon(){
        int count=db.GetCartItems().size();
        if(count>0){
            cart_count.setText(Integer.toString(count));
            cart_count.setVisibility(View.VISIBLE);
        }
        else
            cart_count.setVisibility(View.INVISIBLE);
    }
    //---------------Menu creation------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
//        View cartItem =  menu.findItem(R.id.cart).getActionView();
//        cart_count = (TextView) cartItem.findViewById(R.id.cart_count);
//        int count=db.GetCartItems().size();         if(count>0){             cart_count.setText(Integer.toString(count));             cart_count.setVisibility(View.VISIBLE);         }                     else              cart_count.setVisibility(View.INVISIBLE);
//        cartItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intentCart = new Intent(ItemDetails.this, Cart.class);
//                intentCart.putExtra("productid",productID);
//                intentCart.putExtra("from","itemdetails");
//                intentCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                finish();
//                startActivity(intentCart);
//                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user:
                Intent intentUser = new Intent(this, User.class);
                startActivity(intentUser);
                finish();
                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                break;
//            case R.id.cart:
//                Intent intentCart = new Intent(this, Cart.class);
//                intentCart.putExtra("productid",productID);
//                intentCart.putExtra("from","itemdetails");
//                intentCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                finish();
//                startActivity(intentCart);
//                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
//                break;
            case R.id.sidebar:
                DrawerLayout drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
                RelativeLayout drawer=(RelativeLayout)findViewById(R.id.rightDrawer);
                if(drawerLayout.isDrawerOpen(Gravity.RIGHT))
                    drawerLayout.closeDrawer(drawer);
                else
                    drawerLayout.openDrawer(drawer);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        productDetails.cancel(true);
        productImages.cancel(true);
        if(relatedProducts!=null){
            relatedProducts.cancel(true);
        }
        if(getProductTypes!=null){
            getProductTypes.cancel(true);
        }

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
                    intentgrid.putExtra("CategoryCode", extras.getString("categorycode"));
                    intentgrid.putExtra("Category",extras.getString("categoryname"));
                    intentgrid.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentgrid);
                    finish();
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
                case "itemdetails":
                    finish();
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
                case "chat":
                    Intent intentChat = new Intent(ItemDetailsWithCartBuy.this, Chat.class);
                    intentChat.putExtra("productName", productName);
                    intentChat.putExtra("productID", productID);
                    intentChat.putExtra("from", "itemdetailsBack");
                    startActivity(intentChat);
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
                case "cart":
                    Intent intentCart = new Intent(ItemDetailsWithCartBuy.this, Cart.class);
                    startActivity(intentCart);
                    overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                    break;
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

    //-------------------- Async tasks---------------------------------
    public class ProductDetails extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView itemLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
        String descriptionString,priceString,discount,designerID,designerName;
        Boolean isOutOfStock;
        LinearLayout cartButtons=(LinearLayout)findViewById(R.id.cartButtons);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            //Taking data from database
       /*     if(db.GetProductDetail(productID,"ProductID")!=null){
                isProductInDB=true;
            }
            else {
                isProductInDB=false;
            }

            if(isProductInDB){
                productName=db.GetProductDetail(productID,"ProductName");
                descriptionString=db.GetProductDetail(productID,"Description");
                priceString=db.GetProductDetail(productID,"Price");
                discount=db.GetProductDetail(productID,"Discount");
                isOutOfStock =Boolean.parseBoolean(db.GetProductDetail(productID,"IsOutOfStock"));
                designerID=db.GetProductDetail(productID,"DesignerID");
                productNoInt=Integer.parseInt(db.GetProductDetail(productID,"ProductNo"));
                designerName=db.GetProductDetail(productID,"DesignerName");
                isFav=Boolean.parseBoolean(db.GetProductDetail(productID,"IsFav"));
                favCount=Integer.parseInt(db.GetProductDetail(productID,"FavCount"));

                cartButtons.setVisibility(View.VISIBLE);
                LinearLayout fav_share=(LinearLayout)findViewById(R.id.fav_share_buttons);
                fav_share.setVisibility(View.VISIBLE);
                itemLoadingIndicatorView.setVisibility(View.GONE);
                android.support.v7.app.ActionBar ab = getSupportActionBar();
                if (ab != null) {
                    ab.setTitle(productName);
                }
                description.setText(descriptionString);
                productNo.setText(getResources().getString(R.string.product_no, productNoInt));
                productNameView.setText(getResources().getString(R.string.product_name, productName));
                price.setText(getResources().getString(R.string.rs, priceString));
                if(!discount.equals("null")){
                    if(Integer.parseInt(discount)>0){
                        discount=String.format(Locale.US,"%.2f", Double.parseDouble(discount));
                        price.setText(getResources().getString(R.string.rs, String.format(Locale.US,"%.2f",(Double.parseDouble(priceString)-Double.parseDouble(discount)))));
                        actualPrice.setVisibility(View.VISIBLE);
                        actualPrice.setText(getResources().getString(R.string.rs, priceString));
                        actualPrice.setPaintFlags(actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        offer.setVisibility(View.VISIBLE);
                    }
                    else {
                        price.setText(getResources().getString(R.string.rs, priceString));
                    }
                }
                else {
                    price.setText(getResources().getString(R.string.rs, priceString));
                }

                if(isOutOfStock){
                    stock.setText(R.string.out_of_Stock);
                    stock.setTextColor(Color.RED);
                }
                else {
                    stock.setText(R.string.in_stock);
                }
                if (isFav) {
                    favorite.setImageResource(R.drawable.fav);
                } else {
                    favorite.setImageResource(R.drawable.fav_no);
                }
                if(favCount==0){
                    favCountString.setText(R.string.favorite_count_0);
                }
                else {
                    favCountString.setText(getResources().getString(R.string.favorite_count, favCount));
                }
                viewDesigner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(ItemDetails.this, OwnerAndDesigner.class);
                        intent.putExtra("ownerORdesigner","designer");
                        intent.putExtra("designerID",designerID);
                        startActivity(intent  );
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                if(!designerName.equals("null")){
                    viewDesigner.setText(getResources().getString(R.string.designer_name, designerName));
                }
            }*/

            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/Products";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID"))+ "\"}";
                String response=db.GetResponses(url,postData);
                if(!response.equals("")){
                    strJson=response;
                }
                else {
                    URL u = new URL(url);
                    c = (HttpURLConnection) u.openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                    c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                    c.setDoInput(true);
                    c.setDoOutput(true);
                    c.setUseCaches(false);
                    c.setConnectTimeout(10000);
                    c.setReadTimeout(10000);
                    DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                    status = c.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                            sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            int a=sb.indexOf("[");
                            int b=sb.lastIndexOf("]");
                            strJson=sb.substring(a, b + 1);
                            //   strJson=cryptography.Decrypt(strJson);
                            strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    productName=jsonObject.optString("Name");
                    descriptionString=jsonObject.optString("Description");
                    priceString =String.format(Locale.US,"%.2f", jsonObject.optDouble("Price"));
                    discount=jsonObject.optString("Discount");
                    isOutOfStock =jsonObject.optBoolean("IsOutOfStock");
                    designerID=jsonObject.optString("DesignerID");
                    productNoInt=jsonObject.optInt("ProductNo");
                    designerName=jsonObject.optString("DesignerName");
                    isFav=jsonObject.optBoolean("isFav");
                    favCount=jsonObject.optInt("FavCount");
                }
                if(pass){
                    if(c!=null)
                        db.ResponsesSaving(url,postData,strJson,"fav"+productID);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
                new AlertDialog.Builder(ItemDetailsWithCartBuy.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();
            }
            else {

                cartButtons.setVisibility(View.VISIBLE);
                LinearLayout fav_share=(LinearLayout)findViewById(R.id.fav_share_buttons);
                fav_share.setVisibility(View.VISIBLE);
                itemLoadingIndicatorView.setVisibility(View.GONE);
                android.support.v7.app.ActionBar ab = getSupportActionBar();
                if (ab != null) {
                    ab.setTitle(productName);
                }
                description.setText(descriptionString);
                productNo.setText(getResources().getString(R.string.product_no, productNoInt));
                productNameView.setText(getResources().getString(R.string.product_name, productName));
//                price.setText(getResources().getString(R.string.rs, priceString));
//                if(!discount.equals("null")){
//                    if(Integer.parseInt(discount)>0){
//                        discount=String.format(Locale.US,"%.2f", Double.parseDouble(discount));
//                        priceStringGlobal=String.format(Locale.US,"%.2f",(Double.parseDouble(priceString)-Double.parseDouble(discount)));
//                        price.setText(getResources().getString(R.string.rs, priceStringGlobal));
//                        actualPrice.setVisibility(View.VISIBLE);
//                        actualPrice.setText(getResources().getString(R.string.rs, priceString));
//                        actualPrice.setPaintFlags(actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                        offer.setVisibility(View.VISIBLE);
//                    }
//                    else {
//                        price.setText(getResources().getString(R.string.rs, priceString));
//                    }
//                }
//                else {
//                    priceStringGlobal=priceString;
//                    price.setText(getResources().getString(R.string.rs, priceString));
//                }
//
//                if(isOutOfStock){
//                    stock.setText(R.string.out_of_Stock);
//                    stock.setTextColor(Color.RED);
//                }
//                else {
//                    stock.setText(R.string.in_stock);
//                }
                if (isFav) {
                    favorite.setImageResource(R.drawable.fav);
                } else {
                    favorite.setImageResource(R.drawable.fav_no);
                }
                if(favCount==0){
                    favCountString.setText(R.string.favorite_count_0);
                }
                else {
                    favCountString.setText(getResources().getString(R.string.favorite_count, favCount));
                }
                viewDesigner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(ItemDetailsWithCartBuy.this, OwnerAndDesigner.class);
                        intent.putExtra("ownerORdesigner","designer");
                        intent.putExtra("designerID",designerID);
                        startActivity(intent  );
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                if(!designerName.equals("null")){
                    viewDesigner.setText(getResources().getString(R.string.designer_name, designerName));
                }

            }
        }
    }
    public class AddOrRemoveFromFavorite extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg,AddOrRemove;
        boolean pass=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFav) {
                AddOrRemove="remove";
                favCount--;
                favorite.setImageResource(R.drawable.fav_no);
                Toast.makeText(ItemDetailsWithCartBuy.this, R.string.remove_fav_msg, Toast.LENGTH_LONG).show();
                if(favCount==0){
                    favCountString.setText(R.string.favorite_count_0);
                }
                else {
                    favCountString.setText(getResources().getString(R.string.favorite_count, favCount));
                }
                isFav = false;
            } else {
                AddOrRemove="add";
                favCount++;
                favorite.setImageResource(R.drawable.fav);
                Toast.makeText(ItemDetailsWithCartBuy.this, R.string.add_fav_msg, Toast.LENGTH_LONG).show();
                favCountString.setText(getResources().getString(R.string.favorite_count, favCount));
                isFav = true;
            }

            //Change Database
            db.flushInvalidResponses("fav"+productID);
            db.flushInvalidResponses("fav");
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/Favorites";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + db.GetUserDetail("UserID")+ "\",\"AddOrRemove\":\"" + AddOrRemove + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a, b + 1);
                        //   strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
                new AlertDialog.Builder(ItemDetailsWithCartBuy.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();
            }
        }
    }
    public class ProductImages extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String> imgurls=new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Load from local database
          /*  imgurlsFromDB=db.GetProductImages(productID);
            if(imgurlsFromDB.size()>0){
                for (int i=0;i<imgurlsFromDB.size();i++) {
                    final int fi=i;
                    DefaultSliderView sliderViews = new DefaultSliderView(ItemDetails.this);
                    sliderViews
                            .description(SliderLayout.Transformer.DepthPage.toString())
                            .image(imgurlsFromDB.get(i))
                            .setScaleType(BaseSliderView.ScaleType.CenterInside);
                    sliderViews.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            Intent intent=new Intent(ItemDetails.this,ImageViewer.class);
                            intent.putExtra("Imageurl",imgurlsFromDB.get(fi));
                            intent.putExtra("ProductName",productName);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                        }
                    });
                    itemImages.addSlider(sliderViews);
                    itemImages.setCustomIndicator((PagerIndicator) findViewById(R.id.custom_indicator));
                    if(imgurlsFromDB.size()<2){
                        itemImages.stopAutoCycle();
                    }

                    share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(ItemDetails.this,R.string.share_image,Toast.LENGTH_LONG).show();

                            // Get access to bitmap image from view
                            final ImageView ivImage = new ImageView(ItemDetails.this);
                            *//*Glide.with(ItemDetails.this)
                                    .load(imgurlsFromDB.get(itemImages.getCurrentPosition()))
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            Uri bmpUri = getLocalBitmapUri(ivImage,productName+"@"+constants.BoutiqueName);
                                            if (bmpUri != null) {
                                                Intent shareIntent = new Intent();
                                                shareIntent.setAction(Intent.ACTION_SEND);
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                                shareIntent.setType("image*//**//**//**//*");
                                                shareIntent.putExtra(Intent.EXTRA_TEXT, productName+"\t@\t"+constants.BoutiqueName);
                                                startActivity(Intent.createChooser(shareIntent, "Share Image"));
                                            }
                                            return true;
                                        }
                                    })
                                    .into(ivImage);*//*
                            Picasso.with(ItemDetails.this)
                                    .load(imgurlsFromDB.get(itemImages.getCurrentPosition()))
                                    .into(ivImage, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            Uri bmpUri = getLocalBitmapUri(ivImage,productName+"@"+constants.BoutiqueName);
                                            if (bmpUri != null) {
                                                Intent shareIntent = new Intent();
                                                shareIntent.setAction(Intent.ACTION_SEND);
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                                shareIntent.setType("image*//**//*");
                                                shareIntent.putExtra(Intent.EXTRA_TEXT, productName+"\t@\t"+constants.BoutiqueName);
                                                startActivity(Intent.createChooser(shareIntent, "Share Image"));
                                            }
                                        }
                                        @Override
                                        public void onError() {
                                        }
                                    });
                        }
                    });
                }
            }*/
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ProductImages";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
                String response=db.GetResponses(url,postData);
                if(!response.equals("")){
                    strJson=response;
                }
                else {
                    URL u = new URL(url);
                    c = (HttpURLConnection) u.openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                    c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                    c.setDoInput(true);
                    c.setDoOutput(true);
                    c.setUseCaches(false);
                    c.setConnectTimeout(10000);
                    c.setReadTimeout(10000);
                    DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                    status = c.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                            sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            int a=sb.indexOf("[");
                            int b=sb.lastIndexOf("]");
                            strJson=sb.substring(a, b + 1);
                            //   strJson=cryptography.Decrypt(strJson);
                            strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    imgurls.add(getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media")));
                    if(jsonObject.optBoolean("IsMain")){
                        productImage=getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
                    }
                }
                if(pass){
                    if(c!=null)
                        db.ResponsesSaving(url,postData,strJson,"null");
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
                new AlertDialog.Builder(ItemDetailsWithCartBuy.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();
            }
            else {

                for (int i=0;i<imgurls.size();i++) {
                    final int fi=i;
                    DefaultSliderView sliderViews = new DefaultSliderView(ItemDetailsWithCartBuy.this);
                    sliderViews
                            .description(SliderLayout.Transformer.DepthPage.toString())
                            .image(imgurls.get(i))
                            .setScaleType(BaseSliderView.ScaleType.CenterInside);
                    sliderViews.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            Intent intent=new Intent(ItemDetailsWithCartBuy.this,ImageViewer.class);
                            intent.putExtra("Imageurl",imgurls.get(fi));
                            intent.putExtra("ProductName",productName);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                        }
                    });
                    itemImages.addSlider(sliderViews);
                    itemImages.setCustomIndicator((PagerIndicator) findViewById(R.id.custom_indicator));
                    if(imgurls.size()<2){
                        itemImages.stopAutoCycle();
                    }
                    share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(ItemDetailsWithCartBuy.this,R.string.share_image,Toast.LENGTH_LONG).show();

                            // Get access to bitmap image from view
                            final ImageView ivImage = new ImageView(ItemDetailsWithCartBuy.this);
                               /* Glide.with(ItemDetails.this)
                                        .load(imgurls.get(itemImages.getCurrentPosition()))
                                        .listener(new RequestListener<String, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                return false;
                                            }
                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                Uri bmpUri = getLocalBitmapUri(ivImage,productName+"@"+constants.BoutiqueName);
                                                if (bmpUri != null) {
                                                    Intent shareIntent = new Intent();
                                                    shareIntent.setAction(Intent.ACTION_SEND);
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                                    shareIntent.setType("image*//**//*");
                                                    shareIntent.putExtra(Intent.EXTRA_TEXT, productName+"\t@\t"+constants.BoutiqueName);
                                                    startActivity(Intent.createChooser(shareIntent, "Share Image"));
                                                }
                                                return true;
                                            }
                                        })
                                        .into(ivImage);*/
                            Picasso.with(ItemDetailsWithCartBuy.this)
                                    .load(imgurls.get(itemImages.getCurrentPosition()))
                                    .into(ivImage, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            Uri bmpUri = getLocalBitmapUri(ivImage,productName+"@"+constants.BoutiqueName);
                                            if (bmpUri != null) {
                                                Intent shareIntent = new Intent();
                                                shareIntent.setAction(Intent.ACTION_SEND);
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                                shareIntent.setType("image/*");
                                                shareIntent.putExtra(Intent.EXTRA_TEXT, productName+"\t@\t"+constants.BoutiqueName);
                                                startActivity(Intent.createChooser(shareIntent, "Share Image"));
                                            }
                                        }
                                        @Override
                                        public void onError() {
                                        }
                                    });
                        }
                    });
                }
            }
        }
    }
    public class RelatedProducts extends AsyncTask<Void, Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> productItems=new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            relatedItemsCard.setVisibility(View.GONE);
         /*   pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();*/
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/RelatedProducts";
            HttpURLConnection c = null;
            try {
                postData =  "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"limit\":\"" + constants.relatedProductsCountLimit + "\"}";
                String response=db.GetResponses(url,postData);
                if(!response.equals("")){
                    strJson=response;
                }
                else {
                    URL u = new URL(url);
                    c = (HttpURLConnection) u.openConnection();
                    c.setRequestMethod("POST");
                    c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                    c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                    c.setDoInput(true);
                    c.setDoOutput(true);
                    c.setUseCaches(false);
                    c.setConnectTimeout(10000);
                    c.setReadTimeout(10000);
                    DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                    status = c.getResponseCode();
                    switch (status) {
                        case 200:
                        case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                            sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();
                            int a=sb.indexOf("[");
                            int b=sb.lastIndexOf("]");
                            strJson=sb.substring(a, b + 1);
                            //   strJson=cryptography.Decrypt(strJson);
                            strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    String[] data=new String[4];
                    data[0]=jsonObject.optString("RelatedProductsID");
                    data[1]=jsonObject.optString("Name");
                    data[2]=jsonObject.optString("Image");
                    data[3]=jsonObject.optString("Discount");
                    productItems.add(data);
                }
                if(pass){
                    if(c!=null)
                        db.ResponsesSaving(url,postData,strJson,"null");
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            super.onPostExecute(result);
            if(!pass) {
                relatedItemsCard.setVisibility(View.GONE);
            }
            else {
                relatedItemsCard.setVisibility(View.VISIBLE);
                CustomAdapter adapter=new CustomAdapter(ItemDetailsWithCartBuy.this, productItems,"relatedItems");
                TwoWayView horizontalGrid=(TwoWayView)findViewById(R.id.gridRelatedItems);
                horizontalGrid.setOrientation(TwoWayView.Orientation.HORIZONTAL);
                horizontalGrid.setItemMargin(15);
                horizontalGrid.setAdapter(adapter);
            }
        }
    }
    public class GetProductTypes extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> productTypes=new ArrayList<>();
        String priceToSave;
        ProgressDialog pDialog=new ProgressDialog(ItemDetailsWithCartBuy.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ProductTypesByProductID";
            HttpURLConnection c = null;
            try {
                postData = "{\"ProductID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a, b + 1);
                        //   strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    String[] data=new String[4];
                    data[0]=jsonObject.optString("Code");
                    data[1]=jsonObject.optString("Description");
                    data[2]=String.format(Locale.US,"%.2f", jsonObject.optDouble("Amount"));
                    data[3]=jsonObject.optString("DiscountAmount");
                    productTypes.add(data);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(!pass) {//Product don't have any types----------------
                AlertDialog.Builder alert = new AlertDialog.Builder(ItemDetailsWithCartBuy.this);
                View popup=getLayoutInflater().inflate(R.layout.product_types, null);

                RelativeLayout types=(RelativeLayout)popup.findViewById(R.id.typesDisplay);
                types.setVisibility(View.GONE);

                priceToSave= priceStringGlobal;
                //Quantity-----
                final NumberPicker numberPicker=(NumberPicker)popup.findViewById(R.id.quantity);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(1000);
                numberPicker.setWrapSelectorWheel(false);
                //Image--------
                ImageView productImg=(ImageView)popup.findViewById(R.id.productImage);
                Glide.with(ItemDetailsWithCartBuy.this)
                        .load(productImage).thumbnail(0.1f)
                        .into(productImg)
                ;
                //Alert------
                alert.setView( popup);
                alert.setTitle("Select Quantity");
                alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            if(numberPicker.getValue()>0){
                                db.AddToCart(productID,
                                        "",
                                        "",
                                        numberPicker.getValue(),
                                        priceToSave,
                                        Integer.toString(productNoInt),
                                        productName,
                                        productImage);
                                Toast.makeText(ItemDetailsWithCartBuy.this, getResources().getString(R.string.added_to_cart, productName), Toast.LENGTH_LONG).show();
                                itemInCart();
                                updateCartIcon();
                                if(purchaseButton.equals("buynow")){
//                                    Toast.makeText(ItemDetails.this, getResources().getString(R.string.added_to_cart, productName), Toast.LENGTH_LONG).show();
                                    Intent intentCart = new Intent(ItemDetailsWithCartBuy.this, Cart.class);
                                    intentCart.putExtra("productid",productID);
                                    intentCart.putExtra("from","itemdetails");
                                    intentCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    startActivity(intentCart);
                                    overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                                    itemInCart();
//                                    updateCartIcon();
                                }
                            }
                            else {
                                Toast.makeText(ItemDetailsWithCartBuy.this, R.string.no_types_selected, Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e){
                            Toast.makeText(ItemDetailsWithCartBuy.this, getResources().getString(R.string.already_added_to_cart,productName), Toast.LENGTH_LONG).show();
                        }

                    }
                });
                alert.setNegativeButton(R.string.cancel, null);
                alert.setCancelable(true);
                alert.show();
            }
            else {//Product has different types--------------------------
                AlertDialog.Builder alert = new AlertDialog.Builder(ItemDetailsWithCartBuy.this);
                View popup=getLayoutInflater().inflate(R.layout.product_types, null);

                //Radio buttons and prizing----
                final RadioGroup radioGroup=(RadioGroup)popup.findViewById(R.id.types);
                final TextView price=(TextView)popup.findViewById(R.id.price);
                final TextView actualPrice=(TextView)popup.findViewById(R.id.actualPrice);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        price.setVisibility(View.VISIBLE);

                        if(!productTypes.get(i)[3].equals("null")){
                            String priceString=productTypes.get(i)[2];
                            if(Double.parseDouble(productTypes.get(i)[3])>0){
                                String discount=String.format(Locale.US,"%.2f", Double.parseDouble(productTypes.get(i)[3]));
                                priceToSave=String.format(Locale.US,"%.2f",(Double.parseDouble(priceString)-Double.parseDouble(discount)));
                                price.setText(getResources().getString(R.string.rs, priceToSave));
                                actualPrice.setVisibility(View.VISIBLE);
                                actualPrice.setText(getResources().getString(R.string.rs, priceString));
                                actualPrice.setPaintFlags(actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                offer.setVisibility(View.VISIBLE);
                            }
                            else {
                                actualPrice.setVisibility(View.INVISIBLE);
                                priceToSave=priceString;
                                price.setText(getResources().getString(R.string.rs, priceToSave));
                            }
                        }
                    }
                });
                for(int i=0;i<productTypes.size();i++){
                    RadioButton rb=new RadioButton(ItemDetailsWithCartBuy.this);
                    rb.setId(i);
                    rb.setText(productTypes.get(i)[1]);
                    radioGroup.addView(rb);
                    if(i==0)
                        rb.setChecked(true);
                }

                //Quantity-----
                final NumberPicker numberPicker=(NumberPicker)popup.findViewById(R.id.quantity);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(1000);
                numberPicker.setWrapSelectorWheel(false);
                //Image--------
                ImageView productImg=(ImageView)popup.findViewById(R.id.productImage);
                Glide.with(ItemDetailsWithCartBuy.this)
                        .load(productImage).thumbnail(0.1f)
                        .into(productImg)
                ;
                //Alert------
                alert.setView( popup);
                alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            if(radioGroup.getCheckedRadioButtonId()!=-1
                                    &&numberPicker.getValue()>0){
                                db.AddToCart(productID,
                                        productTypes.get(radioGroup.getCheckedRadioButtonId())[0],
                                        productTypes.get(radioGroup.getCheckedRadioButtonId())[1],
                                        numberPicker.getValue(),
                                        priceToSave,
                                        Integer.toString(productNoInt),
                                        productName,
                                        productImage);
                                Toast.makeText(ItemDetailsWithCartBuy.this, getResources().getString(R.string.added_to_cart, productName), Toast.LENGTH_LONG).show();
                                itemInCart();
                                updateCartIcon();
                                if(purchaseButton.equals("buynow")){
//                                    Toast.makeText(ItemDetails.this, getResources().getString(R.string.added_to_cart, productName), Toast.LENGTH_LONG).show();
                                    Intent intentCart = new Intent(ItemDetailsWithCartBuy.this, Cart.class);
                                    intentCart.putExtra("productid",productID);
                                    intentCart.putExtra("from","itemdetails");
                                    intentCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    startActivity(intentCart);
                                    overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                                    itemInCart();
//                                    updateCartIcon();
                                }
                            }
                            else {
                                Toast.makeText(ItemDetailsWithCartBuy.this, R.string.no_types_selected, Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e){
                            Toast.makeText(ItemDetailsWithCartBuy.this, getResources().getString(R.string.already_added_to_cart,productName), Toast.LENGTH_LONG).show();
                        }

                    }
                });
                alert.setNegativeButton(R.string.cancel, null);
                alert.setCancelable(true);
                alert.show();
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri(ImageView imageView,String fileName) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
