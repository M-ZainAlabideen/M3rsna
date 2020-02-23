package app.m3resna.classes;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import app.m3resna.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class SessionManager {
    Context context;
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor editor;
    public static final String USER_PREF = "userPreference";
    private static final String IS_LOGGED = "isLogged";
    private static final String USER_ID = "id";
    private static final String LANGUAGE_CODE = "languageCode";
    private static final String USER_TOKEN = "userToken";
    private static final String COUNTRY_ID = "countryId";
    private static final String ARABIC_COUNTRY_NAME = "arabic_country_name";
    private static final String ENGLISH_COUNTRY_NAME = "english_country_name";
    private static final String IS_GUEST = "isGuest";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String PHONE_NUMBER = "phoneNumber";


    public SessionManager(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(USER_PREF, MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void LoginSession() {
        editor.putBoolean(IS_LOGGED, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPref.getBoolean(IS_LOGGED, false);
    }

    public void guestSession() {
        editor.putBoolean(IS_GUEST, true);
        editor.apply();
    }

    public boolean isGuest() {
        return sharedPref.getBoolean(IS_GUEST, false);
    }

    public void guestLogout() {
        editor.putBoolean(IS_GUEST, false);
        editor.apply();
    }

    public void logout() {
        setUserId(0);
        setUserToken(null);
        setCountryId(0);
        setName(null);
        setEmail(null);
        setCountryName(null,null);
        editor.putBoolean(IS_LOGGED, false);
        editor.apply();
    }

    public void setUserToken(String token) {
        editor.putString(USER_TOKEN, "bearer" + " " + token);
        editor.apply();
    }

    public String getUserToken() {
        return sharedPref.getString(USER_TOKEN, "");
    }

    public void setUserId(int id) {
        editor.putInt(USER_ID, id);
        editor.apply();
    }

    public int getUserId() {
        return sharedPref.getInt(USER_ID, 0);
    }

    public void setCountryId(int countryId) {
        editor.putInt(COUNTRY_ID, countryId);
        editor.apply();
    }

    public int getCountryId() {
        return sharedPref.getInt(COUNTRY_ID, 0);
    }

    public void setUserLanguage(String languageCode) {
        editor.putString(LANGUAGE_CODE, languageCode);
        editor.apply();
    }

    public String getUserLanguage() {
        return sharedPref.getString(LANGUAGE_CODE, "");
    }

    public void setName(String name) {
        editor.putString(NAME, name);
        editor.apply();
    }

    public String getName() {
        return sharedPref.getString(NAME, "");
    }


    public void setEmail(String email) {
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return sharedPref.getString(EMAIL, "");
    }


    public void setPhoneNumber(String phoneNumber) {
        editor.putString(PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    public String getPhoneNumber() {
        return sharedPref.getString(PHONE_NUMBER, "");
    }


    public void setCountryName(String arabicCountryName,String englishCountryName) {
        editor.putString(ARABIC_COUNTRY_NAME, arabicCountryName);
        editor.putString(ENGLISH_COUNTRY_NAME, englishCountryName);
        editor.apply();
    }

    public String getCountryName() {
        if (MainActivity.isEnglish)
            return sharedPref.getString(ENGLISH_COUNTRY_NAME, "");
        else
            return sharedPref.getString(ARABIC_COUNTRY_NAME, "");
    }
}
