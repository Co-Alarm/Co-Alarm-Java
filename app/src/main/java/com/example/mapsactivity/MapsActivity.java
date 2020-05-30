package com.example.mapsactivity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    private static final int PERMISSION_REQUESTS = 1;

    private EditText entertext;
    private InputMethodManager imm;
    boolean isPageOpen = false;
    boolean isImgOpen = false;
    Animation tranlateLeftAnim;
    Animation tranlateRightAnim;
    Animation fade_in;
    Animation fade_out;
    LinearLayout page;
    ImageView imgNotice;
    Button button;
    Button btnHelp;
//    LinearLayout layout1;

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

        page = findViewById(R.id.page);

        //anim 폴더의 애니메이션을 가져와서 준비
        tranlateLeftAnim = AnimationUtils.loadAnimation(this,R.anim.translate_left);
        tranlateRightAnim = AnimationUtils.loadAnimation(this,R.anim.translate_right);

        final FrameLayout.LayoutParams plControl = (FrameLayout.LayoutParams) page.getLayoutParams();

        //페이지 슬라이딩 이벤트가 발생했을때 애니메이션이 시작 됐는지 종료 됐는지 감지할 수 있다.
        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        tranlateLeftAnim.setAnimationListener(animListener);
        tranlateRightAnim.setAnimationListener(animListener);
        button = findViewById(R.id.btnDraw); button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(isPageOpen){
                    page.startAnimation(tranlateRightAnim);
                    plControl.rightMargin = -500;
                    page.setLayoutParams(plControl);
                }
                else{
                    page.startAnimation(tranlateLeftAnim);
                    plControl.rightMargin = 0;
                    page.setLayoutParams(plControl);
                }
            }
        });

        //도움말 버튼 해결
        imgNotice = findViewById(R.id.imgNotice);

        //anim 폴더의 애니메이션을 가져와서 준비
        fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(this,R.anim.fade_out);

        //페이지 슬라이딩 이벤트가 발생했을때 애니메이션이 시작 됐는지 종료 됐는지 감지할 수 있다.
        ImageAnimationListener imgListener = new ImageAnimationListener();
        fade_in.setAnimationListener(imgListener);
        fade_out.setAnimationListener(imgListener);
        btnHelp = findViewById(R.id.btnHelp); btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(isImgOpen){
                    imgNotice.startAnimation(fade_out);
                }
                else{
                    imgNotice.startAnimation(fade_in);
                    imgNotice.setVisibility(View.VISIBLE);
                }
            }
        });

//        최초 실행 여부를 판단 ->>>
//        SharedPreferences pref = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE);
//        boolean checkFirst = pref.getBoolean("checkFirst", false);
//        if(checkFirst==false){
//            // 앱 최초 실행시 하고 싶은 작업
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putBoolean("checkFirst",true);
//            editor.commit();
//
//            Intent intent = new Intent(MapsActivity.this, TutorialActivity.class);
//            startActivity(intent);
//            finish();
//        }else{
//            // 최초 실행이 아닐때 진행할 작업
//        }
//          <<<-


// 세일 주석처리 
//        layout1 = (LinearLayout) findViewById(R.id.menu_bar);
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                /*변경하고 싶은 레이아웃의 파라미터 값을 가져 옴*/
//                LinearLayout.LayoutParams plControl = (LinearLayout.LayoutParams) layout1.getLayoutParams();
//
//            }
//        });

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

//            final ToggleButton btn_menu = (ToggleButton) this.findViewById(R.id.btn_tap);
//            btn_menu.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(btn_menu.isChecked()) {
//                        /*해당 margin값 변경*/
//                        plControl.rightMargin = 0;
//                        /*변경된 값의 파라미터를 해당 레이아웃 파라미터 값에 셋팅*/
//                        menu.setLayoutParams(plControl);
//                    }
//                }
//            });


//            //토글버튼 기능 ON/OFF
//            final ToggleButton tb2 =
//                    (ToggleButton) this.findViewById(R.id.btn_search2);
//            tb2.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(tb2.isChecked()) {
//                        anim(entertext);
//                    }
//                    else{
//                        anim(entertext);
//
//                    } // end if
//                } // end onClick()
//            });



//            final ToggleButton tb3 =
//                    (ToggleButton) this.findViewById(R.id.btn_help);
//            tb3.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(tb3.isChecked()) {
//                        anim(imgNotice);
//                    }
//                    else{
//                        anim(imgNotice);
//                    } // end if
//                } // end onClick()
//            });


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
                                placeMarkerOnMap(futurels.get());

                                System.out.println("fetchGeocoding 성공: " + searchedLocation.getLatitude() + " " + searchedLocation.getLongitude());

                                //키보드 사라지는 기능 + Toast기능 + edittext 리셋 기능
                                imm.hideSoftInputFromWindow(entertext.getWindowToken(), 0);
                                Toast.makeText(getApplicationContext(), "\"" + inputtext + "\"" + " 검색결과입니다", Toast.LENGTH_LONG).show();
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

        }

//        //레이아웃을 위에 겹쳐서 올리는 부분
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        //레이아웃 객체생성
//        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.activity_menu, null);
//
//        //레이아웃 위에 겹치기
//        LinearLayout.LayoutParams paramll = new LinearLayout.LayoutParams
//                (LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
//        addContentView(ll, paramll);
//
//        if (!allPermissionsGranted()) {
//            getRuntimePermissions();
//        }
    }


    private void setUpMap() {
        Log.e(TAG,"h");
        final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        final boolean enabled = Objects.requireNonNull(service)
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            Log.e(TAG,"g");

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
            Log.e(TAG,"hg");

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
                                placeMarkerOnMap(temp);
                            }
                        });
                    }
                }

                @Override
                public void onFinish() { //GPS 켜지지 않고 15초 다 됨

                }
            }.start();
            Log.e(TAG,"h12");

        }
        if(ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            Log.e(TAG,"dfsh");

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
        map = googleMap;
        Log.e(TAG,"hi");
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
        setUpMap();
        setDefaultLocation();
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);


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
                placeMarkerOnMap(temp);

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
                map.setMyLocationEnabled(true);
                if(lc == null) return;
                Log.e(TAG,"testingonSucceess");
                LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                ExecutorService service = Executors.newSingleThreadExecutor();
                Future<List<Store>> future = service.submit(taskLast);
                try {
//                    temp = fTask.execute(lastLocation).get();
                    placeMarkerOnMap(future.get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClick_gps(View v){
        map.clear();
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastLocation = location;
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
                placeMarkerOnMap(temp);
            }
        });
    }

    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        map.moveCamera(cameraUpdate);
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
        placeMarkerOnMap(temp);
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
    void placeMarkerOnMap(List<Store> storesByGeo) {
        if(storesByGeo == null )Log.e(TAG,"thisisfucxingnull");
        if (storesByGeo != null) {
            Log.e(TAG,"isnotnull");
            Log.e(TAG,"is: "+storesByGeo.get(0).getAddr());
            for (final Store store : storesByGeo) {
                final LatLng pinLocation = new LatLng(store.getLat(), store.getLng());
                final String remain = store.getRemain_stat();
                if(remain == null) continue;
                this.runOnUiThread(new Runnable() {
                    public final void run() {
                        switch (remain) {
                            case "plenty":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_green_pin)));
                                break;
                            case "some":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_yellow_pin)));
                                break;
                            case "few":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_red_pin)));
                                break;
                            default:
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_gray_pin)));
                                break;
                        }
                    }
                });
            }
        }
    }

    // 애니메이션 메소드
    public void anim (final View v)
    {
        if(v.getVisibility() == v.GONE) {

            v.setAlpha(0.0f);
            v.setVisibility(View.VISIBLE);
            v.animate().alpha(1.0f).setDuration(600);
            System.out.println("재욱아 재우가앙아ㅏ아아ㅏ");
        }
        else if((v.getVisibility() == v.VISIBLE))
        {
            System.out.println("세일아 세일아아아ㅏㅇ");
            v.animate().alpha(0.0f).setDuration(600).setListener((new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.GONE);

                }
            }));
        }

    }

    public static void wait(int ms){
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
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

    private class SlidingPageAnimationListener implements Animation.AnimationListener{
        @Override public void onAnimationStart(Animation animation) {

        }
        public void onAnimationEnd(Animation animation){
            if(isPageOpen){
                page.setVisibility(View.VISIBLE);
                button.setText("");
                isPageOpen = false;
            }
            else{
                button.setText("");
                isPageOpen = true;
            }
        }
        @Override public void onAnimationRepeat(Animation animation) {

        }
    }

    private class ImageAnimationListener implements Animation.AnimationListener{
        @Override public void onAnimationStart(Animation animation) {

        }
        public void onAnimationEnd(Animation animation){
            if(isImgOpen){
                imgNotice.setVisibility(View.INVISIBLE);
                btnHelp.setText("");
                isImgOpen = false;
            }
            else{
                btnHelp.setText("");
                isImgOpen = true;
            }
        }
        @Override public void onAnimationRepeat(Animation animation) {

        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}