package com.example.mapsactivity;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static com.example.mapsactivity.MapsActivity.fusedLocationClient;

public class fetchtask extends AsyncTask<Location,Void, List<Store>> {
    private final static String TAG = "fetchtask";

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
        if (response.isSuccessful()) {
                String body = null;
                try {
                    body = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Gson gson = (new GsonBuilder()).create();
                JsonParser parser = new JsonParser();
                JsonElement rootObj = parser.parse(body)
                        .getAsJsonObject().get("stores");
                System.out.println("--------rootObj---------");
                System.out.println(rootObj.toString());
                storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>() {
                }.getType());
                System.out.println("--------store[0]---------");
                if (storesByGeo != null) {
                    Log.e(TAG, storesByGeo.get(0).getName());
                }
                else {
                    System.out.println("--------데이터 없음---------");
                    System.out.println("--------데이터 없음---------");
                }
            }
            else {
                Log.e(TAG, "failed");
            }
        return storesByGeo;
    }

//    @Override
//    protected void onPostExecute(List<Store> stores) {
//        getStoresByGeo(stores);
//    }
//    private List<Store> getStoresByGeo(List<Store> storeList){return storeList;}
}