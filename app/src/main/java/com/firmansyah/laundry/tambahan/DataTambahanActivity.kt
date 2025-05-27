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

        // Ambil data dari Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tambahanList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ModelTambahan::class.java)
                    if (item != null) {
                        tambahanList.add(item)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahTambahanActivity::class.java))
        }
    }
}
