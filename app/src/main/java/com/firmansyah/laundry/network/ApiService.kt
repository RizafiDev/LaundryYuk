package com.firmansyah.laundry.network

import com.firmansyah.laundry.model.PaymentRequest
import com.firmansyah.laundry.model.PaymentResponse
import com.firmansyah.laundry.model.PaymentStatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/payment/create")
    fun createTransaction(
        @Body paymentRequest: PaymentRequest
    ): Call<PaymentResponse>

    @GET("api/payment/status")
    fun checkTransactionStatus(
        @Query("order_id") orderId: String
    ): Call<PaymentStatusResponse>
}