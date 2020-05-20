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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref.ObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
        mv = {1, 1, 16},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 $2\u00020\u00012\u00020\u00022\u00020\u0003:\u0001$B\u0005¢\u0006\u0002\u0010\u0004J\u0018\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0012\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0014J\u0010\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\fH\u0016J\u0012\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0016J\u0018\u0010\u001f\u001a\u00020\u00162\u000e\u0010 \u001a\n\u0012\u0004\u0012\u00020\"\u0018\u00010!H\u0002J\b\u0010#\u001a\u00020\u0016H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.¢\u0006\u0002\n\u0000¨\u0006%"},
        d2 = {"Lcom/example/mapsactivity/MapsActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "Lcom/google/android/gms/maps/OnMapReadyCallback;", "Lcom/google/android/gms/maps/GoogleMap$OnMarkerClickListener;", "()V", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "inputtext", "", "lastLocation", "Landroid/location/Location;", "map", "Lcom/google/android/gms/maps/GoogleMap;", "networkController", "Lcom/example/mapsactivity/NetworkController;", "bitmapDescriptorFromVector", "Lcom/google/android/gms/maps/model/BitmapDescriptor;", "context", "Landroid/content/Context;", "vectorResId", "", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onMapReady", "googleMap", "onMarkerClick", "", "p0", "Lcom/google/android/gms/maps/model/Marker;", "placeMarkerOnMap", "storesByGeo", "", "Lcom/example/mapsactivity/Store;", "setUpMap", "Companion", "app"}
)
public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {
    private GoogleMap map;
    private NetworkController networkController;
    private FusedLocationProviderClient fusedLocationClient;
    private String inputtext;
    private Location lastLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final MapsActivity.Companion Companion = new MapsActivity.Companion((DefaultConstructorMarker)null);
    private HashMap _$_findViewCache;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(1300037);
        Fragment var10000 = this.getSupportFragmentManager().findFragmentById(1000050);
        if (var10000 == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.google.android.gms.maps.SupportMapFragment");
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment)var10000;
            mapFragment.getMapAsync((OnMapReadyCallback)this);
            FusedLocationProviderClient var10001 = LocationServices.getFusedLocationProviderClient((Activity)this);
            Intrinsics.checkExpressionValueIsNotNull(var10001, "LocationServices.getFuse…ationProviderClient(this)");
            this.fusedLocationClient = var10001;
            Button searchbtn = (Button)this.findViewById(1000090);
            searchbtn.setOnClickListener((OnClickListener)(new OnClickListener() {
                public final void onClick(View it) {
                    String var2 = "************************************";
                    boolean var3 = false;
                    System.out.println(var2);
                    MapsActivity var10000 = MapsActivity.this;
                    EditText var10001 = (EditText)MapsActivity.this._$_findCachedViewById(id.entertext);
                    Intrinsics.checkExpressionValueIsNotNull(var10001, "entertext");
                    var10000.inputtext = var10001.getText().toString();
                    var2 = "************************************" + MapsActivity.access$getInputtext$p(MapsActivity.this);
                    var3 = false;
                    System.out.println(var2);
                    MapsActivity.this.networkController = new NetworkController();
                    Location var4 = MapsActivity.access$getNetworkController$p(MapsActivity.this).fetchGeocoding(MapsActivity.access$getInputtext$p(MapsActivity.this));
                }
            }));
        }
    }

    public void onMapReady(@NotNull GoogleMap googleMap) {
        Intrinsics.checkParameterIsNotNull(googleMap, "googleMap");
        this.map = googleMap;
        GoogleMap var10000 = this.map;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("map");
        }

        UiSettings var2 = var10000.getUiSettings();
        Intrinsics.checkExpressionValueIsNotNull(var2, "map.uiSettings");
        var2.setZoomControlsEnabled(true);
        var10000 = this.map;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("map");
        }

        var10000.setOnMarkerClickListener((OnMarkerClickListener)this);
        this.setUpMap();
        var10000 = this.map;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("map");
        }

        var10000.setMyLocationEnabled(true);
        FusedLocationProviderClient var3 = this.fusedLocationClient;
        if (var3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("fusedLocationClient");
        }

        var3.getLastLocation().addOnSuccessListener((Activity)this, (OnSuccessListener)(new OnSuccessListener() {
            // $FF: synthetic method
            // $FF: bridge method
            public void onSuccess(Object var1) {
                this.onSuccess((Location)var1);
            }

            public final void onSuccess(Location location) {
                if (location != null) {
                    MapsActivity.this.lastLocation = location;
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MapsActivity.access$getMap$p(MapsActivity.this).animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16.0F));
                }

                MapsActivity.this.networkController = new NetworkController();
                MapsActivity.access$getNetworkController$p(MapsActivity.this).fetchStore(MapsActivity.access$getLastLocation$p(MapsActivity.this), (Function1)(new Function1() {
                    // $FF: synthetic method
                    // $FF: bridge method
                    public Object invoke(Object var1) {
                        this.invoke((List)var1);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@Nullable List storesByGeo) {
                        MapsActivity.this.placeMarkerOnMap(storesByGeo);
                    }
                }));
            }
        }));
    }

    private final BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
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

    private final void placeMarkerOnMap(List storesByGeo) {
        if (storesByGeo != null) {
            Iterator var3 = storesByGeo.iterator();

            while(var3.hasNext()) {
                final Store store = (Store)var3.next();
                final ObjectRef pinLocation = new ObjectRef();
                pinLocation.element = new LatLng(store.getLat(), store.getLng());
                final ObjectRef remain = new ObjectRef();
                remain.element = store.getRemain_stat();
                this.runOnUiThread((Runnable)(new Runnable() {
                    public final void run() {
                        if (Intrinsics.areEqual((String)remain.element, "plenty")) {
                            MapsActivity.access$getMap$p(MapsActivity.this).addMarker((new MarkerOptions()).position((LatLng)pinLocation.element).title(store.getName()).icon(MapsActivity.this.bitmapDescriptorFromVector((Context)MapsActivity.this, 700134)));
                        } else if (Intrinsics.areEqual((String)remain.element, "some")) {
                            MapsActivity.access$getMap$p(MapsActivity.this).addMarker((new MarkerOptions()).position((LatLng)pinLocation.element).title(store.getName()).icon(MapsActivity.this.bitmapDescriptorFromVector((Context)MapsActivity.this, 700123)));
                        } else if (Intrinsics.areEqual((String)remain.element, "few")) {
                            MapsActivity.access$getMap$p(MapsActivity.this).addMarker((new MarkerOptions()).position((LatLng)pinLocation.element).title(store.getName()).icon(MapsActivity.this.bitmapDescriptorFromVector((Context)MapsActivity.this, 700012)));
                        } else {
                            MapsActivity.access$getMap$p(MapsActivity.this).addMarker((new MarkerOptions()).position((LatLng)pinLocation.element).title(store.getName()).icon(MapsActivity.this.bitmapDescriptorFromVector((Context)MapsActivity.this, 700084)));
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

    public boolean onMarkerClick(@Nullable Marker p0) {
        return false;
    }

    // $FF: synthetic method
    public static final String access$getInputtext$p(MapsActivity $this) {
        String var10000 = $this.inputtext;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("inputtext");
        }

        return var10000;
    }

    // $FF: synthetic method
    public static final NetworkController access$getNetworkController$p(MapsActivity $this) {
        NetworkController var10000 = $this.networkController;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("networkController");
        }

        return var10000;
    }

    // $FF: synthetic method
    public static final Location access$getLastLocation$p(MapsActivity $this) {
        Location var10000 = $this.lastLocation;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("lastLocation");
        }

        return var10000;
    }

    // $FF: synthetic method
    public static final GoogleMap access$getMap$p(MapsActivity $this) {
        GoogleMap var10000 = $this.map;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("map");
        }

        return var10000;
    }

    // $FF: synthetic method
    public static final void access$setMap$p(MapsActivity $this, GoogleMap var1) {
        $this.map = var1;
    }

    public View _$_findCachedViewById(int var1) {
        if (this._$_findViewCache == null) {
            this._$_findViewCache = new HashMap();
        }

        View var2 = (View)this._$_findViewCache.get(var1);
        if (var2 == null) {
            var2 = this.findViewById(var1);
            this._$_findViewCache.put(var1, var2);
        }

        return var2;
    }

    public void _$_clearFindViewByIdCache() {
        if (this._$_findViewCache != null) {
            this._$_findViewCache.clear();
        }

    }

    @Metadata(
            mv = {1, 1, 16},
            bv = {1, 0, 3},
            k = 1,
            d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T¢\u0006\u0002\n\u0000¨\u0006\u0005"},
            d2 = {"Lcom/example/mapsactivity/MapsActivity$Companion;", "", "()V", "LOCATION_PERMISSION_REQUEST_CODE", "", "app"}
    )
    public static final class Companion {
        private Companion() {
        }

        // $FF: synthetic method
        public Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }
    }
}
