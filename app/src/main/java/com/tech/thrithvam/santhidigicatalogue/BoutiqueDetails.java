package com.tech.thrithvam.santhidigicatalogue;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

public class BoutiqueDetails extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;
    ImageView boutiqueImg;
    TextView aboutUs;
    TextView caption;
    TextView year;
    TextView location;
    TextView address;
    TextView viewMap;
    TextView phone;
    TextView timing;
    TextView workingDays;
    TextView fbLink;
    TextView instagramLink;
    TextView owners;
    TextView designers;
    ListView sideBar;
    ArrayList<String> categoryList;
    Dictionary<String,String> categoryCode=new Hashtable<>();
    ArrayAdapter categoryAdapter;
    AsyncTask boutiqueDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boutique_details);
        db=DatabaseHandler.getInstance(this);

        if (isOnline()){
            boutiqueDetails=new GetBoutiqueDetails().execute();
        }
        else {
            Toast.makeText(BoutiqueDetails.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
            finish();
        }
        loadCategories();
        //----------------setting fonts------------------------
        Typeface fontType1 = Typeface.createFromAsset(getAssets(), "fonts/segoeui.ttf");
        Typeface fontType2 = Typeface.createFromAsset(getAssets(), "fonts/handwriting.ttf");

        boutiqueImg=(ImageView)findViewById(R.id.boutiqueImg);
        aboutUs=(TextView)findViewById(R.id.aboutUs);
        caption=(TextView)findViewById(R.id.caption);
        year=(TextView)findViewById(R.id.year);
        location=(TextView)findViewById(R.id.location);
        address=(TextView)findViewById(R.id.address);
        viewMap=(TextView)findViewById(R.id.view_map);
        phone=(TextView)findViewById(R.id.phone);
        timing=(TextView)findViewById(R.id.timing);
        workingDays=(TextView)findViewById(R.id.workingDays);
        fbLink=(TextView)findViewById(R.id.fbLink);
        instagramLink=(TextView)findViewById(R.id.instagramLink);
        owners=(TextView)findViewById(R.id.owners);
       // designers=(TextView)findViewById(R.id.designers);
        //---------------set boutique details------------------
        aboutUs.setTypeface(fontType1);
        caption.setTypeface(fontType2);
        year.setTypeface(fontType1);
        location.setTypeface(fontType1);
        address.setTypeface(fontType1);
        viewMap.setTypeface(fontType1);
        phone.setTypeface(fontType1);
    }
    //---------------------------Categories loading------------------------------------
    public void loadCategories(){
     //   Handler handler = new Handler();
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
            categoryAdapter = new ArrayAdapter<>(BoutiqueDetails.this, R.layout.side_bar_item, categoryList);
            sideBar.setAdapter(categoryAdapter);
            sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!categoryList.get(position).equals("")) {
                        if (categoryList.get(position).equals(getResources().getString(R.string.my_orders_sidebar))) {
                            Intent orderIntent = new Intent(BoutiqueDetails.this, OrderStatus.class);
                            startActivity(orderIntent);
                            overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                        }
                        else {
                            Intent categoryIntent = new Intent(BoutiqueDetails.this, GridOfProducts.class);
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
       /* else {
            handler.postDelayed(new Runnable() {
                public void run() {
                    loadCategories();
                }
            },300);
        }*/
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
//                Intent intentCart = new Intent(BoutiqueDetails.this, Cart.class);
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
        boutiqueDetails.cancel(true);
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
    //-------------------- Async tasks---------------------------------
    public class GetBoutiqueDetails extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView avLoadingIndicatorView;
        ScrollView detailsScroll;
        String nameString, startedYearString, aboutUsString, captionString, locationString, addressString, phoneString, timingString, workingDaysString, fBLinkString, instagramLinkString,latlong,image;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
            detailsScroll=(ScrollView)findViewById(R.id.detailsScroll);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/Boutique";
            HttpURLConnection c = null;
            try {
                postData = "{\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                    nameString =jsonObject.optString("Name");
                    startedYearString =jsonObject.optString("StartedYear");
                    aboutUsString =jsonObject.optString("AboutUs");
                    captionString =jsonObject.optString("Caption");
                    locationString =jsonObject.optString("Location");
                    addressString =jsonObject.optString("Address");
                    phoneString =jsonObject.optString("Phone");
                    timingString =jsonObject.optString("Timing");
                    workingDaysString =jsonObject.optString("WorkingDays");
                    fBLinkString ="https://"+jsonObject.optString("FBLink");
                    instagramLinkString ="https://"+jsonObject.optString("InstagramLink");
                    latlong=jsonObject.optString("latlong");
                    image=jsonObject.optString("Image","null");
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
                new AlertDialog.Builder(BoutiqueDetails.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
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
                detailsScroll.setVisibility(View.VISIBLE);
                android.support.v7.app.ActionBar ab = getSupportActionBar();
                if (ab != null) {
                    ab.setTitle(nameString);
                }
                if(!image.equals("null"))
                {
                    Glide.with(BoutiqueDetails.this)
                            .load(getString(R.string.url)+image.substring(image.indexOf("Media")))
                            .crossFade()
                            .into(boutiqueImg)
                    ;}
                caption.setText(captionString);
                aboutUs.setText(aboutUsString);
                year.setText(getResources().getString(R.string.since,startedYearString));
                location.setText(locationString);
                address.setText(addressString);
                viewMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + (latlong)));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                phone.setText(phoneString);
                phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {                                   //Phone call function
                        Uri number = Uri.parse("tel:" + phoneString);
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                        startActivity(callIntent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });


                timing.setText(timingString);
                workingDays.setText(workingDaysString);

                fbLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {                                                   //opening links
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fBLinkString));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });

                instagramLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {                                           //opening links
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramLinkString));
                        startActivity(browserIntent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                owners.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(BoutiqueDetails.this, OwnerAndDesigner.class);
                        intent.putExtra("ownerORdesigner","owner");
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
               /* designers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(BoutiqueDetails.this, OwnerAndDesigner.class);
                        intent.putExtra("ownerORdesigner","designer");
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });*/
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
