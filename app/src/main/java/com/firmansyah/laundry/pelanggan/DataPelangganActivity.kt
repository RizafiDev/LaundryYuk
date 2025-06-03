package com.firmansyah.laundry.pelanggan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataPelangganAdapter
import com.firmansyah.laundry.model.ModelPelanggan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataPelangganActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")

    private lateinit var rvDataPelanggan: RecyclerView
    private lateinit var fabTambahPelanggan: FloatingActionButton
    private lateinit var etSearchPelanggan: EditText

    private lateinit var pelangganList: ArrayList<ModelPelanggan>
    private lateinit var adapter: DataPelangganAdapter

    private fun init() {
        rvDataPelanggan = findViewById(R.id.rvDATA_PELANGGAN)
        fabTambahPelanggan = findViewById(R.id.fab_DATA_PELANGGAN_TAMBAH)
        etSearchPelanggan = findViewById(R.id.etSearchPelanggan)

        rvDataPelanggan.layoutManager = LinearLayoutManager(this)
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
                            it.idPelanggan = dataSnapshot.key
                            pelangganList.add(it)
                        }
                    }

                    adapter = DataPelangganAdapter(pelangganList,
                        onItemClick = { pelanggan ->
                            val intent = Intent(this@DataPelangganActivity, EditPelangganActivity::class.java)
                            intent.putExtra("PELANGGAN_DATA", pelanggan)
                            startActivity(intent)
                        },
                        onHubungiClick = { pelanggan ->
                            val nomor = pelanggan.noHPPelanggan ?: return@DataPelangganAdapter
                            val nomorFormatted = nomor.replaceFirst("^0".toRegex(), "62")
                            val url = "https://wa.me/$nomorFormatted"
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = android.net.Uri.parse(url)
                            startActivity(intent)
                        }
                    )
                    rvDataPelanggan.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

        pelangganList = ArrayList()
        init()
        getDATA()

        etSearchPelanggan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabTambahPelanggan.setOnClickListener {
            val intent = Intent(this, TambahPelangganActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
