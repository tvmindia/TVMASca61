package com.tech.thrithvam.bakeryapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomAdapter extends BaseAdapter {
    Context adapterContext;
    private static LayoutInflater inflater=null;
    private ArrayList<String[]> objects;
    private String calledFrom;
    DatabaseHandler db;
    public CustomAdapter(Context context, ArrayList<String[]> objects, String calledFrom) {
       // super(context, textViewResourceId, objects);
        adapterContext=context;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects=objects;
        this.calledFrom=calledFrom;
        db=DatabaseHandler.getInstance(context);
    }
    public static class Holder
    {
        //Grid items-----------------------------------------------
        ImageView imageView;
        TextView title;
        ImageView offer;
        TextView viewCount;
        AVLoadingIndicatorView loading;
        //Order items-----------------------------------------------
        TextView orderDescription,orderNo,amount,orderDate, deliveryDate,lastUpdatedDate, orderStatus, dateLabel, deliveryTime;
        //Order products--------------------------
        TextView productName,orderProductType,orderQuantity,itemprice,remarks;
        //Product Reviews-------------------------
        TextView userName,reviewDescription,date;
        //Chat------------------------------------
        TextView message,time;
        RelativeLayout msgBox;
        CardView productDetail;
        //Cart-------------------------------
        TextView productNo,productPrice,productType,quantity;
        ImageView productImg,cancelItem;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        Calendar cal= Calendar.getInstance();
        switch (calledFrom) {
            //--------------------------for home screen items------------------
            case "categoryGrid":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.grid_item, null);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.gridImg);
                    holder.title = (TextView) convertView.findViewById(R.id.gridTxt);
                    holder.offer=(ImageView)convertView.findViewById(R.id.offer);
                    holder.viewCount=(TextView) convertView.findViewById(R.id.viewCount);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.title.setText(objects.get(position)[1]);
                //Image Loading-------------------
                final int FinalPosition = position;
                Glide.with(adapterContext)
                        .load(adapterContext.getResources().getString(R.string.url) + objects.get(position)[2].substring((objects.get(position)[2]).indexOf("Media")))
//                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .thumbnail(0.1f)
                        .into(holder.imageView)
                ;
                //Offer Label-----------------
                if(!objects.get(position)[3].equals("null")){
                    if(Integer.parseInt(objects.get(position)[3])>0){
                        holder.offer.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.offer.setVisibility(View.GONE);
                    }
                }else {
                    holder.offer.setVisibility(View.GONE);
                }
                //View count-----------------
               /* if(!objects.get(position)[4].equals("null")){
                    if(Integer.parseInt(objects.get(position)[4])>0){
                        holder.viewCount.setVisibility(View.VISIBLE);
                        if(Integer.parseInt(objects.get(position)[4])<=999)
                            holder.viewCount.setText(objects.get(position)[4]);
                        else
                            holder.viewCount.setText("999+");
                    }
                    else {
                        holder.viewCount.setVisibility(View.GONE);
                    }
                }else {
                    holder.viewCount.setVisibility(View.GONE);
                }*/
                //Navigation------------------
                /*convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                            Intent intent=new Intent(adapterContext,ItemDetails.class);
                            intent.putExtra("ProductID",objects.get(FinalPosition)[0]);
                            intent.putExtra("from","gridofproducts");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            adapterContext.startActivity(intent);
                            ((Activity)adapterContext).finish();
                            ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                        }
                        else {
                            Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                        }
                    }
                });*/
                break;
            case "homeGrid":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.homescreen_items, null);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.gridImg);
                    holder.title = (TextView) convertView.findViewById(R.id.gridTxt);
                    holder.offer=(ImageView)convertView.findViewById(R.id.offer);
                    holder.loading=(AVLoadingIndicatorView)convertView.findViewById(R.id.itemsLoading);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
             /*   //More Image----------------------------------
                if(objects.get(position)[0].equals("")){        //More
                    holder.loading.setVisibility(View.GONE);
                    Picasso.with(adapterContext)
                            .load(R.drawable.more)
                            .into(holder.imageView)
                    ;
                    holder.title.setVisibility(View.GONE);
                    holder.offer.setVisibility(View.GONE);
                    final int FinalPos = position;
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isOnline()) {
                                Intent categoryIntent=new Intent(adapterContext,GridOfProducts.class);
                                categoryIntent.putExtra("CategoryCode",objects.get(FinalPos)[1]);
                                categoryIntent.putExtra("Category",objects.get(FinalPos)[2].replace("\uD83D\uDC49\t",""));
                                categoryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                adapterContext.startActivity(categoryIntent);
                                ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                            }
                            else {
                                Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    break;
                }
                else {
                    holder.title.setVisibility(View.VISIBLE);
                }*/
                //Label loading--------------------

                Typeface type = Typeface.createFromAsset(adapterContext.getAssets(), "fonts/segoeui.ttf");
                holder.title.setText(objects.get(position)[1]);
                holder.title.setTypeface(type);
                //Image Loading-------------------
                final int FinalPos = position;
                /*Picasso.with(adapterContext)
                        .load(objects.get(position)[2])
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .resize(75,75)
                        .into(holder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onError() {
                                Picasso.with(adapterContext)
                                        .load(adapterContext.getResources().getString(R.string.url) + objects.get(FinalPos)[2].substring((objects.get(FinalPos)[2]).indexOf("Media")))
                                        .into(holder.imageView);
                            }
                        })
                ;*/
                Glide.with(adapterContext)
                        .load(objects.get(position)[2]).thumbnail(0.1f)
//                        .networkPolicy(NetworkPolicy.OFFLINE)
//                        .resize(75,75)
                        .into(holder.imageView)
                ;
               /* Picasso.with(adapterContext)
                        .load(adapterContext.getResources().getString(R.string.url) + objects.get(position)[2].substring((objects.get(position)[2]).indexOf("Media")))
                        .into(holder.imageView)
                ;*/
                //Offer Label-----------------
                if(!objects.get(position)[3].equals("null")){
                    if(Integer.parseInt(objects.get(position)[3])>0){
                        holder.offer.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.offer.setVisibility(View.GONE);
                    }
                }else {
                    holder.offer.setVisibility(View.GONE);
                }
                //Navigation------------------
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                            Intent intent=new Intent(adapterContext,ItemDetails.class);
                            intent.putExtra("ProductID",objects.get(FinalPos)[0]);
                            intent.putExtra("from","home");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            adapterContext.startActivity(intent);
                            ((Activity)adapterContext).finish();
                            ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                        }
                        else {
                            Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            //--------------------------for order status items------------------
            case "orders":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.order_item, null);
                    holder.orderDescription = (TextView) convertView.findViewById(R.id.description);
                    holder.orderNo = (TextView) convertView.findViewById(R.id.orderNo);
                    holder.amount = (TextView) convertView.findViewById(R.id.amount);
                    holder.dateLabel =(TextView)convertView.findViewById(R.id.readyDateLabel);
                    holder.orderDate = (TextView) convertView.findViewById(R.id.orderDate);
                    holder.deliveryDate = (TextView) convertView.findViewById(R.id.expectedDeliveryDate);
                    holder.deliveryTime =(TextView)convertView.findViewById(R.id.expectedDeliveryTime);
                    holder.lastUpdatedDate = (TextView) convertView.findViewById(R.id.lastUpdatedDate);
                    holder.orderStatus =(TextView) convertView.findViewById(R.id.orderStatus);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                if(!objects.get(position)[0].equals("null")){
                    holder.orderDescription.setText(objects.get(position)[0]);
                    holder.orderDescription.setVisibility(View.VISIBLE);
                }
                else {
                    holder.orderDescription.setVisibility(View.GONE);
                }

                holder.orderNo.setText(objects.get(position)[1]);

                if(!objects.get(position)[2].equals("null")){
                    holder.amount.setText(adapterContext.getResources().getString(R.string.rs, objects.get(position)[2]));
                    holder.amount.setVisibility(View.VISIBLE);
                }
                else{
                    holder.amount.setVisibility(View.INVISIBLE);
                }

                if(!objects.get(position)[3].equals("null")) {
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[3]));
                    holder.orderDate.setText(formatted.format(cal.getTime()));
                }
                else
                    holder.orderDate.setText("-");

                int status=Integer.parseInt(objects.get(position)[11]);

                switch (status){
                    case 0:
                        holder.dateLabel.setText(R.string.requested_delivery_on);

                        if(!objects.get(position)[4].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(objects.get(position)[4]));
                            holder.deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            holder.deliveryDate.setText("-");

                        if(!objects.get(position)[13].equals("null")){
                            holder.deliveryTime.setText(objects.get(position)[13]);
                            holder.deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            holder.deliveryTime.setVisibility(View.INVISIBLE);
                        holder.orderStatus.setTextColor(Color.BLACK);
                        break;
                    case 1:
                        holder.dateLabel.setText(R.string.delivery_on);

                        if(!objects.get(position)[4].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(objects.get(position)[4]));
                            holder.deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            holder.deliveryDate.setText("-");

                        if(!objects.get(position)[13].equals("null")){
                            holder.deliveryTime.setText(objects.get(position)[13]);
                            holder.deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            holder.deliveryTime.setVisibility(View.INVISIBLE);
                        holder.orderStatus.setTextColor(Color.parseColor("#A52A2A"));
                        break;
                    case 2:
                        holder.dateLabel.setText(R.string.ready_on);

                        if(!objects.get(position)[5].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(objects.get(position)[5]));
                            holder.deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            holder.deliveryDate.setText("-");

                        if(!objects.get(position)[13].equals("null")){
                            holder.deliveryTime.setText(objects.get(position)[13]);
                            holder.deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            holder.deliveryTime.setVisibility(View.INVISIBLE);
                        holder.orderStatus.setTextColor(Color.parseColor("#FFA500"));
                        break;
                    case 3:
                        holder.dateLabel.setText(R.string.delivered_on);

                        if(!objects.get(position)[6].equals("null")){
                            cal.setTimeInMillis(Long.parseLong(objects.get(position)[6]));
                            holder.deliveryDate.setText(formatted.format(cal.getTime()));
                        }
                        else
                            holder.deliveryDate.setText("-");

                        if(!objects.get(position)[13].equals("null")){
                            holder.deliveryTime.setText(objects.get(position)[13]);
                            holder.deliveryTime.setVisibility(View.VISIBLE);
                        }
                        else
                            holder.deliveryTime.setVisibility(View.INVISIBLE);
                        holder.orderStatus.setTextColor(Color.GREEN);
                        break;
                    default:
                        holder.dateLabel.setText("-");
                        holder.deliveryDate.setText("-");
                        holder.deliveryTime.setVisibility(View.INVISIBLE);
                        holder.orderStatus.setTextColor(Color.DKGRAY);
                        break;
                }
                holder.orderStatus.setText(objects.get(position)[12]);

                //last updated------------
                if(!objects.get(position)[8].equals("null")){                                    //updated
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[8]));
                    holder.lastUpdatedDate.setText(formatted.format(cal.getTime()));
                }
                else if(!objects.get(position)[7].equals("null")) {                         //Only created
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[7]));
                    holder.lastUpdatedDate.setText(formatted.format(cal.getTime()));
                }
                else
                    holder.lastUpdatedDate.setText("-");
                break;
            //--------------------------------Order Items--------------------------------------
            case "orderItems":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.product_list, null);
                    holder.productName = (TextView) convertView.findViewById(R.id.itemLeft1);
                    holder.orderProductType=(TextView)convertView.findViewById(R.id.itemRight1);
                    holder.orderQuantity=(TextView)convertView.findViewById(R.id.itemMiddle);
                    holder.itemprice=(TextView)convertView.findViewById(R.id.itemRight);
                    holder.remarks = (TextView) convertView.findViewById(R.id.itemLeft);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }

                //Label loading--------------------
                holder.productName.setText(objects.get(position)[1]);

                if(!objects.get(position)[2].equals("null"))
                    holder.orderProductType.setText(objects.get(position)[2]);
                else
                    holder.orderProductType.setVisibility(View.GONE);

                if(!objects.get(position)[3].equals("null"))
                    holder.orderQuantity.setText(objects.get(position)[3]);
                else
                    holder.orderQuantity.setVisibility(View.INVISIBLE);

                if(!objects.get(position)[4].equals("null"))
                    holder.itemprice.setText(adapterContext.getResources().getString(R.string.rs, objects.get(position)[4]));
                else
                    holder.itemprice.setVisibility(View.INVISIBLE);

                if(!objects.get(position)[5].equals("null"))
                    holder.remarks.setText(objects.get(position)[5]);
                else
                    holder.remarks.setVisibility(View.INVISIBLE);

                break;
            //--------------------------------Order Items--------------------------------------
            case "productReviews":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.reviews_item, null);
                    holder.userName = (TextView) convertView.findViewById(R.id.name);
                    holder.reviewDescription = (TextView) convertView.findViewById(R.id.reviewDescription);
                    holder.date = (TextView) convertView.findViewById(R.id.date);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }

                //Label loading--------------------
                holder.userName.setText(objects.get(position)[1]);
                holder.reviewDescription.setText(objects.get(position)[2]);
                if(!objects.get(position)[4].equals("true")){//Not Approved
                    holder.reviewDescription.setTextColor(Color.GRAY);
                }
                if(!objects.get(position)[3].equals("null")) {
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[3]));
                    holder.date.setText(formatted.format(cal.getTime()));
                }
                else
                    holder.date.setText("-");
                break;
            //-------------------------------Related Items----------------------------------------
            case "relatedItems":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.related_items, null);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.gridImg);
                    holder.title = (TextView) convertView.findViewById(R.id.gridTxt);
                    holder.offer=(ImageView)convertView.findViewById(R.id.offer);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.title.setText(objects.get(position)[1]);
                //Image Loading-------------------
                final int FinalP = position;
                Glide.with(adapterContext)
                        .load(adapterContext.getResources().getString(R.string.url) + objects.get(position)[2].substring((objects.get(position)[2]).indexOf("Media")))
                       // .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.imageView)
                ;
                //Offer Label-----------------
                if(!objects.get(position)[3].equals("null")){
                    if(Integer.parseInt(objects.get(position)[3])>0){
                        holder.offer.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.offer.setVisibility(View.GONE);
                    }
                }else {
                    holder.offer.setVisibility(View.GONE);
                }
                //Navigation------------------
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOnline()) {
                            Intent intent=new Intent(adapterContext,ItemDetails.class);
                            intent.putExtra("ProductID",objects.get(FinalP)[0]);
                            intent.putExtra("from","itemdetails");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            adapterContext.startActivity(intent);
                            ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                        }
                        else {
                            Toast.makeText(adapterContext, R.string.network_off_alert, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
                //-------------------------------Chat Items----------------------------------------

            case "chat":
                if (convertView == null) {
                holder = new Holder();
                convertView = inflater.inflate(R.layout.message_item, null);
                holder.message = (TextView) convertView.findViewById(R.id.msgItem);
                holder.time = (TextView) convertView.findViewById(R.id.msgDate);
                holder.msgBox=(RelativeLayout)convertView.findViewById(R.id.msgBox);
                holder.productDetail=(CardView)convertView.findViewById(R.id.productDetail);
                convertView.setTag(holder);
                }
                else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                if(objects.get(position)[0].equals("$$NewProduct$$")){
                    holder.productDetail.setVisibility(View.VISIBLE);
                    holder.msgBox.setVisibility(View.GONE);
                    new ProductDetailsForChat(objects.get(position)[3],convertView).execute();
                    break;
                }
                else {
                    holder.productDetail.setVisibility(View.GONE);
                    holder.msgBox.setVisibility(View.VISIBLE);
                    holder.message.setText(objects.get(position)[0]);
                    if(!objects.get(position)[1].equals("null")){
                        SimpleDateFormat formattedWithTime = new SimpleDateFormat("hh:mm a dd-MMM-yyyy", Locale.US);
                        cal.setTimeInMillis(Long.parseLong(objects.get(position)[1]));
                        holder.time.setText(formattedWithTime.format(cal.getTime()));
                    }
                    //Left Right positioning of message boxes-------
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    if(objects.get(position)[2].equals("out")){
                        params.setMargins(100, 7, 7, 7);//(left, top, right, bottom)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            holder.msgBox.setBackground(adapterContext.getDrawable(R.drawable.out_chat));
                        }
                        else {
                            holder.msgBox.setBackgroundDrawable(adapterContext.getResources().getDrawable(R.drawable.out_chat));
                        }
                    }
                    else if(objects.get(position)[2].equals("in")){
                        params.setMargins(7, 7, 100, 7);//(left, top, right, bottom)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            holder.msgBox.setBackground(adapterContext.getDrawable(R.drawable.in_chat));
                        }
                        else {
                            holder.msgBox.setBackgroundDrawable(adapterContext.getResources().getDrawable(R.drawable.in_chat));
                        }
                    }
                    holder.msgBox.setLayoutParams(params);
                    break;
                }
            case "cart":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.cart_item, null);
                    holder.productName=(TextView) convertView.findViewById(R.id.productName);
                    holder.productNo=(TextView) convertView.findViewById(R.id.productNo);
                    holder.productPrice=(TextView) convertView.findViewById(R.id.productPrice);
                    holder.productType=(TextView) convertView.findViewById(R.id.type);
                    holder.quantity=(TextView) convertView.findViewById(R.id.quantity);
                    holder.productImg=(ImageView)convertView.findViewById(R.id.productImg);
                    holder.cancelItem=(ImageView)convertView.findViewById(R.id.cancel_item);
                    convertView.setTag(holder);
                }
                else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.productName.setText(objects.get(position)[6]);
                holder.productNo.setText(objects.get(position)[5]);
                holder.quantity.setText(adapterContext.getResources().getString(R.string.quantity_value,objects.get(position)[3]));
                if(objects.get(position)[2].equals("null")){
                    holder.productType.setVisibility(View.INVISIBLE);
                }
                else {
                    holder.productType.setVisibility(View.VISIBLE);
                    holder.productType.setText(objects.get(position)[2]);
                }
                holder.productPrice.setText(objects.get(position)[4]);
                Glide.with(adapterContext)
                        .load(objects.get(position)[7])
                        .into(holder.productImg)
                ;
                holder.cancelItem.setTag(objects.get(position)[0]);
                break;
            default:
                break;
        }
        return convertView;
    }

    public boolean isOnline() {
            ConnectivityManager cm =(ConnectivityManager) adapterContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class ProductDetailsForChat extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        String productName,priceString,productImage;
        Integer productNoInt;
        String productID;
        View prodDetView;
        Constants constants=new Constants();
        public ProductDetailsForChat(String productID,View convertView){
            this.productID=productID;
            this.prodDetView=convertView;
        }
        TextView pName;
        TextView pNo;
        TextView pPrice;
        ImageView pImage;
        AVLoadingIndicatorView avLoadingIndicatorView;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView=(AVLoadingIndicatorView)prodDetView.findViewById(R.id.prodDetLoading);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            pName=(TextView)prodDetView.findViewById(R.id.productName);
            pNo=(TextView)prodDetView.findViewById(R.id.productNo);
            pPrice=(TextView)prodDetView.findViewById(R.id.productPrice);
            pImage=(ImageView)prodDetView.findViewById(R.id.productImg);


          /*  if(db.GetProductDetail(productID,"ProductID")!=null){
                productName=db.GetProductDetail(productID,"ProductName");
                priceString=db.GetProductDetail(productID,"Price");
                productNoInt=Integer.parseInt(db.GetProductDetail(productID,"ProductNo"));
                productImage=db.GetProductImages(productID).get(0);

                pName.setText(productName);
                pNo.setText(adapterContext.getResources().getString(R.string.product_no, productNoInt));
                pPrice.setText(adapterContext.getResources().getString(R.string.rs, priceString));
                Glide.with(adapterContext).load(productImage).into(pImage);
                prodDetView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(adapterContext,ItemDetails.class);
                        intent.putExtra("ProductID",productID);
                        intent.putExtra("from","chat");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        adapterContext.startActivity(intent);
                        ((Activity)adapterContext).finish();
                        ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                avLoadingIndicatorView.setVisibility(View.GONE);
            }
            else {
                pName.setVisibility(View.INVISIBLE);
                pNo.setVisibility(View.INVISIBLE);
                pPrice.setVisibility(View.INVISIBLE);
                pImage.setVisibility(View.INVISIBLE);
            }*/
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =adapterContext.getResources().getString(R.string.url) + "WebServices/WebService.asmx/GetProductDetailsOnChat";
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
                    productName=jsonObject.optString("Name");
                    priceString=String.format(Locale.US,"%.2f", jsonObject.optDouble("Price"));
                    productNoInt=jsonObject.optInt("ProductNo");
                    productImage=adapterContext.getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
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
                /*new AlertDialog.Builder(Chat.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();*/
            }
            else {
                pName.setVisibility(View.VISIBLE);
                pNo.setVisibility(View.VISIBLE);
                pPrice.setVisibility(View.VISIBLE);
                pImage.setVisibility(View.VISIBLE);

                pName.setText(productName);
                pNo.setText(adapterContext.getResources().getString(R.string.product_no, productNoInt));
                pPrice.setText(adapterContext.getResources().getString(R.string.rs, priceString));
                Glide.with(adapterContext).load(productImage).into(pImage);
                prodDetView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(adapterContext,ItemDetails.class);
                        intent.putExtra("ProductID",productID);
                        intent.putExtra("from","chat");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        adapterContext.startActivity(intent);
                        ((Activity)adapterContext).finish();
                        ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                avLoadingIndicatorView.setVisibility(View.GONE);
            }
        }
    }
}
