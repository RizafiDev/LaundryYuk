package com.firmansyah.laundry.pelanggan

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.R
import com.firmansyah.laundry.pegawai.TambahPegawaiActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DataPelangganActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

//        tambah pelanggan
        val tambahDataPelanggan = findViewById<FloatingActionButton>(R.id.TambahPelanggan)
        tambahDataPelanggan.setOnClickListener{
            val intent = Intent(this, TambahPelangganActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}