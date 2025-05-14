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
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDataHargaLayanan)

        fun bind(layanan: ModelLayanan) {
            tvNama.text = layanan.namaLayanan
            tvID.text = "ID: ${layanan.idLayanan}"
            tvHarga.text = "Harga: ${layanan.hargaLayanan}"

            itemView.setOnClickListener {
                onItemClick(layanan)
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
        holder.bind(list[position])
    }
}