package com.firmansyah.laundry.pegawai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataPegawaiAdapter
import com.firmansyah.laundry.model.ModelPegawai
import com.google.firebase.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataPegawaiActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var fabTambahPegawai: FloatingActionButton
    private lateinit var fabPilihPegawai: FloatingActionButton
    private lateinit var adapter: DataPegawaiAdapter
    private lateinit var database: DatabaseReference

    private val listPegawai = ArrayList<ModelPegawai>()
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pegawai)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("pegawai")

        // Initialize views
        recyclerView = findViewById(R.id.rvDATA_PEGAWAI)
        etSearch = findViewById(R.id.etSearchPegawai)
        fabTambahPegawai = findViewById(R.id.fabTambahPegawai)
        fabPilihPegawai = findViewById(R.id.fabPilihPegawai)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup search functionality
        setupSearch()

        // Setup FAB click listeners
        setupFabs()

        // Load data from Firebase
        loadPegawaiData()
    }

    private fun setupRecyclerView() {
        adapter = DataPegawaiAdapter(
            listPegawai,
            onItemClick = { pegawai ->
                // Navigate to detail/edit activity (hanya jika tidak dalam mode edit)
                if (!isEditMode) {
                    val intent = Intent(this, EditPegawaiActivity::class.java)
                    intent.putExtra("PEGAWAI_DATA", pegawai)
                    startActivity(intent)
                }
            },
            onHubungiClick = { pegawai ->
                // Open WhatsApp or phone dialer
                hubungiPegawai(pegawai)
            },
            onSelectionChanged = { selectedCount ->
                // Update FAB icon based on selection
                updateFabPilihPegawai(selectedCount)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFabs() {
        // FAB Tambah Pegawai
        fabTambahPegawai.setOnClickListener {
            val intent = Intent(this, TambahPegawaiActivity::class.java)
            startActivity(intent)
        }

        // FAB Pilih/Edit Pegawai
        fabPilihPegawai.setOnClickListener {
            if (!isEditMode) {
                // Masuk ke mode edit
                enterEditMode()
            } else {
                val selectedCount = adapter.getSelectedCount()
                if (selectedCount > 0) {
                    // Ada item yang dipilih, tampilkan dialog konfirmasi delete
                    showDeleteConfirmationDialog()
                } else {
                    // Tidak ada item yang dipilih, keluar dari mode edit
                    exitEditMode()
                }
            }
        }
    }

    private fun enterEditMode() {
        isEditMode = true
        adapter.setEditMode(true)

        // Update FAB icons
        fabPilihPegawai.setImageResource(R.drawable.xmark_solid)
        fabPilihPegawai.imageTintList = resources.getColorStateList(R.color.primary_color)
    }

    private fun exitEditMode() {
        isEditMode = false
        adapter.setEditMode(false)

        // Reset FAB icons
        fabPilihPegawai.setImageResource(R.drawable.pen_solid)
        fabPilihPegawai.imageTintList = resources.getColorStateList(R.color.primary_color)

    }

    private fun updateFabPilihPegawai(selectedCount: Int) {
        if (isEditMode) {
            if (selectedCount > 0) {
                // Ada item yang dipilih, tampilkan icon trash
                fabPilihPegawai.setImageResource(R.drawable.trash_solid)
                fabPilihPegawai.imageTintList = resources.getColorStateList(android.R.color.holo_red_dark)
            } else {
                // Tidak ada item yang dipilih, tampilkan icon X
                fabPilihPegawai.setImageResource(R.drawable.xmark_solid)
                fabPilihPegawai.imageTintList = resources.getColorStateList(R.color.primary_color)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedCount = adapter.getSelectedCount()
        val message = if (selectedCount == 1) {
            "Apakah Anda yakin ingin menghapus 1 pegawai?"
        } else {
            "Apakah Anda yakin ingin menghapus $selectedCount pegawai?"
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage(message)
            .setPositiveButton("Hapus") { _, _ ->
                deleteSelectedItems()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteSelectedItems() {
        val selectedIds = adapter.getSelectedItemIds()
        var deletedCount = 0
        val totalCount = selectedIds.size

        selectedIds.forEach { id ->
            database.child(id).removeValue().addOnCompleteListener { task ->
                deletedCount++

                if (task.isSuccessful) {
                    if (deletedCount == totalCount) {
                        // Semua item berhasil dihapus
                        Toast.makeText(
                            this,
                            "$totalCount pegawai berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                        exitEditMode()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Gagal menghapus beberapa data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadPegawaiData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPegawai.clear()
                for (dataSnapshot in snapshot.children) {
                    val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                    pegawai?.let {
                        // Pastikan ID pegawai tersimpan
                        it.idPegawai = dataSnapshot.key
                        listPegawai.add(it)
                    }
                }
                adapter.updateData(listPegawai)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataPegawaiActivity,
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun hubungiPegawai(pegawai: ModelPegawai) {
        val phoneNumber = pegawai.noHPPegawai
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Nomor HP tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Langsung arahkan ke WhatsApp karena nomor sudah format 62
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber")
            }
            startActivity(whatsappIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from edit activity
        loadPegawaiData()
    }

    override fun onBackPressed() {
        if (isEditMode) {
            // Jika dalam mode edit, keluar dari mode edit
            exitEditMode()
        } else {
            // Jika tidak dalam mode edit, keluar dari activity
            super.onBackPressed()
        }
    }
}