package com.tech.thrithvam.bakeryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
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

import static com.tech.thrithvam.bakeryapp.User.FROM_E;
import static com.tech.thrithvam.bakeryapp.User.KEY_EXTRA;

public class OwnerAndDesigner extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;
    TextView profile;
    TextView phone;
    ImageView phoneSymbol;
    Bundle extras;
    Spinner spinner;
    ArrayList<String> arrayListName;
    ArrayList<String> arrayListProfile;
    ArrayList<String> arrayListPhone;
    ArrayList<String> designerID;
    ArrayList<String> imageURL;
    ArrayAdapter<String> adapter;
    ImageView dp;
    ListView sideBar;
    ArrayList<String> categoryList;
    Dictionary<String,String> categoryCode=new Hashtable<>();
    ArrayAdapter categoryAdapter;
    AsyncTask details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_and_designer);
        db=DatabaseHandler.getInstance(this);
        getSupportActionBar().setElevation(0);
        extras= getIntent().getExtras();
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if(extras.getString("ownerORdesigner").equals("designer")){
                ab.setTitle("Designer");
            }
            else {
                ab.setTitle("Owner");
            }
        }
        dp=(ImageView)findViewById(R.id.dp);
        arrayListName = new ArrayList<>();
        arrayListProfile=new ArrayList<>();
        arrayListPhone=new ArrayList<>();
        designerID =new ArrayList<>();
        imageURL =new ArrayList<>();
        profile=(TextView)findViewById(R.id.profile);
        phone=(TextView)findViewById(R.id.phone);
        phoneSymbol=(ImageView)findViewById(R.id.callSymbol);
        if (isOnline()){
            details=new GetDetails().execute();
        }
        else {
            Toast.makeText(OwnerAndDesigner.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
        }
        loadCategories();
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
            categoryAdapter = new ArrayAdapter<>(OwnerAndDesigner.this, R.layout.side_bar_item, categoryList);
            sideBar.setAdapter(categoryAdapter);
            sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!categoryList.get(position).equals("")) {
                        if (categoryList.get(position).equals(getResources().getString(R.string.my_orders_sidebar))) {
                            Intent orderIntent = new Intent(OwnerAndDesigner.this, OrderStatus.class);
                            startActivity(orderIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else if (categoryList.get(position).equals(getResources().getString(R.string.boutique_details))) {
                            Intent boutiqueIntent = new Intent(OwnerAndDesigner.this, BoutiqueDetails.class);
                            startActivity(boutiqueIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else {
                            Intent categoryIntent = new Intent(OwnerAndDesigner.this, GridOfProducts.class);
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
    @Override
    public void onBackPressed() {
        finish();
        details.cancel(true);
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
    //---------------Menu creation---------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
//        View cartItem =  menu.findItem(R.id.cart).getActionView();
//        TextView cart_count = (TextView) cartItem.findViewById(R.id.cart_count);
//        int count=db.GetCartItems().size();         if(count>0){             cart_count.setText(Integer.toString(count));             cart_count.setVisibility(View.VISIBLE);         }                     else              cart_count.setVisibility(View.INVISIBLE);
//        cartItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intentCart = new Intent(OwnerAndDesigner.this, Cart.class);
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
                intentUser.putExtra(KEY_EXTRA,FROM_E);
                startActivity(intentUser);
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
    //------------------------------Async Tasks-----------------------------
    public class GetDetails extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView avLoadingIndicatorView;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/OwnersAndDesigners";
            HttpURLConnection c = null;
            try {
                postData = "{\"ownerORdesigner\":\"" + extras.getString("ownerORdesigner") + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                    arrayListName.add(jsonObject.optString("Name"));
                    arrayListProfile.add(jsonObject.optString("Profile"));
                    arrayListPhone.add(jsonObject.optString("Mobile"));
                    designerID.add(jsonObject.optString("DesignerID"));
                    imageURL.add(jsonObject.optString("Image","null"));
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
                new AlertDialog.Builder(OwnerAndDesigner.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();
            }
            else {
                avLoadingIndicatorView.setVisibility(View.GONE);
                adapter = new ArrayAdapter<>(OwnerAndDesigner.this, R.layout.spinner_item, arrayListName);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner=(Spinner)findViewById(R.id.name);
                spinner.setAdapter(adapter);
                //--------Setting spinner-------------------
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        profile.setText(arrayListProfile.get(spinner.getSelectedItemPosition()));
                        if(!imageURL.get(position).equals("null"))
                        {
                            Glide.with(OwnerAndDesigner.this)
                                    .load(getString(R.string.url)+imageURL.get(position).substring(imageURL.get(position).indexOf("Media")))
                                    .placeholder(R.drawable.dp)
                                    .into(dp)
                            ;
                        }
                        else {
                            Glide.with(OwnerAndDesigner.this)
                                    .load(R.drawable.dp)
                                    .into(dp)
                            ;
                        }
                        final String phoneString=arrayListPhone.get(spinner.getSelectedItemPosition());
                        if(!phoneString.equals("null")){
                        phone.setText(phoneString);
                        phone.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri number = Uri.parse("tel:" + phoneString);
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                            }
                        });
                        phoneSymbol.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri number = Uri.parse("tel:" + phoneString);
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                                startActivity(callIntent);
                                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                            }
                        });}
                        else {
                            phone.setVisibility(View.GONE);
                            phoneSymbol.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                if("designer".equals(extras.getString("ownerORdesigner")) && getIntent().hasExtra("designerID"))   //to show specific designer details when comes from product details screen
                    {           spinner.setSelection(designerID.indexOf(extras.getString("designerID")));
                    }
                else if(arrayListName.size()>1){
                    spinner.performClick();                 //to allow user to see the list of owners as a popup- to avoid priority in owners
                }

            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
