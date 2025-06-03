package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPelanggan

class DataPelangganAdapter(
    private val listPelanggan: ArrayList<ModelPelanggan>,
    private val onItemClick: (ModelPelanggan) -> Unit,
    private val onHubungiClick: (ModelPelanggan) -> Unit
) : RecyclerView.Adapter<DataPelangganAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPelanggan[position]
        holder.tvNama.text = item.namaPelanggan ?: ""
        holder.tvAlamat.text = item.alamatPelanggan ?: ""
        holder.tvNoHP.text = item.noHPPelanggan ?: ""
        holder.tvTerdaftar.text = "Bergabung pada ${item.tanggalTerdaftar ?: "-"}"
        holder.tvCabang.text = "Cabang ${item.cabangPelanggan ?: "Tidak Terdaftar"}"

        holder.btHubungi.setOnClickListener {
            onHubungiClick(item)
        }

        holder.btLihat.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPelanggan)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangPelanggan)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tvDataTerdaftarPelanggan)
        val btHubungi: TextView = itemView.findViewById(R.id.btnHubungi)
        val btLihat: TextView = itemView.findViewById(R.id.btnLihat)
    }
}