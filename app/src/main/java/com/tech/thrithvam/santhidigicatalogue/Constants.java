package com.tech.thrithvam.santhidigicatalogue;


public class Constants {
    String AppID="ADB68305-64C5-4181-830F-43BA6210B226";  //Find way to generate this
    //Define which is the boutique
//    String BoutiqueID= "e482b390-cfbb-4bb7-9a7d-60adf5da8d34";//BakeryApp
    String BoutiqueID= "cc08cd6d-1026-4463-8bec-5730bfc5a12c";//SanthiPlastic
//    String BoutiqueID= "5357a4d7-899f-4486-bca9-f6f296c17e64";//SanthiPlastic
    String BoutiqueName="Santhi plastics";
    int MobileNumberMax=10;
    int MobileNumberMin=10;
    String MobileNumberRegularExpression = "^[0-9]*$";
    String UserNameRegularExpression="^[a-zA-Z\\. ]+$";                 //^[a-z0-9_-]{3,15}$
    int UserNameMin=3;
    int productsCountLimit=3;           //0 For no limits
    int relatedProductsCountLimit=0;    //0 For no limits
}
