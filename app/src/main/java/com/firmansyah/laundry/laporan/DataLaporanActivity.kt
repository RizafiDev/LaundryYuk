package com.firmansyah.laundry.laporan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataTransaksiAdapter
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DataLaporanActivity : BaseActivity() {

    private lateinit var rvDataLaporan: RecyclerView
    private lateinit var etSearchLaporan: EditText
    private lateinit var database: FirebaseDatabase
    private lateinit var dataTransaksiAdapter: DataTransaksiAdapter

    private var allTransaksi = mutableListOf<Map<String, Any>>()
    private var filteredTransaksi = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_laporan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupSearch()
        loadTransaksiData()
    }

    private fun initViews() {
        rvDataLaporan = findViewById(R.id.rvDATA_LAPORAN)
        etSearchLaporan = findViewById(R.id.etSearchLaporan)
        database = FirebaseDatabase.getInstance()
    }

    private fun setupRecyclerView() {
        dataTransaksiAdapter = DataTransaksiAdapter(filteredTransaksi)
        rvDataLaporan.apply {
            layoutManager = LinearLayoutManager(this@DataLaporanActivity)
            adapter = dataTransaksiAdapter
        }
    }

    private fun setupSearch() {
        etSearchLaporan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterTransaksi(s.toString())
            }
        })
    }

    private fun filterTransaksi(query: String) {
        filteredTransaksi.clear()

        if (query.isEmpty()) {
            filteredTransaksi.addAll(allTransaksi)
        } else {
            val lowerCaseQuery = query.lowercase()

            allTransaksi.forEach { transaksi ->
                val transactionId = (transaksi["transactionId"] as? String)?.lowercase() ?: ""
                val pelangganData = transaksi["pelanggan"] as? Map<String, Any>
                val namaPelanggan = (pelangganData?.get("nama") as? String)?.lowercase() ?: ""
                val noHp = (pelangganData?.get("noHp") as? String)?.lowercase() ?: ""
                val cabang = (transaksi["cabang"] as? String)?.lowercase() ?: ""
                val karyawanData = transaksi["karyawan"] as? Map<String, Any>
                val namaKaryawan = (karyawanData?.get("nama") as? String)?.lowercase() ?: ""
                val layananData = transaksi["layanan"] as? Map<String, Any>
                val namaLayanan = (layananData?.get("nama") as? String)?.lowercase() ?: ""
                val pembayaranData = transaksi["pembayaran"] as? Map<String, Any>
                val metodePembayaran = (pembayaranData?.get("metodePembayaran") as? String)?.lowercase() ?: ""

                if (transactionId.contains(lowerCaseQuery) ||
                    namaPelanggan.contains(lowerCaseQuery) ||
                    noHp.contains(lowerCaseQuery) ||
                    cabang.contains(lowerCaseQuery) ||
                    namaKaryawan.contains(lowerCaseQuery) ||
                    namaLayanan.contains(lowerCaseQuery) ||
                    metodePembayaran.contains(lowerCaseQuery)) {
                    filteredTransaksi.add(transaksi)
                }
            }
        }

        dataTransaksiAdapter.notifyDataSetChanged()
    }

    private fun loadTransaksiData() {
        val transaksiRef = database.reference.child("transaksi")

        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allTransaksi.clear()

                for (dataSnapshot in snapshot.children) {
                    val transaksi = dataSnapshot.value as? Map<String, Any>
                    transaksi?.let {
                        allTransaksi.add(it)
                    }
                }

                // Sort by timestamp descending (newest first)
                allTransaksi.sortByDescending { transaksi ->
                    (transaksi["timestamp"] as? Long) ?: 0L
                }

                filteredTransaksi.clear()
                filteredTransaksi.addAll(allTransaksi)
                dataTransaksiAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataLaporanActivity", "Error loading transaksi data", error.toException())
                Toast.makeText(this@DataLaporanActivity, "Gagal memuat data transaksi", Toast.LENGTH_SHORT).show()
            }
        })
    }
}