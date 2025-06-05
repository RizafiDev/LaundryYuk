package com.firmansyah.laundry.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.tambahan.EditTambahanActivity
import com.firmansyah.laundry.model.ModelTambahan
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Filter
import android.widget.Filterable

class DataTambahanAdapter(
    private var listFull: ArrayList<ModelTambahan>
) : RecyclerView.Adapter<DataTambahanAdapter.ViewHolder>(), Filterable {

    private var listFiltered = ArrayList<ModelTambahan>(listFull)
    private val database = FirebaseDatabase.getInstance().getReference("tambahan")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listFiltered[position]
        holder.nama.text = item.namaLayanan ?: "Tidak Ada Nama"
        holder.harga.text = "Rp. ${item.hargaLayanan ?: "-"}"

        // Handle button clicks
        holder.btnHapus.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, item)
        }

        holder.btnLihat.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditTambahanActivity::class.java)
            intent.putExtra("TAMBAHAN_DATA", item)
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun showDeleteConfirmationDialog(context: android.content.Context, item: ModelTambahan) {
        AlertDialog.Builder(context)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus layanan tambahan \"${item.namaLayanan}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteItem(context, item)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteItem(context: android.content.Context, item: ModelTambahan) {
        val itemId = item.idLayanan
        if (itemId.isNullOrEmpty()) {
            Toast.makeText(context, "ID layanan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(itemId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Layanan tambahan berhasil dihapus", Toast.LENGTH_SHORT).show()
                // Biarkan Firebase listener yang menangani update data
                // Tidak perlu manipulasi list manual
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Gagal menghapus layanan tambahan: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = listFiltered.size

    fun updateData(newList: ArrayList<ModelTambahan>) {
        listFull = ArrayList(newList)
        listFiltered = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(keyword: String) {
        this.filter.filter(keyword)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keyword = constraint.toString().lowercase(Locale.ROOT)
                val filtered = if (keyword.isEmpty()) {
                    ArrayList(listFull)
                } else {
                    listFull.filter {
                        it.namaLayanan?.lowercase()?.contains(keyword) == true ||
                                it.hargaLayanan?.toString()?.contains(keyword) == true
                    } as ArrayList<ModelTambahan>
                }

                val result = FilterResults()
                result.values = filtered
                return result
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let { values ->
                    listFiltered.clear()
                    listFiltered.addAll(values as ArrayList<ModelTambahan>)
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.tv_nama_tambahan)
        val harga: TextView = itemView.findViewById(R.id.tv_harga_tambahan)
        val btnHapus: TextView = itemView.findViewById(R.id.btnHapus)
        val btnLihat: TextView = itemView.findViewById(R.id.btnLihat)
    }
}