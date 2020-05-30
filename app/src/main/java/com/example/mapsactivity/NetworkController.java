package com.example.mapsactivity;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

final class NetworkController {

    private static final String TAG = "NetworkController";

    //네트워크 기능: Location을 받아 주변 약국 리스트 리턴
    @Nullable
    List<Store> fetchStore(final Location location) throws IOException {
        List<Store> storesByGeo = null;
        String url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
        String query = "/storesByGeo/json?lat=" + location.getLatitude() + "&lng=" + location.getLongitude() + "&m=1000";
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url + query).get();
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            if(response.body() ==null) return null;
            String body = response.body().string();
            Gson gson = (new GsonBuilder()).create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(body)
                    .getAsJsonObject().get("stores");
            storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>() {
            }.getType());
            if (storesByGeo != null) {
                Log.e(TAG, storesByGeo.get(0).getName());
            }
        } else {
            Log.e(TAG, "failed");
        }
        return storesByGeo;
    }

    //네트워크 기능: 사용자가 입력한 검색어를 받아 해당 위치를 Location으로 리턴
    @Nullable
    final Location fetchGeocoding(@NotNull String address, @NotNull Location location) throws IOException {
        Intrinsics.checkParameterIsNotNull(address, "address");
        String text = URLEncoder.encode(address, "UTF-8");
        URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + text);
        String clientId = "uyuvw49pig";
        String clientSecret = "BOJCfcK5klMMoKOgtb1LUhwlpCOsxPXZepAAE0Kb";
        Request request = new Builder().url(url)
                .addHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .addHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .method("GET", null)
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            if(response.body() ==null) return null;
            String body = response.body().string();
            Gson gson = (new GsonBuilder()).create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(body).getAsJsonObject().get("addresses");
            TypeToken<List<Address>> typeToken = new TypeToken<List<Address>>() {
            };
            Type type = typeToken.getType();
            ArrayList<Address> addresses = gson.fromJson(rootObj, type);
            location.setLatitude(Double.parseDouble(addresses.get(0).getY()));
            location.setLongitude(Double.parseDouble(addresses.get(0).getX()));
            Log.e(TAG,"address success: "+location.getLatitude()+", "+location.getLongitude());
        } else {
            Log.e(TAG, "fetchGeoCoding failed");
            return null;
        }
        return location;
    }
}