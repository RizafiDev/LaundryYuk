// Fixed PilihLayananAdapter
package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelLayanan

class PilihLayananAdapter(
    private val list: List<ModelLayanan>,
    private val onItemClick: (ModelLayanan) -> Unit
) : RecyclerView.Adapter<PilihLayananAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaLayanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDataHargaLayanan)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangLayanan)

        fun bind(layanan: ModelLayanan) {
            try {
                tvNama.text = "Layanan ${layanan.namaLayanan ?: "-"}"
                tvHarga.text = "Rp. ${layanan.hargaLayanan ?: "0"},-"
                tvCabang.text = "Cabang ${layanan.cabangLayanan ?: "-"}"

                itemView.setOnClickListener {
                    onItemClick(layanan)
                }
            } catch (e: Exception) {
                // Handle binding error gracefully
                tvNama.text = "Error loading data"
                tvHarga.text = "Rp. 0,-"
                tvCabang.text = "Unknown"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_transaksi_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= 0 && position < list.size) {
            holder.bind(list[position])
        }
    }
}