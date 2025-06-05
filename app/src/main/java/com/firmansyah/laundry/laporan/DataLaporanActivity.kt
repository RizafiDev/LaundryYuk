package com.firmansyah.laundry.laporan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firmansyah.laundry.adapter.DataLaporanAdapter
import com.firmansyah.laundry.databinding.ActivityDataLaporanBinding
import com.firmansyah.laundry.model.Laporan
import com.google.firebase.database.*



class DataLaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataLaporanBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: DataLaporanAdapter
    private val laporanList = mutableListOf<Laporan>()
    private val filteredLaporanList = mutableListOf<Laporan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("laporan")

        // Setup RecyclerView
        adapter = DataLaporanAdapter(filteredLaporanList)
        binding.rvDATA_LAPORAN.layoutManager = LinearLayoutManager(this)
        binding.rvDATA_LAPORAN.adapter = adapter

        // Fetch data from Firebase
        fetchLaporanData()

        // Setup search functionality
        binding.etSearchLaporan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLaporan(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchLaporanData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                laporanList.clear()
                for (data in snapshot.children) {
                    val laporan = data.getValue(Laporan::class.java)
                    laporan?.let { laporanList.add(it) }
                }
                filteredLaporanList.clear()
                filteredLaporanList.addAll(laporanList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error (e.g., show Toast)
            }
        })
    }

    private fun filterLaporan(query: String) {
        filteredLaporanList.clear()
        if (query.isEmpty()) {
            filteredLaporanList.addAll(laporanList)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredLaporanList.addAll(laporanList.filter {
                it.namaPelanggan.lowercase().contains(lowerCaseQuery) ||
                        it.idTransaksi.lowercase().contains(lowerCaseQuery) ||
                        it.cabang.lowercase().contains(lowerCaseQuery)
            })
        }
        adapter.notifyDataSetChanged()
    }
}