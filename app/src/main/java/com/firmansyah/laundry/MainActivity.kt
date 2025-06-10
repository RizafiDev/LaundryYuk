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
import android.widget.Toast
import com.firmansyah.laundry.cabang.DataCabangActivity
import com.firmansyah.laundry.auth.AkunActivity
import com.firmansyah.laundry.pelanggan.DataPelangganActivity
import com.firmansyah.laundry.pegawai.DataPegawaiActivity
import com.firmansyah.laundry.layanan.DataLayananActivity
import com.firmansyah.laundry.laporan.DataLaporanActivity
import com.firmansyah.laundry.tambahan.DataTambahanActivity
import com.firmansyah.laundry.transaksi.DataTransaksiActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class MainActivity : BaseActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tvWaktu: TextView
    private lateinit var tvNamaUser: TextView
    private lateinit var tvSaldo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        // Store reference to this activity in BaseActivity
        BaseActivity.mainActivityInstance = this

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi TextView
        tvWaktu = findViewById(R.id.tvWaktu)
        tvNamaUser = findViewById(R.id.tvNamaUser)
        tvSaldo = findViewById(R.id.saldo)

        // Set greeting dengan nama user
        setDynamicGreeting()

        // Load saldo dari database pendapatan karyawan
        loadSaldoFromPendapatanKaryawan()

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

        // redirect to akun
        val appDataAkun = findViewById<LinearLayout>(R.id.AppDataAkun)
        appDataAkun.setOnClickListener{
            val intent = Intent(this, AkunActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0) // Menghilangkan transisi
        }

        // redirect to cabang
        val appDataCabang = findViewById<LinearLayout>(R.id.AppDataCabang)
        appDataCabang.setOnClickListener{
            val intent = Intent(this, DataCabangActivity::class.java)
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

    private fun loadSaldoFromPendapatanKaryawan() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val currentUserUid = currentUser.uid

            // Set loading text based on current language
            val loadingText = if (LanguageHelper.getCurrentLanguage(this) == "en") {
                "Loading..."
            } else {
                "Memuat..."
            }
            tvSaldo.text = loadingText

            // Dapatkan bulan dan tahun saat ini
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // +1 karena Calendar.MONTH dimulai dari 0
            val currentYear = calendar.get(Calendar.YEAR)

            // Reference ke node pendapatan_karyawan di database
            val pendapatanRef = FirebaseDatabase.getInstance().getReference("pendapatan_karyawan")

            // Query untuk mendapatkan pendapatan karyawan berdasarkan UID
            pendapatanRef.orderByChild("karyawanUid").equalTo(currentUserUid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalSaldo = 0.0
                        var jumlahTransaksi = 0

                        // Loop melalui semua pendapatan karyawan
                        for (pendapatanSnapshot in snapshot.children) {
                            // Ambil timestamp dari data
                            val timestamp = pendapatanSnapshot.child("timestamp").getValue(Long::class.java)

                            if (timestamp != null) {
                                // Konversi timestamp ke Calendar untuk mendapatkan bulan dan tahun
                                val transactionCalendar = Calendar.getInstance()
                                transactionCalendar.timeInMillis = timestamp

                                val transactionMonth = transactionCalendar.get(Calendar.MONTH) + 1
                                val transactionYear = transactionCalendar.get(Calendar.YEAR)

                                // Filter hanya transaksi bulan dan tahun ini
                                if (transactionMonth == currentMonth && transactionYear == currentYear) {
                                    val jumlahPendapatan = pendapatanSnapshot.child("jumlahPendapatan").getValue(Double::class.java)
                                        ?: pendapatanSnapshot.child("jumlahPendapatan").getValue(Long::class.java)?.toDouble()
                                        ?: pendapatanSnapshot.child("jumlahPendapatan").getValue(Int::class.java)?.toDouble()

                                    if (jumlahPendapatan != null) {
                                        totalSaldo += jumlahPendapatan
                                        jumlahTransaksi++
                                    }
                                }
                            }
                        }

                        // Update TextView dengan format mata uang Indonesia
                        updateSaldoDisplay(totalSaldo, jumlahTransaksi, currentMonth, currentYear)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                        tvSaldo.text = "Rp. 0"
                        val errorMessage = if (LanguageHelper.getCurrentLanguage(this@MainActivity) == "en") {
                            "Failed to load balance: ${error.message}"
                        } else {
                            "Gagal memuat saldo: ${error.message}"
                        }
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            // Jika tidak ada user yang login, redirect ke login
            startActivity(Intent(this, com.firmansyah.laundry.auth.LoginActivity::class.java))
            finish()
        }
    }

    private fun updateSaldoDisplay(saldo: Double, jumlahTransaksi: Int, bulan: Int, tahun: Int) {
        // Format saldo ke mata uang Indonesia
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        val formattedSaldo = "Rp. ${formatter.format(saldo.toLong())}"

        // Nama bulan berdasarkan bahasa
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)
        val namaBulan = if (currentLanguage == "en") {
            arrayOf(
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
        } else {
            arrayOf(
                "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
        }

        // Update TextView dengan informasi bulan
        tvSaldo.text = formattedSaldo

        // Optional: Log untuk debugging dengan info bulan
        println("Pendapatan ${namaBulan[bulan]} $tahun: $formattedSaldo dari $jumlahTransaksi transaksi")
    }

    // Fungsi tambahan untuk mendapatkan pendapatan bulan tertentu
    private fun getPendapatanByMonth(month: Int, year: Int, callback: (Double, Int) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val pendapatanRef = FirebaseDatabase.getInstance().getReference("pendapatan_karyawan")

            pendapatanRef.orderByChild("karyawanUid").equalTo(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var totalSaldo = 0.0
                        var jumlahTransaksi = 0

                        for (pendapatanSnapshot in snapshot.children) {
                            val timestamp = pendapatanSnapshot.child("timestamp").getValue(Long::class.java)

                            if (timestamp != null) {
                                val transactionCalendar = Calendar.getInstance()
                                transactionCalendar.timeInMillis = timestamp

                                val transactionMonth = transactionCalendar.get(Calendar.MONTH) + 1
                                val transactionYear = transactionCalendar.get(Calendar.YEAR)

                                if (transactionMonth == month && transactionYear == year) {
                                    val jumlahPendapatan = pendapatanSnapshot.child("jumlahPendapatan").getValue(Double::class.java)
                                        ?: pendapatanSnapshot.child("jumlahPendapatan").getValue(Long::class.java)?.toDouble()
                                        ?: pendapatanSnapshot.child("jumlahPendapatan").getValue(Int::class.java)?.toDouble()

                                    if (jumlahPendapatan != null) {
                                        totalSaldo += jumlahPendapatan
                                        jumlahTransaksi++
                                    }
                                }
                            }
                        }

                        callback(totalSaldo, jumlahTransaksi)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(0.0, 0)
                    }
                })
        }
    }

    // Fungsi untuk menampilkan nama bulan
    private fun getMonthName(month: Int): String {
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)
        val months = if (currentLanguage == "en") {
            arrayOf(
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
        } else {
            arrayOf(
                "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
        }
        return months[month]
    }

    private fun updateSaldoDisplay(saldo: Double, jumlahTransaksi: Int) {
        // Format saldo ke mata uang Indonesia
        val formatter = NumberFormat.getInstance(Locale("id", "ID"))
        val formattedSaldo = "Rp. ${formatter.format(saldo.toLong())}"

        // Update TextView
        tvSaldo.text = formattedSaldo

        // Optional: Log untuk debugging
        println("Total Saldo Karyawan: $formattedSaldo dari $jumlahTransaksi transaksi")
    }

    // Fungsi untuk refresh saldo secara manual jika diperlukan
    private fun refreshSaldo() {
        loadSaldoFromPendapatanKaryawan()
    }

    private fun getGreetingByTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Check current language and return appropriate greeting
        val currentLanguage = LanguageHelper.getCurrentLanguage(this)

        return if (currentLanguage == "en") {
            when (hour) {
                in 0..11 -> "Good Morning"
                in 12..14 -> "Good Afternoon"
                in 15..17 -> "Good Evening"
                else -> "Good Night"
            }
        } else {
            when (hour) {
                in 0..11 -> "Selamat Pagi"
                in 12..14 -> "Selamat Siang"
                in 15..17 -> "Selamat Sore"
                else -> "Selamat Malam"
            }
        }
    }

    // Fungsi untuk dipanggil saat activity kembali terlihat (optional)
    override fun onResume() {
        super.onResume()
        // Refresh dynamic content when returning to activity
        refreshDynamicContent()
        // Refresh saldo ketika kembali ke activity ini
        refreshSaldo()
    }

    // Add this method to refresh language-dependent content
    private fun refreshDynamicContent() {
        // Refresh greeting text
        setDynamicGreeting()

        // Refresh any other dynamic content that depends on language
        // This ensures that when user changes language and returns to MainActivity,
        // all dynamic text is updated to the new language
    }

    // Add this method to be called from AkunActivity when language changes
    fun refreshLanguageContent() {
        refreshDynamicContent()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    // Clear the reference when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        if (BaseActivity.mainActivityInstance == this) {
            BaseActivity.mainActivityInstance = null
        }
    }

    // Fungsi tambahan untuk mendapatkan detail pendapatan (opsional)
    private fun getDetailPendapatan() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val pendapatanRef = FirebaseDatabase.getInstance().getReference("pendapatan_karyawan")

            pendapatanRef.orderByChild("karyawanUid").equalTo(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val pendapatanList = mutableListOf<Map<String, Any>>()

                        for (pendapatanSnapshot in snapshot.children) {
                            val pendapatanData = pendapatanSnapshot.value as? Map<String, Any>
                            if (pendapatanData != null) {
                                pendapatanList.add(pendapatanData)
                            }
                        }

                        // Urutkan berdasarkan timestamp terbaru
                        pendapatanList.sortByDescending {
                            (it["timestamp"] as? Long) ?: 0L
                        }

                        // Log atau gunakan data sesuai kebutuhan
                        println("Detail Pendapatan: $pendapatanList")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error loading detail pendapatan: ${error.message}")
                    }
                })
        }
    }
}