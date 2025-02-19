package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPegawai

class DataPegawaiAdapter(private val listPegawai: ArrayList<ModelPegawai>) :
    RecyclerView.Adapter<DataPegawaiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]
        holder.tvDataIDPegawai.text = item.idPegawai ?: ""
        holder.tvNama.text = item.namaPegawai ?: ""
        holder.tvAlamat.text = item.alamatPegawai ?: ""
        holder.tvNoHP.text = item.noHPPegawai ?: ""
        holder.tvTerdaftar.text = "Terdaftar: ${item.tanggalTerdaftar ?: "-"}"
        holder.tvCabang.text = "Cabang ${item.cabangPegawai ?: "Tidak Ada Cabang"}"

        holder.btHubungi.setOnClickListener {
            // Tambahkan aksi klik untuk menghubungi pegawai
        }

        holder.btLihat.setOnClickListener {
            // Tambahkan aksi klik untuk melihat detail pegawai
        }
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataIDPegawai: TextView = itemView.findViewById(R.id.tvDataIDPegawai)
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaPegawai)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPegawai)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvDataNoHpPegawai)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangPegawai)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tvDataTerdaftarPegawai)
        val btHubungi: Button = itemView.findViewById(R.id.btnHubungi)
        val btLihat: Button = itemView.findViewById(R.id.btnLihat)
    }
}