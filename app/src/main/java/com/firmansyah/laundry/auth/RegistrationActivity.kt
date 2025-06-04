package com.firmansyah.laundry.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.auth.LoginActivity
import com.firmansyah.laundry.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Google Sign-In
    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your web client ID
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase Auth dan Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Tombol Google Sign-In
        binding.btnGoogle.setOnClickListener {
            googleSignIn()
        }

        // Tombol Navigasi ke Login
        binding.tvHelp.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.result
            firebaseAuthWithGoogle(account)
        } else {
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        user?.let { firebaseUser ->
                            // Update profile dengan nama dari Google Account
                            val displayName = account.displayName ?: account.email?.substringBefore("@") ?: "User"
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .build()

                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                                // Simpan data user ke database
                                val userData = hashMapOf(
                                    "uid" to firebaseUser.uid,
                                    "username" to displayName,
                                    "email" to firebaseUser.email,
                                    "createdAt" to ServerValue.TIMESTAMP
                                )

                                val userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)
                                userRef.setValue(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Google Sign-In berhasil!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Gagal menyimpan data pengguna: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        // Tetap pindah ke MainActivity meskipun gagal simpan ke database
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Gagal Sign-In dengan Google: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}