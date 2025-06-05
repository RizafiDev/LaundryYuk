package com.firmansyah.laundry.network

import com.firmansyah.laundry.model.KabupatenKota
import com.firmansyah.laundry.model.Provinsi
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WilayahApiService {

    @GET("provinces.json")
    fun getProvinsi(): Call<List<Provinsi>>

    @GET("regencies/{province_id}.json")
    fun getKabupatenKota(@Path("province_id") provinceId: String): Call<List<KabupatenKota>>
}