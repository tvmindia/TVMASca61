package com.tech.thrithvam.bakeryapp;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "BakeryApp.db";
    private SQLiteDatabase db;
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private static DatabaseHandler dbInstance = null;
    public static DatabaseHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (dbInstance == null) {
            dbInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return dbInstance;
    }
    // Creating Tables
    // IMPORTANT: if you are changing anything in the below function onCreate(), DO DELETE THE DATABASE file in
    // the emulator or uninstall the application in the phone, to run the application
    @Override
    public void onCreate(SQLiteDatabase db) {
        //----------------Variables------------------------------
        String CREATE_VARIABLES_TABLE = "CREATE TABLE IF NOT EXISTS Variables (variable TEXT PRIMARY KEY, value TEXT);";//store the variables in the program
        db.execSQL(CREATE_VARIABLES_TABLE);
        db.execSQL("insert into variables values ('CategoryTable','UNLOCKED');");//store notifications IDs
        //---------------Tables----------------------------------
        String CREATE_USER_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS UserAccount (UserID TEXT PRIMARY KEY, Address TEXT);";
        db.execSQL(CREATE_USER_ACCOUNTS_TABLE);
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE IF NOT EXISTS Notifications (NotificationIDs TEXT, ExpiryDate DATE);";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        String CREATE_CHAT_TABLE = "CREATE TABLE IF NOT EXISTS Chat (MsgIDs TEXT PRIMARY KEY, Msg TEXT, Direction TEXT, MsgTime DATETIME, ProductID TEXT);";
        db.execSQL(CREATE_CHAT_TABLE);
        //Category Items-----
        String CREATE_CATEGORY_TABLE = "CREATE TABLE IF NOT EXISTS Category (CatCode TEXT PRIMARY KEY, Category TEXT,OrderNo NUMBER);";
        db.execSQL(CREATE_CATEGORY_TABLE);
        //Cart----
        String CREATE_CART_TABLE = "CREATE TABLE IF NOT EXISTS Cart (ProductID TEXT, TypeCode TEXT, TypeDescription TEXT, Quantity TEXT, Price TEXT, ProductNo TEXT,ProductName TEXT,ProductImage TEXT, TakenOrder INTEGER PRIMARY KEY AUTOINCREMENT, UNIQUE (ProductID, TypeCode));";
        db.execSQL(CREATE_CART_TABLE);
        //Locally storing Requests and responses----
        String CREATE_LOCALRESPONSE_TABLE = "CREATE TABLE IF NOT EXISTS Responses (URL TEXT,Request TEXT, Response TEXT, RequestIdentifier TEXT NULL,UsedFlag TEXT NULL, PRIMARY KEY (URL, Request));";
        db.execSQL(CREATE_LOCALRESPONSE_TABLE);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME );
        // Create tables again
        onCreate(db);
    }
    //---------------------Variables Table------------------
    public String getVarValue(String var)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select (value) from Variables where variable='"+var+"';",null);
        if (cursor != null)
        {cursor.moveToFirst();//db.close();
            String value=cursor.getString(cursor.getColumnIndex("value"));
            cursor.close();
            return value;
        }
        else{
            //db.close();
            cursor.close();
            return "";
        }

    }

    public void updateVarValue(String var,String val)
    {
        db=this.getWritableDatabase();
        db.execSQL("UPDATE Variables SET value='"+val+"' WHERE variable='"+var+"';");
        //db.close();
    }
    //--------------------------User Accounts-----------------------------
    public void UserLogin(String UserID, String Address)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO UserAccount (UserID,Address) VALUES ('"+UserID+"','"+Address+"');");
        //db.close();
    }
    public void UserLogout()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM UserAccount;");
        db.execSQL("DELETE FROM Chat;");
        //db.close();
    }
    public String GetUserDetail(String detail)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT UserID,Address FROM UserAccount;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            String result=cursor.getString(cursor.getColumnIndex(detail));
            cursor.close();
            return result;
        }
        else {
            cursor.close();
            return null;
        }
    }
    //------------------------Notifications table------------------------------
    public void insertNotificationIDs(String notIds, String date)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO Notifications (NotificationIDs,ExpiryDate) VALUES ('"+notIds+"','"+date+"');");
        //db.close();
    }
    public String getNotificationIDs()
    {
        db=this.getReadableDatabase();
        String nIDs="";
        Cursor cursor = db.rawQuery("SELECT (NotificationIDs) FROM Notifications;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            nIDs=cursor.getString(cursor.getColumnIndex("NotificationIDs"));
            do {
                nIDs=nIDs+","+cursor.getString(cursor.getColumnIndex("NotificationIDs"));
            }while (cursor.moveToNext());
            cursor.close();
            //db.close();
            return nIDs;
        }
        else {
            //db.close();
            cursor.close();
            return "";
        }
    }
    public void flushNotifications()
    {
        db=this.getWritableDatabase();
        long time= System.currentTimeMillis();
        db.execSQL("DELETE FROM Notifications WHERE ExpiryDate<"+time+";");
        //db.close;
    }
    //------------------------------Chat table---------------------------------------
    public void insertMessage(String MsgIDs,String Msg,String Direction,String MsgTime,String ProductID)
    {
        db=this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO Chat (MsgIDs,Msg,Direction,MsgTime,ProductID) VALUES ('"+MsgIDs+"',"+DatabaseUtils.sqlEscapeString(Msg)+",'"+Direction+"','"+MsgTime+"','"+ProductID+"');");
        }
        catch (Exception ex){
        }
        //db.close;
    }
    public ArrayList<String[]> GetMsgs()
    {
        db=this.getReadableDatabase();
        ArrayList<String[]> msgs=new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT Msg,MsgTime,Direction,ProductID FROM Chat ORDER BY MsgTime ASC;",null);
        String productID="";
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                if(!cursor.getString(cursor.getColumnIndex("ProductID")).equals("null") && !productID.equals(cursor.getString(cursor.getColumnIndex("ProductID"))))
                {
                    String[] data = new String[4];
                    data[0] = "$$NewProduct$$";
                    data[1] = "null";//cursor.getString(cursor.getColumnIndex("MsgTime"));
                    data[2] = "";
                    data[3] = cursor.getString(cursor.getColumnIndex("ProductID"));
                    msgs.add(data);
                    productID=cursor.getString(cursor.getColumnIndex("ProductID"));
                }
                String[] data = new String[4];
                data[0] = cursor.getString(cursor.getColumnIndex("Msg"));
                data[1] = cursor.getString(cursor.getColumnIndex("MsgTime"));
                data[2] = cursor.getString(cursor.getColumnIndex("Direction"));
                data[3] = cursor.getString(cursor.getColumnIndex("ProductID"));
                msgs.add(data);
            }while (cursor.moveToNext());
            cursor.close();
            //db.close;
            return msgs;
        }
        else
        {
            //db.close;
            cursor.close();
            return msgs;//empty array list to avoid exception in custom adapter
        }
    }
    //--------------------------Category Items-----------------------------
    public void flushOldCategories()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Category");
        //db.close;
    }
    public void CategoryInsert(String CatCode,String Category, int OrderNo)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO Category (CatCode,Category,OrderNo) VALUES ('"+CatCode+"',"+DatabaseUtils.sqlEscapeString(Category)+",'"+OrderNo+"');");
        //db.close;
    }
    public ArrayList<String []> GetCategories()
    {
        db=this.getReadableDatabase();
        ArrayList<String[]> categories=new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT CatCode,Category FROM Category ORDER BY OrderNo ASC;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                String[] data = new String[3];
                data[0] = cursor.getString(cursor.getColumnIndex("CatCode"));
                data[1] = cursor.getString(cursor.getColumnIndex("Category"));
                categories.add(data);
            }while (cursor.moveToNext());
        }
        //db.close;
        cursor.close();
        return categories;
    }
    public String GetCategoryName(String CategoryCode)
    {
        db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Category FROM Category WHERE CatCode=='"+CategoryCode+"';",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            String result=cursor.getString(cursor.getColumnIndex("Category"));
            cursor.close();
            //db.close;
            return result;
        }
        else {
            //db.close;
            cursor.close();
            return "";
        }
    }

    //-----------------------------------Cart------------------------------------------
    public void flushOldCart()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Cart;");
        //db.close;
    }
    public void AddToCart(String ProductID,String TypeCode, String TypeDescription, int Quantity, String Price, String ProductNo,String ProductName,String ProductImage)
    {
            db=this.getWritableDatabase();
            db.execSQL("INSERT INTO Cart (ProductID,TypeCode,TypeDescription,Quantity,Price,ProductNo,ProductName,ProductImage) VALUES ('"+ProductID+"','"+TypeCode+"','"+TypeDescription+"','"+Quantity+"','"+Price+"','"+ProductNo+"','"+ProductName+"','"+ProductImage+"');");
            //db.close;
    }
    public void RemoveFromCart(String ProductID)
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Cart WHERE ProductID='"+ProductID+"';");
        //db.close;
    }
    public ArrayList<String[]> GetCartItems()
    {
        db=this.getReadableDatabase();
        ArrayList<String[]> products=new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT ProductID,TypeCode,TypeDescription,Quantity,Price,ProductNo,ProductName,ProductImage FROM Cart ORDER BY TakenOrder ASC;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                String[] data = new String[8];
                data[0] = cursor.getString(cursor.getColumnIndex("ProductID"));
                data[1] = cursor.getString(cursor.getColumnIndex("TypeCode"));
                data[2] = cursor.getString(cursor.getColumnIndex("TypeDescription"));
                data[3] = cursor.getString(cursor.getColumnIndex("Quantity"));
                data[4] = cursor.getString(cursor.getColumnIndex("Price"));
                data[5] = cursor.getString(cursor.getColumnIndex("ProductNo"));
                data[6] = cursor.getString(cursor.getColumnIndex("ProductName"));
                data[7] = cursor.getString(cursor.getColumnIndex("ProductImage"));
                products.add(data);
            }while (cursor.moveToNext());
        }
        //db.close;
        cursor.close();
        return products;
    }
    public String GetCartItemsJson()
    {
        db=this.getReadableDatabase();
        ArrayList<Map> list = new ArrayList<>();

        Gson gson = new Gson();
        Cursor cursor = db.rawQuery("SELECT ProductID,TypeCode,Quantity,Price FROM Cart ORDER BY TakenOrder ASC;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                Map hashMap = new HashMap();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    hashMap.put(cursor.getColumnName(i),cursor.getString(i));
                }
                list.add(hashMap);
            }while (cursor.moveToNext());
        }
        //db.close;
        cursor.close();
        return gson.toJson(list);
    }
    public void UpdateQuantity(String ProductID,String TypeCode, int Quantity)
    {
        db=this.getWritableDatabase();
        db.execSQL("UPDATE Cart SET Quantity='"+Quantity+"' WHERE ProductID='"+ProductID+"' AND TypeCode='"+TypeCode+"';");
        //db.close();
    }

    //-------------------------------Responses------------------------------------------
  /*  public void flushOldResponses()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Responses");
        //db.close;
    }
    public void flushOldResponses(String URL, String request)
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Responses WHERE URL='"+ URL +"' AND Request='"+request+"'");
        //db.close;
    }*/
    public void flushInvalidResponses(String RequestIdentifier)
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Responses WHERE RequestIdentifier='"+ RequestIdentifier +"';");
        //db.close;
    }
    public void ResponsesSaving(String URL, String Request,String Response, String RequestIdentifier)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO Responses (URL,Request,Response,RequestIdentifier) VALUES ('"+URL+"','"+Request+"','"+Response+"','"+RequestIdentifier+"');");
    }
    public String GetResponses(String URL, String Request)
    {
        db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Response FROM Responses WHERE URL='"+ URL +"' AND Request='"+Request+"';",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            String result=cursor.getString(cursor.getColumnIndex("Response"));
            cursor.close();
            //Flag used
            db=this.getWritableDatabase();
            db.execSQL("UPDATE Responses SET UsedFlag='used' WHERE URL='"+ URL +"' AND Request='"+Request+"';");
            //db.close;
            return result;
        }
        else {
            //db.close;
            cursor.close();
            return "";
        }
    }
    public String[] GetUsedResponses()
    {
        db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT URL,Request FROM Responses WHERE UsedFlag='used' LIMIT 1;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            String result[]=new String[2];
            result[0]=cursor.getString(cursor.getColumnIndex("URL"));
            result[1]=cursor.getString(cursor.getColumnIndex("Request"));
            cursor.close();
            //db.close;
            return result;
        }
        else {
            //db.close;
            cursor.close();
            return null;
        }
    }
    public void ResponsesUpdating(String URL, String Request,String Response)
    {
        db=this.getWritableDatabase();
        db.execSQL("UPDATE Responses SET Response='"+Response+"',UsedFlag='updated' WHERE URL='"+ URL +"' AND Request='"+Request+"';");
    }
}
