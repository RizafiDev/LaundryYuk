package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPelanggan

class PilihPelangganAdapter(
    private val list: List<ModelPelanggan>,
    private val onItemClick: (ModelPelanggan) -> Unit
) : RecyclerView.Adapter<PilihPelangganAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaPelanggan)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPelanggan)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangPelanggan)

        fun bind(pelanggan: ModelPelanggan) {
            try {
                tvNama.text = pelanggan.namaPelanggan ?: "-"
                tvNoHp.text = pelanggan.noHPPelanggan ?: "-"
                tvAlamat.text = pelanggan.alamatPelanggan ?: "-"
                tvCabang.text = "Cabang ${pelanggan.cabangPelanggan ?: "-"}"

                itemView.setOnClickListener {
                    onItemClick(pelanggan)
                }
            } catch (e: Exception) {
                // Handle binding error gracefully
                tvNama.text = "Error loading data"
                tvNoHp.text = "-"
                tvAlamat.text = "-"
                tvCabang.text = "Unknown"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_transaksi_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= 0 && position < list.size) {
            holder.bind(list[position])
        }
    }
}