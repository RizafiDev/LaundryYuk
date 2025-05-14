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
        val tvID: TextView = itemView.findViewById(R.id.tvDataIDPelanggan)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPelanggan)

        fun bind(pelanggan: ModelPelanggan) {
            tvNama.text = pelanggan.namaPelanggan
            tvID.text = "ID: ${pelanggan.idPelanggan}"
            tvNoHp.text = "No HP: ${pelanggan.noHPPelanggan}"
            tvAlamat.text = "Alamat: ${pelanggan.alamatPelanggan}"

            itemView.setOnClickListener {
                onItemClick(pelanggan)
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
        holder.bind(list[position])
    }
}
