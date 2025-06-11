package com.firmansyah.laundry.laporan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
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
    private lateinit var spinnerPeriode: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerCabang: Spinner
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalTransactions: TextView
    private lateinit var tvFilterInfo: TextView

    private lateinit var database: FirebaseDatabase
    private lateinit var dataTransaksiAdapter: DataTransaksiAdapter

    private var allTransaksi = mutableListOf<Map<String, Any>>()
    private var filteredTransaksi = mutableListOf<Map<String, Any>>()
    private var allCabang = mutableSetOf<String>()

    private lateinit var filterHeader: LinearLayout
    private lateinit var filterContent: LinearLayout
    private lateinit var iconExpand: ImageView
    private lateinit var tvFilterSummary: TextView
    private lateinit var btnClearFilters: TextView
    private var isFilterExpanded = false

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

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
        setupSpinners()
        setupRecyclerView()
        setupSearch()
        setupFilterListeners()
        loadTransaksiData()
    }

    private fun initViews() {
        rvDataLaporan = findViewById(R.id.rvDATA_LAPORAN)
        etSearchLaporan = findViewById(R.id.etSearchLaporan)
        spinnerPeriode = findViewById(R.id.spinnerPeriode)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerCabang = findViewById(R.id.spinnerCabang)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions)

        // Filter UI elements
        filterHeader = findViewById(R.id.filterHeader)
        filterContent = findViewById(R.id.filterContent)
        iconExpand = findViewById(R.id.iconExpand)
        tvFilterSummary = findViewById(R.id.tvFilterSummary)
        btnClearFilters = findViewById(R.id.btnClearFilters)

        database = FirebaseDatabase.getInstance()

        // Setup filter expand/collapse functionality
        setupFilterToggle()
    }

    private fun setupFilterToggle() {
        filterHeader.setOnClickListener {
            toggleFilterVisibility()
        }

        btnClearFilters.setOnClickListener {
            clearAllFilters()
        }
    }
    private fun toggleFilterVisibility() {
        if (isFilterExpanded) {
            // Collapse the filter
            filterContent.visibility = View.GONE
            iconExpand.setImageResource(R.drawable.plus_solid)
            isFilterExpanded = false
        } else {
            // Expand the filter
            filterContent.visibility = View.VISIBLE
            iconExpand.setImageResource(R.drawable.minus_solid) // You'll need to add this drawable
            isFilterExpanded = true
        }

        // Add rotation animation to the icon
        iconExpand.animate()
            .rotation(if (isFilterExpanded) 45f else 0f)
            .setDuration(200)
            .start()
    }

    // Method to clear all filters
    private fun clearAllFilters() {
        // Clear search text
        etSearchLaporan.setText("")

        // Reset spinners to first position (Semua...)
        spinnerPeriode.setSelection(0)
        spinnerStatus.setSelection(0)
        spinnerCabang.setSelection(0)

        // Update filter summary
        updateFilterSummary()

        // Apply filters to refresh data
        applyFilters()
    }
    private fun setupSpinners() {
        // Setup Period Spinner
        val periodeItems = arrayOf("Semua Periode", "Hari Ini", "Minggu Ini", "Bulan Ini", "Bulan Lalu")
        val periodeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodeItems)
        periodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriode.adapter = periodeAdapter

        // Setup Status Spinner
        val statusItems = arrayOf("Semua Status", "Sudah Diambil", "Belum Diambil")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusItems)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // Setup Cabang Spinner (will be populated after data loads)
        val cabangItems = mutableListOf("Semua Cabang")
        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cabangItems)
        cabangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = cabangAdapter
    }

    private fun setupRecyclerView() {
        dataTransaksiAdapter = DataTransaksiAdapter(filteredTransaksi) { transactionId ->
            updateStatusDiambil(transactionId)
        }
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
                applyFilters()
            }
        })
    }

    private fun setupFilterListeners() {
        spinnerPeriode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCabang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyFilters() {
        filteredTransaksi.clear()

        val searchQuery = etSearchLaporan.text.toString().lowercase().trim()
        val selectedPeriode = spinnerPeriode.selectedItem?.toString() ?: "Semua Periode"
        val selectedStatus = spinnerStatus.selectedItem?.toString() ?: "Semua Status"
        val selectedCabang = spinnerCabang.selectedItem?.toString() ?: "Semua Cabang"

        // Get date range based on period filter
        val (startDate, endDate) = getDateRange(selectedPeriode)

        allTransaksi.forEach { transaksi ->
            // Apply search filter
            if (searchQuery.isNotEmpty() && !matchesSearchQuery(transaksi, searchQuery)) {
                return@forEach
            }

            // Apply period filter
            if (selectedPeriode != "Semua Periode") {
                val tanggalTransaksi = transaksi["tanggalTransaksi"] as? String
                if (tanggalTransaksi != null) {
                    try {
                        val transactionDate = dateFormat.parse(tanggalTransaksi)
                        if (transactionDate == null || transactionDate.before(startDate) || transactionDate.after(endDate)) {
                            return@forEach
                        }
                    } catch (e: Exception) {
                        return@forEach
                    }
                }
            }

            // Apply status filter
            if (selectedStatus != "Semua Status") {
                val statusDiambil = transaksi["statusDiambil"] as? Boolean ?: false
                val expectedStatus = when (selectedStatus) {
                    "Sudah Diambil" -> true
                    "Belum Diambil" -> false
                    else -> statusDiambil
                }
                if (statusDiambil != expectedStatus) {
                    return@forEach
                }
            }

            // Apply cabang filter
            if (selectedCabang != "Semua Cabang") {
                val cabang = transaksi["cabang"] as? String ?: ""
                if (cabang != selectedCabang) {
                    return@forEach
                }
            }

            filteredTransaksi.add(transaksi)
        }

        // Sort by timestamp descending (newest first)
        filteredTransaksi.sortByDescending { transaksi ->
            (transaksi["timestamp"] as? Long) ?: 0L
        }

        dataTransaksiAdapter.notifyDataSetChanged()
        updateTotalIncome()
        updateFilterSummary() // Add this line
    }

    private fun matchesSearchQuery(transaksi: Map<String, Any>, query: String): Boolean {
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

        return transactionId.contains(query) ||
                namaPelanggan.contains(query) ||
                noHp.contains(query) ||
                cabang.contains(query) ||
                namaKaryawan.contains(query) ||
                namaLayanan.contains(query) ||
                metodePembayaran.contains(query)
    }

    private fun getDateRange(periode: String): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        when (periode) {
            "Hari Ini" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return Pair(calendar.time, endDate)
            }
            "Minggu Ini" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return Pair(calendar.time, endDate)
            }
            "Bulan Ini" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return Pair(calendar.time, endDate)
            }
            "Bulan Lalu" -> {
                val endLastMonth = Calendar.getInstance()
                endLastMonth.set(Calendar.DAY_OF_MONTH, 1)
                endLastMonth.add(Calendar.DAY_OF_MONTH, -1)
                endLastMonth.set(Calendar.HOUR_OF_DAY, 23)
                endLastMonth.set(Calendar.MINUTE, 59)
                endLastMonth.set(Calendar.SECOND, 59)

                val startLastMonth = Calendar.getInstance()
                startLastMonth.time = endLastMonth.time
                startLastMonth.set(Calendar.DAY_OF_MONTH, 1)
                startLastMonth.set(Calendar.HOUR_OF_DAY, 0)
                startLastMonth.set(Calendar.MINUTE, 0)
                startLastMonth.set(Calendar.SECOND, 0)
                startLastMonth.set(Calendar.MILLISECOND, 0)

                return Pair(startLastMonth.time, endLastMonth.time)
            }
            else -> {
                // Return a very wide range for "Semua Periode"
                calendar.set(2020, 0, 1) // Start from 2020
                return Pair(calendar.time, endDate)
            }
        }
    }

    private fun updateTotalIncome() {
        var totalIncome = 0L
        var totalTransactions = filteredTransaksi.size

        filteredTransaksi.forEach { transaksi ->
            val pembayaranData = transaksi["pembayaran"] as? Map<String, Any>
            val totalPembayaran = when (val total = pembayaranData?.get("totalPembayaran")) {
                is Number -> total.toLong()
                else -> 0L
            }
            totalIncome += totalPembayaran
        }

        tvTotalIncome.text = formatRupiah(totalIncome)
        tvTotalTransactions.text = "$totalTransactions Transaksi"
    }

    private fun updateFilterInfo() {
        val selectedPeriode = spinnerPeriode.selectedItem?.toString() ?: "Semua Periode"
        val selectedStatus = spinnerStatus.selectedItem?.toString() ?: "Semua Status"
        val selectedCabang = spinnerCabang.selectedItem?.toString() ?: "Semua Cabang"
        val searchQuery = etSearchLaporan.text.toString().trim()

        val filters = mutableListOf<String>()

        if (selectedPeriode != "Semua Periode") filters.add(selectedPeriode)
        if (selectedStatus != "Semua Status") filters.add(selectedStatus)
        if (selectedCabang != "Semua Cabang") filters.add(selectedCabang)
        if (searchQuery.isNotEmpty()) filters.add("Pencarian")

        // Update filter summary in the header
        updateFilterSummary()
    }

    private fun updateFilterSummary() {
        val selectedPeriode = spinnerPeriode.selectedItem?.toString() ?: "Semua Periode"
        val selectedStatus = spinnerStatus.selectedItem?.toString() ?: "Semua Status"
        val selectedCabang = spinnerCabang.selectedItem?.toString() ?: "Semua Cabang"
        val searchQuery = etSearchLaporan.text.toString().trim()

        val activeFilters = mutableListOf<String>()

        if (selectedPeriode != "Semua Periode") activeFilters.add(selectedPeriode)
        if (selectedStatus != "Semua Status") activeFilters.add(selectedStatus)
        if (selectedCabang != "Semua Cabang") activeFilters.add(selectedCabang)
        if (searchQuery.isNotEmpty()) activeFilters.add("Pencarian")

        val summaryText = if (activeFilters.isEmpty()) {
            "Semua Data"
        } else {
            "${activeFilters.size} Filter Aktif"
        }

        tvFilterSummary.text = summaryText

        // Change background color based on active filters
        if (activeFilters.isEmpty()) {
            tvFilterSummary.setBackgroundResource(R.drawable.button_background) // Default background
        } else {
            tvFilterSummary.setBackgroundResource(R.drawable.badge_active_background) // You may need to create this
        }
    }

    // Add this method to handle smooth expand/collapse animation (optional enhancement)
    private fun animateFilterToggle() {
        if (isFilterExpanded) {
            // Collapse animation
            val slideUp = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
            filterContent.startAnimation(slideUp)
            filterContent.visibility = View.GONE
            iconExpand.setImageResource(R.drawable.plus_solid)
            isFilterExpanded = false
        } else {
            // Expand animation
            filterContent.visibility = View.VISIBLE
            val slideDown = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
            filterContent.startAnimation(slideDown)
            iconExpand.setImageResource(R.drawable.minus_solid)
            isFilterExpanded = true
        }

        // Rotate icon animation
        iconExpand.animate()
            .rotation(if (isFilterExpanded) 45f else 0f)
            .setDuration(200)
            .start()
    }


    private fun formatRupiah(amount: Long): String {
        return "Rp ${String.format(Locale("id", "ID"), "%,d", amount)}"
    }

    private fun updateCabangSpinner() {
        val cabangAdapter = spinnerCabang.adapter as ArrayAdapter<String>
        cabangAdapter.clear()
        cabangAdapter.add("Semua Cabang")
        cabangAdapter.addAll(allCabang.sorted())
        cabangAdapter.notifyDataSetChanged()
    }

    private fun loadTransaksiData() {
        val transaksiRef = database.reference.child("transaksi")

        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allTransaksi.clear()
                allCabang.clear()

                for (dataSnapshot in snapshot.children) {
                    val transaksi = dataSnapshot.value as? Map<String, Any>
                    transaksi?.let {
                        allTransaksi.add(it)
                        // Collect unique cabang names
                        val cabang = it["cabang"] as? String
                        if (!cabang.isNullOrEmpty()) {
                            allCabang.add(cabang)
                        }
                    }
                }

                // Sort by timestamp descending (newest first)
                allTransaksi.sortByDescending { transaksi ->
                    (transaksi["timestamp"] as? Long) ?: 0L
                }

                // Update cabang spinner
                updateCabangSpinner()

                // Apply filters (initially shows all data)
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DataLaporanActivity", "Error loading transaksi data", error.toException())
                Toast.makeText(this@DataLaporanActivity, "Gagal memuat data transaksi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateStatusDiambil(transactionId: String) {
        val transaksiRef = database.reference.child("transaksi")

        // Query untuk mencari transaksi berdasarkan transactionId
        transaksiRef.orderByChild("transactionId").equalTo(transactionId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val key = childSnapshot.key
                        if (key != null) {
                            val updates = mapOf(
                                "statusDiambil" to true,
                                "timestampDiambil" to System.currentTimeMillis()
                            )

                            transaksiRef.child(key).updateChildren(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@DataLaporanActivity,
                                        "Status berhasil diperbarui",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("DataLaporanActivity", "Error updating status", exception)
                                    Toast.makeText(
                                        this@DataLaporanActivity,
                                        "Gagal memperbarui status",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DataLaporanActivity", "Error finding transaction", error.toException())
                    Toast.makeText(
                        this@DataLaporanActivity,
                        "Gagal mencari transaksi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isFilterExpanded", isFilterExpanded)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isFilterExpanded = savedInstanceState.getBoolean("isFilterExpanded", false)

        if (isFilterExpanded) {
            filterContent.visibility = View.VISIBLE
            iconExpand.setImageResource(R.drawable.minus_solid)
        } else {
            filterContent.visibility = View.GONE
            iconExpand.setImageResource(R.drawable.plus_solid)
        }
    }
}