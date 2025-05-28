package com.firmansyah.laundry.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Periksa apakah ada pengguna yang sudah login
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Pengguna sudah login, langsung ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Pengguna belum login, lanjutkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Tutup SplashActivity agar tidak bisa kembali
        finish()
    }
}
