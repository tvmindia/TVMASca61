package com.tech.thrithvam.bakeryapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.tech.thrithvam.bakeryapp.User.FROM_C;
import static com.tech.thrithvam.bakeryapp.User.KEY_EXTRA;

public class GridOfProducts extends AppCompatActivity {
    ListView sideBar;
    ArrayList<String> categoryList;
    Dictionary<String,String> categoryCode=new Hashtable<>();
    ArrayAdapter categoryAdapter;
    Constants constants=new Constants();
    DatabaseHandler db;
    Bundle extras;
    AsyncTask productsByCategory,productsBySearch;
    TextView cart_count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_of_products);
        db=DatabaseHandler.getInstance(this);
        extras=getIntent().getExtras();
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(extras.getString("Category"));
        }
        //----------------force to login if not logged in and wanna see favorites---------------

        if("myfav".equals(extras.getString("CategoryCode")))  {
            ab.setTitle(R.string.favorites);
            if(db.GetUserDetail("UserID")==null){
            Toast.makeText(GridOfProducts.this,R.string.please_login,Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(GridOfProducts.this, User.class);
            intentUser.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            startActivity(intentUser);
            finish();
            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
            }
        }
        if("trends".equals(extras.getString("CategoryCode")))  {
            ab.setTitle(R.string.trending_products);
        }
        loadCategories();
        //---------threading----------------
        if (isOnline()){
            //getCategories=new GetCategories().execute();
            if(getIntent().hasExtra("SearchString")){
                ab.setTitle(R.string.search_results);
                productsBySearch=new GetProductsBySearch().execute();
            }
            else {
                productsByCategory=new GetProductsByCategory().execute();
            }
        }
        else {
            Toast.makeText(GridOfProducts.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
        }

        sideBar=(ListView)findViewById(R.id.drawer);

    }
    //---------------------------Categories loading------------------------------------
    public void loadCategories(){
      //  Handler handler = new Handler();
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
            categoryAdapter = new ArrayAdapter<>(GridOfProducts.this, R.layout.side_bar_item, categoryList);
            sideBar.setAdapter(categoryAdapter);
            sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!categoryList.get(position).equals("")) {
//                        if (categoryList.get(position).equals(getResources().getString(R.string.my_orders_sidebar))) {
//                            Intent orderIntent = new Intent(GridOfProducts.this, OrderStatus.class);
//                            startActivity(orderIntent);
//                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
//                        }
                         if (categoryList.get(position).equals(getResources().getString(R.string.boutique_details))) {
                            Intent boutiqueIntent = new Intent(GridOfProducts.this, BoutiqueDetails.class);
                            startActivity(boutiqueIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else {
                            Intent categoryIntent = new Intent(GridOfProducts.this, GridOfProducts.class);
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
      /*  else {
            handler.postDelayed(new Runnable() {
                public void run() {
                    loadCategories();
                }
            },300);
        }*/
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
//                Intent intentCart = new Intent(GridOfProducts.this, Cart.class);
//                intentCart.putExtra("categorycode",extras.getString("CategoryCode"));
//                intentCart.putExtra("categoryname",extras.getString("Category"));
//                intentCart.putExtra("from","gridofproducts");
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
                intentUser.putExtra(KEY_EXTRA, FROM_C);
                startActivity(intentUser);
                finish();
                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                break;
//            case R.id.cart:
//                Intent intentCart = new Intent(this, Cart.class);
//                intentCart.putExtra("categorycode",extras.getString("CategoryCode"));
//                intentCart.putExtra("categoryname",extras.getString("Category"));
//                intentCart.putExtra("from","gridofproducts");
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
    //------------------------------Async Tasks-----------------------------
    public class GetProductsByCategory extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView pDialog=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
        AVLoadingIndicatorView pDialog2=(AVLoadingIndicatorView)findViewById(R.id.itemsLoadingBelow);
        ArrayList<String[]> productItems=new ArrayList<>();
        CustomAdapter adapter;
        GridView productGrid=(GridView)findViewById(R.id.productGrid);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setVisibility(View.VISIBLE);
            pDialog2.setVisibility(View.VISIBLE);

            adapter=new CustomAdapter(GridOfProducts.this, productItems,"categoryGrid");
            productGrid.setAdapter(adapter);
        /*    //Taking data from database


            ArrayList<String[]> productItemsTemp;           //To loop to add items to productGrid and update Grid view
            productItemsTemp=db.GetProductByCategory(extras.getString("CategoryCode"));
            if(productItemsTemp.size()>0){
                for (int i=0;i<productItemsTemp.size();i++){
                    productItems.add(productItemsTemp.get(i));
                    adapter.notifyDataSetChanged();
                }
            }


*/
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ProductsByCategory";
            HttpURLConnection c = null;
            try {
                postData =  "{\"CategoryCode\":\"" + extras.getString("CategoryCode") + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID"))+ "\",\"limit\":\"" + "" + "\"}";
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

                    if(pass){



                            String[] data=new String[5];
                            data[0]=jsonObject.optString("ProductID");
                            data[1]=jsonObject.optString("Name");
                            data[2]=jsonObject.optString("Image");
                            data[3]=jsonObject.optString("Discount");
                            data[4]=jsonObject.optString("ProductCounts","null");

                            productItems.add(data);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                    }

                }
                if(pass){
                    if(c!=null){
                        if("myfav".equals(extras.getString("CategoryCode")))  {
                            db.ResponsesSaving(url,postData,strJson,"fav");
                        }
                        else {
                            db.ResponsesSaving(url,postData,strJson,"null");
                        }
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
            pDialog.setVisibility(View.GONE);
            pDialog2.setVisibility(View.GONE);
            if(!pass) {
                new AlertDialog.Builder(GridOfProducts.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(R.string.no_items)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();
            }
            else {
                productGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//====================================================================================================>Intent to itemdetails with buy and cart button or enquiry
                        Intent intent=new Intent(GridOfProducts.this,ItemDetails.class);
//                      Intent intent=new Intent(GridOfProducts.this,ItemDetailsWithCartBuy.class);
                        intent.putExtra("ProductID",productItems.get(position)[0]);
                        intent.putExtra("from","gridofproducts");
                        intent.putExtra("categorycode",extras.getString("CategoryCode"));
                        intent.putExtra("categoryname",extras.getString("Category"));
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);

                    }
                });
            }
        }
    }
    public class GetProductsBySearch extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView pDialog=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
        AVLoadingIndicatorView pDialog2=(AVLoadingIndicatorView)findViewById(R.id.itemsLoadingBelow);
        ArrayList<String[]> productItems=new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setVisibility(View.VISIBLE);
            pDialog2.setVisibility(View.VISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ProductsBySearch";
            HttpURLConnection c = null;
            try {
                postData =  "{\"searchString\":\"" + extras.getString("SearchString") + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                    String[] data=new String[5];
                    data[0]=jsonObject.optString("ProductID");
                    data[1]=jsonObject.optString("Name");
                    data[2]=jsonObject.optString("Image");
                    data[3]=jsonObject.optString("Discount");
                    data[4]=jsonObject.optString("ProductCounts","null");
                    productItems.add(data);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.setVisibility(View.GONE);
            pDialog2.setVisibility(View.GONE);
            if(!pass) {
                new AlertDialog.Builder(GridOfProducts.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(R.string.no_items)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();
            }
            else {
                CustomAdapter adapter=new CustomAdapter(GridOfProducts.this, productItems,"categoryGrid");
                GridView productGrid=(GridView)findViewById(R.id.productGrid);
                productGrid.setAdapter(adapter);
                productGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(GridOfProducts.this,ItemDetails.class);
                        intent.putExtra("ProductID",productItems.get(position)[0]);
                        intent.putExtra("from","search");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed() {
        if(productsByCategory!=null){
            productsByCategory.cancel(true);
        }
        if(productsBySearch!=null){
            productsBySearch.cancel(true);
        }
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
}
