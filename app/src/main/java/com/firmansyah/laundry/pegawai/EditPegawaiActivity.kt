package com.firmansyah.laundry.pegawai

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPegawai
import com.google.firebase.database.FirebaseDatabase

class EditPegawaiActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private lateinit var pegawai: ModelPegawai
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pegawai)

        // Initialize views
        etNama = findViewById(R.id.etTambahNamaPegawai)
        etAlamat = findViewById(R.id.etTambahAlamatPegawai)
        etNoHP = findViewById(R.id.etTambahNoHpPegawai)
        etCabang = findViewById(R.id.etTambahCabangPegawai)
        btnSimpan = findViewById(R.id.btnSimpanPegawai)

        // Get pegawai data from intent
        pegawai = intent.getParcelableExtra("PEGAWAI_DATA") ?: ModelPegawai()

        // Set initial data
        etNama.setText(pegawai.namaPegawai)
        etAlamat.setText(pegawai.alamatPegawai)
        etNoHP.setText(pegawai.noHPPegawai)
        etCabang.setText(pegawai.cabangPegawai)

        // Initially set fields to readonly
        setFieldsEditable(false)

        btnSimpan.setOnClickListener {
            if (isEditMode) {
                // Save changes
                updatePegawai()
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

    private fun updatePegawai() {
        // Validate inputs
        if (etNama.text.toString().isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }

        if (etAlamat.text.toString().isEmpty()) {
            etAlamat.error = "Alamat tidak boleh kosong"
            return
        }

        if (etNoHP.text.toString().isEmpty()) {
            etNoHP.error = "No HP tidak boleh kosong"
            return
        }

        if (etCabang.text.toString().isEmpty()) {
            etCabang.error = "Cabang tidak boleh kosong"
            return
        }

        val updatedPegawai = ModelPegawai(
            idPegawai = pegawai.idPegawai,
            namaPegawai = etNama.text.toString(),
            alamatPegawai = etAlamat.text.toString(),
            noHPPegawai = etNoHP.text.toString(),
            cabangPegawai = etCabang.text.toString(),
            tanggalTerdaftar = pegawai.tanggalTerdaftar
        )

        database.getReference("pegawai").child(pegawai.idPegawai ?: "")
            .setValue(updatedPegawai)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}