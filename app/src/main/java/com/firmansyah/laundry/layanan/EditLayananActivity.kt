package com.firmansyah.laundry.layanan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.model.ModelLayanan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditLayananActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var spinnerCabang: Spinner
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private val cabangRef = database.getReference("cabang")

    private lateinit var layanan: ModelLayanan
    private var isEditMode = false

    private var listCabang = mutableListOf<ModelCabang>()
    private var listNamaCabang = mutableListOf<String>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layanan)

        // Initialize views
        etNama = findViewById(R.id.etNamaLayanan)
        etHarga = findViewById(R.id.etHargaLayanan)
        spinnerCabang = findViewById(R.id.etCabangLayanan)
        btnSimpan = findViewById(R.id.btnSimpanLayanan)

        // Get layanan data from intent
        layanan = intent.getParcelableExtra("LAYANAN_DATA") ?: ModelLayanan()

        // Setup Spinner
        setupSpinner()

        // Load data cabang dari Firebase
        loadDataCabang()

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

    private fun setupSpinner() {
        spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listNamaCabang
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = spinnerAdapter
    }

    private fun loadDataCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                listNamaCabang.clear()

                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    cabang?.let {
                        listCabang.add(it)
                        listNamaCabang.add(it.namaCabang ?: "")
                    }
                }
                spinnerAdapter.notifyDataSetChanged()

                // Set initial data setelah data cabang dimuat
                setInitialData()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@EditLayananActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setInitialData() {
        // Set data untuk EditText
        etNama.setText(layanan.namaLayanan)
        etHarga.setText(layanan.hargaLayanan)

        // Set data untuk Spinner
        val cabangLayanan = layanan.cabangLayanan
        if (!cabangLayanan.isNullOrEmpty()) {
            val position = listNamaCabang.indexOf(cabangLayanan)
            if (position >= 0) {
                spinnerCabang.setSelection(position)
            }
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etNama.isEnabled = editable
        etHarga.isEnabled = editable
        spinnerCabang.isEnabled = editable

        val backgroundRes = if (editable) {
            R.drawable.edittext_background_enabled
        } else {
            R.drawable.edittext_background_disabled
        }

        etNama.setBackgroundResource(backgroundRes)
        etHarga.setBackgroundResource(backgroundRes)

        // Untuk spinner, bisa menggunakan alpha untuk menunjukkan disabled state
        spinnerCabang.alpha = if (editable) 1.0f else 0.6f
    }

    private fun updateLayanan() {
        // Validate inputs
        if (etNama.text.toString().trim().isEmpty()) {
            etNama.error = "Nama Layanan tidak boleh kosong"
            etNama.requestFocus()
            return
        }

        if (etHarga.text.toString().trim().isEmpty()) {
            etHarga.error = "Harga tidak boleh kosong"
            etHarga.requestFocus()
            return
        }

        val selectedCabangPosition = spinnerCabang.selectedItemPosition
        if (selectedCabangPosition < 0 || selectedCabangPosition >= listNamaCabang.size) {
            Toast.makeText(this, "Silahkan pilih cabang", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCabang = listNamaCabang[selectedCabangPosition]

        val updatedLayanan = ModelLayanan(
            idLayanan = layanan.idLayanan,
            namaLayanan = etNama.text.toString().trim(),
            hargaLayanan = etHarga.text.toString().trim(),
            cabangLayanan = selectedCabang,
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