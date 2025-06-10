package com.firmansyah.laundry.layanan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.BaseActivity
import com.firmansyah.laundry.R
import com.firmansyah.laundry.adapter.DataLayananAdapter
import com.firmansyah.laundry.adapter.DataTambahanAdapter
import com.firmansyah.laundry.model.ModelLayanan
import com.firmansyah.laundry.model.ModelTambahan
import com.firmansyah.laundry.tambahan.TambahTambahanActivity
import com.google.firebase.database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataLayananActivity : BaseActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val layananRef = database.getReference("layanan")
    private val tambahanRef = database.getReference("tambahan")

    private lateinit var rvDataLayanan: RecyclerView
    private lateinit var fabTambahLayanan: FloatingActionButton
    private lateinit var tvLayananUtama: TextView
    private lateinit var tvLayananTambahan: TextView
    private lateinit var etSearchLayanan: EditText

    private lateinit var layananList: ArrayList<ModelLayanan>
    private lateinit var tambahanList: ArrayList<ModelTambahan>
    private lateinit var layananAdapter: DataLayananAdapter
    private lateinit var tambahanAdapter: DataTambahanAdapter

    private var isShowingTambahan = false

    private fun init() {
        rvDataLayanan = findViewById(R.id.rvDATA_LAYANAN)
        fabTambahLayanan = findViewById(R.id.fabTambahLayanan)
        tvLayananUtama = findViewById(R.id.tvLayananUtama)
        tvLayananTambahan = findViewById(R.id.tvLayananTambahan)
        etSearchLayanan = findViewById(R.id.etSearchLayanan)

        rvDataLayanan.layoutManager = LinearLayoutManager(this)

        // Initialize adapters
        layananAdapter = DataLayananAdapter(layananList)
        tambahanAdapter = DataTambahanAdapter(tambahanList)

        // Set initial adapter
        rvDataLayanan.adapter = layananAdapter

        // Setup search functionality
        setupSearch()

        // Set click listeners for category toggle
        tvLayananUtama.setOnClickListener {
            if (isShowingTambahan) {
                switchToLayananUtama()
            }
        }

        tvLayananTambahan.setOnClickListener {
            if (!isShowingTambahan) {
                switchToLayananTambahan()
            }
        }
    }

    private fun setupSearch() {
        etSearchLayanan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (isShowingTambahan) {
                    tambahanAdapter.filter(searchText)
                } else {
                    layananAdapter.filter(searchText)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun switchToLayananUtama() {
        isShowingTambahan = false
        updateCategoryButtons()
        updateSearchHint()
        clearSearchText()

        // Animate from right to left (tambahan -> utama)
        animateRecyclerView(slideToLeft = true) {
            rvDataLayanan.adapter = layananAdapter
        }
    }

    private fun switchToLayananTambahan() {
        isShowingTambahan = true
        updateCategoryButtons()
        updateSearchHint()
        clearSearchText()

        // Animate from left to right (utama -> tambahan)
        animateRecyclerView(slideToLeft = false) {
            rvDataLayanan.adapter = tambahanAdapter
        }
    }

    private fun updateSearchHint() {
        etSearchLayanan.hint = if (isShowingTambahan) {
            "Cari layanan tambahan..."
        } else {
            "Cari layanan utama..."
        }
    }

    private fun clearSearchText() {
        etSearchLayanan.setText("")
    }

    private fun animateRecyclerView(slideToLeft: Boolean, onAnimationEnd: () -> Unit) {
        val width = rvDataLayanan.width.toFloat()

        if (slideToLeft) {
            // Moving from tambahan to utama: slide out to left, slide in from right
            val slideOut = TranslateAnimation(0f, -width, 0f, 0f)
            slideOut.duration = 200
            slideOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    // Change adapter
                    onAnimationEnd()

                    // Slide in from right
                    val slideIn = TranslateAnimation(width, 0f, 0f, 0f)
                    slideIn.duration = 200
                    rvDataLayanan.startAnimation(slideIn)
                }
            })
            rvDataLayanan.startAnimation(slideOut)
        } else {
            // Moving from utama to tambahan: slide out to right, slide in from left
            val slideOut = TranslateAnimation(0f, width, 0f, 0f)
            slideOut.duration = 200
            slideOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    // Change adapter
                    onAnimationEnd()

                    // Slide in from left
                    val slideIn = TranslateAnimation(-width, 0f, 0f, 0f)
                    slideIn.duration = 200
                    rvDataLayanan.startAnimation(slideIn)
                }
            })
            rvDataLayanan.startAnimation(slideOut)
        }
    }

    private fun updateCategoryButtons() {
        if (isShowingTambahan) {
            // Update button styles for "Layanan Tambahan" selected
            tvLayananUtama.setBackgroundResource(R.drawable.button_background_outlined)
            tvLayananUtama.setTextColor(getColor(R.color.primary_color))

            tvLayananTambahan.setBackgroundResource(R.drawable.button_background)
            tvLayananTambahan.setTextColor(getColor(R.color.white))
        } else {
            // Update button styles for "Layanan Utama" selected
            tvLayananUtama.setBackgroundResource(R.drawable.button_background)
            tvLayananUtama.setTextColor(getColor(R.color.white))

            tvLayananTambahan.setBackgroundResource(R.drawable.button_background_outlined)
            tvLayananTambahan.setTextColor(getColor(R.color.primary_color))
        }
    }

    private fun getLayananData() {
        val query = layananRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    layananList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(ModelLayanan::class.java)
                        layanan?.let {
                            // Pastikan ID tersimpan
                            it.idLayanan = dataSnapshot.key
                            layananList.add(it)
                        }
                    }
                    layananAdapter.updateData(layananList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun getTambahanData() {
        tambahanRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tambahanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(ModelTambahan::class.java)
                    if (item != null) {
                        // Pastikan ID tersimpan
                        item.idLayanan = dataSnapshot.key
                        tambahanList.add(item)
                    }
                }
                tambahanAdapter.updateData(tambahanList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_layanan)

        layananList = ArrayList()
        tambahanList = ArrayList()

        init()
        getLayananData()
        getTambahanData()

        fabTambahLayanan.setOnClickListener {
            val intent = if (isShowingTambahan) {
                Intent(this, TambahTambahanActivity::class.java)
            } else {
                Intent(this, TambahLayananActivity::class.java)
            }
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from edit activity
        getLayananData()
        getTambahanData()
    }
}