package com.example.mapsactivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jetbrains.annotations.Nullable;

import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {
    private final static String TAG = "MapsActivity";
    private static GoogleMap map;
    private static NetworkController networkController = new NetworkController();
    public FusedLocationProviderClient fusedLocationClient;
    private static String inputtext = null;
    private static Location lastLocation; // 가장 현위치
    private static Location searchedLocation;
    private static Location currentLocation; //카메라 시점 현위치
    private static Location curLocation;

    private EditText entertext;
    private InputMethodManager imm;
    private static View markerView;
    MarkerItemAdapter markerItemAdapter;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    RecyclerView mRecyclerView = null ;
    RecyclerAdapter mAdapter = null ;
    List<FStore> mStore;
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    Callable<List<Store>> taskSearch = new Callable<List<Store>>() {
        @Override
        public List<Store> call() throws Exception {
            return networkController.fetchStore(searchedLocation);
        }
    };
    Callable<List<Store>> taskLast = new Callable<List<Store>>() {
        @Override
        public List<Store> call() throws Exception {
            return networkController.fetchStore(lastLocation);
        }
    };
    Callable<Location> geocodingtask = new Callable<Location>() {
        @Override
        @Nullable
        public Location call() throws Exception {
            return networkController.fetchGeocoding(inputtext, lastLocation);
        }
    };

    //private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_maps);
            final EditText enterText = this.findViewById(R.id.entertext);

        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.google.android.gms.maps.SupportMapFragment");
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) fragment;
            mapFragment.getMapAsync(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            Intrinsics.checkExpressionValueIsNotNull(fusedLocationClient, "LocationServices.getFuse…ationProviderClient(this)");

            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            mRecyclerView = findViewById(R.id.recyclerview) ;
            mAdapter = new RecyclerAdapter(mStore);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this)) ;
            Button fvb = findViewById(R.id.Fvbtn);
            fvb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) { //map의 북마크 버튼 클릭하면 목록 visibility = true;
                    if(mRecyclerView.getVisibility() == View.VISIBLE) mRecyclerView.setVisibility(View.INVISIBLE);
                    else mRecyclerView.setVisibility(View.GONE);
                }
            });
            final SwipeRefreshLayout swipeRefreshLayout= findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    for(FStore fStore: mStore){
                        if(!fStore.getFavorites()){
                            mStore.remove(fStore);
                            Log.e(TAG,"refresing FV: "+fStore.name);
                        }
                    }
                    //새로 recyclerview를 업데이트해야되는지?
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@

            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            entertext = findViewById(R.id.entertext);

            //토글버튼 기능 ON/OFF
            final ToggleButton tb2 =                    this.findViewById(R.id.btn_search2);
            tb2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(tb2.isChecked()) {
                        entertext.setVisibility(View.VISIBLE);
                    }
                    else{
                        entertext.setVisibility(View.INVISIBLE);
                    } // end if
                } // end onClick()
            });

            //
            entertext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    switch (actionId) {
                        case EditorInfo.IME_ACTION_SEARCH:
                            // 검색 동작
                            System.out.println("************************************");

                            inputtext = entertext.getText().toString();
                            System.out.println("************************************" + inputtext);
                            ExecutorService service = Executors.newSingleThreadExecutor();
                            Future<Location> futurelc = service.submit(geocodingtask);
                            try {


                                searchedLocation = futurelc.get();
                                Future<List<Store>> futurels = service.submit(taskSearch);
                                Log.e(TAG, "searchedLocation");
                                LatLng currentLatLng = new LatLng(searchedLocation.getLatitude(), searchedLocation.getLongitude());
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                                map.clear();
                                placeMarkerOnMap(map, futurels.get());

                                System.out.println("fetchGeocoding 성공: " + searchedLocation.getLatitude() + " " + searchedLocation.getLongitude());

                                //키보드 사라지는 기능 + Toast기능 + edittext 리셋 기능
                                imm.hideSoftInputFromWindow(entertext.getWindowToken(), 0);
                                Toast.makeText(getApplicationContext(), "\""+inputtext +"\""+" 검색결과입니다", Toast.LENGTH_LONG).show();
                                entertext.setText("");
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }


                            break;
                        default:
                            // 기본 엔터키 동작
                            return false;
                    }
                    return true;
                }
            });
        }
    }

    private void setUpMap() {

        //앱에게 위치권한 주는가 물어보는 부분
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled)
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // 앱의 gps 기능 off상태면 on 하도록 설정으로 이동하는 부분
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
        //현재 gps 위치 가져옴
        curLocation = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);

//        //gps값이 안받아와졌을 때, 네트워크좌표로 대체
//        if(curLocation != null){
//            curLocation = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        /* 마커 관련 코드 */
        markerView = getLayoutInflater().inflate(R.layout.marker_view, null);

        map = googleMap;
        Log.e(TAG,"hi");
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
        setUpMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
//                View b = findViewById(R.id.btn_reset);
//                b.setVisibility(View.GONE);

                //이전 마커 지우기
                map.clear();

                // JSON 파싱, 마커생성
                StoreFetchTask storeFetchTask = new StoreFetchTask();
                List<Store> temp = null;
                try {
                    temp = storeFetchTask.execute(curLocation).get();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                placeMarkerOnMap(map, temp);
                return false;
            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //현재 카메라 중앙좌표
                CameraPosition test = map.getCameraPosition();

                //Location으로 변환
                Location cameraLocation = new Location("");
                cameraLocation.setLongitude(test.target.longitude);
                cameraLocation.setLatitude(test.target.latitude);

                currentLocation = cameraLocation;
            }
        });

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location lc) {
                lastLocation = lc;
                Log.e(TAG,"testingonSucceess");
                LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<List<Store>> future = service.submit(taskLast);
                try {
                    placeMarkerOnMap(map, future.get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClick_gps(View v){
        map.clear();
        LatLng latLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        map.animateCamera(cameraUpdate);

        // JSON 파싱, 마커생성
        StoreFetchTask storeFetchTask = new StoreFetchTask();
        List<Store> temp = null;
        try {
            temp = storeFetchTask.execute(curLocation).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        placeMarkerOnMap(map, temp);
    }

    //현위치에서 재검색 버튼 기능
    public void onClick_reset(View v){
        //이전 마커 지우기
        map.clear();

        // JSON 파싱, 마커생성
        StoreFetchTask storeFetchTask = new StoreFetchTask();
        List<Store> temp = null;
        try {
            temp = storeFetchTask.execute(currentLocation).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        placeMarkerOnMap(map, temp);
    }

    //사용자 sgv파일 이용하기 위한 메소드
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
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

    //핀 찍는 기능
    void placeMarkerOnMap(final GoogleMap map, List<Store> storesByGeo) {
        /* 마커 관련 코드 */
        if (storesByGeo != null) {
            for (final Store store : storesByGeo) {
                final LatLng pinLocation = new LatLng(store.getLat(), store.getLng());
                final String remain = store.getRemain_stat();
                if(remain == null)
                    continue;
                this.runOnUiThread(new Runnable() {
                    public final void run() {
                        final MarkerOptions markerOptions = new MarkerOptions();
                        //녹색(100개 이상)/노랑색(30~99개)/빨강색(2~29개)/회색(0~1)개
                        switch (remain) {
                            case "plenty":
                                markerOptions.position(pinLocation);
                                //MarkerOptions의 매개변수에 color를 넣어야함
                                markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_green));
                                break;
                            case "some":
                                markerOptions.position(pinLocation);
                                //MarkerOptions의 매개변수에 color를 넣어야함
                                markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_yellow));
                                break;
                            case "few":
                                markerOptions.position(pinLocation);
                                //MarkerOptions의 매개변수에 color를 넣어야함
                                markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_red));
                                break;
                            default:
                                markerOptions.position(pinLocation);
                                //MarkerOptions의 매개변수에 color를 넣어야함
                                markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_gray));
                                break;
                        }
                        markerItemAdapter = new MarkerItemAdapter(markerView, store.getName(), remainState(remain));
                        map.setInfoWindowAdapter(markerItemAdapter);
                        Log.e(TAG, "in runonuithread : " + store.getName());
                        map.addMarker(markerOptions).showInfoWindow();
                    }
                });
            }
        }
    }

    public static String remainState (String remain) {
        switch (remain) {
            case "plenty":
                return "100개 이상";
            case "some":
                return "30~99개";
            case "few":
                return "2~29개";
            default:
                return "0~1개";
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@
    //markerview의 북마크 버튼 클릭시 실행
    public void addFV_Store(Store store){
        FStore fStore = new FStore();
        fStore.setAddr(store.addr);
        fStore.setCode(store.code);
        fStore.setName(store.name);
        fStore.setFavorites(true);
        Log.e(TAG,"addFV:"+store.name);

        mStore.add(fStore);
    }

    //marckerview에 북마크가 이미 클릭되어 있는 상태에서 클릭되면 실행
    public void deleteFV_Store(Store store){
        String code = store.code;

        for(FStore fStore: mStore){
            if(fStore.getCode().equals(code)){
                mStore.remove(fStore);
                Log.e(TAG,"deleteFV: "+store.name);
                break;
            }
        }
        Log.e(TAG,"if no logcat, no delete");
    }

    //recyclerview에서 북마크 버튼을 클릭하면 실행
//    public void deleteFV_FS(FStore fStore){
//        mStore.remove(fStore);
//        Log.e(TAG,"deleteFV: "+fStore.name);
//    }
    // >>>>>>>>Adapter에서 실행하게 변경함: 삭제해도 ㄱㅊ
    //@@@@@@@@@@@@@@@@@@@@@@@@@@

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
    //onclick_reset이랑 같은 기능을 하는 것 같아서 주석처리
//    public void onLocationChanged(Location location) {
//        Log.e(TAG,"ChangedonSucceess");
//        StoreFetchTask storeFetchTask = new StoreFetchTask();
//
//        // 기존 맵 초기화
//        map.clear();
//
//        // 새로운 위치 객체 설정
//        LatLng changeLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//        Location changeLocation = new Location("");
//        changeLocation.setLongitude(changeLatLng.longitude);
//        changeLocation.setLatitude(changeLatLng.latitude);
//
//        // 변경되는 위치로 이동
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(changeLatLng, 16f));
//
//        // JSON 파싱
//        List<Store> temp = null;
//        try {
//            temp = storeFetchTask.execute(changeLocation).get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        placeMarkerOnMap(temp);
//    }

}