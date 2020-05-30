package com.example.mapsactivity;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StoreFetchTask extends AsyncTask<Location,Void, List<Store>> {
    private final static String TAG = "StoreFetchTask";

    private static List<Store> storesByGeo = null;

    @Override
    protected List<Store> doInBackground(Location... lastlocation) {
        Location location = lastlocation[0];
        System.out.println("데이터를 가져오는 중...");
        String url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
        String query = "/storesByGeo/json?lat=" + location.getLatitude() + "&lng=" + location.getLongitude() + "&m=1000";
        System.out.println("--------query---------\n" + url + query);
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url + query).get();
        Request request = builder.build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        if (response.isSuccessful()) {
                String body = null;
                try {
                    assert response.body() != null;
                    body = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Gson gson = (new GsonBuilder()).create();
                JsonParser parser = new JsonParser();
            assert body != null;
            JsonElement rootObj = parser.parse(body)
                        .getAsJsonObject().get("stores");
                storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>() {
                }.getType());
                if (storesByGeo != null) {
                    Log.e(TAG, storesByGeo.get(0).getName());
                }
            }
            else {
                Log.e(TAG, "failed");
            }
        return storesByGeo;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}