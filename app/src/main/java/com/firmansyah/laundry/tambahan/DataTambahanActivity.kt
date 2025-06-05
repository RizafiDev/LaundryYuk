package com.firmansyah.laundry.tambahan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataTambahanAdapter
import com.firmansyah.laundry.model.ModelTambahan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataTambahanActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tambahanList: ArrayList<ModelTambahan>
    private lateinit var adapter: DataTambahanAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fabTambah: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_tambahan)

        recyclerView = findViewById(R.id.rvDATA_TAMBAHAN)
        fabTambah = findViewById(R.id.fab_DATA_TAMBAH_TAMBAH)

        recyclerView.layoutManager = LinearLayoutManager(this)
        tambahanList = ArrayList()
        adapter = DataTambahanAdapter(tambahanList)
        recyclerView.adapter = adapter

        databaseReference = FirebaseDatabase.getInstance().getReference("tambahan")

        getTambahanData()

        fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahTambahanActivity::class.java))
        }
    }

    private fun getTambahanData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tambahanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(ModelTambahan::class.java)
                    if (item != null) {
                        // PENTING: Set ID dari Firebase key
                        item.idLayanan = dataSnapshot.key
                        tambahanList.add(item)
                    }
                }
                // Menggunakan updateData() untuk konsistensi dengan adapter
                adapter.updateData(tambahanList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika kembali dari edit activity
        getTambahanData()
    }
}