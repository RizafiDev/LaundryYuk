package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelLayanan

class DataLayananAdapter(private val listLayanan: ArrayList<ModelLayanan>) :
    RecyclerView.Adapter<DataLayananAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]
        holder.tvDataIDLayanan.text = item.idLayanan ?: ""
        holder.tvNama.text = item.namaLayanan ?: ""
        holder.tvHarga.text = item.hargaLayanan ?: ""
        holder.tvTerdaftar.text = "Terdaftar: ${item.tanggalTerdaftar ?: "-"}"
        holder.tvCabang.text = "Cabang ${item.cabangLayanan ?: "Tidak Ada Cabang"}"

    }

    override fun getItemCount(): Int {
        return listLayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataIDLayanan: TextView = itemView.findViewById(R.id.tvDataIDLayanan)
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaLayanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDataHargaLayanan)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangLayanan)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tvDataTerdaftarLayanan)
    }
}