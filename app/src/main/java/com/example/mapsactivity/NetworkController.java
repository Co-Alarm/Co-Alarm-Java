package com.example.mapsactivity;
import android.location.Location;
import android.util.Log;

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
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Request.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NetworkController {

    private static final String TAG = "NetworkController";
    String clientId = "uyuvw49pig";
    String clientSecret = "BOJCfcK5klMMoKOgtb1LUhwlpCOsxPXZepAAE0Kb";

//    List<Store> fetchStore(final Location location) throws IOException {
//        List<Store> storesByGeo = null;
//        System.out.println("데이터를 가져오는 중...");
//        String url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
//        String query = "/storesByGeo/json?lat=" + location.getLatitude() + "&lng=" + location.getLongitude() + "&m=1000";
//        System.out.println("--------query---------\n" + url + query);
//        OkHttpClient client = new OkHttpClient();
//        Request.Builder builder = new Request.Builder().url(url + query).get();
//        Request request = builder.build();
//        Response response = client.newCall(request).execute();
//        if (response.isSuccessful()) {
//            String body = response.body().string();
//            Gson gson = (new GsonBuilder()).create();
//            JsonParser parser = new JsonParser();
//            JsonElement rootObj = parser.parse(body)
//                    .getAsJsonObject().get("stores");
//            System.out.println("--------rootObj---------");
//            System.out.println(rootObj.toString());
//            storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>() {
//            }.getType());
//            System.out.println("--------store[0]---------");
//            if (storesByGeo != null) {
//                Log.e(TAG, storesByGeo.get(0).getName());
//            } else {
//                System.out.println("--------데이터 없음---------");
//                System.out.println("--------데이터 없음---------");
//            }
//        } else {
//            Log.e(TAG, "failed");
//        }
//        return storesByGeo;
//    }

//    List<Store> fetchStore(final Location location) throws ExecutionException, InterruptedException {
//        System.out.println("데이터를 가져오는 중...");
//        String url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
//        String query = "/storesByGeo/json?lat=" + location.getLatitude() + "&lng=" + location.getLongitude() + "&m=1000";
//        System.out.println("--------query---------\n" + url + query);
//        final Request request = (new Request.Builder().url(url + query)).build();
//        Callable<List<Store>> task = new Callable<List<Store>>() {
//            @Override
//            public List<Store> call() throws Exception {
//                List<Store> storesByGeo = null;
//                new OkHttpClient().newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        System.out.println("리퀘스트 실패");
//                    }
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
//                        final String body = response.body().string();
//                        try {
//                            Gson gson = (new GsonBuilder()).create();
//                            JsonParser parser = new JsonParser();
//                            JsonElement rootObj = parser.parse(body)
//                                    .getAsJsonObject().get("stores");
//                            System.out.println("--------rootObj---------");
//                            System.out.println(rootObj.toString());
//                            List<Store> storesByGeo = gson.fromJson(rootObj, new TypeToken<List<Store>>(){}.getType());
//                            System.out.println("--------store[0]---------");
//                            if(storesByGeo != null){
//                                Log.e(TAG,storesByGeo.get(0).getName());
//                            }
//                            else {
//                                System.out.println("--------데이터 없음---------");
//                                System.out.println("--------데이터 없음---------");
//                            }
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                });
//
//            }
//        };
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        Future<List<Store>> future = executorService.submit(task);
//        return future.get();
//
////        try {
////            return futureResult;
////            Log.e(TAG,"goingtoSuccess");
////        } catch (Exception e) {
////            Log.e(TAG,"catch");
////        }
//
//    }



    @Nullable
    public final Location fetchGeocoding(@NotNull String address) throws UnsupportedEncodingException, MalformedURLException {
        final Location searchLocation = null;
        Intrinsics.checkParameterIsNotNull(address, "address");
        String text = URLEncoder.encode(address, "UTF-8");
        System.out.println(text);
        URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + text);
        Request request = new Builder().url(url)
                .addHeader("X-NCP-APIGW-API-KEY-ID", this.clientId)
                .addHeader("X-NCP-APIGW-API-KEY", this.clientSecret)
                .method("GET", null)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
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
                String var3 = "Failed to execute request";
                boolean var4 = false;
                System.out.println(var3);
            }
        });
        return searchLocation;
    }
}