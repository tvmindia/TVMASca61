package com.tech.thrithvam.santhidigicatalogue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.tech.thrithvam.santhidigicatalogue.User.FROM_A;
import static com.tech.thrithvam.santhidigicatalogue.User.KEY_EXTRA;

public class Home extends AppCompatActivity implements ObservableScrollViewCallbacks {
    Constants constants=new Constants();
    DatabaseHandler db;
    LinearLayout homeScreen;
    LayoutInflater inflater;
    ListView sideBar;
    ArrayList<String> categoryList;
    Dictionary<String,String> categoryCode=new Hashtable<>();
    ArrayAdapter categoryAdapter;
    SliderLayout newArrivals;
    //  int loadedCategoryCount=0;
    //   ArrayList<View> cards=new ArrayList<>();
    ObservableScrollView scrollView;
    AVLoadingIndicatorView loadingIndicator;
    AsyncTask productsByCategory, bannerslider;
    SearchView searchView;
    ArrayList<String []> cats;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db=DatabaseHandler.getInstance(this);
        getSupportActionBar().setElevation(0);
        db.flushNotifications();
        loadingIndicator = (AVLoadingIndicatorView) findViewById(R.id.itemsLoading);



        inflater = (LayoutInflater) Home.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        homeScreen = (LinearLayout) findViewById(R.id.homeScreen);

        //-------------------------hide actionbar on scroll----------------------------
        scrollView = (ObservableScrollView) findViewById(R.id.homeScroll);
        scrollView.setScrollViewCallbacks(this);

        //------------------------------Home Screen Slider-------------------------------
        newArrivals = (SliderLayout) findViewById(R.id.newArrivals);

        //----------------------------Searching---------------------------------------------
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(Home.this, GridOfProducts.class);
                intent.putExtra("SearchString", searchView.getQuery().toString());
                startActivity(intent);
                searchView.setQuery("", false);
                searchView.clearFocus();
                overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

//        handler.postDelayed(new Runnable() {
//            public void run() {
        if(isOnline()){
            loadCategories();
        }
        else{
            Intent intentHome = new Intent(Home.this, NetworkError.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish();
        }
//            }
//        },300);

        bannerslider = new BannerSlider().execute();

//        Toast.makeText(Home.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();

    }
    public void tiquesinnsite(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.thrithvam.com/"));
        startActivity(browserIntent);
        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
    }

    //---------------------------Categories loading------------------------------------
    int tryAgainCount=0;
    public void loadCategories(){
        categoryList=new ArrayList<>();
        sideBar = (ListView) findViewById(R.id.drawer);
        cats = db.GetCategories();
        if (cats.size()>0 && (db.getVarValue("CategoryTable").equals("UNLOCKED")))
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
            categoryAdapter = new ArrayAdapter<>(Home.this, R.layout.side_bar_item, categoryList);
            sideBar.setAdapter(categoryAdapter);
            sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!categoryList.get(position).equals("")) {
//                        if (categoryList.get(position).equals(getResources().getString(R.string.my_orders_sidebar))) {
//                            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
//                            startActivity(orderIntent);
//                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                        }
                         if (categoryList.get(position).equals(getResources().getString(R.string.boutique_details))) {
                            Intent boutiqueIntent = new Intent(Home.this, BoutiqueDetails.class);
                            startActivity(boutiqueIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else {
                            Intent categoryIntent = new Intent(Home.this, GridOfProducts.class);
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
            //products under category loading on Home screen
            productsOfCategory(-1);
        }
        else {
            handler.postDelayed(new Runnable() {
                public void run() {
                    tryAgainCount++;
                    if(tryAgainCount<100){
                        loadCategories();
                    }
                    else {
                        Intent intentHome = new Intent(Home.this, NetworkError.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentHome);
                        finish();
                    }
                }
            },300);
        }
    }
    //-------------------------------- Items Grid----------------------------------
    public void productsOfCategory(Integer loadedCategoryCount){
        int currentPos=loadedCategoryCount+1;
        if(currentPos<categoryList.size())
        {
            if(!categoryList.get(currentPos).equals("")
//                    &&!categoryList.get(currentPos).equals(getResources().getString(R.string.my_orders_sidebar))
                    &&!categoryList.get(currentPos).equals(getResources().getString(R.string.boutique_details))
                    )
            {
                productsByCategory=new GetProductsByCategory(currentPos).execute();
            }
            else {
                productsOfCategory(currentPos);
            }

        }
        else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
//        View cartItem =  menu.findItem(R.id.cart).getActionView();
//        TextView cart_count = (TextView) cartItem.findViewById(R.id.cart_count);
//        int count=db.GetCartItems().size();
//        if(count>0){
//            cart_count.setText(Integer.toString(count));
//            cart_count.setVisibility(View.VISIBLE);
//        }
//        else
//            cart_count.setVisibility(View.INVISIBLE);
//        cartItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intentCart = new Intent(Home.this, Cart.class);
//                intentCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                intentCart.putExtra("from","home");
//                startActivity(intentCart);
//                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
//                finish();
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user:
                Intent intentUser = new Intent(this, User.class);
                intentUser.putExtra(KEY_EXTRA, FROM_A);
                startActivity(intentUser);
                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                break;
//            case R.id.cart:
//                Intent intentCart = new Intent(this, Cart.class);
//                intentCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                intentCart.putExtra("from","home");
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
    //--------------Actionbar hiding while scrolling-------------------------------
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }
    @Override
    public void onDownMotionEvent() {
    }
    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if (scrollState == ScrollState.UP) {
                if (ab.isShowing()) {
                    ab.hide();
                }
            } else if (scrollState == ScrollState.DOWN) {
                if (!ab.isShowing()) {
                    ab.show();
                }
            }
        }
    }

    //------------------------------Async Tasks-----------------------------

    public class GetProductsByCategory extends AsyncTask<Void, Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        // ProgressDialog pDialog=new ProgressDialog(Home.this);
        ArrayList<String[]> productItems=new ArrayList<>();

        LinearLayout categoryCard= (LinearLayout) inflater.inflate(R.layout.products_of_category,null);
        Integer viewPos;
        public GetProductsByCategory(Integer viewPos){
            this.viewPos=viewPos;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
/*

            //Taking data from database
            productItemsFromDB=db.GetProductByCategory(categoryCode.get(categoryList.get(viewPos)));
            if(productItemsFromDB.size()>0){
                TextView categoryTitle =(TextView)categoryCard.findViewById(R.id.title);
                Typeface type = Typeface.createFromAsset(getAssets(), "fonts/avenirnextregular.ttf");
                categoryTitle.setTypeface(type);
                categoryTitle.setText(categoryList.get(viewPos).replace("\uD83D\uDC49\t",""));
                categoryTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent categoryIntent=new Intent(Home.this,GridOfProducts.class);
                        categoryIntent.putExtra("CategoryCode",categoryCode.get(categoryList.get(viewPos)));
                        categoryIntent.putExtra("Category",categoryList.get(viewPos).replace("\uD83D\uDC49\t",""));
                        startActivity(categoryIntent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });

                CustomAdapter adapter=new CustomAdapter(Home.this, productItemsFromDB,"homeGrid");
                TwoWayView horizontalGrid=(TwoWayView)categoryCard.findViewById(R.id.gridHorizontal);
                horizontalGrid.setOrientation(TwoWayView.Orientation.HORIZONTAL);
                horizontalGrid.setItemMargin(5);
                horizontalGrid.setAdapter(adapter);

                homeScreen.addView(categoryCard);

                if(viewPos>2)
                {ImageView footer=(ImageView)findViewById(R.id.tiquesinnlabel);
                    footer.setVisibility(View.VISIBLE);}

                handler.postDelayed(new Runnable() {
                    public void run() {
                        productsOfCategory(viewPos);
                    }
                },500);
            }
*/


         /*   pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();*/
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ProductsByCategory";
            HttpURLConnection c = null;
            try {
                postData =  "{\"CategoryCode\":\"" + categoryCode.get(categoryList.get(viewPos)) + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID"))+ "\",\"limit\":\"" + constants.productsCountLimit + "\"}";
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
                    data[0]=jsonObject.optString("ProductID");
                    data[1]=jsonObject.optString("Name");
                    data[2]=getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
                    data[3]=jsonObject.optString("Discount");
                    productItems.add(data);
                }
                if(pass){
                    if(c!=null)
                        if(categoryCode.get(categoryList.get(viewPos)).equals("myfav")){
                            db.ResponsesSaving(url,postData,strJson,"fav");
                        }
                        else {
                            db.ResponsesSaving(url,postData,strJson,"null");
                        }

                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        /*    if (pDialog.isShowing())
                pDialog.dismiss();*/
            if(!pass) {
              /*  new AlertDialog.Builder(Home.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(R.string.no_items)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                             //   finish();
                            }
                        }).setCancelable(false).show();*/
            }
            else {

             /*   db.flushOldProductsByCategory(categoryCode.get(categoryList.get(viewPos)));
                for(int i=0;i<productItems.size();i++){
                    db.ProductByCategorySaving(productItems.get(i)[0],
                            productItems.get(i)[1],
                            categoryCode.get(categoryList.get(viewPos)),
                            productItems.get(i)[3],
                            productItems.get(i)[2],
                            i);
                }*/

                //  if(!(productItemsFromDB.size()>0)){
                TextView categoryTitle =(TextView)categoryCard.findViewById(R.id.title);
                Typeface type = Typeface.createFromAsset(getAssets(), "fonts/avenirnextregular.ttf");
                categoryTitle.setTypeface(type);
                categoryTitle.setText(categoryList.get(viewPos).replace("\uD83D\uDC49\t",""));
                categoryTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent categoryIntent=new Intent(Home.this,GridOfProducts.class);
                        categoryIntent.putExtra("CategoryCode",categoryCode.get(categoryList.get(viewPos)));
                        categoryIntent.putExtra("Category",categoryList.get(viewPos).replace("\uD83D\uDC49\t",""));
                        startActivity(categoryIntent);
                        finish();
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });

                CustomAdapter adapter=new CustomAdapter(Home.this, productItems,"homeGrid");
                GridView horizontalGrid=(GridView)categoryCard.findViewById(R.id.gridHorizontal);
                //   horizontalGrid.setOrientation(TwoWayView.Orientation.HORIZONTAL);
                //    horizontalGrid.setItemMargin(5);
                horizontalGrid.setAdapter(adapter);

                homeScreen.addView(categoryCard);

                if(viewPos>2)
                {ImageView footer=(ImageView)findViewById(R.id.tiquesinnlabel);
                    footer.setVisibility(View.VISIBLE);}


                //      }

            }
//            if(!(productItemsFromDB.size()>0)){
            productsOfCategory(viewPos);
            //     }
        }
    }
    public class BannerSlider extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> bannerItems=new ArrayList<>();
        ArrayList<String[]> bannerItemsFromDB=new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

           /* bannerItemsFromDB=db.GetBanners();

            if(bannerItemsFromDB.size()>0){
                for (int i = 0; i < bannerItemsFromDB.size(); i++) {
                    final int fi = i;
                    DefaultSliderView sliderViews = new DefaultSliderView(Home.this);
                    sliderViews
                            .image(bannerItemsFromDB.get(fi)[3])
                            .setScaleType(BaseSliderView.ScaleType.CenterCrop);

                    sliderViews.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            Intent intent;
                            if (!bannerItemsFromDB.get(fi)[1].equals("null")) {
                                intent = new Intent(Home.this, ItemDetails.class);
                                intent.putExtra("ProductID", bannerItemsFromDB.get(fi)[1]);
                                intent.putExtra("from","home");
                            } else if (!bannerItemsFromDB.get(fi)[2].equals("null")) {
                                intent = new Intent(Home.this, GridOfProducts.class);
                                intent.putExtra("CategoryCode", bannerItemsFromDB.get(fi)[2]);
                                intent.putExtra("Category", db.GetCategoryName(bannerItemsFromDB.get(fi)[2]).replace("\uD83D\uDC49\t", ""));
                            } else {
                                return;
                            }
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                    });
                    newArrivals.addSlider(sliderViews);
                    newArrivals.setPresetTransformer(SliderLayout.Transformer.Accordion);
                }
            }*/

            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/BannerImages";
            HttpURLConnection c = null;
            try {
                postData =  "{\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                    data[0]=jsonObject.optString("ImageID");
                    data[1]=jsonObject.optString("ProductID");
                    data[2]=jsonObject.optString("CategoryCode");
                    data[3]=getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
                    bannerItems.add(data);
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
                // newArrivals.setVisibility(View.GONE);
            }
            else {
               /* db.flushOldBanners();
                for(int i=0;i<bannerItems.size();i++){
                    db.BannersSaving(bannerItems.get(i)[0],bannerItems.get(i)[3],bannerItems.get(i)[1],bannerItems.get(i)[2],i);
                }

                if (!(bannerItemsFromDB.size()>0))
                {*/
                for (int i = 0; i < bannerItems.size(); i++) {
                    final int fi = i;
                    DefaultSliderView sliderViews = new DefaultSliderView(Home.this);
                    sliderViews
                            .image(bannerItems.get(fi)[3])
                            .setScaleType(BaseSliderView.ScaleType.CenterCrop);

                    sliderViews.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            Intent intent;
                            if (!bannerItems.get(fi)[1].equals("null")) {
                                //===================================================================>Intent to itemdetails with buy and cart button or enquiry

                                intent = new Intent(Home.this, ItemDetails.class);
//                              intent = new Intent(Home.this, ItemDetailsWithCartBuy.class);
                                intent.putExtra("ProductID", bannerItems.get(fi)[1]);
                                intent.putExtra("from","home");
                            } else if (!bannerItems.get(fi)[2].equals("null")) {
                                intent = new Intent(Home.this, GridOfProducts.class);
                                intent.putExtra("CategoryCode", bannerItems.get(fi)[2]);
                                intent.putExtra("Category", db.GetCategoryName(bannerItems.get(fi)[2]).replace("\uD83D\uDC49\t", ""));
                            } else {
                                return;
                            }
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                    });
                    newArrivals.addSlider(sliderViews);
                    newArrivals.setPresetTransformer(SliderLayout.Transformer.Accordion);
                }
                //  }
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                .setMessage(R.string.exit_q)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        handler.removeCallbacksAndMessages(null);
                        productsByCategory.cancel(true);
                        if(bannerslider!=null)bannerslider.cancel(true);

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                      /*  //clear cache
                        Toast.makeText(Home.this,"Cache Memory Cleared!!",Toast.LENGTH_SHORT).show();
                        try {
                            File dir = getApplicationContext().getCacheDir();
                            if (dir != null && dir.isDirectory()) {
                                deleteDir(dir);
                            }
                        } catch (Exception e) {}*/
                        finish();

                    }
                }).setNegativeButton(R.string.no, null).show();

    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();}
}
