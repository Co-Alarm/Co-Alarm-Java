package com.example.mapsactivity;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class PreferenceManager {

    public static final String PREFERENCES_NAME = "PreferenceData";
    public static final String TAG = "PreferenceManager";

    private static final String KEY = "mStore";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    //SP에서 mStore를 불러오는 메소드
    //Gson 사용
    //즐겨찾기 버튼 클릭할 때마다 실행
    public List<FStore> getmStoreFromSP(Context context){
        SharedPreferences prefs = PreferenceManager.getPreferences(context);
        Gson gson = new GsonBuilder().create();
        JsonParser parser = new JsonParser();
        String str = prefs.getString(KEY, null);
        List<FStore> temp;
        if(str == null) return null;
        JsonElement rootObj = parser.parse(str)
                .getAsJsonObject().get("stores");
        temp = gson.fromJson(rootObj, new TypeToken<List<Store>>() {
        }.getType());
        return temp;
    }

    //mStore를 SP에 쓰는 메소드
    //Gson 사용
    public static void setmStoretoSP(Context context, List<FStore> temp){
        SharedPreferences prefs = PreferenceManager.getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new GsonBuilder().create();
        String str = gson.toJson(temp);
        editor.putString(KEY, str);
        editor.commit();
    }
}