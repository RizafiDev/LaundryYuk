package com.firmansyah.laundry.model

import java.io.Serializable

data class ModelTambahan(
    val idLayanan: String? = "",
    val namaLayanan: String? = "",
    val hargaLayanan: String? = "",
    val tanggalTerdaftar: String? = ""
) : Serializable