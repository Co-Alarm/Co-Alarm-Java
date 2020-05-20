package com.example.mapsactivity

import java.lang.reflect.Constructor

data class Store(
    val addr: String,
    val code: String,
    val created_at: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val remain_stat: String,
    val stock_at: String,
    val type: String
)

data class StoresByGeo(
    val stores: List<Store>
)

data class Addresses (
    val addresses: List<Address>
)

data class Address(
    val distance: Double,
    val englishAddress: String,
    val jibunAddress: String,
    val roadAddress: String,
    val x: String,
    val y: String
)