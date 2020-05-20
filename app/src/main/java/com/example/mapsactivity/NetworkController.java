package com.example.mapsactivity;

import android.location.Location;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref.ObjectRef;
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

@Metadata(
        mv = {1, 1, 16},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0007\b\u0016¢\u0006\u0002\u0010\u0002J\u0010\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\u0004J*\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\n2\u001a\u0010\u000f\u001a\u0016\u0012\f\u0012\n\u0012\u0004\u0012\u00020\u0012\u0018\u00010\u0011\u0012\u0004\u0012\u00020\r0\u0010R\u0014\u0010\u0003\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0086D¢\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006¨\u0006\u0013"},
        d2 = {"Lcom/example/mapsactivity/NetworkController;", "", "()V", "clientId", "", "getClientId", "()Ljava/lang/String;", "clientSecret", "getClientSecret", "fetchGeocoding", "Landroid/location/Location;", "address", "fetchStore", "", "location", "completion", "Lkotlin/Function1;", "", "Lcom/example/mapsactivity/Store;", "app"}
)
public final class NetworkController {
    @NotNull
    private final String clientId = "uyuvw49pig";
    @NotNull
    private final String clientSecret = "BOJCfcK5klMMoKOgtb1LUhwlpCOsxPXZepAAE0Kb";

    @NotNull
    public final String getClientId() {
        return this.clientId;
    }

    @NotNull
    public final String getClientSecret() {
        return this.clientSecret;
    }

    public final void fetchStore(@NotNull Location location, @NotNull final Function1 completion) {
        Intrinsics.checkParameterIsNotNull(location, "location");
        Intrinsics.checkParameterIsNotNull(completion, "completion");
        String url = "데이터를 가져 오는 중...";
        boolean var4 = false;
        System.out.println(url);
        url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
        String query = "/storesByGeo/json?lat=" + location.getLatitude() + "&lng=" + location.getLongitude() + "&m=1000";
        String var5 = "--------query---------\n" + url + query;
        boolean var6 = false;
        System.out.println(var5);
        Request request = (new Builder()).url(url + query).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue((Callback)(new Callback() {
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                Intrinsics.checkParameterIsNotNull(call, "call");
                Intrinsics.checkParameterIsNotNull(response, "response");
                ResponseBody var10000 = response.body();
                String body = var10000 != null ? var10000.string() : null;
                if (body == null) {
                    String var4 = "error!";
                    boolean var5 = false;
                    System.out.println(var4);
                }

                String var9;
                boolean var10;
                String var14;
                label20: {
                    Gson gson = (new GsonBuilder()).create();
                    JsonParser parser = new JsonParser();
                    JsonElement rootObj = parser.parse(String.valueOf(body)).getAsJsonObject().get("stores");
                    Type type = (new TypeToken() {
                    }).getType();
                    List storesByGeo = (List)gson.fromJson(rootObj, type);
                    completion.invoke(storesByGeo);
                    var9 = "--------store[0]---------";
                    var10 = false;
                    System.out.println(var9);
                    if (storesByGeo != null) {
                        Store var13 = (Store)storesByGeo.get(0);
                        if (var13 != null) {
                            var14 = var13.getName();
                            break label20;
                        }
                    }

                    var14 = null;
                }

                var9 = var14;
                var10 = false;
                System.out.println(var9);
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Intrinsics.checkParameterIsNotNull(call, "call");
                Intrinsics.checkParameterIsNotNull(e, "e");
                String var3 = "리퀘스트 실패";
                boolean var4 = false;
                System.out.println(var3);
            }
        }));
    }

    @Nullable
    public final Location fetchGeocoding(@NotNull String address) {
        Intrinsics.checkParameterIsNotNull(address, "address");
        String text = URLEncoder.encode(address, "UTF-8");
        final ObjectRef searchlocation = new ObjectRef();
        searchlocation.element = (Location)null;
        boolean var4 = false;
        System.out.println(text);
        URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + text);
        Request request = (new Builder()).url(url).addHeader("X-NCP-APIGW-API-KEY-ID", this.clientId).addHeader("X-NCP-APIGW-API-KEY", this.clientSecret).method("GET", (RequestBody)null).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue((Callback)(new Callback() {
            public void onResponse(@Nullable Call call, @Nullable Response response) {
                String var16;
                label67: {
                    if (response != null) {
                        ResponseBody var10000 = response.body();
                        if (var10000 != null) {
                            var16 = var10000.string();
                            break label67;
                        }
                    }

                    var16 = null;
                }

                String body = var16;
                String var4 = "*********************";
                boolean var5 = false;
                System.out.println(var4);
                var4 = "*********************";
                var5 = false;
                System.out.println(var4);
                var4 = "Success to execute request : " + body;
                var5 = false;
                System.out.println(var4);
                var4 = "*********************";
                var5 = false;
                System.out.println(var4);
                var4 = "*********************";
                var5 = false;
                System.out.println(var4);
                Gson gson = (new GsonBuilder()).create();
                JsonParser parser = new JsonParser();
                JsonElement rootObj = parser.parse(String.valueOf(body)).getAsJsonObject().get("addresses");
                Type type = (new TypeToken() {
                }).getType();
                List addresses = (List)gson.fromJson(rootObj, type);
                Location var17 = (Location)searchlocation.element;
                if (var17 == null) {
                    Intrinsics.throwNpe();
                }

                String var9;
                boolean var10;
                Location var11;
                double var12;
                Address var10001;
                String var18;
                Double var19;
                label61: {
                    if (addresses != null) {
                        var10001 = (Address)addresses.get(0);
                        if (var10001 != null) {
                            var18 = var10001.getX();
                            if (var18 != null) {
                                var9 = var18;
                                var11 = var17;
                                var10 = false;
                                var12 = Double.parseDouble(var9);
                                var17 = var11;
                                var19 = var12;
                                break label61;
                            }
                        }
                    }

                    var19 = null;
                }

                if (var19 == null) {
                    Intrinsics.throwNpe();
                }

                var17.setLongitude(var19);
                var17 = (Location)searchlocation.element;
                if (var17 == null) {
                    Intrinsics.throwNpe();
                }

                label52: {
                    if (addresses != null) {
                        var10001 = (Address)addresses.get(0);
                        if (var10001 != null) {
                            var18 = var10001.getY();
                            if (var18 != null) {
                                var9 = var18;
                                var11 = var17;
                                var10 = false;
                                var12 = Double.parseDouble(var9);
                                var17 = var11;
                                var19 = var12;
                                break label52;
                            }
                        }
                    }

                    var19 = null;
                }

                if (var19 == null) {
                    Intrinsics.throwNpe();
                }

                var17.setLatitude(var19);
                StringBuilder var20 = (new StringBuilder()).append("fetchGeocoding 성공");
                Location var22 = (Location)searchlocation.element;
                if (var22 == null) {
                    Intrinsics.throwNpe();
                }

                var20 = var20.append(var22.getLatitude()).append(" ");
                var22 = (Location)searchlocation.element;
                if (var22 == null) {
                    Intrinsics.throwNpe();
                }

                label42: {
                    var9 = var20.append(var22.getLongitude()).toString();
                    var10 = false;
                    System.out.println(var9);
                    var9 = "*********************";
                    var10 = false;
                    System.out.println(var9);
                    var9 = "*********************";
                    var10 = false;
                    System.out.println(var9);
                    if (addresses != null) {
                        Address var21 = (Address)addresses.get(0);
                        if (var21 != null) {
                            var16 = var21.getX();
                            break label42;
                        }
                    }

                    var16 = null;
                }

                var9 = var16;
                var10 = false;
                System.out.println(var9);
                var9 = "*********************";
                var10 = false;
                System.out.println(var9);
                var9 = "*********************";
                var10 = false;
                System.out.println(var9);
            }

            public void onFailure(@Nullable Call call, @Nullable IOException e) {
                String var3 = "Failed to execute request";
                boolean var4 = false;
                System.out.println(var3);
            }
        }));
        return (Location)searchlocation.element;
    }
}
