package com.firmansyah.laundry.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class ModelTambahan(
    var idLayanan: String? = null,
    var namaLayanan: String? = null,
    var hargaLayanan: String? = null,
    var tanggalTerdaftar: String? = null
) : Parcelable