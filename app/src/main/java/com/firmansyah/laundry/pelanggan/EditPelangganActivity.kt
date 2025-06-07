package com.firmansyah.laundry.pelanggan

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.model.ModelPelanggan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditPelangganActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: Spinner
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private val cabangRef = database.getReference("cabang")
    private lateinit var pelanggan: ModelPelanggan
    private var isEditMode = false

    private var listCabang = mutableListOf<ModelCabang>()
    private var listNamaCabang = mutableListOf<String>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pelanggan)

        // Initialize views
        etNama = findViewById(R.id.etTambahNamaPelanggan)
        etAlamat = findViewById(R.id.etTambahAlamatPelanggan)
        etNoHP = findViewById(R.id.etTambahNoHpPelanggan)
        etCabang = findViewById(R.id.etTambahCabangPelanggan)
        btnSimpan = findViewById(R.id.btnSimpanPelanggan)

        // Setup Spinner
        setupSpinner()

        // Get pelanggan data from intent
        pelanggan = intent.getParcelableExtra("PELANGGAN_DATA") ?: ModelPelanggan()

        // Load data cabang dari Firebase
        loadDataCabang()

        // Initially set fields to readonly
        setFieldsEditable(false)

        btnSimpan.setOnClickListener {
            if (isEditMode) {
                // Save changes
                updatePelanggan()
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
        etCabang.adapter = spinnerAdapter
    }

    private fun loadDataCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                listNamaCabang.clear()

                // Tambahkan item default
                listNamaCabang.add("Pilih Cabang")

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
                    this@EditPelangganActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setInitialData() {
        // Set initial data
        etNama.setText(pelanggan.namaPelanggan)
        etAlamat.setText(pelanggan.alamatPelanggan)
        etNoHP.setText(pelanggan.noHPPelanggan)

        // Set spinner selection berdasarkan cabang pelanggan
        val cabangPosition = listNamaCabang.indexOf(pelanggan.cabangPelanggan)
        if (cabangPosition != -1) {
            etCabang.setSelection(cabangPosition)
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etNama.isEnabled = editable
        etAlamat.isEnabled = editable
        etNoHP.isEnabled = editable
        etCabang.isEnabled = editable

        val backgroundRes = if (editable) {
            R.drawable.edittext_background_enabled
        } else {
            R.drawable.edittext_background_disabled
        }

        etNama.setBackgroundResource(backgroundRes)
        etAlamat.setBackgroundResource(backgroundRes)
        etNoHP.setBackgroundResource(backgroundRes)
        // Note: Spinner background mungkin perlu handling khusus jika diperlukan
    }

    private fun updatePelanggan() {
        val selectedCabangPosition = etCabang.selectedItemPosition

        // Validate inputs
        if (etNama.text.toString().isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }
        if (selectedCabangPosition == 0) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil nama cabang yang dipilih
        val namaCabang = listNamaCabang[selectedCabangPosition]

        val updatedPelanggan = ModelPelanggan(
            idPelanggan = pelanggan.idPelanggan,
            namaPelanggan = etNama.text.toString(),
            alamatPelanggan = etAlamat.text.toString(),
            noHPPelanggan = etNoHP.text.toString(),
            cabangPelanggan = namaCabang,
            tanggalTerdaftar = pelanggan.tanggalTerdaftar
        )

        database.getReference("pelanggan").child(pelanggan.idPelanggan ?: "")
            .setValue(updatedPelanggan)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pelanggan berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}