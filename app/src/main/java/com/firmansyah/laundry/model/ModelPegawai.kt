package com.firmansyah.laundry.model

data class ModelPegawai(
    val idPegawai: String = "",
    val namaPegawai: String = "",
    val alamatPegawai: String = "",
    val noHPPegawai: String = "",
    val cabangPegawai: String = "",
    val tanggalTerdaftar: String? = ""
)