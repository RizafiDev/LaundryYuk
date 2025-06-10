package com.firmansyah.laundry.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R

class SplashActivity : BaseActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var appLogo: ImageView
    private lateinit var appName: TextView
    private lateinit var appSubName: TextView
    private val splashTimeOut: Long = 3000 // 3 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi Views
        appLogo = findViewById(R.id.applogo)
        appName = findViewById(R.id.appname)
        appSubName = findViewById(R.id.appsubname)

        // Mulai animasi
        startAnimations()

        // Tampilkan splash selama 3 detik, kemudian cek autentikasi
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthenticationAndNavigate()
        }, splashTimeOut)
    }

    private fun startAnimations() {
        // Animasi fade in + scale untuk logo dengan efek yang lebih dramatis
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        appLogo.startAnimation(logoAnimation)

        // Animasi slide up untuk nama app "LaundryYuk!" (dengan delay)
        val nameAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_animation)
        nameAnimation.startOffset = 600 // delay 0.6 detik
        appName.startAnimation(nameAnimation)

        // Animasi fade in untuk tagline "Bersih Sempurna, Kepercayaan Utama" (dengan delay lebih lama)
        val subtitleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation)
        subtitleAnimation.startOffset = 1200 // delay 1.2 detik untuk tagline
        appSubName.startAnimation(subtitleAnimation)

        // Tambahkan pulsing effect ke logo setelah semua animasi berjalan
        addPulsingEffect()
    }

    private fun addPulsingEffect() {
        Handler(Looper.getMainLooper()).postDelayed({
            val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
            appLogo.startAnimation(pulseAnimation)
        }, 1200) // mulai setelah animasi logo selesai (1.2 detik)
    }

    private fun checkAuthenticationAndNavigate() {
        val currentUser = firebaseAuth.currentUser
        val intent = if (currentUser != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        // Animasi transisi smooth
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}