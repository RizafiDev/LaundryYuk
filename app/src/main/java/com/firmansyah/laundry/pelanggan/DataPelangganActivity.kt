package com.firmansyah.laundry.pelanggan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataPelangganAdapter
import com.firmansyah.laundry.model.ModelPelanggan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataPelangganActivity : BaseActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")

    private lateinit var rvDataPelanggan: RecyclerView
    private lateinit var fabTambahPelanggan: FloatingActionButton
    private lateinit var fabPilihPelanggan: FloatingActionButton
    private lateinit var etSearchPelanggan: EditText

    private lateinit var pelangganList: ArrayList<ModelPelanggan>
    private lateinit var adapter: DataPelangganAdapter
    private var isEditMode = false

    private fun init() {
        rvDataPelanggan = findViewById(R.id.rvDATA_PELANGGAN)
        fabTambahPelanggan = findViewById(R.id.fab_DATA_PELANGGAN_TAMBAH)
        fabPilihPelanggan = findViewById(R.id.fabPilihPelanggan)
        etSearchPelanggan = findViewById(R.id.etSearchPelanggan)

        rvDataPelanggan.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAdapter() {
        adapter = DataPelangganAdapter(
            pelangganList,
            onItemClick = { pelanggan ->
                // Navigate to detail/edit activity (hanya jika tidak dalam mode edit)
                if (!isEditMode) {
                    val intent = Intent(this@DataPelangganActivity, EditPelangganActivity::class.java)
                    intent.putExtra("PELANGGAN_DATA", pelanggan)
                    startActivity(intent)
                }
            },
            onHubungiClick = { pelanggan ->
                hubungiPelanggan(pelanggan)
            },
            onSelectionChanged = { selectedCount ->
                // Update FAB icon based on selection
                updateFabPilihPelanggan(selectedCount)
            }
        )
        rvDataPelanggan.adapter = adapter
    }

    private fun setupSearch() {
        etSearchPelanggan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFabs() {
        // FAB Tambah Pelanggan
        fabTambahPelanggan.setOnClickListener {
            val intent = Intent(this, TambahPelangganActivity::class.java)
            startActivity(intent)
        }

        // FAB Pilih/Edit Pelanggan
        fabPilihPelanggan.setOnClickListener {
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
        fabPilihPelanggan.setImageResource(R.drawable.xmark_solid)
        fabPilihPelanggan.imageTintList = resources.getColorStateList(R.color.primary_color)
    }

    private fun exitEditMode() {
        isEditMode = false
        adapter.setEditMode(false)

        // Reset FAB icons
        fabPilihPelanggan.setImageResource(R.drawable.pen_solid)
        fabPilihPelanggan.imageTintList = resources.getColorStateList(R.color.primary_color)
    }

    private fun updateFabPilihPelanggan(selectedCount: Int) {
        if (isEditMode) {
            if (selectedCount > 0) {
                // Ada item yang dipilih, tampilkan icon trash
                fabPilihPelanggan.setImageResource(R.drawable.trash_solid)
                fabPilihPelanggan.imageTintList = resources.getColorStateList(android.R.color.holo_red_dark)
            } else {
                // Tidak ada item yang dipilih, tampilkan icon X
                fabPilihPelanggan.setImageResource(R.drawable.xmark_solid)
                fabPilihPelanggan.imageTintList = resources.getColorStateList(R.color.primary_color)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedCount = adapter.getSelectedCount()
        val message = if (selectedCount == 1) {
            "Apakah Anda yakin ingin menghapus 1 pelanggan?"
        } else {
            "Apakah Anda yakin ingin menghapus $selectedCount pelanggan?"
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
            myRef.child(id).removeValue().addOnCompleteListener { task ->
                deletedCount++

                if (task.isSuccessful) {
                    if (deletedCount == totalCount) {
                        // Semua item berhasil dihapus
                        Toast.makeText(
                            this,
                            "$totalCount pelanggan berhasil dihapus",
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

    private fun getDATA() {
        val query = myRef.orderByChild("idPelanggan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    pelangganList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val pelanggan = dataSnapshot.getValue(ModelPelanggan::class.java)
                        pelanggan?.let {
                            // Pastikan ID pelanggan tersimpan
                            it.idPelanggan = dataSnapshot.key
                            pelangganList.add(it)
                        }
                    }
                    adapter.updateData(pelangganList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataPelangganActivity,
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun hubungiPelanggan(pelanggan: ModelPelanggan) {
        val phoneNumber = pelanggan.noHPPelanggan
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

        pelangganList = ArrayList()
        init()
        setupAdapter()
        setupSearch()
        setupFabs()
        getDATA()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from edit activity
        getDATA()
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