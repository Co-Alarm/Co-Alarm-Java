package com.example.mapsactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private static final int PERMISSION_REQUESTS = 1;

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
final int PERMISSIONS_REQUEST_CODE = 1;

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

//                        searchedLocation = gTask.execute(inputtext).get();
//                    } catch (ExecutionException | InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("fetchGeocoding 성...공?"+ searchedLocation.getLatitude() +" "+ searchedLocation.getLongitude());
//                    onLocationChanged(searchedLocation);

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

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    public void checkPermission(){
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void setUpMap() {
        final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        final boolean enabled = Objects.requireNonNull(service)
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2.setTitle("GPS 알림")
                    .setMessage("GPS 기능이 꺼져 있습니다. 확인을 누르면 설정으로 이동합니다.")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MapsActivity.this, "GPS 기능을 껐습니다.", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            Toast.makeText(MapsActivity.this, "GPS 기능을 켜 주세요.", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog2 = builder2.create();
            dialog2.show();
            CountDownTimer countDownTimer = new CountDownTimer(15000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(Objects.requireNonNull((LocationManager)getSystemService(LOCATION_SERVICE))
                            .isProviderEnabled(LocationManager.GPS_PROVIDER)){ //GPS 켜짐 감지
                        Log.e(TAG,"GPS 켜짐 감지");
                        map.clear();
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                        fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                curLocation = location;
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                                map.animateCamera(cameraUpdate);

                                // JSON 파싱, 마커생성
                                StoreFetchTask storeFetchTask = new StoreFetchTask();
                                List<Store> temp = null;
                                try {
                                    temp = storeFetchTask.execute(location).get();

                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                placeMarkerOnMap(map, temp);
                            }
                        });
                    }
                }

                @Override
                public void onFinish() { //GPS 켜지지 않고 15초 다 됨

                }
            }.start();
        }
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("위치 접근 권한")
                    .setMessage("위치 접근 권한을 허용해 주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        /* 마커 관련 코드 */

        map = googleMap;
        Log.e(TAG,"hi");
        map.getUiSettings().setZoomControlsEnabled(true);
        setUpMap();
        setDefaultLocation();
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
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
                map.setMyLocationEnabled(true);
                if(lc == null) return;
                lastLocation = lc;
                LatLng currentLatLng = new LatLng(lc.getLatitude(), lc.getLongitude());
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

    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        map.moveCamera(cameraUpdate);
    }

    public void onClick_gps(View v){
        map.clear();
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                curLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                map.animateCamera(cameraUpdate);

                // JSON 파싱, 마커생성
                StoreFetchTask storeFetchTask = new StoreFetchTask();
                List<Store> temp = null;
                try {
                    temp = storeFetchTask.execute(location).get();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                placeMarkerOnMap(map, temp);
            }
        });
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

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return false;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return true;
    }
}