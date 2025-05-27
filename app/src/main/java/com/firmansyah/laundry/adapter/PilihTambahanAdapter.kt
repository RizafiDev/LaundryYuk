package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelTambahan

class PilihTambahanAdapter(
    private val list: List<ModelTambahan>,
    private val onItemClick: (ModelTambahan) -> Unit
) : RecyclerView.Adapter<PilihTambahanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaLayanan)
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDataHargaLayanan)

        fun bind(tambahan: ModelTambahan) {
            tambahan.apply {
                tvNama.text = namaLayanan ?: "-"
                tvID.text = "ID: ${idLayanan ?: "-"}"
                tvHarga.text = "Harga: Rp ${hargaLayanan ?: "0"}"
            }

            itemView.setOnClickListener {
                onItemClick(tambahan)
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
        if (position in 0 until list.size) {
            holder.bind(list[position])
        }
    }
}