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
import com.firmansyah.laundry.adapter.LayananTambahanAdapter
import com.firmansyah.laundry.adapter.PilihTambahanAdapter
import com.firmansyah.laundry.model.ModelTambahan
import com.google.firebase.database.*

class DataTransaksiActivity : AppCompatActivity() {

    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHp: TextView
    private lateinit var tvNamaLayanan: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var btnTambahan: Button
    private lateinit var btnProses: Button
    private lateinit var rvLayananTambahan: RecyclerView

    private lateinit var layananTambahanAdapter: LayananTambahanAdapter
    private val selectedTambahanList = mutableListOf<ModelTambahan>()
    private val allTambahanList = mutableListOf<ModelTambahan>()
    private lateinit var databaseReference: DatabaseReference

    private var selectedPelangganNama: String = ""
    private var selectedPelangganHP: String = ""
    private var selectedLayananNama: String = ""
    private var selectedLayananHarga: String = ""

    private val pilihPelangganLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val namaPelanggan = data?.getStringExtra("namaPelanggan")
                val noHPPelanggan = data?.getStringExtra("noHPPelanggan")

                selectedPelangganNama = namaPelanggan ?: ""
                selectedPelangganHP = noHPPelanggan ?: ""

                tvNamaPelanggan.text = "Nama Pelanggan: $selectedPelangganNama"
                tvNoHp.text = "No HP: $selectedPelangganHP"
            }
        }

    private val pilihLayananLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val namaLayanan = data?.getStringExtra("namaLayanan")
                val hargaLayanan = data?.getStringExtra("hargaLayanan")

                selectedLayananNama = namaLayanan ?: ""
                selectedLayananHarga = hargaLayanan ?: ""

                tvNamaLayanan.text = "Nama Layanan: $selectedLayananNama"
                tvHargaLayanan.text = "Harga: Rp $selectedLayananHarga"

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
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan)
        tvNoHp = findViewById(R.id.tv_no_hp)
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan)
        tvHargaLayanan = findViewById(R.id.tv_harga_layanan)
        btnTambahan = findViewById(R.id.btn_tambahan)
        btnProses = findViewById(R.id.btn_proses)
        rvLayananTambahan = findViewById(R.id.rv_layanan_tambahan)
    }

    private fun setupRecyclerView() {
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
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_pilih_pelanggan).setOnClickListener {
            val intent = Intent(this, PilihPelangganActivity::class.java)
            pilihPelangganLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btn_pilih_layanan).setOnClickListener {
            val intent = Intent(this, PilihLayananActivity::class.java)
            pilihLayananLauncher.launch(intent)
        }

        btnTambahan.setOnClickListener {
            showTambahanDialog()
        }

        btnProses.setOnClickListener {
            processTransaction()
        }
    }

    private fun showTambahanDialog() {
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

        rvPilihTambahan.apply {
            adapter = pilihTambahanAdapter
            layoutManager = LinearLayoutManager(this@DataTransaksiActivity)
        }

        AlertDialog.Builder(this)
            .setTitle("Pilih Layanan Tambahan")
            .setView(dialogView)
            .setNegativeButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun initFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("tambahan")
    }

    private fun loadTambahanData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allTambahanList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ModelTambahan::class.java)
                    if (item != null && !item.namaLayanan.isNullOrBlank() && !item.hargaLayanan.isNullOrBlank()) {
                        allTambahanList.add(item)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataTransaksiActivity,
                    "Gagal memuat data layanan tambahan: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseHarga(harga: String?): Int {
        return harga
            ?.replace("Rp", "")
            ?.replace(".", "")
            ?.replace(",", "")
            ?.trim()
            ?.toIntOrNull() ?: 0
    }

    private fun calculateTotal() {
        var total = parseHarga(selectedLayananHarga)

        selectedTambahanList.forEach { tambahan ->
            total += parseHarga(tambahan.hargaLayanan)
        }

        println("Total Harga: Rp $total")
    }

    private fun calculateTotalForProcess(): Int {
        var total = parseHarga(selectedLayananHarga)

        selectedTambahanList.forEach { tambahan ->
            total += parseHarga(tambahan.hargaLayanan)
        }

        return total
    }

    private fun processTransaction() {
        if (selectedPelangganNama.isEmpty()) {
            Toast.makeText(this, "Silakan pilih pelanggan terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedLayananNama.isEmpty()) {
            Toast.makeText(this, "Silakan pilih layanan utama terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val totalHarga = calculateTotalForProcess()

        // Langsung menuju CheckoutActivity tanpa modal konfirmasi
        val intent = Intent(this, CheckoutActivity::class.java).apply {
            putExtra("namaPelanggan", selectedPelangganNama)
            putExtra("noHPPelanggan", selectedPelangganHP)
            putExtra("namaLayanan", selectedLayananNama)
            putExtra("hargaLayanan", selectedLayananHarga)
            putExtra("tambahanList", ArrayList(selectedTambahanList))
            putExtra("totalHarga", totalHarga.toString())
        }
        startActivity(intent)
    }


    private fun buildTransactionSummary(totalHarga: Int): String {
        val summary = StringBuilder()
        summary.append("Detail Transaksi:\n\n")
        summary.append("Pelanggan: $selectedPelangganNama\n")
        summary.append("No HP: $selectedPelangganHP\n")
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
        return summary.toString()
    }
}
