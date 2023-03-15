package com.example.socialapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager{
    private final SharedPreferences shr;
    public PreferenceManager(Context context){
        shr = context.getSharedPreferences(Constants.KEY_PREFERENCE,Context.MODE_PRIVATE);
    }

    public String getString(String key){
        return shr.getString(key,"");
    }

    public Boolean getBoolean(String key){
        return shr.getBoolean(key,false);
    }

    public void putString(String key , String value){
        SharedPreferences.Editor editor = shr.edit();
        editor.putString(key , value);
        editor.apply();
    }

    public void putBoolean(String key , Boolean value){
        SharedPreferences.Editor editor = shr.edit();
        editor.putBoolean(key , value);
        editor.apply();
    }

    public void putInt(String key , int value){
        SharedPreferences.Editor editor = shr.edit();
        editor.putInt(key , value);
        editor.apply();
    }

    public int getInt(String key){
        return shr.getInt(key,-1);
    }

    public void clearPreference(){
        SharedPreferences.Editor editor = shr.edit();
        editor.clear();
        editor.apply();
    }
}
