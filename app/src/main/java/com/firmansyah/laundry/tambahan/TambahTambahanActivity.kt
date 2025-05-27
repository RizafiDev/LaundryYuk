package com.firmansyah.laundry.tambahan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelTambahan
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahTambahanActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("tambahan")

    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var btnSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_tambahan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNama = findViewById(R.id.etNamaLayananTambahan)
        etHarga = findViewById(R.id.etHargaLayananTambahan)
        btnSimpan = findViewById(R.id.btnSimpanTambahan)

        btnSimpan.setOnClickListener {
            validasi()
        }
    }

    private fun validasi() {
        val nama = etNama.text.toString().trim()
        val hargaStr = etHarga.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama layanan harus diisi"
            etNama.requestFocus()
            return
        }
        if (hargaStr.isEmpty()) {
            etHarga.error = "Harga layanan harus diisi"
            etHarga.requestFocus()
            return
        }

        val harga = hargaStr.toIntOrNull()
        if (harga == null) {
            etHarga.error = "Harga harus berupa angka"
            etHarga.requestFocus()
            return
        }

        simpan(nama, harga)
    }

    private fun simpan(nama: String, harga: Int) {
        val tambahanBaru = myRef.push()
        val tambahanId = tambahanBaru.key ?: return

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalDitambahkan = sdf.format(Date())

        val data = ModelTambahan(
            idLayanan = tambahanId,
            namaLayanan = nama,
            hargaLayanan = harga.toString(),  // Ubah Int ke String di sini
            tanggalTerdaftar = tanggalDitambahkan
        )

        tambahanBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil simpan layanan tambahan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal simpan layanan tambahan", Toast.LENGTH_SHORT).show()
            }
    }

}
