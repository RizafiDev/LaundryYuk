package com.firmansyah.laundry.model

data class Laporan(
    val idTransaksi: String = "",
    val namaPelanggan: String = "",
    val noHpPelanggan: String = "",
    val cabang: String = "",
    val namaPegawai: String = "",
    val layanan: String = "",
    val metodePembayaran: String = "",
    val terdaftarTransaksi: String = "",
    val totalBayar: String = ""
)