package com.firmansyah.laundry.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class ModelCabang(
    var idCabang: String? = null,
    var namaCabang: String? = null,
    var alamatCabang: String? = null,
    var provinsiCabang: String? = null,
    var kabKotaCabang: String? = null,
    var noHpCabang: String? = null,
    var tanggalTerdaftar: String? = null
): Parcelable