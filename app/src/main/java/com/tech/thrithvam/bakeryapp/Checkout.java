package com.tech.thrithvam.bakeryapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Checkout extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;
    RadioGroup rButtons;
    EditText addressInput;
    RadioButton defaultAddress,chooseAddress;
    AsyncTask placeOrder;
    TextView reqDate,reqTime;
    Calendar reqDateCal=null, reqTimeCal=null;
    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        db=DatabaseHandler.getInstance(this);
        extras=getIntent().getExtras();
        rButtons=(RadioGroup)findViewById(R.id.deliveryAddress);
        addressInput=(EditText)findViewById(R.id.addressinput);
        defaultAddress=(RadioButton)findViewById(R.id.defaultAddress);
        chooseAddress=(RadioButton)findViewById(R.id.chooseAddress);
        rButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (rButtons.getCheckedRadioButtonId())
                {
                    case R.id.defaultAddress:
                        addressInput.setEnabled(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            addressInput.setTextColor(Color.LTGRAY);
                            defaultAddress.setTextColor(getColor(R.color.primary_text));
                        }
                        else {
                            addressInput.setTextColor(Color.LTGRAY);
                            defaultAddress.setTextColor(getResources().getColor(R.color.primary_text));
                        }
                        break;
                    case R.id.chooseAddress:
                        addressInput.setEnabled(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            defaultAddress.setTextColor(Color.LTGRAY);
                            addressInput.setTextColor(getColor(R.color.primary_text));
                        }
                        else {
                            defaultAddress.setTextColor(Color.LTGRAY);
                            addressInput.setTextColor(getResources().getColor(R.color.primary_text));
                        }
                        break;
                    default:
                }
            }
        });
        if(db.GetUserDetail("Address")!=null && !db.GetUserDetail("Address").equals("null")&& !db.GetUserDetail("Address").equals(""))
                    defaultAddress.setText(db.GetUserDetail("Address"));
        else{
            rButtons.removeViewAt(0);
            chooseAddress.setChecked(true);
        }

        //---------------Request Delivery date and time-----------------------------
        reqDate=(TextView)findViewById(R.id.requestDeliveryDate);
        reqTime=(TextView)findViewById(R.id.requestDeliveryTime);
        final ImageView cancelDate=(ImageView) findViewById(R.id.cancel_date);
        final ImageView cancelTime=(ImageView)findViewById(R.id.cancel_time);
        reqDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar today = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        reqDateCal=Calendar.getInstance();
                        reqDateCal.set(Calendar.YEAR, year);
                        reqDateCal.set(Calendar.MONTH, monthOfYear);
                        reqDateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);

                        reqDate.setText(formatted.format(reqDateCal.getTime()));
                        reqTime.setVisibility(View.VISIBLE);
                        cancelDate.setVisibility(View.VISIBLE);
                    }
                };
                new DatePickerDialog(Checkout.this, dateSetListener, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        cancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reqDate.setText(R.string.request_delivery_date_label);
                reqDateCal=null;
                cancelDate.setVisibility(View.GONE);
                reqTime.setText(R.string.request_delivery_time_label);
                reqTime.setVisibility(View.GONE);
                cancelTime.setVisibility(View.GONE);
            }
        });
        reqTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog= new TimePickerDialog(Checkout.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        reqTimeCal=Calendar.getInstance();
                        reqTimeCal.set(Calendar.HOUR_OF_DAY,selectedHour);
                        reqTimeCal.set(Calendar.MINUTE,selectedMinute);

                        SimpleDateFormat formatted = new SimpleDateFormat("hh:mm a", Locale.US);
                        reqTime.setText(formatted.format(reqTimeCal.getTime()));
                        cancelTime.setVisibility(View.VISIBLE);
                    }
                }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, false);
                timePickerDialog.show();
            }
        });
        cancelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reqTime.setText(R.string.request_delivery_time_label);
                cancelTime.setVisibility(View.GONE);
            }
        });
    }
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
    public void placeOrder(View view){
        if (isOnline()) {
            placeOrder=new PlaceOrder().execute();
        } else {
            Toast.makeText(Checkout.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    //----------------------------AsuyncTasks----------------------
    public class PlaceOrder extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ProgressDialog pDialog=new ProgressDialog(Checkout.this);
        String OrderItemsJson, TotalOrderAmount, DeliveryAddress, requestDeliveryDate, requestDeliveryTime;
        String orderNo;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();

            OrderItemsJson=db.GetCartItemsJson();
            TotalOrderAmount=extras.getString("totalAmount");
            switch (rButtons.getCheckedRadioButtonId())
            {
                case R.id.defaultAddress:
                    DeliveryAddress=db.GetUserDetail("Address");
                    break;
                case R.id.chooseAddress:
                    DeliveryAddress=addressInput.getText().toString();
                    break;
                default:
                    placeOrder.cancel(true);
                    break;
            }

            if(DeliveryAddress.equals("") || DeliveryAddress==null){
                placeOrder.cancel(true);
                return;
            }

            SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy MM dd", Locale.US);
            if(reqDate.getText().toString().equals(getResources().getString(R.string.request_delivery_date_label))){
                requestDeliveryDate="";
            }
            else {
                requestDeliveryDate = formattedDate.format(reqDateCal.getTime());
            }
            SimpleDateFormat formattedTime = new SimpleDateFormat("hh:mm:a", Locale.US);
            if(reqTime.getText().toString().equals(getResources().getString(R.string.request_delivery_time_label))){
                requestDeliveryTime="";
            }
            else {
                requestDeliveryTime = formattedTime.format(reqTimeCal.getTime());
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/AddOrder";
            HttpURLConnection c = null;
            try {
                postData = "{\"BoutiqueID\":\"" + constants.BoutiqueID + "\",\"UserID\":\"" + db.GetUserDetail("UserID") + "\",\"OrderItemsJson\":\"" + OrderItemsJson.replace("\"","\\\"") + "\",\"TotalOrderAmount\":\"" + TotalOrderAmount + "\",\"DeliveryAddress\":\"" + DeliveryAddress + "\",\"requestDeliveryDate\":\"" + requestDeliveryDate + "\",\"requestDeliveryTime\":\"" + requestDeliveryTime +"\"}";
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
                    orderNo=jsonObject.optString("OrderNo");
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
                new AlertDialog.Builder(Checkout.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();
            }
            else {
                db.flushOldCart();
                new AlertDialog.Builder(Checkout.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(getResources().getString(R.string.placed_order,orderNo))
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Checkout.this, OrderStatus.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.slide_entry1, R.anim.slide_entry2);
                            }
                        }).setCancelable(false).show();
            }
        }
    }
}
