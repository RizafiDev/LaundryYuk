package com.firmansyah.laundry.pelanggan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.model.ModelPelanggan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahPelangganActivity : BaseActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")
    private val cabangRef = database.getReference("cabang")

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: Spinner
    private lateinit var btSimpan: TextView

    private var listCabang = mutableListOf<ModelCabang>()
    private var listNamaCabang = mutableListOf<String>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pelanggan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi View
        etNama = findViewById(R.id.etTambahNamaPelanggan)
        etAlamat = findViewById(R.id.etTambahAlamatPelanggan)
        etNoHP = findViewById(R.id.etTambahNoHpPelanggan)
        etCabang = findViewById(R.id.etTambahCabangPelanggan)
        btSimpan = findViewById(R.id.btnSimpanPelanggan)

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
        etCabang.adapter = spinnerAdapter
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
                    this@TambahPelangganActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun validasi() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHP = etNoHP.text.toString().trim()
        val selectedCabangPosition = etCabang.selectedItemPosition

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
        if (selectedCabangPosition == 0) {
            Toast.makeText(this, getString(R.string.validasi_cabang_pelanggan), Toast.LENGTH_SHORT).show()
            return
        }
        if (!noHP.matches(Regex("\\d{10,13}"))) {
            etNoHP.error = "Nomor HP harus terdiri dari 10-13 angka"
            etNoHP.requestFocus()
            return
        }

        // Ambil nama cabang yang dipilih
        val namaCabang = listNamaCabang[selectedCabangPosition]

        // Jika semua validasi lolos, simpan data
        simpan(nama, alamat, noHP, namaCabang)
    }

    private fun simpan(nama: String, alamat: String, noHP: String, cabang: String) {
        val pelangganBaru = myRef.push()
        val pelangganId = pelangganBaru.key ?: return

        // Tambahkan tanggal saat ini
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalTerdaftar = sdf.format(Date())

        val data = ModelPelanggan(
            idPelanggan = pelangganId,
            namaPelanggan = nama,
            alamatPelanggan = alamat,
            noHPPelanggan = noHP,
            cabangPelanggan = cabang,
            tanggalTerdaftar = tanggalTerdaftar
        )

        pelangganBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.sukses_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_pelanggan), Toast.LENGTH_SHORT).show()
            }
    }
}