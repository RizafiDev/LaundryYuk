package com.firmansyah.laundry.transaksi

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.PaymentStatusResponse
import com.firmansyah.laundry.network.ApiService
import com.firmansyah.laundry.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MidtransWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var apiService: ApiService
    private var orderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_midtrans_web_view)

        initViews()
        setupApiService()
        setupBackPressedHandler()
        loadPaymentPage()
    }

    private fun setupApiService() {
        apiService = RetrofitClient.instance
    }


    private fun initViews() {
        webView = findViewById(R.id.webview_midtrans)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    when {
                        it.contains("payment-success") -> {
                            handlePaymentSuccess()
                            return true
                        }
                        it.contains("payment-error") -> {
                            handlePaymentError()
                            return true
                        }
                        it.contains("payment-pending") -> {
                            handlePaymentPending()
                            return true
                        }
                        else -> return false
                    }
                }
                return false
            }
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    AlertDialog.Builder(this@MidtransWebViewActivity)
                        .setTitle("Batalkan Pembayaran")
                        .setMessage("Apakah Anda yakin ingin membatalkan pembayaran?")
                        .setPositiveButton("Ya") { _, _ ->
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                        .setNegativeButton("Tidak", null)
                        .show()
                }
            }
        })
    }

    private fun loadPaymentPage() {
        val redirectUrl = intent.getStringExtra("redirect_url")
        orderId = intent.getStringExtra("order_id") ?: ""

        redirectUrl?.let {
            webView.loadUrl(it)
        } ?: run {
            showErrorDialog("Redirect URL not found")
            finish()
        }
    }

    private fun handlePaymentSuccess() {
        checkTransactionStatus("success")
    }

    private fun handlePaymentError() {
        checkTransactionStatus("error")
    }

    private fun handlePaymentPending() {
        checkTransactionStatus("pending")
    }

    private fun checkTransactionStatus(expectedStatus: String) {
        apiService.checkTransactionStatus(orderId).enqueue(object : Callback<PaymentStatusResponse> {
            override fun onResponse(
                call: Call<PaymentStatusResponse>,
                response: Response<PaymentStatusResponse>
            ) {
                if (response.isSuccessful) {
                    val status = response.body()?.transactionStatus
                    showPaymentResult(expectedStatus, status)
                } else {
                    showPaymentResult(expectedStatus, "unknown")
                }
            }

            override fun onFailure(call: Call<PaymentStatusResponse>, t: Throwable) {
                showPaymentResult(expectedStatus, "error")
            }
        })
    }

    private fun showPaymentResult(expectedStatus: String, actualStatus: String?) {
        val message = when (expectedStatus) {
            "success" -> "Pembayaran berhasil!"
            "error" -> "Pembayaran gagal atau dibatalkan"
            "pending" -> "Pembayaran sedang diproses"
            else -> "Status pembayaran tidak diketahui"
        }

        AlertDialog.Builder(this)
            .setTitle("Status Pembayaran")
            .setMessage("$message\nOrder ID: $orderId")
            .setPositiveButton("OK") { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}