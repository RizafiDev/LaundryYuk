package com.firmansyah.laundry.pegawai

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPegawai
import com.google.firebase.database.FirebaseDatabase


class TambahPegawaiActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val pegawaiRef = database.getReference("pegawai")

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etNama = findViewById(R.id.etTambahNamaPegawai)
        etAlamat = findViewById(R.id.etTambahAlamatPegawai)
        etNoHP = findViewById(R.id.etTambahNoHpPegawai)
        etCabang = findViewById(R.id.etTambahCabangPegawai)
        btSimpan = findViewById(R.id.btnSimpanPegawai)
    }

    private fun setupListeners() {
        btSimpan.setOnClickListener {
            if (validateInput()) {
                val pegawai = createPegawaiFromInput()
                savePegawai(pegawai)
            }
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        var isValid = true

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama_pelanggan)
            etNama.requestFocus()
            isValid = false
        }

        if (alamat.isEmpty()) {
            etAlamat.error = getString(R.string.validasi_alamat_pelanggan)
            etAlamat.requestFocus()
            isValid = false
        }


        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_cabang_pelanggan)
            etCabang.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun createPegawaiFromInput(): ModelPegawai {
        val pegawaiId = pegawaiRef.push().key ?: throw IllegalStateException("Failed to generate ID")

        return ModelPegawai(
            idPegawai = pegawaiId,
            namaPegawai = etNama.text.toString().trim(),
            alamatPegawai = etAlamat.text.toString().trim(),
            noHPPegawai = etNoHP.text.toString().trim(),
            cabangPegawai = etCabang.text.toString().trim()
        )
    }

    private fun savePegawai(pegawai: ModelPegawai) {
        pegawaiRef.child(pegawai.idPegawai).setValue(pegawai)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.sukses_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, getString(R.string.gagal_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error saving employee", exception)
            }
    }

    companion object {
        private const val TAG = "TambahPegawaiActivity"
    }
}