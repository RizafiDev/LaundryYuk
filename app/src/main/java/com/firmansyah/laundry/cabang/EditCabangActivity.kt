package com.firmansyah.laundry.cabang

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.KabupatenKota
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.model.Provinsi
import com.firmansyah.laundry.network.ApiClient
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditCabangActivity : BaseActivity() {

    // Firebase Database
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var spinnerProvinsi: Spinner
    private lateinit var spinnerKabKota: Spinner
    private lateinit var etNamaCabang: EditText
    private lateinit var etAlamatCabang: EditText
    private lateinit var etNoHp: EditText
    private lateinit var btnSimpan: TextView

    private var listProvinsi = mutableListOf<Provinsi>()
    private var listKabupatenKota = mutableListOf<KabupatenKota>()
    private var selectedProvinsiId = ""

    private lateinit var cabang: ModelCabang
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        // Get cabang data from intent
        cabang = intent.getParcelableExtra("CABANG_DATA") ?: ModelCabang()

        // Load provinsi data first, then set initial data
        loadProvinsiData()

        setupSpinners()
        setupClickListeners()

        // Initially set fields to readonly
        setFieldsEditable(false)
    }

    private fun initViews() {
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi)
        spinnerKabKota = findViewById(R.id.spinnerKabKota)
        etNamaCabang = findViewById(R.id.etTambahNamaCabang)
        etAlamatCabang = findViewById(R.id.etTambahAlamatCabang)
        etNoHp = findViewById(R.id.etTambahNoHpCabang)
        btnSimpan = findViewById(R.id.btnSimpanCabang)
    }

    private fun setFieldsEditable(editable: Boolean) {
        etNamaCabang.isEnabled = editable
        etAlamatCabang.isEnabled = editable
        etNoHp.isEnabled = editable
        spinnerProvinsi.isEnabled = editable
        spinnerKabKota.isEnabled = editable

        val backgroundRes = if (editable) {
            R.drawable.edittext_background_enabled
        } else {
            R.drawable.edittext_background_disabled
        }

        etNamaCabang.setBackgroundResource(backgroundRes)
        etAlamatCabang.setBackgroundResource(backgroundRes)
        etNoHp.setBackgroundResource(backgroundRes)
    }

    private fun setInitialData() {
        // Set text fields
        etNamaCabang.setText(cabang.namaCabang)
        etAlamatCabang.setText(cabang.alamatCabang)
        etNoHp.setText(cabang.noHpCabang)

        // Set spinner selections after data is loaded
        setSpinnerSelections()
    }

    private fun setSpinnerSelections() {
        // Set provinsi selection
        val provinsiPosition = listProvinsi.indexOfFirst { it.name == cabang.provinsiCabang }
        if (provinsiPosition >= 0) {
            spinnerProvinsi.setSelection(provinsiPosition + 1) // +1 karena ada hint
            selectedProvinsiId = listProvinsi[provinsiPosition].id

            // Load kabupaten/kota data for selected provinsi
            loadKabupatenKotaData(selectedProvinsiId) {
                // Set kab/kota selection after data is loaded
                val kabKotaPosition = listKabupatenKota.indexOfFirst { it.name == cabang.kabKotaCabang }
                if (kabKotaPosition >= 0) {
                    spinnerKabKota.setSelection(kabKotaPosition + 1) // +1 karena ada hint
                }
            }
        }
    }

    private fun setupSpinners() {
        // Setup Spinner Provinsi
        spinnerProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && isEditMode) { // Skip hint item and only when in edit mode
                    selectedProvinsiId = listProvinsi[position - 1].id
                    loadKabupatenKotaData(selectedProvinsiId)
                    spinnerKabKota.isEnabled = true
                } else if (position == 0) {
                    if (isEditMode) {
                        spinnerKabKota.isEnabled = false
                        spinnerKabKota.adapter = null
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Spinner Kab/Kota
        spinnerKabKota.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle selection if needed
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadProvinsiData() {
        ApiClient.apiService.getProvinsi().enqueue(object : Callback<List<Provinsi>> {
            override fun onResponse(call: Call<List<Provinsi>>, response: Response<List<Provinsi>>) {
                if (response.isSuccessful) {
                    response.body()?.let { provinsiList ->
                        listProvinsi.clear()
                        listProvinsi.addAll(provinsiList)
                        setupProvinsiSpinner()
                        // Set initial data after provinsi data is loaded
                        setInitialData()
                    }
                } else {
                    Toast.makeText(this@EditCabangActivity, "Gagal memuat data provinsi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Provinsi>>, t: Throwable) {
                Toast.makeText(this@EditCabangActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadKabupatenKotaData(provinsiId: String, onComplete: (() -> Unit)? = null) {
        ApiClient.apiService.getKabupatenKota(provinsiId).enqueue(object : Callback<List<KabupatenKota>> {
            override fun onResponse(call: Call<List<KabupatenKota>>, response: Response<List<KabupatenKota>>) {
                if (response.isSuccessful) {
                    response.body()?.let { kabKotaList ->
                        listKabupatenKota.clear()
                        listKabupatenKota.addAll(kabKotaList)
                        setupKabKotaSpinner()
                        onComplete?.invoke()
                    }
                } else {
                    Toast.makeText(this@EditCabangActivity, "Gagal memuat data kabupaten/kota", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<KabupatenKota>>, t: Throwable) {
                Toast.makeText(this@EditCabangActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupProvinsiSpinner() {
        val provinsiNames = mutableListOf("Pilih Provinsi")
        provinsiNames.addAll(listProvinsi.map { it.name })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinsiNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvinsi.adapter = adapter
    }

    private fun setupKabKotaSpinner() {
        val kabKotaNames = mutableListOf("Pilih Kabupaten/Kota")
        kabKotaNames.addAll(listKabupatenKota.map { it.name })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kabKotaNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKabKota.adapter = adapter
    }

    private fun setupClickListeners() {
        btnSimpan.setOnClickListener {
            if (isEditMode) {
                // Save changes
                validasi()
            } else {
                // Switch to edit mode
                isEditMode = true
                setFieldsEditable(true)
                btnSimpan.text = "Simpan Perubahan"
            }
        }
    }

    private fun validasi() {
        val namaCabang = etNamaCabang.text.toString().trim()
        val alamat = etAlamatCabang.text.toString().trim()
        val noHp = etNoHp.text.toString().trim()
        val provinsiPosition = spinnerProvinsi.selectedItemPosition
        val kabKotaPosition = spinnerKabKota.selectedItemPosition

        if (namaCabang.isEmpty()) {
            etNamaCabang.error = "Nama cabang tidak boleh kosong"
            etNamaCabang.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamatCabang.error = "Alamat tidak boleh kosong"
            etAlamatCabang.requestFocus()
            return
        }
        if (provinsiPosition == 0) {
            Toast.makeText(this, "Silakan pilih provinsi", Toast.LENGTH_SHORT).show()
            return
        }
        if (kabKotaPosition == 0) {
            Toast.makeText(this, "Silakan pilih kabupaten/kota", Toast.LENGTH_SHORT).show()
            return
        }
        if (noHp.isEmpty()) {
            etNoHp.error = "No HP tidak boleh kosong"
            etNoHp.requestFocus()
            return
        }
        if (!noHp.matches(Regex("\\d{10,13}"))) {
            etNoHp.error = "Nomor HP harus terdiri dari 10-13 angka"
            etNoHp.requestFocus()
            return
        }

        // Jika semua validasi lolos, update data
        val selectedProvinsi = listProvinsi[provinsiPosition - 1].name
        val selectedKabKota = listKabupatenKota[kabKotaPosition - 1].name

        updateDataCabang(namaCabang, alamat, selectedProvinsi, selectedKabKota, noHp)
    }

    private fun updateDataCabang(nama: String, alamat: String, provinsi: String, kabKota: String, noHp: String) {
        val updatedCabang = ModelCabang(
            idCabang = cabang.idCabang,
            namaCabang = nama,
            alamatCabang = alamat,
            provinsiCabang = provinsi,
            kabKotaCabang = kabKota,
            noHpCabang = noHp,
            tanggalTerdaftar = cabang.tanggalTerdaftar // Keep original registration date
        )

        myRef.child(cabang.idCabang ?: "")
            .setValue(updatedCabang)
            .addOnSuccessListener {
                Toast.makeText(this, "Data cabang berhasil diupdate!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data cabang", Toast.LENGTH_SHORT).show()
            }
    }
}