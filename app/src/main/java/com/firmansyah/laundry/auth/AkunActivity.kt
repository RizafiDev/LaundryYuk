package com.firmansyah.laundry.auth

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.LanguageHelper
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import com.firmansyah.laundry.transaksi.DataTransaksiActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.*

class AkunActivity : BaseActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var toggleContainer: FrameLayout
    private lateinit var toggleThumb: View
    private lateinit var labelID: TextView
    private lateinit var labelEN: TextView
    private lateinit var tvNamaUser: TextView
    private lateinit var tvDataTerdaftarUser: TextView
    private lateinit var tvAvatarInitials: TextView
    private var isEnglish = false
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)
        enableEdgeToEdge()

        // Initialize views
        initializeViews()

        // Setup language toggle
        setupLanguageToggle()

        setupNavigationListeners()

        firebaseAuth = FirebaseAuth.getInstance()

        // Load user profile data
        loadUserProfile()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoutButton = findViewById<LinearLayout>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun initializeViews() {
        toggleContainer = findViewById(R.id.toggleContainer)
        toggleThumb = findViewById(R.id.toggleThumb)
        labelID = findViewById(R.id.labelID)
        labelEN = findViewById(R.id.labelEN)
        tvNamaUser = findViewById(R.id.tvNamaUser)
        tvDataTerdaftarUser = findViewById(R.id.tvDataTerdaftarUser)
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials)
    }

    private fun loadUserProfile() {
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            // Get user name
            val displayName = currentUser.displayName ?: "Unknown User"
            val email = currentUser.email ?: ""

            // Update name
            tvNamaUser.text = displayName.uppercase()

            // Generate initials for avatar
            val initials = generateInitials(displayName)
            tvAvatarInitials.text = initials

            // Get registration date (creation timestamp)
            val creationTimestamp = currentUser.metadata?.creationTimestamp
            if (creationTimestamp != null) {
                val joinDate = formatJoinDate(creationTimestamp)
                val joinText = if (isEnglish) "Joined on $joinDate" else "Bergabung pada $joinDate"
                tvDataTerdaftarUser.text = joinText
            } else {
                // Fallback if timestamp is not available
                val joinText = if (isEnglish) "Member since registration" else "Anggota sejak pendaftaran"
                tvDataTerdaftarUser.text = joinText
            }
        } else {
            // Handle case when user is not logged in
            tvNamaUser.text = if (isEnglish) "GUEST USER" else "PENGGUNA TAMU"
            tvAvatarInitials.text = "GU"
            tvDataTerdaftarUser.text = if (isEnglish) "Not logged in" else "Belum masuk"
        }
    }

    private fun generateInitials(fullName: String): String {
        if (fullName.isBlank()) return "UN" // Unknown

        val words = fullName.trim().split("\\s+".toRegex())

        return when {
            words.size >= 2 -> {
                // Take first letter of first name and first letter of last name
                "${words[0].first().uppercaseChar()}${words[words.size - 1].first().uppercaseChar()}"
            }
            words.size == 1 -> {
                // If only one word, take first two letters or duplicate first letter
                val word = words[0]
                if (word.length >= 2) {
                    "${word[0].uppercaseChar()}${word[1].uppercaseChar()}"
                } else {
                    "${word[0].uppercaseChar()}${word[0].uppercaseChar()}"
                }
            }
            else -> "UN" // Fallback
        }
    }

    private fun formatJoinDate(timestamp: Long): String {
        val date = Date(timestamp)
        val locale = if (isEnglish) Locale.ENGLISH else Locale("id", "ID")

        return if (isEnglish) {
            val formatter = SimpleDateFormat("MMMM dd, yyyy", locale)
            formatter.format(date)
        } else {
            val formatter = SimpleDateFormat("dd MMMM yyyy", locale)
            formatter.format(date)
        }
    }

    private fun setupNavigationListeners() {
        val appDataMain = findViewById<LinearLayout>(R.id.AppDataMain)
        appDataMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        val appDataTransaksi = findViewById<LinearLayout>(R.id.AppDataTransaksi)
        appDataTransaksi.setOnClickListener {
            val intent = Intent(this, DataTransaksiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupLanguageToggle() {
        // Load saved language preference
        loadLanguagePreference()

        // Update UI based on current language (without animation on initial load)
        updateToggleUI(animateThumb = false)

        // Set click listener for toggle
        toggleContainer.setOnClickListener {
            toggleLanguage()
        }

        isInitialLoad = false
    }

    private fun loadLanguagePreference() {
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)
        isEnglish = currentLanguage == "en"

        // Apply saved language
        LanguageHelper.setAppLanguage(this, currentLanguage)
    }

    private fun toggleLanguage() {
        isEnglish = !isEnglish
        val newLanguage = if (isEnglish) "en" else "id"

        // Save preference
        LanguageHelper.saveLanguagePreference(this, newLanguage)

        // Apply language
        LanguageHelper.setAppLanguage(this, newLanguage)

        // Update UI with animation
        updateToggleUI(animateThumb = true)

        // Show feedback message in the new language
        showLanguageChangeMessage()

        // Update user profile text with new language
        loadUserProfile()

        // Recreate activity with delay to show animation
        recreateActivityWithNewLanguage()
    }

    private fun showLanguageChangeMessage() {
        val message = if (isEnglish) "Language changed to English" else "Bahasa diubah ke Indonesia"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateToggleUI(animateThumb: Boolean = false) {
        // Update toggle background
        val backgroundRes = if (isEnglish) R.drawable.toggle_bg_on else R.drawable.toggle_bg_off
        toggleContainer.background = ContextCompat.getDrawable(this, backgroundRes)

        // Update label colors
        val activeColor = ContextCompat.getColor(this, R.color.primary_color)
        val inactiveColor = ContextCompat.getColor(this, R.color.card_color_dark)

        if (isEnglish) {
            labelID.setTextColor(inactiveColor)
            labelEN.setTextColor(activeColor)
        } else {
            labelID.setTextColor(activeColor)
            labelEN.setTextColor(inactiveColor)
        }

        // Update toggle state
        toggleContainer.isSelected = isEnglish

        // Update thumb position
        val density = resources.displayMetrics.density
        val moveDistance = (32 * density).toInt() // 32dp in pixels
        val targetX = if (isEnglish) moveDistance.toFloat() else 0f

        if (animateThumb && !isInitialLoad) {
            // Animate thumb movement
            val animator = ObjectAnimator.ofFloat(toggleThumb, "translationX", toggleThumb.translationX, targetX)
            animator.duration = 200
            animator.start()
        } else {
            // Set thumb position immediately (for initial load or recreation)
            toggleThumb.translationX = targetX
        }
    }

    private fun recreateActivityWithNewLanguage() {
        // Small delay to allow animation to complete
        toggleContainer.postDelayed({
            // Notify MainActivity to refresh its content if it exists
            BaseActivity.mainActivityInstance?.refreshLanguageContent()
            recreate()
        }, 250)
    }

    private fun logoutUser() {
        firebaseAuth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            val message = if (isEnglish) "Successfully logged out" else "Berhasil logout"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user profile when activity resumes
        loadUserProfile()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }
}