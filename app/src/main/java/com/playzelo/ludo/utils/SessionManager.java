package com.playzelo.ludo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LudoUserSession";
    private static final String KEY_TOKEN = "user_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";



    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveLoginSession(String token, String username, String email) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveToken(String token){
        pref.edit().putString(KEY_TOKEN,token).apply();
    }




    public boolean isLoggerIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, "");
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }
    public String getUserId(){
        return pref.getString(KEY_USER_ID,"");
    }


    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isSessionValid() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
                && getToken() != null && !getToken().isEmpty()
                && getUsername() != null && !getUsername().isEmpty();
    }


}
