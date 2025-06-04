package com.firmansyah.laundry.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelPegawai(
    var idPegawai: String? = null,
    var namaPegawai: String? = null,
    var alamatPegawai: String? = null,
    var noHPPegawai: String? = null,
    var cabangPegawai: String? = null,
    var tanggalTerdaftar: String? = null
) : Parcelable