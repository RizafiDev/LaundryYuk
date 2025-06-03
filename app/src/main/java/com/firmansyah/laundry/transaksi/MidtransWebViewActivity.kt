package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import org.json.JSONObject

class MidtransWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var orderId: String = ""
    private var paymentMethod: String = "E-Money" // Default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_midtrans_web_view)

        initViews()
        setupBackPressedHandler()
        loadPaymentPage()
    }

    private fun initViews() {
        webView = findViewById(R.id.webview_midtrans)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
        }

        // Add JavaScript interface to detect payment method
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    when {
                        it.contains("payment/finish") ||
                                it.contains("payment-success") ||
                                it.contains("status_code=200") ||
                                it.contains("transaction_status=settlement") ||
                                it.contains("transaction_status=capture") -> {
                            handlePaymentSuccess()
                            return true
                        }
                        it.contains("payment/error") ||
                                it.contains("payment-error") ||
                                it.contains("status_code=201") ||
                                it.contains("transaction_status=deny") ||
                                it.contains("transaction_status=cancel") ||
                                it.contains("transaction_status=expire") ||
                                it.contains("transaction_status=failure") -> {
                            handlePaymentError()
                            return true
                        }
                        it.contains("payment/pending") ||
                                it.contains("payment-pending") ||
                                it.contains("transaction_status=pending") -> {
                            handlePaymentPending()
                            return true
                        }
                        else -> return false
                    }
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Inject JavaScript to detect payment method when page finishes loading
                view?.evaluateJavascript("""
                    if (typeof window.paymentMethod !== 'undefined') {
                        Android.setPaymentMethod(window.paymentMethod);
                    } else if (typeof Snap !== 'undefined') {
                        Android.setPaymentMethod(Snap.getActivePaymentMethod());
                    } else {
                        // Fallback to check URL for payment method
                        var url = window.location.href;
                        if (url.includes('gopay')) Android.setPaymentMethod('Gopay');
                        else if (url.includes('shopeepay')) Android.setPaymentMethod('ShopeePay');
                        else if (url.includes('qris')) Android.setPaymentMethod('QRIS');
                        else if (url.includes('bank_transfer')) Android.setPaymentMethod('Bank Transfer');
                        else Android.setPaymentMethod('E-Money');
                    }
                """.trimIndent(), null)
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun setPaymentMethod(method: String) {
            paymentMethod = when (method.toLowerCase()) {
                "gopay" -> "Gopay"
                "shopeepay" -> "ShopeePay"
                "qris" -> "QRIS"
                "bank_transfer" -> "Transfer Bank"
                else -> method
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
        // Return success result with payment method
        val resultIntent = Intent().apply {
            putExtra("payment_method", paymentMethod)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun handlePaymentError() {
        AlertDialog.Builder(this)
            .setTitle("Pembayaran Gagal")
            .setMessage("Pembayaran gagal atau dibatalkan\nOrder ID: $orderId")
            .setPositiveButton("OK") { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePaymentPending() {
        AlertDialog.Builder(this)
            .setTitle("Pembayaran Pending")
            .setMessage("Pembayaran sedang diproses\nOrder ID: $orderId\n\nAnda akan diarahkan ke invoice.")
            .setPositiveButton("OK") { _, _ ->
                val resultIntent = Intent().apply {
                    putExtra("payment_method", paymentMethod)
                }
                setResult(RESULT_OK, resultIntent)
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