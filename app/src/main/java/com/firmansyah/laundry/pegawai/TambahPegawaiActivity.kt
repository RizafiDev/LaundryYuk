package com.firmansyah.laundry.pegawai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPegawai
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahPegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        // Inisialisasi View
        etNama = findViewById(R.id.etTambahNamaPegawai)
        etAlamat = findViewById(R.id.etTambahAlamatPegawai)
        etNoHP = findViewById(R.id.etTambahNoHpPegawai)
        etCabang = findViewById(R.id.etTambahCabangPegawai)
        btSimpan = findViewById(R.id.btnSimpanPegawai)

        // Set event listener untuk tombol simpan
        btSimpan.setOnClickListener {
            validasi()
        }
    }

    private fun validasi() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama_pelanggan)
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = getString(R.string.validasi_alamat_pelanggan)
            etAlamat.requestFocus()
            return
        }
        if (noHP.isEmpty()) {
            etNoHP.error = getString(R.string.validasi_nohp_pelanggan)
            etNoHP.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_cabang_pelanggan)
            etCabang.requestFocus()
            return
        }
        if (!noHP.matches(Regex("\\d{10,13}"))) {
            etNoHP.error = "Nomor HP harus terdiri dari 10-13 angka"
            etNoHP.requestFocus()
            return
        }

        // Jika semua validasi lolos, simpan data
        simpan(nama, alamat, noHP, cabang)
    }

    private fun simpan(nama: String, alamat: String, noHP: String, cabang: String) {
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key ?: return

        // Tambahkan tanggal saat ini
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalTerdaftar = sdf.format(Date())

        val data = ModelPegawai(
            idPegawai = pegawaiId,
            namaPegawai = nama,
            alamatPegawai = alamat,
            noHPPegawai = noHP,
            cabangPegawai = cabang, // Pastikan dikirim
            tanggalTerdaftar = tanggalTerdaftar // Tambahkan tanggal
        )

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.sukses_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.sukses_simpan_pelanggan), Toast.LENGTH_SHORT).show()
            }
    }
}
