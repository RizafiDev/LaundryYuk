package com.firmansyah.laundry.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelPelanggan(
    var idPelanggan: String? = null,  // Change from val to var
    var namaPelanggan: String? = null,
    var alamatPelanggan: String? = null,
    var noHPPelanggan: String? = null,
    var cabangPelanggan: String? = null,
    var tanggalTerdaftar: String? = null
) : Parcelable