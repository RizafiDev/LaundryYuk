package com.firmansyah.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.PilihPelangganAdapter
import com.firmansyah.laundry.model.ModelPelanggan
import com.google.firebase.database.*

class PilihPelangganActivity : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private val listPelanggan = mutableListOf<ModelPelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_pelanggan)

        recyclerView = findViewById(R.id.rvDATA_TRANSAKSI_PELANGGAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbRef = FirebaseDatabase.getInstance().getReference("pelanggan")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listPelanggan.clear()
                for (data in snapshot.children) {
                    val pelanggan = data.getValue(ModelPelanggan::class.java)
                    if (pelanggan != null) {
                        listPelanggan.add(pelanggan)
                    }
                }

                val adapter = PilihPelangganAdapter(listPelanggan) { selectedPelanggan ->
                    val intent = Intent()
                    intent.putExtra("idPelanggan", selectedPelanggan.idPelanggan)
                    intent.putExtra("namaPelanggan", selectedPelanggan.namaPelanggan)
                    intent.putExtra("noHPPelanggan", selectedPelanggan.noHPPelanggan)
                    setResult(RESULT_OK, intent)
                    finish()
                }


                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PilihPelangganActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
