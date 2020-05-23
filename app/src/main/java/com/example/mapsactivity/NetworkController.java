package com.example.mapsactivity;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
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
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public final class NetworkController {

    String clientId = "uyuvw49pig";
    String clientSecret = "BOJCfcK5klMMoKOgtb1LUhwlpCOsxPXZepAAE0Kb";

    List<Store> fetchStore(Location location){
        List<Store> storesByGeo = null;
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try { ;
                            Gson gson = (new GsonBuilder()).create();
                            JsonParser parser = new JsonParser();
                            JsonElement rootObj = parser.parse(body.toString())
                                    .getAsJsonObject().get("stores");
                            System.out.println("--------rootObj---------");
                            System.out.println(rootObj.toString());
                            // 여기까지 문제없음
//                            Type type = (new TypeToken() {}).getType();
                            List<Store> storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>(){}.getType());
                            System.out.println("--------store[0]---------");
                            if(storesByGeo != null)
                                System.out.println(storesByGeo.get(0).getName());
                            else {
                                System.out.println("--------데이터 없음---------");
                                System.out.println("--------데이터 없음---------");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return storesByGeo;
    }

    @Nullable
    public final Location fetchGeocoding(@NotNull String address) throws UnsupportedEncodingException, MalformedURLException {
        final Location searchLocation = null;
        Intrinsics.checkParameterIsNotNull(address, "address");
        String text = URLEncoder.encode(address, "UTF-8");
        boolean var4 = false;
        System.out.println(text);
        URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + text);
        Request request = new Builder().url(url)
                                       .addHeader("X-NCP-APIGW-API-KEY-ID", this.clientId)
                                       .addHeader("X-NCP-APIGW-API-KEY", this.clientSecret)
                                       .method("GET", (RequestBody)null)
                                       .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue((Callback)(new Callback() {
            public void onResponse(@Nullable Call call, @Nullable Response response) throws IOException {
                String body = response.body().string();

                System.out.println("*********************");
                System.out.println("*********************");
                System.out.println("Success to execute request : " + body);
                System.out.println("*********************");
                System.out.println("*********************");

                Gson gson = (new GsonBuilder()).create();
                JsonParser parser = new JsonParser();

                JsonElement rootObj = parser.parse(String.valueOf(body)).getAsJsonObject().get("addresses");

                TypeToken<List<Address>> typeToken = new TypeToken<List<Address>>(){};
                Type type = typeToken.getType();
                ArrayList<Address> addresses = gson.fromJson(rootObj, type);

                searchLocation.setLatitude(Double.valueOf(addresses.get(0).getX()));
                searchLocation.setLongitude(Double.valueOf(addresses.get(0).getY()));
                System.out.println("*********************");
                System.out.println("*********************");
                if (addresses != null) {
                    Address address = (Address) addresses.get(0);
                    System.out.println(address);
                }
                System.out.println("*********************");
                System.out.println("*********************");
            }

            public void onFailure(@Nullable Call call, @Nullable IOException e) {
                System.out.println("Failed to execute request");
            }
        }));

        return searchLocation;
    }
}