package com.firmansyah.laundry.pegawai

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.R
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import com.firmansyah.laundry.pegawai.TambahPegawaiActivity
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataPegawaiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)

//        add pelanggan
        val tambahDataPegawai = findViewById<FloatingActionButton>(R.id.TambahPegawai)
        tambahDataPegawai.setOnClickListener{
            val intent = Intent(this, TambahPegawaiActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}