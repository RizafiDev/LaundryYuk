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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahCabangActivity : BaseActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_cabang)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupSpinners()
        loadProvinsiData()
        setupClickListeners()
    }

    private fun initViews() {
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi)
        spinnerKabKota = findViewById(R.id.spinnerKabKota)
        etNamaCabang = findViewById(R.id.etTambahNamaCabang)
        etAlamatCabang = findViewById(R.id.etTambahAlamatCabang)
        etNoHp = findViewById(R.id.etTambahNoHpPegawai)
        btnSimpan = findViewById(R.id.btnSimpanPegawai)
    }

    private fun setupSpinners() {
        // Setup Spinner Provinsi
        spinnerProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Skip hint item
                    selectedProvinsiId = listProvinsi[position - 1].id
                    loadKabupatenKotaData(selectedProvinsiId)
                    spinnerKabKota.isEnabled = true
                } else {
                    spinnerKabKota.isEnabled = false
                    spinnerKabKota.adapter = null
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
                    }
                } else {
                    Toast.makeText(this@TambahCabangActivity, "Gagal memuat data provinsi", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Provinsi>>, t: Throwable) {
                Toast.makeText(this@TambahCabangActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadKabupatenKotaData(provinsiId: String) {
        ApiClient.apiService.getKabupatenKota(provinsiId).enqueue(object : Callback<List<KabupatenKota>> {
            override fun onResponse(call: Call<List<KabupatenKota>>, response: Response<List<KabupatenKota>>) {
                if (response.isSuccessful) {
                    response.body()?.let { kabKotaList ->
                        listKabupatenKota.clear()
                        listKabupatenKota.addAll(kabKotaList)
                        setupKabKotaSpinner()
                    }
                } else {
                    Toast.makeText(this@TambahCabangActivity, "Gagal memuat data kabupaten/kota", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<KabupatenKota>>, t: Throwable) {
                Toast.makeText(this@TambahCabangActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
            validasi()
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

        // Jika semua validasi lolos, simpan data
        val selectedProvinsi = listProvinsi[provinsiPosition - 1].name
        val selectedKabKota = listKabupatenKota[kabKotaPosition - 1].name

        simpanDataCabang(namaCabang, alamat, selectedProvinsi, selectedKabKota, noHp)
    }

    private fun simpanDataCabang(nama: String, alamat: String, provinsi: String, kabKota: String, noHp: String) {
        val cabangBaru = myRef.push()
        val cabangId = cabangBaru.key ?: return

        // Tambahkan tanggal saat ini
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalTerdaftar = sdf.format(Date())

        val data = ModelCabang(
            idCabang = cabangId,
            namaCabang = nama,
            alamatCabang = alamat,
            provinsiCabang = provinsi,
            kabKotaCabang = kabKota,
            noHpCabang = noHp,
            tanggalTerdaftar = tanggalTerdaftar
        )

        cabangBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Data cabang berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data cabang", Toast.LENGTH_SHORT).show()
            }
    }
}