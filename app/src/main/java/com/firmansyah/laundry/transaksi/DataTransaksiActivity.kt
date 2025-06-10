package com.firmansyah.laundry.transaksi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.adapter.LayananTambahanAdapter
import com.firmansyah.laundry.adapter.PilihTambahanAdapter
import com.firmansyah.laundry.model.ModelTambahan
import com.firmansyah.laundry.model.ModelPelanggan
import com.firmansyah.laundry.model.ModelLayanan
import com.google.firebase.database.*

class DataTransaksiActivity : BaseActivity() {

    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHp: TextView
    private lateinit var tvNamaLayanan: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var btnTambahan: LinearLayout
    private lateinit var btnProses: TextView
    private lateinit var rvLayananTambahan: RecyclerView

    // Card containers for selected items
    private lateinit var rvSelectedPelanggan: RecyclerView
    private lateinit var rvSelectedLayanan: RecyclerView

    private lateinit var layananTambahanAdapter: LayananTambahanAdapter
    private val selectedTambahanList = mutableListOf<ModelTambahan>()
    private val allTambahanList = mutableListOf<ModelTambahan>()
    private lateinit var databaseReference: DatabaseReference

    private var selectedPelangganNama: String = ""
    private var selectedPelangganHP: String = ""
    private var selectedPelangganAlamat: String = ""
    private var selectedPelangganCabang: String = ""
    private var selectedLayananNama: String = ""
    private var selectedLayananHarga: String = ""
    private var selectedLayananCabang: String = ""

    private val pilihPelangganLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                selectedPelangganNama = data?.getStringExtra("namaPelanggan") ?: ""
                selectedPelangganHP = data?.getStringExtra("noHPPelanggan") ?: ""
                // Now properly get the additional data
                selectedPelangganAlamat = data?.getStringExtra("alamatPelanggan") ?: ""
                selectedPelangganCabang = data?.getStringExtra("cabangPelanggan") ?: ""

                // Show selected customer card
                showSelectedPelanggan()
            }
        }

    private val pilihLayananLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                selectedLayananNama = data?.getStringExtra("namaLayanan") ?: ""
                selectedLayananHarga = data?.getStringExtra("hargaLayanan") ?: ""
                // Now properly get the additional data
                selectedLayananCabang = data?.getStringExtra("cabangLayanan") ?: ""

                // Show selected service card
                showSelectedLayanan()
                calculateTotal()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_transaksi)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        initFirebase()
        loadTambahanData()
    }

    private fun initViews() {
        try {
            tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan)
            tvNoHp = findViewById(R.id.tv_no_hp)
            tvNamaLayanan = findViewById(R.id.tv_nama_layanan)
            tvHargaLayanan = findViewById(R.id.tv_harga_layanan)
            btnTambahan = findViewById(R.id.btn_tambahan)
            btnProses = findViewById(R.id.btn_proses)
            rvLayananTambahan = findViewById(R.id.rv_layanan_tambahan)
            rvSelectedPelanggan = findViewById(R.id.rv_selected_pelanggan)
            rvSelectedLayanan = findViewById(R.id.rv_selected_layanan)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            layananTambahanAdapter = LayananTambahanAdapter(selectedTambahanList) { _, position ->
                layananTambahanAdapter.removeItem(position)
                calculateTotal()
                Toast.makeText(this, "Layanan tambahan dihapus", Toast.LENGTH_SHORT).show()
            }

            rvLayananTambahan.apply {
                adapter = layananTambahanAdapter
                layoutManager = LinearLayoutManager(this@DataTransaksiActivity)
                isNestedScrollingEnabled = false
            }

            // Setup RecyclerViews for selected customer and service
            rvSelectedPelanggan.apply {
                layoutManager = LinearLayoutManager(this@DataTransaksiActivity)
                isNestedScrollingEnabled = false
            }

            rvSelectedLayanan.apply {
                layoutManager = LinearLayoutManager(this@DataTransaksiActivity)
                isNestedScrollingEnabled = false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up RecyclerView: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSelectedPelanggan() {
        try {
            // Create a single item adapter that shows selected customer
            val adapter = SingleCustomerAdapter(
                selectedPelangganNama,
                selectedPelangganHP,
                selectedPelangganAlamat,
                selectedPelangganCabang
            )

            rvSelectedPelanggan.adapter = adapter
            rvSelectedPelanggan.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing selected customer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSelectedLayanan() {
        try {
            // Create a single item adapter that shows selected service
            val adapter = SingleServiceAdapter(
                selectedLayananNama,
                selectedLayananHarga,
                selectedLayananCabang
            )

            rvSelectedLayanan.adapter = adapter
            rvSelectedLayanan.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing selected service: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Inner class for single customer adapter
    private inner class SingleCustomerAdapter(
        private val nama: String,
        private val hp: String,
        private val alamat: String,
        private val cabang: String
    ) : RecyclerView.Adapter<SingleCustomerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNama: TextView = view.findViewById(R.id.tvDataNamaPelanggan)
            val tvNoHp: TextView = view.findViewById(R.id.tvDataNoHpPelanggan)
            val tvAlamat: TextView = view.findViewById(R.id.tvDataAlamatPelanggan)
            val tvCabang: TextView = view.findViewById(R.id.tvDataCabangPelanggan)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_data_transaksi_pelanggan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvNama.text = if (nama.isNotEmpty()) nama else "-"
            holder.tvNoHp.text = if (hp.isNotEmpty()) hp else "-"
            holder.tvAlamat.text = if (alamat.isNotEmpty()) alamat else "-"
            holder.tvCabang.text = if (cabang.isNotEmpty()) "Cabang $cabang" else "Cabang -"
        }

        override fun getItemCount(): Int = 1
    }

    // Inner class for single service adapter
    private inner class SingleServiceAdapter(
        private val nama: String,
        private val harga: String,
        private val cabang: String
    ) : RecyclerView.Adapter<SingleServiceAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvNama: TextView = view.findViewById(R.id.tvDataNamaLayanan)
            val tvHarga: TextView = view.findViewById(R.id.tvDataHargaLayanan)
            val tvCabang: TextView = view.findViewById(R.id.tvDataCabangLayanan)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_data_transaksi_layanan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvNama.text = if (nama.isNotEmpty()) "Layanan $nama" else "Layanan -"
            holder.tvHarga.text = if (harga.isNotEmpty()) "Rp. $harga,-" else "Rp. 0,-"
            holder.tvCabang.text = if (cabang.isNotEmpty()) "Cabang $cabang" else "Cabang -"
        }

        override fun getItemCount(): Int = 1
    }

    private fun setupClickListeners() {
        try {
            findViewById<LinearLayout>(R.id.btn_pilih_pelanggan)?.setOnClickListener {
                val intent = Intent(this, PilihPelangganActivity::class.java)
                pilihPelangganLauncher.launch(intent)
            }

            findViewById<LinearLayout>(R.id.btn_pilih_layanan)?.setOnClickListener {
                val intent = Intent(this, PilihLayananActivity::class.java)
                pilihLayananLauncher.launch(intent)
            }

            btnTambahan.setOnClickListener {
                showTambahanDialog()
            }

            btnProses.setOnClickListener {
                processTransaction()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up click listeners: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTambahanDialog() {
        try {
            if (allTambahanList.isEmpty()) {
                Toast.makeText(this, "Sedang memuat data layanan tambahan...", Toast.LENGTH_SHORT).show()
                return
            }

            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pilih_tambahan, null)
            val rvPilihTambahan = dialogView.findViewById<RecyclerView>(R.id.rvPilihTambahan)

            val pilihTambahanAdapter = PilihTambahanAdapter(allTambahanList) { selectedTambahan ->
                val isAlreadySelected = selectedTambahanList.any {
                    it.idLayanan == selectedTambahan.idLayanan
                }

                if (!isAlreadySelected) {
                    layananTambahanAdapter.addItem(selectedTambahan)
                    calculateTotal()
                    Toast.makeText(this, "Layanan tambahan ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Layanan tambahan sudah dipilih", Toast.LENGTH_SHORT).show()
                }
            }

            rvPilihTambahan?.apply {
                adapter = pilihTambahanAdapter
                layoutManager = LinearLayoutManager(this@DataTransaksiActivity)
            }

            AlertDialog.Builder(this)
                .setTitle("Pilih Layanan Tambahan")
                .setView(dialogView)
                .setNegativeButton("Tutup") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initFirebase() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference("tambahan")
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTambahanData() {
        try {
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        allTambahanList.clear()
                        for (data in snapshot.children) {
                            val item = data.getValue(ModelTambahan::class.java)
                            if (item != null && !item.namaLayanan.isNullOrBlank() && !item.hargaLayanan.isNullOrBlank()) {
                                allTambahanList.add(item)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DataTransaksiActivity,
                            "Error processing data: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DataTransaksiActivity,
                        "Gagal memuat data layanan tambahan: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseHarga(harga: String?): Int {
        return try {
            harga
                ?.replace("Rp", "")
                ?.replace(".", "")
                ?.replace(",", "")
                ?.replace("-", "")
                ?.trim()
                ?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateTotal() {
        try {
            var total = parseHarga(selectedLayananHarga)

            selectedTambahanList.forEach { tambahan ->
                total += parseHarga(tambahan.hargaLayanan)
            }

            println("Total Harga: Rp $total")
        } catch (e: Exception) {
            Toast.makeText(this, "Error calculating total: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTotalForProcess(): Int {
        return try {
            var total = parseHarga(selectedLayananHarga)

            selectedTambahanList.forEach { tambahan ->
                total += parseHarga(tambahan.hargaLayanan)
            }

            total
        } catch (e: Exception) {
            0
        }
    }

    private fun processTransaction() {
        try {
            if (selectedPelangganNama.isEmpty()) {
                Toast.makeText(this, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }

            if (selectedLayananNama.isEmpty()) {
                Toast.makeText(this, "Silakan pilih layanan utama terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }

            val totalHarga = calculateTotalForProcess()

            // Navigate to CheckoutActivity
            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("namaPelanggan", selectedPelangganNama)
                putExtra("noHPPelanggan", selectedPelangganHP)
                putExtra("alamatPelanggan", selectedPelangganAlamat)
                putExtra("cabangPelanggan", selectedPelangganCabang)
                putExtra("namaLayanan", selectedLayananNama)
                putExtra("hargaLayanan", selectedLayananHarga)
                putExtra("cabangLayanan", selectedLayananCabang)
                putExtra("tambahanList", ArrayList(selectedTambahanList))
                putExtra("totalHarga", totalHarga.toString())
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing transaction: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun buildTransactionSummary(totalHarga: Int): String {
        return try {
            val summary = StringBuilder()
            summary.append("Detail Transaksi:\n\n")
            summary.append("Pelanggan: $selectedPelangganNama\n")
            summary.append("No HP: $selectedPelangganHP\n")
            summary.append("Alamat: $selectedPelangganAlamat\n")
            summary.append("Cabang: $selectedPelangganCabang\n")
            summary.append("Layanan: $selectedLayananNama\n")
            summary.append("Harga Layanan: Rp $selectedLayananHarga\n\n")

            if (selectedTambahanList.isNotEmpty()) {
                summary.append("Layanan Tambahan:\n")
                selectedTambahanList.forEach { tambahan ->
                    summary.append("• ${tambahan.namaLayanan}: Rp ${tambahan.hargaLayanan}\n")
                }
                summary.append("\n")
            }

            summary.append("Total: Rp $totalHarga")
            summary.toString()
        } catch (e: Exception) {
            "Error building summary: ${e.message}"
        }
    }
}