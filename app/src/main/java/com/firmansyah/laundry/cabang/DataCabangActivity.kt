package com.firmansyah.laundry.cabang

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelCabang
import com.firmansyah.laundry.adapter.DataCabangAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.firmansyah.laundry.model.ModelPegawai
import com.firmansyah.laundry.pegawai.EditPegawaiActivity
import com.google.firebase.database.*

class DataCabangActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var rvDataCabang: RecyclerView
    private lateinit var fabTambahCabang: FloatingActionButton
    private lateinit var fabPilihCabang: FloatingActionButton
    private lateinit var etSearchCabang: EditText

    private lateinit var cabangList: ArrayList<ModelCabang>
    private lateinit var cabangAdapter: DataCabangAdapter
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_cabang)

        cabangList = ArrayList()
        initViews()
        initRecyclerView()
        setupClickListeners()
        setupSearchFilter()
        loadDataCabang()
    }

    private fun initViews() {
        rvDataCabang = findViewById(R.id.rvDATA_CABANG)
        fabTambahCabang = findViewById(R.id.fabTambahCabang)
        fabPilihCabang = findViewById(R.id.fabPilihCabang)
        etSearchCabang = findViewById(R.id.etSearchCabang)
    }

    private fun initRecyclerView() {
        cabangAdapter = DataCabangAdapter(
            cabangList,
            onItemClick = { cabang ->
                if (!isEditMode) {
                    val intent = Intent(this, EditCabangActivity::class.java)
                    intent.putExtra("CABANG_DATA", cabang)
                    startActivity(intent)
                }
            },
            onHubungiClick = { cabang ->
                // Handle hubungi action
                hubungiCabang(cabang)
            },
            onSelectionChanged = { selectedCount ->
                updateFabPilihCabang(selectedCount)
            }
        )

        rvDataCabang.apply {
            layoutManager = LinearLayoutManager(this@DataCabangActivity)
            adapter = cabangAdapter
        }
    }
    private fun hubungiCabang(cabang: ModelCabang) {
        val phoneNumber = cabang.noHpCabang
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Nomor HP tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Try to open WhatsApp first
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber")
            }

            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent)
            } else {
                // If WhatsApp is not available, open phone dialer
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(dialIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka aplikasi", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupClickListeners() {
        // FAB Tambah Cabang
        fabTambahCabang.setOnClickListener {
            val intent = Intent(this, TambahCabangActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_TAMBAH_CABANG)
        }

        // FAB Pilih/Edit Cabang
        fabPilihCabang.setOnClickListener {
            if (!isEditMode) {
                enterEditMode()
            } else {
                val selectedCount = cabangAdapter.getSelectedCount()
                if (selectedCount > 0) {
                    showDeleteConfirmationDialog()
                } else {
                    exitEditMode()
                }
            }
        }
    }

    private fun setupSearchFilter() {
        etSearchCabang.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cabangAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadDataCabang() {
        // Remove listener lama jika ada
        cabangListener?.let { myRef.removeEventListener(it) }

        val query = myRef.orderByChild("namaCabang").limitToLast(100)
        cabangListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    cabangList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(ModelCabang::class.java)
                        cabang?.let {
                            // Pastikan ID cabang tersimpan
                            it.idCabang = dataSnapshot.key
                            cabangList.add(it)
                        }
                    }
                    cabangAdapter.updateData(cabangList)

                    if (cabangList.isEmpty()) {
                        Toast.makeText(this@DataCabangActivity, "Belum ada data cabang", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cabangList.clear()
                    cabangAdapter.updateData(cabangList)
                    Toast.makeText(this@DataCabangActivity, "Tidak ada data cabang", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataCabangActivity,
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        query.addValueEventListener(cabangListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener saat activity destroy
        cabangListener?.let { myRef.removeEventListener(it) }
    }

    private fun enterEditMode() {
        isEditMode = true
        cabangAdapter.setEditMode(true)

        // Update FAB icon
        fabPilihCabang.setImageResource(R.drawable.xmark_solid)
    }

    private fun exitEditMode() {
        isEditMode = false
        cabangAdapter.setEditMode(false)

        // Reset FAB icon
        fabPilihCabang.setImageResource(R.drawable.pen_solid)
    }

    private fun updateFabPilihCabang(selectedCount: Int) {
        if (isEditMode) {
            if (selectedCount > 0) {
                // Ada item yang dipilih, tampilkan icon trash
                fabPilihCabang.setImageResource(R.drawable.trash_solid)
                fabPilihCabang.imageTintList = resources.getColorStateList(android.R.color.holo_red_dark)
            } else {
                // Tidak ada item yang dipilih, tampilkan icon X
                fabPilihCabang.setImageResource(R.drawable.xmark_solid)
                fabPilihCabang.imageTintList = resources.getColorStateList(R.color.primary_color, null)
            }
        }
    }

    private fun handleNormalClick(cabang: ModelCabang) {
        // Handle normal click - bisa untuk navigasi ke detail atau action lainnya
        Toast.makeText(this, "Cabang: ${cabang.namaCabang}", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog() {
        val selectedCount = cabangAdapter.getSelectedCount()
        val message = if (selectedCount == 1) {
            "Apakah Anda yakin ingin menghapus 1 cabang?"
        } else {
            "Apakah Anda yakin ingin menghapus $selectedCount cabang?"
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage(message)
            .setPositiveButton("Hapus") { _, _ ->
                deleteSelectedCabang()
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun deleteSelectedCabang() {
        val selectedIds = cabangAdapter.getSelectedItemIds()
        val totalCount = selectedIds.size

        if (totalCount == 0) {
            exitEditMode()
            return
        }

        // Disable FAB sementara proses delete
        fabPilihCabang.isEnabled = false

        var deletedCount = 0
        var hasError = false

        selectedIds.forEach { id ->
            myRef.child(id).removeValue().addOnCompleteListener { task ->
                deletedCount++

                if (!task.isSuccessful) {
                    hasError = true
                }

                // Jika semua delete operation selesai
                if (deletedCount == totalCount) {
                    fabPilihCabang.isEnabled = true

                    if (hasError) {
                        Toast.makeText(
                            this,
                            "Gagal menghapus beberapa data",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "$totalCount cabang berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    exitEditMode()
                    // Firebase listener di loadDataCabang() akan otomatis refresh data
                }
            }
        }
    }

    private var cabangListener: ValueEventListener? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TAMBAH_CABANG && resultCode == RESULT_OK) {
            Toast.makeText(this, "Cabang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Data akan terupdate otomatis melalui Firebase listener
    }

    override fun onBackPressed() {
        if (isEditMode) {
            exitEditMode()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val REQUEST_CODE_TAMBAH_CABANG = 1001
    }
}