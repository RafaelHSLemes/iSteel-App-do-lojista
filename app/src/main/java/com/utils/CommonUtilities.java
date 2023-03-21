package com.utils;


public class CommonUtilities {
    public static final String SERVER = "https://agilemakers.com.br/";
    public static final String SERVER_FOLDER_PATH = "";
    public static final String SERVER_WEBSERVICE_PATH = SERVER_FOLDER_PATH + "webservice_shark.php?";

    public static final String SERVER_URL = SERVER + SERVER_FOLDER_PATH;
    public static final String SERVER_URL_WEBSERVICE = SERVER + SERVER_WEBSERVICE_PATH + "?";
    public static final String SERVER_URL_PHOTOS = SERVER_URL + "webimages/";

    public static final String USER_PHOTO_PATH = CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/";
    public static final String PROVIDER_PHOTO_PATH = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/";
    public static final String COMPANY_PHOTO_PATH = CommonUtilities.SERVER_URL_PHOTOS + "upload/Company/";

    public static String OriginalDateFormate = "dd MMM, yyyy (EEE)";
    public static String OriginalTimeFormate = "hh:mm aa";

    public static final int MY_THERMAL_REQ_CODE = 100;

}
