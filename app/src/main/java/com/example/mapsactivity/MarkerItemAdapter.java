package com.example.mapsactivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerItemAdapter implements GoogleMap.InfoWindowAdapter {
    View markerView;
    private String storeName;
    private String storeStock;

    public MarkerItemAdapter(View markerView, String storeName, String storeStock) {
        this.markerView = markerView;
        this.storeName = storeName;
        this.storeStock = storeStock;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreStock() {
        return storeStock;
    }

    public void setStoreStock(String storeStock) {
        this.storeStock = storeStock;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return getInfoContents(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView tvStoreName = markerView.findViewById(R.id.tvStoreName);
        TextView tvStoreStock = markerView.findViewById(R.id.tvStoreStock);
        Button tvStar = markerView.findViewById(R.id.tvStar);

        tvStoreName.setText(storeName);
        tvStoreStock.setText(storeStock);
        return markerView;
    }
}
