package com.tech.thrithvam.bakeryapp;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class ProductReviews extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;
    Bundle extras;
    ListView sideBar;
    ArrayList<String> categoryList;
    Dictionary<String,String> categoryCode=new Hashtable<>();
    ArrayAdapter categoryAdapter;
    EditText inputReview;
    Button submitReview;
    String productID;
    Button submit;
    AsyncTask productReviews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_reviews);
        db=DatabaseHandler.getInstance(this);
        getSupportActionBar().setElevation(0);
        extras=getIntent().getExtras();
        productID=extras.getString("productID");
        loadCategories();
        if (isOnline()){
            productReviews=new ProductReviewsList().execute();
        }
        else {
            Toast.makeText(ProductReviews.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
            finish();
        }
        submit=(Button)findViewById(R.id.submitReview);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("Reviews: " + extras.getString("productName"));
        }
        inputReview=(EditText)findViewById(R.id.reviewInput);
        submitReview=(Button)findViewById(R.id.submitReview);
        inputReview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                inputReview.setLines(3);
                submitReview.setVisibility(View.VISIBLE);
            }
        });
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
            categoryList.add(getResources().getString(R.string.my_orders_sidebar));
            categoryList.add(getResources().getString(R.string.boutique_details));
            categoryAdapter = new ArrayAdapter<>(ProductReviews.this, R.layout.side_bar_item, categoryList);
            sideBar.setAdapter(categoryAdapter);
            sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!categoryList.get(position).equals("")) {
                        if (categoryList.get(position).equals(getResources().getString(R.string.my_orders_sidebar))) {
                            Intent orderIntent = new Intent(ProductReviews.this, OrderStatus.class);
                            startActivity(orderIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else if (categoryList.get(position).equals(getResources().getString(R.string.boutique_details))) {
                            Intent boutiqueIntent = new Intent(ProductReviews.this, BoutiqueDetails.class);
                            startActivity(boutiqueIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else {
                            Intent categoryIntent = new Intent(ProductReviews.this, GridOfProducts.class);
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
    public void insertReview(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if(db.GetUserDetail("UserID")!=null){
            if(!inputReview.getText().toString().trim().equals(""))
            {
                submit.setEnabled(false);
                new InsertProductReview().execute();
            }
        }
        else {
            Toast.makeText(ProductReviews.this,R.string.please_login,Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(ProductReviews.this, User.class);
            startActivity(intentUser);
//            finish();
            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
        }
    }
    //---------------Menu creation------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
//        View cartItem =  menu.findItem(R.id.cart).getActionView();
//        TextView cart_count = (TextView) cartItem.findViewById(R.id.cart_count);
//        int count=db.GetCartItems().size();
//        if(count>0){
//            cart_count.setText(Integer.toString(count));
//            cart_count.setVisibility(View.VISIBLE);         }
//        else
//            cart_count.setVisibility(View.INVISIBLE);
//        cartItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intentCart = new Intent(ProductReviews.this, Cart.class);
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
        finish();
        productReviews.cancel(true);
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
    //-----------------------Async tasks----------------------------
    public class ProductReviewsList extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> reviews=new ArrayList<>();
        ListView reviewList= (ListView) findViewById(R.id.reviews);
        AVLoadingIndicatorView avLoadingIndicatorView;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reviewList.setAdapter(null);
            avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ReviewsList";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID"))+ "\"}";
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
                    data[0]=jsonObject.optString("ReviewID");
                    data[1]=jsonObject.optString("Name");
                    data[2]=jsonObject.optString("ReviewDescription");
                    data[3]=jsonObject.optString("CreatedDate").replace("/Date(", "").replace(")/", "");
                    data[4]=jsonObject.optString("IsApproved");
                    reviews.add(data);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            TextView loadingTxt=(TextView)findViewById(R.id.loadingText);
            if(!pass) {
                loadingTxt.setText(R.string.no_reviews);
                loadingTxt.setVisibility(View.VISIBLE);
            }
            else {
                loadingTxt.setVisibility(View.GONE);
                CustomAdapter adapter=new CustomAdapter(ProductReviews.this, reviews,"productReviews");

                reviewList.setAdapter(adapter);
                reviewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView t=(TextView) view.findViewById(R.id.reviewDescription);
                        t.setMaxLines(Integer.MAX_VALUE);
                        t.setEllipsize(null);
                    }});
                reviewList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        if(!reviews.get(position)[4].equals("true")) {//If not approved yet
                            new AlertDialog.Builder(ProductReviews.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                                    .setMessage(R.string.delete_review_q)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new DeleteProductReview(reviews.get(position)[0]).execute();
                                        }
                                    }).setNegativeButton(R.string.no, null).show();
                        }
                        return false;
                    }
                });
            }
            avLoadingIndicatorView.setVisibility(View.GONE);
        }
    }
    public class InsertProductReview extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        String reviewDescription;
        ProgressDialog pDialog=new ProgressDialog(ProductReviews.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            reviewDescription=inputReview.getText().toString().trim();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/InsertProductReview";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID"))+ "\",\"reviewDescription\":\"" + reviewDescription + "\"}";
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(!pass) {
                new AlertDialog.Builder(ProductReviews.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).show();
            }
            else {
                productReviews=new ProductReviewsList().execute();

                inputReview.clearFocus();
                inputReview.setText("");
                inputReview.setLines(1);
                submitReview.setVisibility(View.GONE);
                inputReview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        inputReview.setLines(3);
                        submitReview.setVisibility(View.VISIBLE);
                    }
                });
            }
            submit.setEnabled(true);
        }
    }
    public class DeleteProductReview extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ProgressDialog pDialog=new ProgressDialog(ProductReviews.this);
        String reviewIDString;
        public DeleteProductReview(String reviewID){
            reviewIDString=reviewID;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            //----------encrypting -------------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/DeleteProductReview";
            HttpURLConnection c = null;
            try {
                postData = "{\"reviewID\":\"" + reviewIDString + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(!pass) {
                new AlertDialog.Builder(ProductReviews.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).show();
            }
            else {
                productReviews=new ProductReviewsList().execute();
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
