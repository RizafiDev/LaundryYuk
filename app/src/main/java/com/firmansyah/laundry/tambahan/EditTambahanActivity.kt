package com.firmansyah.laundry.tambahan

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelTambahan
import com.google.firebase.database.FirebaseDatabase

class EditTambahanActivity : BaseActivity() {
    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private lateinit var tambahan: ModelTambahan
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tambahan)

        try {
            // Initialize views
            etNama = findViewById(R.id.etNamaLayanan)
            etHarga = findViewById(R.id.etHargaLayanan)
            btnSimpan = findViewById(R.id.btnSimpanLayananTambahan)

            // Get tambahan data from intent with null check
            tambahan = intent.getParcelableExtra("TAMBAHAN_DATA") ?: run {
                Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Set initial data with null checks
            etNama.setText(tambahan.namaLayanan ?: "")
            etHarga.setText(tambahan.hargaLayanan ?: "")

            // Initially set fields to readonly
            setFieldsEditable(false)

            btnSimpan.setOnClickListener {
                if (isEditMode) {
                    // Save changes
                    updateTambahan()
                } else {
                    // Switch to edit mode
                    isEditMode = true
                    setFieldsEditable(true)
                    btnSimpan.text = "Simpan"
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etNama.isEnabled = editable
        etHarga.isEnabled = editable
        // Removed etCabang references since it doesn't exist in XML

        val backgroundRes = if (editable) {
            R.drawable.edittext_background_enabled
        } else {
            R.drawable.edittext_background_disabled
        }

        etNama.setBackgroundResource(backgroundRes)
        etHarga.setBackgroundResource(backgroundRes)
    }

    private fun updateTambahan() {
        // Validate inputs
        val namaLayanan = etNama.text.toString().trim()
        val hargaLayanan = etHarga.text.toString().trim()

        if (namaLayanan.isEmpty()) {
            etNama.error = "Nama Layanan tidak boleh kosong"
            etNama.requestFocus()
            return
        }

        if (hargaLayanan.isEmpty()) {
            etHarga.error = "Harga tidak boleh kosong"
            etHarga.requestFocus()
            return
        }

        // Validate that harga is a valid number
        try {
            hargaLayanan.toDouble()
        } catch (e: NumberFormatException) {
            etHarga.error = "Harga harus berupa angka"
            etHarga.requestFocus()
            return
        }

        // Check if idLayanan exists
        val layananId = tambahan.idLayanan
        if (layananId.isNullOrEmpty()) {
            Toast.makeText(this, "ID Layanan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTambahan = ModelTambahan(
            idLayanan = layananId,
            namaLayanan = namaLayanan,
            hargaLayanan = hargaLayanan,
            tanggalTerdaftar = tambahan.tanggalTerdaftar
        )

        // Show loading state
        btnSimpan.isEnabled = false
        btnSimpan.text = "Menyimpan..."

        database.getReference("tambahan").child(layananId)
            .setValue(updatedTambahan)
            .addOnSuccessListener {
                Toast.makeText(this, "Data layanan berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengupdate data: ${exception.message}", Toast.LENGTH_SHORT).show()

                // Reset button state
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
            }
    }
}