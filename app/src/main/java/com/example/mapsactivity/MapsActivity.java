package com.example.mapsactivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.internal.maps.zzt;
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

import java.util.ArrayList;
import java.util.Collections;
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
    private ArrayList<Marker> markerList;

    public static Context mContext = null;
    private EditText entertext;
    private InputMethodManager imm;
    private static View markerView;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    RecyclerView mRecyclerView = null ;
    List<FStore> mStore = new ArrayList<FStore>();
    RecyclerAdapter mAdapter = new RecyclerAdapter(mStore);
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


//    StoreFetchTask fTask = new StoreFetchTask();
//    GeocodingFetchTask gTask = new GeocodingFetchTask();
//    private static List<Store> temp;

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

            //세일세일세일세일세일세일세일세일

            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            entertext = findViewById(R.id.entertext);

            //토글버튼 기능 ON/OFF
            final ToggleButton tb2 =
                    (ToggleButton) this.findViewById(R.id.btn_search2);
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


            //세일세일세일세일세일세일세일세일

//            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//            mRecyclerView = findViewById(R.id.recyclerview) ;
//            mRecyclerView.setAdapter(mAdapter);
//            mRecyclerView.setLayoutManager(new LinearLayoutManager(this)) ;
//            Button fvb = findViewById(R.id.Favorites);
//            fvb.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) { //map의 북마크 버튼 클릭하면 목록 visibility = true;
//                    if(mRecyclerView.getVisibility() == View.VISIBLE) mRecyclerView.setVisibility(View.INVISIBLE);
//                    else mRecyclerView.setVisibility(View.GONE);
//                }
//            });
//            final SwipeRefreshLayout swipeRefreshLayout= findViewById(R.id.swipe_refresh_layout);
//            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                @Override
//                public void onRefresh() {
//                    for(FStore fStore: mStore){
//                        if(!fStore.getFavorites()){
//                            mStore.remove(fStore);
//                            Log.e(TAG,"refresing FV: "+fStore.name);
//                        }
//                    }
//                    //새로 recyclerview를 업데이트해야되는지?
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//            });
//            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@

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

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
//        /* 마커 관련 코드 */
//        markerView = getLayoutInflater().inflate(R.layout.marker_view, null);

        map = googleMap;
        Log.e(TAG,"hi");
        map.getUiSettings().setZoomControlsEnabled(true);
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

//        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
//            @Override
//            public void onCameraMove() {
////                View b = findViewById(R.id.btn_reset);
////                b.setVisibility(View.VISIBLE);
//            }
//        });

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
//                    temp = fTask.execute(lastLocation).get();
                    placeMarkerOnMap(map, future.get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(final Marker marker) {

                AlertDialog.Builder oDialog = new AlertDialog.Builder(MapsActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog);

                oDialog.setMessage("해당 약국을 즐겨찾기에 추가하시겠습니까?")
                        .setTitle("즐겨찾기 추가")
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("Dialog", "취소");
                                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNeutralButton("예", new DialogInterface.OnClickListener()  {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // store 객체를 받아서 parameter로 넘김.
                                Log.e(TAG, "ㅇㅇㅇㅇㅇㅇㅇㅇㅇ : " + ((Store) marker.getTag()).getName());
                                addFV_Store((Store)marker.getTag());

                                Toast.makeText(getApplicationContext(), "확인", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                        .show();
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
    private static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
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
        if (storesByGeo == null) Log.e(TAG, "thisisfucxingnull");
        if (storesByGeo != null) {
            Log.e(TAG, "isnotnull");
            Log.e(TAG, "is: " + storesByGeo.get(0).getAddr());
            for (final Store store : storesByGeo) {
                final LatLng pinLocation = new LatLng(store.getLat(), store.getLng());
                final String remain = store.getRemain_stat();
                if (remain == null)
                    continue;
                this.runOnUiThread(new Runnable() {
                    public final void run() {
                        final MarkerOptions markerOptions;
                        //녹색(100개 이상)/노랑색(30~99개)/빨강색(2~29개)/회색(0~1)개
                        switch (remain) {
                            case "plenty":
                                markerOptions = newMarker(store, pinLocation, R.drawable.ic_green);
                                break;
                            case "some":
                                markerOptions = newMarker(store, pinLocation, R.drawable.ic_yellow);
                                break;
                            case "few":
                                markerOptions = newMarker(store, pinLocation, R.drawable.ic_red);
                                break;
                            default:
                                markerOptions = newMarker(store, pinLocation, R.drawable.ic_gray);
                                break;
                        }
                        map.addMarker(markerOptions).setTag(store);
                    }
                });
            }
        }
    }

    public MarkerOptions newMarker(Store store, LatLng location, int icon) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.title(store.getName());
        markerOptions.snippet(remainState(store.remain_stat));
        // MarkerOptions의 매개변수에 color를 넣어야함
        markerOptions.icon(bitmapDescriptorFromVector(getApplicationContext(), icon));

        return markerOptions;
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
        fStore.setCode(store.code);
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}