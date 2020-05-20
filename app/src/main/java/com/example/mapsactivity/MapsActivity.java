package com.example.mapsactivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.mapsactivity.R.id;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private static GoogleMap map;
    private static NetworkController networkController;
    private static FusedLocationProviderClient fusedLocationClient;
    private static String inputtext = null;
    private static Location lastLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(id.map);
        if (fragment == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.google.android.gms.maps.SupportMapFragment");
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment)fragment;
            mapFragment.getMapAsync((OnMapReadyCallback)this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient((Activity)this);
            Intrinsics.checkExpressionValueIsNotNull(fusedLocationClient, "LocationServices.getFuse…ationProviderClient(this)");
            Button searchbtn = (Button)this.findViewById(R.id.btn_search);
            searchbtn.setOnClickListener((OnClickListener)(new OnClickListener() {
                public final void onClick(View view) {
                    System.out.println("************************************");
                    EditText entertext = (EditText)view.findViewById(id.entertext);
                    inputtext = entertext.getText().toString();
                    System.out.println("************************************" + inputtext);

                    networkController = new NetworkController();
                    Location searchedLocation = null;
                    try {
                        searchedLocation = networkController.fetchGeocoding(inputtext);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("fetchGeocoding 성...공?"+ searchedLocation.getLatitude() +" "+ searchedLocation.getLongitude());
                }
            }));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
        //map.setUpMap();
        map.setMyLocationEnabled(true);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((Activity)this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    MapsActivity.this.lastLocation = location;
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                }
//                networkController = NetworkController()
//                networkController.fetchStore(lastLocation){storesByGeo :
//                List<Store>? ->
//                    placeMarkerOnMap(storesByGeo)}
            }
        });
    }

    private final BitmapDescriptor bitmapDescriptorFromVector(Runnable context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable((Context) context, vectorResId);
        if (vectorDrawable == null) {
            Intrinsics.throwNpe();
        }
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        BitmapDescriptor var10000 = BitmapDescriptorFactory.fromBitmap(bitmap);
        Intrinsics.checkExpressionValueIsNotNull(var10000, "BitmapDescriptorFactory.fromBitmap(bitmap)");
        return var10000;
    }

    private void placeMarkerOnMap(List storesByGeo ) {
        if (storesByGeo != null) {
            Iterator var3 = storesByGeo.iterator();
            while(var3.hasNext())
            {
                final Store store = (Store)var3.next();
                final LatLng pinLocation = new LatLng(store.getLat(), store.getLng());
                final String remain = store.getRemain_stat();
                this.runOnUiThread((Runnable)(new Runnable() {
                    public final void run() {
                        if (remain == "plenty"){
                            map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                    .position(pinLocation)
                                    .title(store.getName())
                                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_green)));
                        }
                        else if (remain == "some"){
                            map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                    .position(pinLocation)
                                    .title(store.getName())
                                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_yellow)));
                        }
                        else if (remain == "few"){
                            map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                    .position(pinLocation)
                                    .title(store.getName())
                                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_red)));
                        }
                        else
                        {
                            map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                    .position(pinLocation)
                                    .title(store.getName())
                                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_gray)));
                        }
                    }
                }));
            }
        }
    }

    private final void setUpMap() {
        if (ActivityCompat.checkSelfPermission((Context)this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions((Activity)this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
        } else {
            GoogleMap var10000 = this.map;
            if (var10000 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("map");
            }

            var10000.setMyLocationEnabled(true);
            FusedLocationProviderClient var1 = this.fusedLocationClient;
            if (var1 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("fusedLocationClient");
            }

            var1.getLastLocation().addOnSuccessListener((Activity)this, (OnSuccessListener)(new OnSuccessListener() {
                // $FF: synthetic method
                // $FF: bridge method
                public void onSuccess(Object var1) {
                    this.onSuccess((Location)var1);
                }

                public final void onSuccess(Location location) {
                    if (location != null) {
                        MapsActivity.this.lastLocation = location;
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MapsActivity.access$getMap$p(MapsActivity.this).animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12.0F));
                    }

                }
            }));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
