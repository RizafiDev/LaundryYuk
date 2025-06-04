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
import com.firmansyah.laundry.auth.AkunActivity
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import com.firmansyah.laundry.pegawai.DataPegawaiActivity
import com.firmansyah.laundry.layanan.DataLayananActivity
import com.firmansyah.laundry.laporan.DataLaporanActivity
import com.firmansyah.laundry.tambahan.DataTambahanActivity
import com.firmansyah.laundry.transaksi.DataTransaksiActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tvWaktu: TextView
    private lateinit var tvNamaUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi TextView
        tvWaktu = findViewById(R.id.tvWaktu)
        tvNamaUser = findViewById(R.id.tvNamaUser)


        // Set greeting dengan nama user
        setDynamicGreeting()

        // redirect to pelanggan
        val appDataPelanggan = findViewById<LinearLayout>(R.id.AppDataPelanggan)
        appDataPelanggan.setOnClickListener{
            val intent = Intent(this, DataPelangganActivity::class.java)
            startActivity(intent)
        }

        // redirect to pegawai
        val appDataPegawai = findViewById<LinearLayout>(R.id.AppDataPegawai)
        appDataPegawai.setOnClickListener{
            val intent = Intent(this, DataPegawaiActivity::class.java)
            startActivity(intent)
        }

        // redirect to layanan
        val appDataLayanan = findViewById<LinearLayout>(R.id.AppDataLayanan)
        appDataLayanan.setOnClickListener{
            val intent = Intent(this, DataLayananActivity::class.java)
            startActivity(intent)
        }

        // redirect to laporan
        val appDataLaporan = findViewById<LinearLayout>(R.id.AppDataLaporan)
        appDataLaporan.setOnClickListener{
            val intent = Intent(this, DataLaporanActivity::class.java)
            startActivity(intent)
        }

        // redirect to transaksi
        val appDataTransaksi = findViewById<LinearLayout>(R.id.AppDataTransaksi)
        appDataTransaksi.setOnClickListener{
            val intent = Intent(this, DataTransaksiActivity::class.java)
            startActivity(intent)
        }

        // redirect to tambahan
        val appDataTambahan = findViewById<LinearLayout>(R.id.AppDataTambahan)
        appDataTambahan.setOnClickListener{
            val intent = Intent(this, DataTambahanActivity::class.java)
            startActivity(intent)
        }

        // redirect to akun
        val appDataAkun = findViewById<LinearLayout>(R.id.AppDataAkun)
        appDataAkun.setOnClickListener{
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setDynamicGreeting() {
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            // Ambil nama user dari Firebase Auth
            val userName = currentUser.displayName ?: "User"

            // Tentukan greeting berdasarkan waktu
            val greeting = getGreetingByTime()

            // Set text dengan greeting dan nama user terpisah
            tvWaktu.text = greeting
            tvNamaUser.text = userName
        } else {
            // Jika tidak ada user yang login, redirect ke LoginActivity
            startActivity(Intent(this, com.firmansyah.laundry.auth.LoginActivity::class.java))
            finish()
        }
    }

    private fun getGreetingByTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Selamat Pagi"
            in 12..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }
}