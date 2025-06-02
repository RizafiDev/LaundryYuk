package com.firmansyah.laundry.model

data class PaymentRequest(
    val amount: Int,
    val customerDetails: CustomerDetails,
    val itemDetails: List<ItemDetail>
) {
    data class CustomerDetails(
        val first_name: String,
        val phone: String
    )

    data class ItemDetail(
        val id: String,
        val price: Int,
        val quantity: Int,
        val name: String
    )
}