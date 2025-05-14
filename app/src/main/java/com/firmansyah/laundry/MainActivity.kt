package com.firmansyah.laundry

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.widget.LinearLayout
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import com.firmansyah.laundry.pegawai.DataPegawaiActivity
import com.firmansyah.laundry.layanan.DataLayananActivity
import com.firmansyah.laundry.laporan.DataLaporanActivity
import com.firmansyah.laundry.transaksi.DataTransaksiActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        val dateTextView: TextView = findViewById(R.id.date)
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        dateTextView.text = currentDate


//        redirect to pelanggan
        val appDataPelanggan = findViewById<LinearLayout>(R.id.AppDataPelanggan)
        appDataPelanggan.setOnClickListener{
            val intent = Intent(this, DataPelangganActivity::class.java)
        startActivity(intent)
        }

//        redirect to pegawai
        val appDataPegawai = findViewById<LinearLayout>(R.id.AppDataPegawai)
        appDataPegawai.setOnClickListener{
            val intent = Intent(this, DataPegawaiActivity::class.java)
            startActivity(intent)
        }

//        redirect to layanan
        val appDataLayanan = findViewById<LinearLayout>(R.id.AppDataLayanan)
        appDataLayanan.setOnClickListener{
            val intent = Intent(this, DataLayananActivity::class.java)
            startActivity(intent)
        }

//        redirect to laporan
        val appDataLaporan = findViewById<LinearLayout>(R.id.AppDataLaporan)
        appDataLaporan.setOnClickListener{
            val intent = Intent(this, DataLaporanActivity::class.java)
            startActivity(intent)
        }

//        rediret to transaksi
        val appDataTransaksi = findViewById<LinearLayout>(R.id.AppDataTransaksi)
        appDataTransaksi.setOnClickListener{
            val intent = Intent(this, DataTransaksiActivity::class.java)
            startActivity(intent)
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}