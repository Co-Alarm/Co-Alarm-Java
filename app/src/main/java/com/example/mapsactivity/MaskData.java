package com.example.mapsactivity;

import java.util.List;

class Store {
    String addr;
    String code;
    String created_at;
    Double lat;
    Double lng;
    String name;
    String remain_stat;
    String stock_at;
    String type;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemain_stat() {
        return remain_stat;
    }

    public void setRemain_stat(String remain_stat) {
        this.remain_stat = remain_stat;
    }

    public String getStock_at() {
        return stock_at;
    }

    public void setStock_at(String stock_at) {
        this.stock_at = stock_at;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
class StoresByGeo{
    List<Store> stores;

    public List<Store> getStores() {
        return stores;
    }

    public void setStores(List<Store> stores) {
        this.stores = stores;
    }
}

class Addresses {
    List<Address> addresses;

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}

class Address{
    Double distance;
    String englishAddress;
    String jibunAddress;
    String roadAddress;
    String x;
    String y;

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getEnglishAddress() {
        return englishAddress;
    }

    public void setEnglishAddress(String englishAddress) {
        this.englishAddress = englishAddress;
    }

    public String getJibunAddress() {
        return jibunAddress;
    }

    public void setJibunAddress(String jibunAddress) {
        this.jibunAddress = jibunAddress;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
