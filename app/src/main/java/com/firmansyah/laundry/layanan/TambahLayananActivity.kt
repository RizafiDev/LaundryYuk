package com.firmansyah.laundry.layanan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.model.ModelLayanan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahLayananActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")
    private val cabangRef = database.getReference("cabang")

    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var spinnerCabang: Spinner
    private lateinit var btSimpan: TextView

    private var listCabang = mutableListOf<ModelCabang>()
    private var listNamaCabang = mutableListOf<String>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_layanan)

        // Inisialisasi View
        etNama = findViewById(R.id.etTambahNamaLayanan)
        etHarga = findViewById(R.id.etTambahHargaLayanan)
        spinnerCabang = findViewById(R.id.etTambahCabangLayanan)
        btSimpan = findViewById(R.id.btnSimpanLayanan)

        // Setup Spinner
        setupSpinner()

        // Load data cabang dari Firebase
        loadDataCabang()

        // Set event listener untuk tombol simpan
        btSimpan.setOnClickListener {
            validasi()
        }
    }

    private fun setupSpinner() {
        spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listNamaCabang
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = spinnerAdapter
    }

    private fun loadDataCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                listNamaCabang.clear()

                // Tambahkan item default
                listNamaCabang.add("Pilih Cabang")

                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    cabang?.let {
                        listCabang.add(it)
                        listNamaCabang.add(it.namaCabang ?: "")
                    }
                }
                spinnerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@TambahLayananActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun validasi() {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString().trim()
        val selectedCabangPosition = spinnerCabang.selectedItemPosition

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama)
            etNama.requestFocus()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = getString(R.string.validasi_harga)
            etHarga.requestFocus()
            return
        }
        if (selectedCabangPosition == 0) {
            Toast.makeText(this, getString(R.string.validasi_cabang), Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil nama cabang yang dipilih
        val namaCabang = listNamaCabang[selectedCabangPosition]

        // Jika semua validasi lolos, simpan data
        simpan(nama, harga, namaCabang)
    }

    private fun simpan(nama: String, harga: String, cabang: String) {
        val layananBaru = myRef.push()
        val layananId = layananBaru.key ?: return

        // Tambahkan tanggal saat ini
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalTerdaftar = sdf.format(Date())

        val data = ModelLayanan(
            idLayanan = layananId,
            namaLayanan = nama,
            hargaLayanan = harga,
            cabangLayanan = cabang,
            tanggalTerdaftar = tanggalTerdaftar
        )

        layananBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.sukses_simpan_pelanggan),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.gagal_simpan_pelanggan),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}