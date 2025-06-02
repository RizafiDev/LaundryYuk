package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.LayananTambahanAdapter
import com.firmansyah.laundry.model.ModelTambahan

class CheckoutActivity : AppCompatActivity() {

    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHp: TextView
    private lateinit var tvNamaLayanan: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var tvKilogram: TextView
    private lateinit var tvTotalHarga: TextView
    private lateinit var tvPajak: TextView
    private lateinit var tvTotalPembayaran: TextView
    private lateinit var tvTambahanKosong: TextView
    private lateinit var rvLayananTambahan: RecyclerView
    private lateinit var btnMinus: ImageView
    private lateinit var btnPlus: ImageView
    private lateinit var btnBayar: TextView
    private lateinit var btnPilihMetodePembayaran: TextView

    private var kilogram = 1
    private var basePrice = 0
    private val taxRate = 0.12
    private var selectedPaymentMethod = "Pilih metode pembayaran"

    // Activity Result Launcher untuk menerima hasil dari MetodePembayaranActivity
    private val paymentMethodLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val paymentMethod = result.data?.getStringExtra("selected_payment_method")
            paymentMethod?.let {
                selectedPaymentMethod = it
                btnPilihMetodePembayaran.text = it
                // Ubah warna text menjadi hitam setelah memilih metode pembayaran
                btnPilihMetodePembayaran.setTextColor(resources.getColor(android.R.color.black))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        setupRecyclerView()
        loadDataFromIntent()
        setupClickListeners()
    }

    private fun initViews() {
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan)
        tvNoHp = findViewById(R.id.tv_no_hp)
        tvNamaLayanan = findViewById(R.id.tv_nama_layanan)
        tvHargaLayanan = findViewById(R.id.tv_harga_layanan)
        tvKilogram = findViewById(R.id.tv_kilogram)
        tvTotalHarga = findViewById(R.id.tv_total_harga)
        tvPajak = findViewById(R.id.tv_pajak)
        tvTotalPembayaran = findViewById(R.id.tv_total_pembayaran)
        tvTambahanKosong = findViewById(R.id.tv_tambahan_kosong)
        rvLayananTambahan = findViewById(R.id.rv_layanan_tambahan)
        btnMinus = findViewById(R.id.btn_minus)
        btnPlus = findViewById(R.id.btn_plus)
        btnBayar = findViewById(R.id.btn_bayar)
        btnPilihMetodePembayaran = findViewById(R.id.btn_pilih_metode_pembayaran)
    }

    private fun setupRecyclerView() {
        rvLayananTambahan.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun loadDataFromIntent() {
        intent?.extras?.let { bundle ->
            // Load customer data
            tvNamaPelanggan.text = bundle.getString("namaPelanggan", "")
            tvNoHp.text = bundle.getString("noHPPelanggan", "")

            // Load main service data
            tvNamaLayanan.text = bundle.getString("namaLayanan", "")
            val harga = bundle.getString("hargaLayanan", "0")
            tvHargaLayanan.text = "Rp $harga"
            basePrice = parseHarga(harga)

            // Load additional services
            val tambahanList = bundle.getSerializable("tambahanList") as? ArrayList<ModelTambahan>
            if (tambahanList.isNullOrEmpty()) {
                tvTambahanKosong.visibility = View.VISIBLE
                rvLayananTambahan.visibility = View.GONE
            } else {
                tvTambahanKosong.visibility = View.GONE
                rvLayananTambahan.visibility = View.VISIBLE

                val adapter = LayananTambahanAdapter(tambahanList.toMutableList()) { _, _ -> }
                rvLayananTambahan.adapter = adapter

                // Add additional services price to base price
                tambahanList.forEach { tambahan ->
                    basePrice += parseHarga(tambahan.hargaLayanan)
                }
            }

            // Calculate initial total
            calculateTotal()
        }
    }

    private fun setupClickListeners() {
        btnMinus.setOnClickListener {
            if (kilogram > 1) {
                kilogram--
                tvKilogram.text = "$kilogram Kg"
                calculateTotal()
            }
        }

        btnPlus.setOnClickListener {
            kilogram++
            tvKilogram.text = "$kilogram Kg"
            calculateTotal()
        }

        btnPilihMetodePembayaran.setOnClickListener {
            val intent = Intent(this, MetodePembayaranActivity::class.java)
            paymentMethodLauncher.launch(intent)
        }

        btnBayar.setOnClickListener {
            if (selectedPaymentMethod == "Pilih metode pembayaran") {
                // Tampilkan pesan bahwa metode pembayaran harus dipilih terlebih dahulu
                // Anda bisa menggunakan Toast atau AlertDialog
                android.widget.Toast.makeText(
                    this,
                    "Silakan pilih metode pembayaran terlebih dahulu",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                // Proses pembayaran atau navigasi ke halaman selanjutnya
                // Untuk saat ini hanya finish activity
                finish()
            }
        }
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
        val subtotal = basePrice * kilogram
        val tax = (subtotal * taxRate).toInt()
        val total = subtotal + tax

        tvTotalHarga.text = "Rp ${formatRupiah(subtotal)}"
        tvPajak.text = "Rp ${formatRupiah(tax)}"
        tvTotalPembayaran.text = "Rp ${formatRupiah(total)}"
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }
}