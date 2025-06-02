package com.firmansyah.laundry.model

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("transaction_status") val transactionStatus: String?,
    @SerializedName("order_id") val orderId: String?,
    @SerializedName("gross_amount") val grossAmount: String?,
    @SerializedName("payment_type") val paymentType: String?,
    @SerializedName("transaction_time") val transactionTime: String?,
    @SerializedName("fraud_status") val fraudStatus: String?
)