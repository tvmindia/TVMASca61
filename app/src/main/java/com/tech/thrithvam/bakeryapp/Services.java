package com.tech.thrithvam.bakeryapp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Services extends Service {
    DatabaseHandler db;
    Constants constants=new Constants();
    double TIME_INTERVAL_IN_MINUTE=1;


    String messageIDs="";
    public Services() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        db=DatabaseHandler.getInstance(this);
  //      Toast.makeText(this, "The new Service was Created", Toast.LENGTH_SHORT).show();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    //        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        if(isOnline()){
                String req_res[]=db.GetUsedResponses();
                if(req_res!=null){
                    new SyncData(req_res).execute();
                }
                new GetNotifications().execute();
                if(db.GetUserDetail("UserID")!=null){
				new GetMessages().execute();
				}
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
      //  Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + ((int)(1000 * 60 * TIME_INTERVAL_IN_MINUTE)),
                PendingIntent.getService(this, 0, new Intent(this, Services.class), 0)
        );
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    //Async Tasks----------------------------------------------------------------------
    public class SyncData extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson,url,postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        public SyncData(String[] req_res){
            url=req_res[0];
            postData=req_res[1];
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {

            HttpURLConnection c = null;
            try {
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
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
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
                if(pass){
                        db.ResponsesUpdating(url,postData,strJson);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
    private class GetNotifications extends AsyncTask<Void, Void, Void> {
        int status;
        StringBuilder sb;
        String strJson, postData; JSONArray jsonArray4Notifications;
        String Message;
        Boolean pass=false;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ArrayList<String> titles=new ArrayList<>();
        ArrayList<String> messages=new ArrayList<>();
        ArrayList<String> productID=new ArrayList<>();
        ArrayList<String> categoryCode=new ArrayList<>();
        ArrayList<String> orderID=new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... arg0) {           //loading JSONs from server in background
            String url=getResources().getString(R.string.url)+"WebServices/WebService.asmx/Notifications";
            HttpURLConnection c = null;
            try {
                postData = "{\"notificationIDs\":\"" + db.getNotificationIDs() + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID")) + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream ());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                // c.connect();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a,b+1);
                        strJson="{\"JSON\":" + strJson.replace("\\\"", "\"")+ "}";
                }
            } catch (Exception ex) {
                stopSelf();
            }  finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        stopSelf();
                    }
                }
            }
            if(strJson!=null){
                try {
                    JSONObject jsonRootObject = new JSONObject(strJson);
                    jsonArray4Notifications = jsonRootObject.optJSONArray("JSON");
                    for (int i = 0; i < jsonArray4Notifications.length(); i++) {
                        JSONObject jsonObject = jsonArray4Notifications.getJSONObject(i);
                        pass=jsonObject.optBoolean("Flag",true);
                        Message=jsonObject.optString("Message","");
                        if(!jsonObject.optString("NotificationID").equals("")){     //to avoiding inserting null values when NotificationID is absent
                            db.insertNotificationIDs(jsonObject.optString("NotificationID"),jsonObject.optString("EndDate").replace("/Date(", "").replace(")/", ""));
                        }
                        titles.add(jsonObject.optString("Title"));
                        messages.add(jsonObject.optString("Description"));
                        productID.add(jsonObject.optString("ProductID"));
                        categoryCode.add(jsonObject.optString("CategoryCode"));
                        orderID.add(jsonObject.optString("OrderID"));
                    }

                } catch (Exception e) {
                    stopSelf();
                }}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
    //            Toast.makeText(Services.this,"Error in Boutique app background service: "+Message, Toast.LENGTH_LONG).show();
            }
            else {
                for (int i=0;i<titles.size();i++) {
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Services.this);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    mBuilder.setContentTitle(titles.get(i));
                    mBuilder.setContentText(messages.get(i));
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSound(alarmSound);
                    Intent resultIntent;
                    if(!productID.get(i).equals("null")){
                        resultIntent= new Intent(Services.this, ItemDetails.class);
                        resultIntent.putExtra("ProductID",productID.get(i));
                    }
                    else if(!categoryCode.get(i).equals("null")){
                        resultIntent= new Intent(Services.this, GridOfProducts.class);
                        resultIntent.putExtra("CategoryCode",categoryCode.get(i));
                        resultIntent.putExtra("Category","");
                    }
                    else if(!orderID.get(i).equals("null")){
                        resultIntent= new Intent(Services.this, OrderProductList.class);
                        resultIntent.putExtra("orderID",orderID.get(i));
                    }
                    else{
                        resultIntent= new Intent(Services.this, SplashScreen.class);
                    }
                    PendingIntent resultPendingIntent =PendingIntent.getActivity(Services.this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify((int) Math.ceil(Math.random() * 1000), mBuilder.build());//random notification id on phone
                }
            }
        }
    }
    public class GetMessages extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        boolean msgIncomingflag=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/GetMessages";
            HttpURLConnection c = null;
            try {
                postData = "{\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID")) + "\",\"replyPersonID\":\"" + "" + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
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
                    messageIDs+=jsonObject.optString("MessageID")+",";
                    if(jsonObject.has("MessageID")) {
                        db.insertMessage(jsonObject.optString("MessageID")
                                , jsonObject.optString("Message")
                                , jsonObject.optString("Direction")
                                , jsonObject.optString("MessageTime").replace("/Date(", "").replace(")/", "")
                                , jsonObject.optString("ProductID"));
                        if(jsonObject.optString("Direction").equals("in")){
                            msgIncomingflag=true;
                        }
                    }
                }
                messageIDs=messageIDs.substring(0,messageIDs.length() - 1);//Removing last comma
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
            }
            else {
                //Acknowledging------------
                new UpdateDeliveryStatus().execute();

                //Notification----------------
                if(msgIncomingflag){
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Services.this);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    mBuilder.setContentTitle(getResources().getString(R.string.msg_notification));
                    //mBuilder.setContentText("");
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    mBuilder.setSound(alarmSound);
                    Intent resultIntent= new Intent(Services.this, Chat.class);
                    PendingIntent resultPendingIntent =PendingIntent.getActivity(Services.this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(5555, mBuilder.build());
                }
            }
        }
    }
    public class UpdateDeliveryStatus extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/UpdateDeliveryStatus";
            HttpURLConnection c = null;
            try {
                postData = "{\"messageIDs\":\"" + messageIDs + "\",\"boutiqueID\":\"" + constants.BoutiqueID + "\",\"person\":\"" + "Customer" + "\"}";
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
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
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
           /* if(!pass) {
                new AlertDialog.Builder(Chat.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //   finish();
                            }
                        }).setCancelable(false).show();
            }*/
        }
    }
}
