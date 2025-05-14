package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.PilihLayananAdapter
import com.firmansyah.laundry.model.ModelLayanan
import com.google.firebase.database.*

class PilihLayananActivity : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listLayanan = mutableListOf<ModelLayanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_layanan)

        recyclerView = findViewById(R.id.rvDATA_TRANSAKSI_LAYANAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbRef = FirebaseDatabase.getInstance().getReference("layanan")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLayanan.clear()
                for (data in snapshot.children) {
                    val layanan = data.getValue(ModelLayanan::class.java)
                    if (layanan != null) {
                        listLayanan.add(layanan)
                    }
                }

                val adapter = PilihLayananAdapter(listLayanan) { selectedLayanan ->
                    val intent = Intent()
                    intent.putExtra("idLayanan", selectedLayanan.idLayanan)
                    intent.putExtra("namaLayanan", selectedLayanan.namaLayanan)
                    intent.putExtra("hargaLayanan", selectedLayanan.hargaLayanan)
                    setResult(RESULT_OK, intent)
                    finish()
                }


                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihLayananActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
