package com.firmansyah.laundry.pelanggan

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPelanggan
import com.google.firebase.database.FirebaseDatabase

class EditPelangganActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private lateinit var pelanggan: ModelPelanggan
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pelanggan)

        // Initialize views
        etNama = findViewById(R.id.etTambahNamaPelanggan)
        etAlamat = findViewById(R.id.etTambahAlamatPelanggan)
        etNoHP = findViewById(R.id.etTambahNoHpPelanggan)
        etCabang = findViewById(R.id.etTambahCabangPelanggan)
        btnSimpan = findViewById(R.id.btnSimpanPelanggan)

        // Get pelanggan data from intent
        pelanggan = intent.getParcelableExtra("PELANGGAN_DATA") ?: ModelPelanggan()

        // Set initial data
        etNama.setText(pelanggan.namaPelanggan)
        etAlamat.setText(pelanggan.alamatPelanggan)
        etNoHP.setText(pelanggan.noHPPelanggan)
        etCabang.setText(pelanggan.cabangPelanggan)

        // Initially set fields to readonly
        setFieldsEditable(false)

        btnSimpan.setOnClickListener {
            if (isEditMode) {
                // Save changes
                updatePelanggan()
            } else {
                // Switch to edit mode
                isEditMode = true
                setFieldsEditable(true)
                btnSimpan.text = "Simpan"
            }
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etNama.isEnabled = editable
        etAlamat.isEnabled = editable
        etNoHP.isEnabled = editable
        etCabang.isEnabled = editable

        val backgroundRes = if (editable) {
            R.drawable.edittext_background_enabled
        } else {
            R.drawable.edittext_background_disabled
        }

        etNama.setBackgroundResource(backgroundRes)
        etAlamat.setBackgroundResource(backgroundRes)
        etNoHP.setBackgroundResource(backgroundRes)
        etCabang.setBackgroundResource(backgroundRes)
    }


    private fun updatePelanggan() {
        // Validate inputs
        if (etNama.text.toString().isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }

        val updatedPelanggan = ModelPelanggan(
            idPelanggan = pelanggan.idPelanggan,
            namaPelanggan = etNama.text.toString(),
            alamatPelanggan = etAlamat.text.toString(),
            noHPPelanggan = etNoHP.text.toString(),
            cabangPelanggan = etCabang.text.toString(),
            tanggalTerdaftar = pelanggan.tanggalTerdaftar
        )

        database.getReference("pelanggan").child(pelanggan.idPelanggan ?: "")
            .setValue(updatedPelanggan)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pelanggan berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}