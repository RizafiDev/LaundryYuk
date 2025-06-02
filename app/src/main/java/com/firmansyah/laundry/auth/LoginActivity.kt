package com.firmansyah.laundry.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // Google Sign-In
    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Replace with your web client ID
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle system bars for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Login button click listener
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Google Login button
        binding.btnGoogle.setOnClickListener {
            googleSignIn()
        }

        // Handle navigation to registration activity (if needed)
        binding.tvHelp.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
    }

    // Function to login with Firebase Email & Password
    private fun loginUser() {
        val email = binding.editTextPhone.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        // Validasi input
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextPhone.error = "Email tidak valid"
            binding.editTextPhone.requestFocus()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            binding.editTextPassword.error = "Password minimal 6 karakter"
            binding.editTextPassword.requestFocus()
            return
        }

        // Login dengan Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login berhasil, pindah ke MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Login gagal
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Google Sign-In Function
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Handling the result of Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.result
            firebaseAuthWithGoogle(account)
        } else {
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Authenticate with Firebase using Google credentials
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login berhasil, pindah ke MainActivity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Login gagal
                        Toast.makeText(this, "Google Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
