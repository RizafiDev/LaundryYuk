package com.firmansyah.laundry.transaksi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.MainActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelTambahan
import com.firmansyah.laundry.service.PrintService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InvoiceActivity : BaseActivity() {

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

    private lateinit var printService: PrintService

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
    private var isFromLaporan = false

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION = 1001
        private const val REQUEST_ENABLE_BLUETOOTH = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        initViews()
        initPrintService()
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

    private fun initPrintService() {
        printService = PrintService(this)
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

            // Check if this invoice is opened from laporan (existing transaction)
            isFromLaporan = !transactionId.isEmpty() && subtotal > 0

            displayInvoiceData()
        }
    }

    private fun displayInvoiceData() {
        tvIdTransaksi.text = transactionId
        tvNamaPelanggan.text = namaPelanggan
        tvNoHpPelanggan.text = noHpPelanggan
        tvNamaPegawai.text = namaPegawai
        tvMetodePembayaran.text = metodePembayaran

        if (isFromLaporan) {
            // For existing transactions from laporan, show saved data
            displayExistingTransactionData()
        } else {
            // For new transactions, calculate fresh data
            displayNewTransactionData()
        }

        // Always use current date/time for display
        val currentDate = SimpleDateFormat("dd MMMM yyyy • HH.mm", Locale("id", "ID")).format(Date())
        tvTanggalTransaksi.text = "$currentDate WIB"
    }

    private fun displayExistingTransactionData() {
        // Use saved subtotal and calculated service price for display
        val servicePrice = if (baseServicePrice > 0 && kilogram > 0) {
            baseServicePrice * kilogram
        } else {
            // If baseServicePrice is not available, derive from subtotal
            val tambahanTotal = tambahanList?.sumOf { parseHarga(it.hargaLayanan ?: "0") } ?: 0
            subtotal - tambahanTotal
        }

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
    }

    private fun displayNewTransactionData() {
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
    }

    private fun setupClickListeners() {
        btnKirimWa.setOnClickListener {
            sendWhatsAppMessage()
        }

        btnCetak.setOnClickListener {
            checkBluetoothAndPrint()
        }
    }

    private fun checkBluetoothAndPrint() {
        // Check Bluetooth permissions
        if (!checkBluetoothPermissions()) {
            requestBluetoothPermissions()
            return
        }

        // Check if Bluetooth is enabled
        if (!printService.isBluetoothEnabled()) {
            showEnableBluetoothDialog()
            return
        }

        // Check if printer is paired
        if (!printService.isPrinterPaired()) {
            showPrinterNotPairedDialog()
            return
        }

        // Everything is ready, start printing
        startPrinting()
    }

    private fun checkBluetoothPermissions(): Boolean {
        val bluetoothPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH
        )
        val bluetoothAdminPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        // For Android 12+ (API level 31+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            return bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED &&
                    bluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
        }

        return bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
                bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        ActivityCompat.requestPermissions(
            this,
            permissions,
            REQUEST_BLUETOOTH_PERMISSION
        )
    }

    private fun showEnableBluetoothDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth Tidak Aktif")
            .setMessage("Untuk mencetak invoice, Bluetooth harus diaktifkan. Aktifkan sekarang?")
            .setPositiveButton("Aktifkan") { _, _ ->
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showPrinterNotPairedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Printer Tidak Terhubung")
            .setMessage("Printer dengan MAC address DC:0D:51:A7:FF:7A belum dipasangkan. Silakan pasangkan printer terlebih dahulu melalui pengaturan Bluetooth.")
            .setPositiveButton("Buka Pengaturan") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun startPrinting() {
        // Show loading dialog
        val loadingDialog = AlertDialog.Builder(this)
            .setTitle("Mencetak Invoice")
            .setMessage("Sedang mengirim data ke printer...")
            .setCancelable(false)
            .create()

        loadingDialog.show()

        // Create invoice data
        val invoiceData = createInvoiceData()

        // Print in background
        lifecycleScope.launch {
            try {
                val success = printService.printInvoice(invoiceData)

                runOnUiThread {
                    loadingDialog.dismiss()
                    if (success) {
                        Toast.makeText(this@InvoiceActivity, "Invoice berhasil dicetak!", Toast.LENGTH_SHORT).show()
                    } else {
                        showPrintErrorDialog()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    loadingDialog.dismiss()
                    Log.e("Print", "Error printing invoice", e)
                    showPrintErrorDialog()
                }
            }
        }
    }

    private fun createInvoiceData(): PrintService.InvoiceData {
        val tambahanItems = tambahanList?.map { tambahan ->
            PrintService.TambahanItem(
                nama = tambahan.namaLayanan ?: "",
                harga = parseHarga(tambahan.hargaLayanan ?: "0")
            )
        } ?: emptyList()

        return PrintService.InvoiceData(
            transactionId = transactionId,
            namaPelanggan = namaPelanggan,
            noHpPelanggan = noHpPelanggan,
            namaLayanan = namaLayanan,
            namaPegawai = namaPegawai,
            metodePembayaran = metodePembayaran,
            kilogram = kilogram,
            subtotal = subtotal,
            pajak = pajak,
            totalPembayaran = totalPembayaran,
            baseServicePrice = baseServicePrice,
            tambahanList = tambahanItems
        )
    }

    private fun showPrintErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Gagal Mencetak")
            .setMessage("Terjadi kesalahan saat mencetak invoice. Pastikan:\n\n• Printer sudah menyala\n• Printer terhubung dengan Bluetooth\n• Kertas tidak habis\n• Jarak dengan printer tidak terlalu jauh")
            .setPositiveButton("Coba Lagi") { _, _ ->
                startPrinting()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted, try printing again
                    checkBluetoothAndPrint()
                } else {
                    Toast.makeText(
                        this,
                        "Izin Bluetooth diperlukan untuk mencetak invoice",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == RESULT_OK) {
                    // Bluetooth enabled, continue with printing
                    checkBluetoothAndPrint()
                } else {
                    Toast.makeText(
                        this,
                        "Bluetooth harus diaktifkan untuk mencetak invoice",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun sendWhatsAppMessage() {
        try {
            val message = createWhatsAppMessage()

            // Clean phone number for WhatsApp
            val cleanNumber = noHpPelanggan.replace("[^\\d+]".toRegex(), "")
            val whatsappNumber = if (cleanNumber.startsWith("0")) {
                cleanNumber.replaceFirst("0", "62")
            } else if (!cleanNumber.startsWith("62") && !cleanNumber.startsWith("+62")) {
                "62$cleanNumber"
            } else {
                cleanNumber.replace("+", "")
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$whatsappNumber&text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("WhatsApp", "Error sending WhatsApp message", e)
            Toast.makeText(this, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createWhatsAppMessage(): String {
        val currentDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

        val servicePrice = if (isFromLaporan) {
            // For existing transactions, calculate service price from subtotal
            val tambahanTotal = tambahanList?.sumOf { parseHarga(it.hargaLayanan ?: "0") } ?: 0
            subtotal - tambahanTotal
        } else {
            baseServicePrice * kilogram
        }

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

    private fun parseHarga(harga: String): Int {
        return try {
            harga.replace("Rp", "")
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "")
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
        if (isFromLaporan) {
            // If opened from laporan, go back to laporan activity
            finish()
        } else {
            // If opened from new transaction, go to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}