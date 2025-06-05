package com.firmansyah.laundry.layanan

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelLayanan
import com.google.firebase.database.FirebaseDatabase


class EditLayananActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var etCabang: EditText
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private lateinit var layanan: ModelLayanan
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layanan)

        // Initialize views
        etNama = findViewById(R.id.etNamaLayanan)
        etHarga = findViewById(R.id.etHargaLayanan)
        etCabang = findViewById(R.id.etCabangLayanan)
        btnSimpan = findViewById(R.id.btnSimpanLayanan)

        // Get pegawai data from intent
        layanan = intent.getParcelableExtra("LAYANAN_DATA") ?: ModelLayanan()

        // Set initial data
        etNama.setText(layanan.namaLayanan)
        etHarga.setText(layanan.hargaLayanan)
        etCabang.setText(layanan.cabangLayanan)

        // Initially set fields to readonly
        setFieldsEditable(false)

        btnSimpan.setOnClickListener {
            if (isEditMode) {
                // Save changes
                updateLayanan()
            } else {
                // Switch to edit mode
                isEditMode = true
                setFieldsEditable(true)
                btnSimpan.text = "Simpan"
            }
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etNama.isEnabled = editable
        etHarga.isEnabled = editable
        etCabang.isEnabled = editable

        val backgroundRes = if (editable) {
            R.drawable.edittext_background_enabled
        } else {
            R.drawable.edittext_background_disabled
        }

        etNama.setBackgroundResource(backgroundRes)
        etHarga.setBackgroundResource(backgroundRes)
        etCabang.setBackgroundResource(backgroundRes)
    }

    private fun updateLayanan() {
        // Validate inputs
        if (etNama.text.toString().isEmpty()) {
            etNama.error = "Nama Layanan tidak boleh kosong"
            return
        }

        if (etHarga.text.toString().isEmpty()) {
            etHarga.error = "Harga tidak boleh kosong"
            return
        }

        if (etCabang.text.toString().isEmpty()) {
            etCabang.error = "Cabang tidak boleh kosong"
            return
        }

        val updatedLayanan = ModelLayanan(
            idLayanan = layanan.idLayanan,
            namaLayanan = etNama.text.toString(),
            hargaLayanan = etHarga.text.toString(),
            cabangLayanan = etCabang.text.toString(),
            tanggalTerdaftar = layanan.tanggalTerdaftar
        )

        database.getReference("layanan").child(layanan.idLayanan ?: "")
            .setValue(updatedLayanan)
            .addOnSuccessListener {
                Toast.makeText(this, "Data layanan berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}