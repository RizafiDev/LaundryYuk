package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelTambahan
import java.text.SimpleDateFormat
import java.util.*

class InvoiceActivity : AppCompatActivity() {

    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvTanggalTransaksi: TextView
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHpPelanggan: TextView
    private lateinit var tvNamaPegawai: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var tvHargaTambahan: TextView
    private lateinit var tvPajak: TextView
    private lateinit var tvTotalPembayaran: TextView
    private lateinit var tvMetodePembayaran: TextView
    private lateinit var btnKirimWa: TextView
    private lateinit var btnCetak: TextView

    private var transactionId = ""
    private var namaPelanggan = ""
    private var noHpPelanggan = ""
    private var namaLayanan = ""
    private var namaPegawai = ""
    private var metodePembayaran = ""
    private var kilogram = 1
    private var subtotal = 0
    private var pajak = 0
    private var totalPembayaran = 0
    private var baseServicePrice = 0
    private var tambahanList: ArrayList<ModelTambahan>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        initViews()
        loadDataFromIntent()
        setupClickListeners()
    }

    private fun initViews() {
        tvIdTransaksi = findViewById(R.id.id_transaksi)
        tvTanggalTransaksi = findViewById(R.id.tv_tanggal_transaksi)
        tvNamaPelanggan = findViewById(R.id.tv_nama_pelanggan)
        tvNoHpPelanggan = findViewById(R.id.nohpPelanggan)
        tvNamaPegawai = findViewById(R.id.tv_nama_pegawai)
        tvHargaLayanan = findViewById(R.id.tv_harga_layanan)
        tvHargaTambahan = findViewById(R.id.tv_harga_tambahan)
        tvPajak = findViewById(R.id.tv_pajak)
        tvTotalPembayaran = findViewById(R.id.tv_total_pembayaran)
        tvMetodePembayaran = findViewById(R.id.tv_metode_pembayaran)
        btnKirimWa = findViewById(R.id.btn_kirimwa)
        btnCetak = findViewById(R.id.btn_cetak)
    }

    private fun loadDataFromIntent() {
        intent?.extras?.let { bundle ->
            transactionId = bundle.getString("transactionId") ?: ""
            namaPelanggan = bundle.getString("namaPelanggan") ?: ""
            noHpPelanggan = bundle.getString("noHpPelanggan") ?: ""
            namaLayanan = bundle.getString("namaLayanan") ?: ""
            namaPegawai = bundle.getString("namaPegawai") ?: "Admin"
            metodePembayaran = bundle.getString("metodePembayaran") ?: ""
            kilogram = bundle.getInt("kilogram", 1)
            subtotal = bundle.getInt("subtotal", 0)
            pajak = bundle.getInt("pajak", 0)
            totalPembayaran = bundle.getInt("totalPembayaran", 0)
            baseServicePrice = bundle.getInt("baseServicePrice", 0)
            tambahanList = bundle.getSerializable("tambahanList") as? ArrayList<ModelTambahan>

            displayInvoiceData()
        }
    }

    private fun displayInvoiceData() {
        tvIdTransaksi.text = transactionId
        tvNamaPelanggan.text = namaPelanggan
        tvNoHpPelanggan.text = noHpPelanggan
        tvNamaPegawai.text = namaPegawai
        tvMetodePembayaran.text = metodePembayaran

        // Calculate base service price (without additional services)
        val servicePrice = baseServicePrice * kilogram
        tvHargaLayanan.text = "Rp ${formatRupiah(servicePrice)} ($namaLayanan - $kilogram Kg)"

        // Display additional services
        if (!tambahanList.isNullOrEmpty()) {
            val tambahanText = tambahanList!!.joinToString("\n") {
                "• ${it.namaLayanan} - Rp ${formatRupiah(parseHarga(it.hargaLayanan ?: "0"))}"
            }
            tvHargaTambahan.text = tambahanText
        } else {
            tvHargaTambahan.text = "Tidak ada layanan tambahan"
        }

        tvPajak.text = "Rp ${formatRupiah(pajak)} (12%)"
        tvTotalPembayaran.text = "Rp ${formatRupiah(totalPembayaran)}"

        val currentDate = SimpleDateFormat("dd MMMM yyyy • HH.mm", Locale("id", "ID")).format(Date())
        tvTanggalTransaksi.text = "$currentDate WIB"
    }

    private fun setupClickListeners() {
        btnKirimWa.setOnClickListener {
            sendWhatsAppMessage()
        }

        btnCetak.setOnClickListener {
            printInvoice()
        }
    }

    private fun sendWhatsAppMessage() {
        try {
            val message = createWhatsAppMessage()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$noHpPelanggan&text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("WhatsApp", "Error sending WhatsApp message", e)
            Toast.makeText(this, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createWhatsAppMessage(): String {
        val currentDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val servicePrice = baseServicePrice * kilogram

        val tambahanText = if (!tambahanList.isNullOrEmpty()) {
            tambahanList!!.joinToString("\n") { tambahan ->
                "  • ${tambahan.namaLayanan} - Rp ${formatRupiah(parseHarga(tambahan.hargaLayanan ?: "0"))}"
            }
        } else {
            "  • Tidak ada layanan tambahan"
        }

        return """🧺 *INVOICE LAUNDRY*

📅 *Tanggal:* $currentDate
🆔 *ID Transaksi:* $transactionId

👤 *Pelanggan:*
Nama: $namaPelanggan
No. HP: $noHpPelanggan

💼 *Layanan Utama:*
$namaLayanan (${kilogram} Kg) - Rp ${formatRupiah(servicePrice)}

➕ *Layanan Tambahan:*
$tambahanText

💰 *Rincian Pembayaran:*
Subtotal : Rp ${formatRupiah(subtotal)}
Pajak (12%) : Rp ${formatRupiah(pajak)}
*Total : Rp ${formatRupiah(totalPembayaran)}*

💳 *Metode Pembayaran:* $metodePembayaran
👨‍💼 *Dilayani oleh:* $namaPegawai

Terima kasih telah menggunakan layanan kami! 🙏
""".trimIndent()
    }


    private fun printInvoice() {
        try {
            Toast.makeText(this, "Fitur cetak belum tersedia", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Print", "Error printing invoice", e)
            Toast.makeText(this, "Gagal mencetak invoice", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseHarga(harga: String): Int {
        return try {
            harga.replace("Rp", "")
                .replace(".", "")
                .replace(",", "")
                .trim()
                .toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}