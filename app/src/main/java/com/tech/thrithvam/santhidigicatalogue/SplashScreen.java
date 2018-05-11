package com.tech.thrithvam.santhidigicatalogue;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SplashScreen extends AppCompatActivity {
    Constants constants=new Constants();
    DatabaseHandler db;

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        db=DatabaseHandler.getInstance(this);
        startService(new Intent(this, Services.class)); //calling the service
        loadCategories();
        //---------------Making it fullscreen----------------------
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else
        {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        //-----------------Image scrolling---------------------
        ImageView splashImage=(ImageView)findViewById(R.id.splashImage);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width=displaymetrics.widthPixels;
        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(splashImage, "x", 0, -1920 + width);
        Anim1.setDuration(3000);
        Anim1.start();

        //-----------------Titles---------------------------------------
        TextView title=(TextView)findViewById(R.id.title);
        TextView tagLine=(TextView)findViewById(R.id.tagLine);
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/avenirnextregular.ttf");
        title.setTypeface(type);
        type = Typeface.createFromAsset(getAssets(),"fonts/handwriting.ttf");
        tagLine.setTypeface(type);
        ObjectAnimator scaleXb1 = ObjectAnimator.ofFloat(tagLine, "scaleX", 0.75f, 1.0f);
        scaleXb1.setDuration(2000);
        scaleXb1.start();

        //---------------------Moving to next screen--------------------------
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent goHome = new Intent(SplashScreen.this, Home.class);
                goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goHome);
                finish();
                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
            }
        },3000);
    }
    public void loadCategories(){
        if (isOnline()) {
            new GetCategories().execute();
            handler.removeCallbacksAndMessages(null);
        } else {
            handler.postDelayed(new Runnable() {
                public void run() {
                    loadCategories();
                }
            },1000);
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public class GetCategories extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ArrayList<String[]> data=new ArrayList<>();
        //  ProgressDialog pDialog=new ProgressDialog(Home.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*if(cats.size()==0){
                pDialog.setMessage(getResources().getString(R.string.wait));
                pDialog.setCancelable(false);
                pDialog.show();
            }*/
            //  categoryList=new ArrayList<>();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/Categories";
            HttpURLConnection c = null;
            try {
                postData = "{\"boutiqueID\":\"" + constants.BoutiqueID + "\"}";
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
                int i;
                for (i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    data.add(new String[]{jsonObject.optString("CategoryCode"),"\uD83D\uDC49\t"+jsonObject.optString("Name"),jsonObject.optString("OrderNo")});
                }/*
                data.add(new String[]{"trends",getResources().getString(R.string.trending),Integer.toString(i++)});
                data.add(new String[]{"myfav",getResources().getString(R.string.my_favorites),Integer.toString(i)});*/
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
              /*  new AlertDialog.Builder(SplashScreen.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).show();*/
                Intent intentHome = new Intent(SplashScreen.this, NetworkError.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentHome);
                finish();
            }
            else {
                db.updateVarValue("CategoryTable","LOCKED");
                db.flushOldCategories();
                for(int i=0;i<data.size();i++){
                    db.CategoryInsert(data.get(i)[0],data.get(i)[1],Integer.parseInt(data.get(i)[2]));
                }
                db.updateVarValue("CategoryTable","UNLOCKED");
            }
        }
    }
}
