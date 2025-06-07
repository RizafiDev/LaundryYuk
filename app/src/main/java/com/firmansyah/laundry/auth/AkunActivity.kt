package com.firmansyah.laundry.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import com.firmansyah.laundry.transaksi.DataTransaksiActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class AkunActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)
        enableEdgeToEdge()
        val appDataMain = findViewById<LinearLayout>(R.id.AppDataMain)
        appDataMain.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0) // Menghilangkan transisi
        }

        // redirect to transaksi
        val appDataTransaksi = findViewById<LinearLayout>(R.id.AppDataTransaksi)
        appDataTransaksi.setOnClickListener{
            val intent = Intent(this, DataTransaksiActivity::class.java)
            startActivity(intent)
        }


        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Tombol logout
        val logoutButton = findViewById<LinearLayout>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        // Logout dari Firebase
        firebaseAuth.signOut()

        // Logout dari Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }
}
