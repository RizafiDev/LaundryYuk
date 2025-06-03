package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPelanggan
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Filter
import android.widget.Filterable

class DataPelangganAdapter(
    private var listFull: ArrayList<ModelPelanggan>,
    private val onItemClick: (ModelPelanggan) -> Unit,
    private val onHubungiClick: (ModelPelanggan) -> Unit
) : RecyclerView.Adapter<DataPelangganAdapter.ViewHolder>(), Filterable {

    private var listFiltered = ArrayList<ModelPelanggan>(listFull)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listFiltered[position]
        holder.tvNama.text = item.namaPelanggan ?: ""
        holder.tvAlamat.text = item.alamatPelanggan ?: ""
        holder.tvNoHP.text = item.noHPPelanggan ?: ""
        holder.tvTerdaftar.text = "Bergabung pada ${item.tanggalTerdaftar ?: "-"}"
        holder.tvCabang.text = "Cabang ${item.cabangPelanggan ?: "Tidak Terdaftar"}"

        holder.btHubungi.setOnClickListener { onHubungiClick(item) }
        holder.btLihat.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = listFiltered.size

    fun filter(keyword: String) {
        this.filter.filter(keyword)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keyword = constraint.toString().lowercase(Locale.ROOT)
                val filtered = if (keyword.isEmpty()) {
                    listFull
                } else {
                    listFull.filter {
                        it.namaPelanggan?.lowercase()?.contains(keyword) == true ||
                                it.alamatPelanggan?.lowercase()?.contains(keyword) == true ||
                                it.noHPPelanggan?.contains(keyword) == true ||
                                it.cabangPelanggan?.lowercase()?.contains(keyword) == true ||
                                it.tanggalTerdaftar?.contains(keyword) == true
                    } as ArrayList<ModelPelanggan>
                }

                val result = FilterResults()
                result.values = filtered
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listFiltered = results?.values as ArrayList<ModelPelanggan>
                notifyDataSetChanged()
            }
        }
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
