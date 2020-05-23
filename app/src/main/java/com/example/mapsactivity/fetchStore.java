package com.example.mapsactivity;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class fetchStore extends AsyncTask<Location,Boolean,List<Store>>{
    private static final String TAG = "fetchStore";
    List<Store> storesByGeo = null;


    @Override
    protected List<Store> doInBackground(Location... lo) {
        Location location = lo[0];
        System.out.println("데이터를 가져오는 중...");
        String url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
        String query = "/storesByGeo/json?lat=" + location.getLatitude() + "&lng=" + location.getLongitude() + "&m=1000";
        System.out.println("--------query---------\n" + url + query);
        Request request = (new Request.Builder().url(url + query)).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("리퀘스트 실패");
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                final String body = response.body().string();
                try {
                    Gson gson = (new GsonBuilder()).create();
                    JsonParser parser = new JsonParser();
                    JsonElement rootObj = parser.parse(body)
                            .getAsJsonObject().get("stores");
                    System.out.println("--------rootObj---------");
                    System.out.println(rootObj.toString());
                    storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>(){}.getType());
                    System.out.println("--------store[0]---------");
                    if(storesByGeo != null){
                        Log.e(TAG,storesByGeo.get(0).getName());

                    }
                    else {
                        System.out.println("--------데이터 없음---------");
                        System.out.println("--------데이터 없음---------");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return storesByGeo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
