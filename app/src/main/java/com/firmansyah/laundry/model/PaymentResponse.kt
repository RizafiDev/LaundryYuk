package com.firmansyah.laundry.model

data class PaymentResponse(
    val success: Boolean,
    val message: String? = null,
    val data: PaymentData
) {
    data class PaymentData(
        val token: String,
        val redirect_url: String,
        val payment_type: String? = null,
        val order_id: String
    )
}