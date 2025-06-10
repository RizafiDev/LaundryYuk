package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R

class MetodePembayaranActivity : BaseActivity() {

    private lateinit var cardCash: CardView
    private lateinit var cardEMoney: CardView
    private lateinit var btnKonfirmasi: TextView

    private var selectedPaymentMethod: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metode_pembayaran)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        cardCash = findViewById(R.id.cash)
        cardEMoney = findViewById(R.id.emoney)
        btnKonfirmasi = findViewById(R.id.btn_konfirmasi)

        // Set initial state - button disabled
        updateKonfirmasiButton()
    }

    private fun setupClickListeners() {
        cardCash.setOnClickListener {
            selectedPaymentMethod = "Cash"
            updateCardSelection()
            updateKonfirmasiButton()
        }

        cardEMoney.setOnClickListener {
            selectedPaymentMethod = "E Money"
            updateCardSelection()
            updateKonfirmasiButton()
        }

        btnKonfirmasi.setOnClickListener {
            if (selectedPaymentMethod.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("selected_payment_method", selectedPaymentMethod)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun updateCardSelection() {
        // Reset all cards to default state
        resetCardStates()

        // Highlight selected card
        when (selectedPaymentMethod) {
            "Cash" -> {
                cardCash.setCardBackgroundColor(resources.getColor(R.color.selected_card_background, null))
                cardCash.elevation = 4f
            }
            "E Money" -> {
                cardEMoney.setCardBackgroundColor(resources.getColor(R.color.selected_card_background, null))
                cardEMoney.elevation = 4f
            }
        }
    }

    private fun resetCardStates() {
        cardCash.setCardBackgroundColor(resources.getColor(R.color.white, null))
        cardCash.elevation = 1f
        cardEMoney.setCardBackgroundColor(resources.getColor(R.color.white, null))
        cardEMoney.elevation = 1f
    }

    private fun updateKonfirmasiButton() {
        if (selectedPaymentMethod.isNotEmpty()) {
            btnKonfirmasi.isEnabled = true
            btnKonfirmasi.alpha = 1.0f
        } else {
            btnKonfirmasi.isEnabled = false
            btnKonfirmasi.alpha = 0.5f
        }
    }
}