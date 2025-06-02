package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.LayananTambahanAdapter
import com.firmansyah.laundry.model.ModelTambahan
import com.firmansyah.laundry.model.PaymentRequest
import com.firmansyah.laundry.model.PaymentResponse
import com.firmansyah.laundry.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

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
    private var tambahanList: ArrayList<ModelTambahan>? = null
    private var namaPelanggan = ""
    private var noHpPelanggan = ""
    private var namaLayanan = ""

    private val apiService by lazy { RetrofitClient.instance }

    // Firebase instances
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val paymentMethodLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("selected_payment_method")?.let {
                selectedPaymentMethod = it
                btnPilihMetodePembayaran.text = it
                btnPilihMetodePembayaran.setTextColor(resources.getColor(android.R.color.black, null))
            }
        }
    }

    private val midtransLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                saveTransactionToDatabase()
                showSuccessDialog("Pembayaran berhasil!")
            }
            RESULT_CANCELED -> showErrorDialog("Pembayaran dibatalkan")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

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
            namaPelanggan = bundle.getString("namaPelanggan") ?: ""
            noHpPelanggan = bundle.getString("noHPPelanggan") ?: ""
            tvNamaPelanggan.text = namaPelanggan
            tvNoHp.text = noHpPelanggan

            namaLayanan = bundle.getString("namaLayanan") ?: ""
            tvNamaLayanan.text = namaLayanan
            val harga = bundle.getString("hargaLayanan") ?: "0"
            tvHargaLayanan.text = "Rp $harga"
            basePrice = parseHarga(harga)

            tambahanList = bundle.getSerializable("tambahanList") as? ArrayList<ModelTambahan>
            if (tambahanList.isNullOrEmpty()) {
                tvTambahanKosong.visibility = View.VISIBLE
                rvLayananTambahan.visibility = View.GONE
            } else {
                tvTambahanKosong.visibility = View.GONE
                rvLayananTambahan.visibility = View.VISIBLE
                rvLayananTambahan.adapter = LayananTambahanAdapter(tambahanList!!.toMutableList()) { _, _ -> }
                tambahanList!!.forEach { tambahan ->
                    basePrice += parseHarga(tambahan.hargaLayanan ?: "0")
                }
            }
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
            when (selectedPaymentMethod) {
                "Pilih metode pembayaran" -> showError("Silakan pilih metode pembayaran")
                "Cash" -> processCashPayment()
                "E Money" -> processEMoneyPayment()
            }
        }
    }

    private fun processCashPayment() {
        AlertDialog.Builder(this)
            .setTitle("Pembayaran Cash")
            .setMessage("Pembayaran akan diproses sebagai cash. Silakan bayar di kasir.")
            .setPositiveButton("OK") { _, _ ->
                saveTransactionToDatabase()
                showSuccessDialog("Transaksi berhasil disimpan")
            }
            .show()
    }

    private fun processEMoneyPayment() {
        val subtotal = basePrice * kilogram
        val tax = (subtotal * taxRate).toInt()
        val totalAmount = subtotal + tax

        val itemDetails = mutableListOf<PaymentRequest.ItemDetail>().apply {
            add(PaymentRequest.ItemDetail(
                id = "main_service",
                price = basePrice,
                quantity = kilogram,
                name = "$namaLayanan ($kilogram Kg)"
            ))

            tambahanList?.forEach { tambahan ->
                add(PaymentRequest.ItemDetail(
                    id = "addon_${tambahan.idLayanan ?: ""}",
                    price = parseHarga(tambahan.hargaLayanan ?: "0"),
                    quantity = 1,
                    name = tambahan.namaLayanan ?: "Layanan Tambahan"
                ))
            }

            add(PaymentRequest.ItemDetail(
                id = "tax",
                price = tax,
                quantity = 1,
                name = "Pajak (12%)"
            ))
        }

        val paymentRequest = PaymentRequest(
            amount = totalAmount,
            customerDetails = PaymentRequest.CustomerDetails(
                first_name = namaPelanggan,
                phone = noHpPelanggan
            ),
            itemDetails = itemDetails
        )

        createMidtransTransaction(paymentRequest)
    }

    private fun createMidtransTransaction(paymentRequest: PaymentRequest) {
        val loadingDialog = AlertDialog.Builder(this)
            .setMessage("Memproses pembayaran...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        apiService.createTransaction(paymentRequest).enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(call: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                loadingDialog.dismiss()

                if (response.isSuccessful) {
                    response.body()?.let { paymentResponse ->
                        if (paymentResponse.success) {
                            openMidtransWebView(paymentResponse.data.redirect_url, paymentResponse.data.order_id)
                        } else {
                            showError("Gagal membuat transaksi: ${paymentResponse.message ?: "Unknown error"}")
                        }
                    } ?: showError("Response body is null")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("Payment", "Error response: $errorBody")
                    showError("Server error: $errorBody")
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                loadingDialog.dismiss()
                Log.e("Payment", "Network failure", t)
                showError("Network error: ${t.localizedMessage}")
            }
        })
    }

    private fun openMidtransWebView(redirectUrl: String, orderId: String) {
        val intent = Intent(this, MidtransWebViewActivity::class.java)
        intent.putExtra("redirect_url", redirectUrl)
        intent.putExtra("order_id", orderId)
        midtransLauncher.launch(intent)
    }

    private fun saveTransactionToDatabase() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e("Firebase", "No user logged in")
            return
        }

        val subtotal = basePrice * kilogram
        val tax = (subtotal * taxRate).toInt()
        val total = subtotal + tax

        // Generate unique transaction ID
        val transactionId = database.reference.child("transactions").push().key ?: return

        // Get current timestamp
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(timestamp))

        // Prepare additional services data
        val layananTambahanData = tambahanList?.map { tambahan ->
            mapOf(
                "id" to (tambahan.idLayanan ?: ""),
                "nama" to (tambahan.namaLayanan ?: ""),
                "harga" to parseHarga(tambahan.hargaLayanan ?: "0")
            )
        } ?: emptyList()

        // Create transaction data
        val transactionData = mapOf(
            "transactionId" to transactionId,
            "timestamp" to timestamp,
            "tanggalTransaksi" to formattedDate,
            "pelanggan" to mapOf(
                "nama" to namaPelanggan,
                "noHp" to noHpPelanggan
            ),
            "layanan" to mapOf(
                "nama" to namaLayanan,
                "hargaPerKg" to (basePrice - (tambahanList?.sumOf { parseHarga(it.hargaLayanan ?: "0") } ?: 0)),
                "kilogram" to kilogram,
                "layananTambahan" to layananTambahanData
            ),
            "pembayaran" to mapOf(
                "metodePembayaran" to selectedPaymentMethod,
                "subtotal" to subtotal,
                "pajak" to tax,
                "totalPembayaran" to total
            ),
            "karyawan" to mapOf(
                "uid" to currentUser.uid,
                "nama" to (currentUser.displayName ?: ""),
                "email" to (currentUser.email ?: "")
            ),
            "status" to "completed"
        )

        // Save to Firebase Realtime Database
        database.reference
            .child("transactions")
            .child(transactionId)
            .setValue(transactionData)
            .addOnSuccessListener {
                Log.d("Firebase", "Transaction saved successfully")
                // Also save to monthly summary for reporting
                saveMonthlyReport(total, timestamp)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Failed to save transaction", exception)
                Toast.makeText(this, "Gagal menyimpan data transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMonthlyReport(totalAmount: Int, timestamp: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val monthKey = "${year}_${String.format("%02d", month)}"

        val currentUser = firebaseAuth.currentUser ?: return

        val monthlyRef = database.reference
            .child("monthly_reports")
            .child(monthKey)

        // Get current monthly data and update it
        monthlyRef.get().addOnSuccessListener { snapshot ->
            val currentData = snapshot.value as? Map<String, Any> ?: emptyMap()
            val currentTotal = (currentData["totalPendapatan"] as? Long)?.toInt() ?: 0
            val currentCount = (currentData["jumlahTransaksi"] as? Long)?.toInt() ?: 0

            val updatedData = mapOf(
                "tahun" to year,
                "bulan" to month,
                "totalPendapatan" to (currentTotal + totalAmount),
                "jumlahTransaksi" to (currentCount + 1),
                "lastUpdated" to timestamp,
                "lastUpdatedBy" to mapOf(
                    "uid" to currentUser.uid,
                    "nama" to (currentUser.displayName ?: ""),
                    "email" to (currentUser.email ?: "")
                )
            )

            monthlyRef.setValue(updatedData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Monthly report updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Failed to update monthly report", exception)
                }
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

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Sukses")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        private const val REQUEST_CODE_PAYMENT_METHOD = 1001
    }
}