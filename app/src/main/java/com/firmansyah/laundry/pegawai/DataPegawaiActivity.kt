package com.firmansyah.laundry.pegawai

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataPegawaiAdapter
import com.firmansyah.laundry.model.ModelPegawai
import com.google.firebase.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataPegawaiActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var rvDataPegawai: RecyclerView
    private lateinit var fabTambahPegawai: FloatingActionButton
    private lateinit var pegawaiList: ArrayList<ModelPegawai>

    private fun init() {
        rvDataPegawai = findViewById(R.id.rvDATA_PEGAWAI)
        fabTambahPegawai = findViewById(R.id.fabTambahPegawai)

        rvDataPegawai.layoutManager = LinearLayoutManager(this)
    }

    private fun getDATA() {
        val query = myRef.orderByChild("idPegawai").limitToLast(100)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    pegawaiList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                        pegawai?.let { pegawaiList.add(it) }
                    }
                    val adapter = DataPegawaiAdapter(pegawaiList)
                    rvDataPegawai.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)

        pegawaiList = ArrayList()
        init()
        getDATA()

        fabTambahPegawai.setOnClickListener {
            val intent = Intent(this, TambahPegawaiActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
