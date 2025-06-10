package com.firmansyah.laundry.pegawai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.model.ModelPegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditPegawaiActivity : BaseActivity() {
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etCabang: Spinner
    private lateinit var btnSimpan: TextView

    private val database = FirebaseDatabase.getInstance()
    private val cabangRef = database.getReference("cabang")
    private lateinit var pegawai: ModelPegawai
    private var isEditMode = false

    private var listCabang = mutableListOf<ModelCabang>()
    private var listNamaCabang = mutableListOf<String>()
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pegawai)

        // Initialize views
        etNama = findViewById(R.id.etTambahNamaPegawai)
        etAlamat = findViewById(R.id.etTambahAlamatPegawai)
        etNoHP = findViewById(R.id.etTambahNoHpPegawai)
        etCabang = findViewById(R.id.etTambahCabangPegawai)
        btnSimpan = findViewById(R.id.btnSimpanPegawai)

        // Get pegawai data from intent
        pegawai = intent.getParcelableExtra("PEGAWAI_DATA") ?: ModelPegawai()

        // Setup Spinner
        setupSpinner()

        // Load data cabang dari Firebase
        loadDataCabang()

        // Set initial data
        setInitialData()

        // Initially set fields to readonly
        setFieldsEditable(false)

        btnSimpan.setOnClickListener {
            if (isEditMode) {
                // Save changes
                updatePegawai()
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

                // Set selected cabang after data loaded
                setSelectedCabang()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@EditPegawaiActivity,
                    "Gagal memuat data cabang: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setInitialData() {
        etNama.setText(pegawai.namaPegawai)
        etAlamat.setText(pegawai.alamatPegawai)
        etNoHP.setText(pegawai.noHPPegawai)
        // Cabang akan di-set setelah data cabang dimuat
    }

    private fun setSelectedCabang() {
        // Cari posisi cabang pegawai dalam list
        val cabangPegawai = pegawai.cabangPegawai
        if (!cabangPegawai.isNullOrEmpty()) {
            val position = listNamaCabang.indexOf(cabangPegawai)
            if (position > 0) { // > 0 karena index 0 adalah "Pilih Cabang"
                etCabang.setSelection(position)
            }
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
        // Note: Spinner background mungkin perlu treatment khusus jika diperlukan
    }

    private fun updatePegawai() {
        // Validate inputs
        if (etNama.text.toString().isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }

        if (etAlamat.text.toString().isEmpty()) {
            etAlamat.error = "Alamat tidak boleh kosong"
            return
        }

        if (etNoHP.text.toString().isEmpty()) {
            etNoHP.error = "No HP tidak boleh kosong"
            return
        }

        val selectedCabangPosition = etCabang.selectedItemPosition
        if (selectedCabangPosition == 0) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate phone number format
        val noHP = etNoHP.text.toString().trim()
        if (!noHP.matches(Regex("\\d{10,13}"))) {
            etNoHP.error = "Nomor HP harus terdiri dari 10-13 angka"
            etNoHP.requestFocus()
            return
        }

        // Ambil nama cabang yang dipilih
        val namaCabang = listNamaCabang[selectedCabangPosition]

        val updatedPegawai = ModelPegawai(
            idPegawai = pegawai.idPegawai,
            namaPegawai = etNama.text.toString(),
            alamatPegawai = etAlamat.text.toString(),
            noHPPegawai = noHP,
            cabangPegawai = namaCabang,
            tanggalTerdaftar = pegawai.tanggalTerdaftar
        )

        database.getReference("pegawai").child(pegawai.idPegawai ?: "")
            .setValue(updatedPegawai)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}