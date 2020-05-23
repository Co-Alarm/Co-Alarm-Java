package com.example.mapsactivity;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GeocodingFetchTask extends AsyncTask<String,Void, Location> {
    private static final String TAG = "GeocodingFetchTask";

    OkHttpClient client = new OkHttpClient();
    Request request = null;
    Response response = null;

    String clientId = "uyuvw49pig";
    String clientSecret = "BOJCfcK5klMMoKOgtb1LUhwlpCOsxPXZepAAE0Kb";

    @Override
    protected Location doInBackground(String... address) {
        String searchAddr = address[0];
        Location searchLocation = new Location(""); // dummy provider ( 임의의 프로바이더 생성 )
        System.out.println("데이터를 가져오는 중...");
        Intrinsics.checkParameterIsNotNull(address, "address");

        String text = null;
        try {
            text = URLEncoder.encode(String.valueOf(searchAddr), "UTF-8");
            System.out.println(text);
            URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + text);
            request = new Request.Builder().url(url)
                    .addHeader("X-NCP-APIGW-API-KEY-ID", this.clientId)
                    .addHeader("X-NCP-APIGW-API-KEY", this.clientSecret)
                    .method("GET", null)
                    .build();

            response = client.newCall(request).execute();

            // 응답이 성공적이면,
            if(response.isSuccessful()) {
                String body = response.body().string();
                System.out.println("*********************");
                System.out.println("*********************");
                System.out.println("Success to execute request : " + body);
                System.out.println("*********************");
                System.out.println("*********************");
                Gson gson = (new GsonBuilder()).create();
                JsonParser parser = new JsonParser();
                JsonElement rootObj = parser.parse(String.valueOf(body)).getAsJsonObject().get("addresses");
                TypeToken<List<Address>> typeToken = new TypeToken<List<Address>>() {};
                Type type = typeToken.getType();
                ArrayList<Address> addresses = gson.fromJson(rootObj, type);
                if(addresses != null) {
                    double lat = Double.parseDouble(addresses.get(0).getX());
                    double lng = Double.valueOf(addresses.get(0).getY());

                    searchLocation.setLatitude(lat);
                    searchLocation.setLongitude(lng);
                } else {
                    Log.e(TAG, "nullnullnull");
                }
                System.out.println("*********************");
                System.out.println("*********************");

                if (addresses != null) {
                    Address addr = (Address) addresses.get(0);
                    System.out.println(addr);
                }

                System.out.println("*********************");
                System.out.println("*********************");
            } else {
                Log.e(TAG, "failed");
            }
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchLocation;
    }
}
