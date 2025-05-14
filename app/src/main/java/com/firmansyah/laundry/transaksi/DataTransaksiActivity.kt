package com.firmansyah.laundry.transaksi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.R
import android.content.Intent
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.TextView
import com.firmansyah.laundry.layanan.DataLayananActivity
import com.firmansyah.laundry.transaksi.PilihPelangganActivity
import com.firmansyah.laundry.transaksi.PilihLayananActivity



class DataTransaksiActivity : AppCompatActivity() {

    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHp: TextView
    private lateinit var tvNamaLayanan: TextView
    private lateinit var tvHargaLayanan: TextView

    private val pilihPelangganLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val namaPelanggan = data?.getStringExtra("namaPelanggan")
                val noHPPelanggan = data?.getStringExtra("noHPPelanggan")

                // ✅ Set ke TextView
                tvNamaPelanggan.text = "Nama Pelanggan: $namaPelanggan"
                tvNoHp.text = "No HP: $noHPPelanggan"
            }
        }

    private val pilihLayananLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val namaLayanan = data?.getStringExtra("namaLayanan")
                val hargaLayanan = data?.getStringExtra("hargaLayanan")

                // ✅ Set ke TextView
                tvNamaLayanan.text = "Nama Layanan: $namaLayanan"
                tvHargaLayanan.text = "Harga: $hargaLayanan"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transaksi)

        // ✅ Inisialisasi TextView
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan)
        tvNoHp = findViewById(R.id.tv_no_hp)
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan)
        tvHargaLayanan = findViewById(R.id.tv_harga_layanan)

        // Tombol pilih pelanggan
        findViewById<Button>(R.id.btn_pilih_pelanggan).setOnClickListener {
            val intent = Intent(this, PilihPelangganActivity::class.java)
            pilihPelangganLauncher.launch(intent)
        }

        // Tombol pilih layanan
        findViewById<Button>(R.id.btn_pilih_layanan).setOnClickListener {
            val intent = Intent(this, PilihLayananActivity::class.java)
            pilihLayananLauncher.launch(intent)
        }
    }
}
