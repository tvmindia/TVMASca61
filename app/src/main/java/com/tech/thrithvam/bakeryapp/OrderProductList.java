package com.tech.thrithvam.bakeryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderProductList extends AppCompatActivity {
    Constants constants=new Constants();
    Bundle extras=new Bundle();
    TextView orderDescription;
    TextView orderNo;
    TextView amount;
    TextView orderDate;
    TextView lastUpdatedDate;
    TextView orderStatus;
    TextView dateLabel;
    TextView deliveryDate;
    TextView deliveryTime;
    TextView address;
    TextView branchName,branchNameLabel;
    AsyncTask orderItems,orderDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_product_list);
        extras=getIntent().getExtras();
        orderDescription = (TextView) findViewById(R.id.description);
        orderNo = (TextView) findViewById(R.id.orderNo);
        amount = (TextView) findViewById(R.id.amount);
        orderDate = (TextView) findViewById(R.id.orderDate);
        lastUpdatedDate = (TextView) findViewById(R.id.lastUpdatedDate);
        orderStatus =(TextView) findViewById(R.id.orderStatus);
        dateLabel =(TextView)findViewById(R.id.readyDateLabel);
        deliveryDate = (TextView) findViewById(R.id.expectedDeliveryDate);
        deliveryTime =(TextView)findViewById(R.id.expectedDeliveryTime);
        address=(TextView)findViewById(R.id.address);
        branchName=(TextView)findViewById(R.id.branchName);
        branchNameLabel=(TextView)findViewById(R.id.branchLabel);
        if(isOnline()){
            orderItems= new OrderItems(extras.getString("orderID")).execute();/*
            if(!(getIntent().hasExtra("orderNo"))){ //Checking whether the details are in intent*/
            orderDetails=new OrderDetails(extras.getString("orderID")).execute();
           /* }
            else {
                orderDescription.setText(extras.getString("orderDescription"));
                orderNo.setText(extras.getString("orderNo"));
                amount.setText(extras.getString("amount"));
                orderDate.setText(extras.getString("orderDate"));
                deliveryDate.setText(extras.getString("deliveryDate"));
                lastUpdatedDate.setText(extras.getString("lastUpdatedDate"));
                orderStatus.setText(extras.getString("orderStatus"));
                readyLabel.setText(extras.getString("dateLabel"));
                products.putExtra("BranchName",orders.get(position)[10]);
                products.putExtra("StatusCode",orders.get(position)[11]);
                products.putExtra("Status",orders.get(position)[12]);
                products.putExtra("PlannedDeliveryTime",orders.get(position)[13]);

              *//*  if(orderStatus.getText().toString().equals(getString(R.string.order_placed))){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        orderStatus.setTextColor(getColor(R.color.primary_text));
                    }
                    else {
                        orderStatus.setTextColor(getResources().getColor(R.color.primary_text));
                    }
                }
                else if(orderStatus.getText().toString().equals(getString(R.string.your_order_is_ready))){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        orderStatus.setTextColor(getColor(R.color.green));
                    }
                    else {
                        orderStatus.setTextColor(getResources().getColor(R.color.green));
                    }
                }
                else if(orderStatus.getText().toString().equals(getString(R.string.item_delivered))){
                    orderStatus.setTextColor(Color.BLUE);
                }
            }*/
        }
        else {
            Toast.makeText(OrderProductList.this,R.string.network_off_alert,Toast.LENGTH_LONG).show();
            finish();
        }


    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public class OrderItems extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> products=new ArrayList<>();
        String orderID;
        TextView loadingTxt;
        AVLoadingIndicatorView avLoadingIndicatorView;
        public OrderItems(String orderID){
            this.orderID=orderID;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingTxt=(TextView)findViewById(R.id.loadingText);
            avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/OrderItems";
            HttpURLConnection c = null;
            try {
                postData = "{\"orderID\":\"" + orderID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                    String[] data=new String[6];
                    data[0]=jsonObject.optString("ProductID");
                    data[1]=jsonObject.optString("Product");
                    data[2]=jsonObject.optString("ProductType");
                    data[3]=jsonObject.optString("Quantity");
                    data[4]=jsonObject.optString("Itemprice");
                    data[5]=jsonObject.optString("CustomerRemarks");
                    products.add(data);
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
                    loadingTxt.setText(R.string.no_items);
                    loadingTxt.setVisibility(View.VISIBLE);
            }
            else {
                loadingTxt.setVisibility(View.GONE);
                CustomAdapter adapter=new CustomAdapter(OrderProductList.this, products,"orderItems");

                ListView productList= (ListView) findViewById(R.id.productList);
                productList.setAdapter(adapter);
                productList.setVisibility(View.VISIBLE);
                productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(OrderProductList.this,ItemDetails.class);
                        intent.putExtra("ProductID",products.get(position)[0]);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }});
            }
            avLoadingIndicatorView.setVisibility(View.GONE);

        }
    }
    public class OrderDetails extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
       // ArrayList<String[]> orders=new ArrayList<>();
        String orderID;
        String[] data=new String[16];
        AVLoadingIndicatorView avLoadingIndicatorView;
        CardView orderCard;
        public OrderDetails(String orderID){
            this.orderID=orderID;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.cardLoading);
            orderCard=(CardView)findViewById(R.id.orderCard);
            orderCard.setVisibility(View.INVISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/OrderDetailsByID";
            HttpURLConnection c = null;
            try {
                postData = "{\"orderID\":\"" + orderID + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                 //   String[] data=new String[14];
                    data[0]=jsonObject.optString("OrderDescription");
                    data[1]=jsonObject.optString("OrderNo");
                    data[2]=String.format(Locale.US,"%.2f", jsonObject.optDouble("TotalOrderAmount"));
                    data[3]=jsonObject.optString("OrderDate").replace("/Date(", "").replace(")/", "");
                    data[4]=jsonObject.optString("PlannedDeliveryDate").replace("/Date(", "").replace(")/", "");
                    data[5]=jsonObject.optString("OrderReadyDate").replace("/Date(", "").replace(")/", "");
                    data[6]=jsonObject.optString("ActualDeliveryDate").replace("/Date(", "").replace(")/", "");
                    data[7]=jsonObject.optString("CreatedDate").replace("/Date(", "").replace(")/", "");
                    data[8]=jsonObject.optString("UpdatedDate").replace("/Date(", "").replace(")/", "");
                    data[9]=jsonObject.optString("OrderID");
                    data[10]=jsonObject.optString("BranchName");
                    data[11]=jsonObject.optString("StatusCode");
                    data[12]=jsonObject.optString("Status");
                    data[13]=jsonObject.optString("PlannedDeliveryTime");
                    data[14]=jsonObject.optString("DeliveryAddress");
                    data[15]=jsonObject.optString("BranchName");
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            Calendar cal= Calendar.getInstance();
            if(!pass) {
                new AlertDialog.Builder(OrderProductList.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(R.string.no_items)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();
            }
            else {
                avLoadingIndicatorView.setVisibility(View.GONE);
                orderCard.setVisibility(View.VISIBLE);
                //Label loading--------------------
                if(!data[0].equals("null")){
                    orderDescription.setText(data[0]);
                    orderDescription.setVisibility(View.VISIBLE);
                }
                else {
                    orderDescription.setVisibility(View.GONE);
                }

                orderNo.setText(data[1]);

                if(!data[2].equals("null")){
                    amount.setText(getResources().getString(R.string.rs, data[2]));
                    amount.setVisibility(View.VISIBLE);
                }
                else{
                    amount.setVisibility(View.INVISIBLE);
                }

                if(!data[3].equals("null")) {
                    cal.setTimeInMillis(Long.parseLong(data[3]));
                    orderDate.setText(formatted.format(cal.getTime()));
                }
                else
                    orderDate.setText("-");

                int status=Integer.parseInt(data[11]);

                switch (status){
                    case 0:
                        dateLabel.setText(R.string.requested_delivery_on);

                        if(!data[4].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(data[4]));
                            deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            deliveryDate.setText("-");

                        if(!data[13].equals("null")){
                            deliveryTime.setText(data[13]);
                            deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            deliveryTime.setVisibility(View.INVISIBLE);
                        orderStatus.setTextColor(Color.BLACK);
                        break;
                    case 1:
                        dateLabel.setText(R.string.delivery_on);

                        if(!data[4].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(data[4]));
                            deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            deliveryDate.setText("-");

                        if(!data[13].equals("null")){
                            deliveryTime.setText(data[13]);
                            deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            deliveryTime.setVisibility(View.INVISIBLE);
                        orderStatus.setTextColor(Color.parseColor("#A52A2A"));
                        break;
                    case 2:
                        dateLabel.setText(R.string.ready_on);

                        if(!data[5].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(data[5]));
                            deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            deliveryDate.setText("-");

                        if(!data[13].equals("null")){
                            deliveryTime.setText(data[13]);
                            deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            deliveryTime.setVisibility(View.INVISIBLE);
                        orderStatus.setTextColor(Color.parseColor("#FFA500"));
                        break;
                    case 3:
                        dateLabel.setText(R.string.delivered_on);

                        if(!data[6].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(data[6]));
                            deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            deliveryDate.setText("-");

                        if(!data[13].equals("null")){
                            deliveryTime.setText(data[13]);
                            deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            deliveryTime.setVisibility(View.INVISIBLE);
                        orderStatus.setTextColor(Color.GREEN);
                        break;
                    default:
                        dateLabel.setText("-");
                        deliveryDate.setText("-");
                        deliveryTime.setVisibility(View.INVISIBLE);
                        orderStatus.setTextColor(Color.DKGRAY);
                        break;
                }
                orderStatus.setText(data[12]);

                //last updated------------
                if(!data[8].equals("null")){                                    //updated
                    cal.setTimeInMillis(Long.parseLong(data[8]));
                    lastUpdatedDate.setText(formatted.format(cal.getTime()));
                }
                else if(!data[7].equals("null")) {                         //Only created
                    cal.setTimeInMillis(Long.parseLong(data[7]));
                    lastUpdatedDate.setText(formatted.format(cal.getTime()));
                }
                else
                    lastUpdatedDate.setText("-");

                //address and branch-----
                if(!data[14].equals("null")) {
                    address.setText(data[14]);
                }
                else
                    address.setText("-");
                if(!data[15].equals("null")) {
                    branchName.setText(data[15]);
                }
                else{
                    branchNameLabel.setVisibility(View.GONE);
                    branchName.setVisibility(View.GONE);
                }

            }
        }

    }
    @Override
    public void onBackPressed() {
        finish();
        if(orderDetails!=null){
            orderDetails.cancel(true);
        }
        orderItems.cancel(true);
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
}
