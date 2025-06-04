package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelPegawai
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout

class DataPegawaiAdapter(
    private var listFull: ArrayList<ModelPegawai>,
    private val onItemClick: (ModelPegawai) -> Unit,
    private val onHubungiClick: (ModelPegawai) -> Unit,
    private val onSelectionChanged: (Int) -> Unit // Callback untuk perubahan selection
) : RecyclerView.Adapter<DataPegawaiAdapter.ViewHolder>(), Filterable {

    private var listFiltered = ArrayList<ModelPegawai>(listFull)
    private var isEditMode = false
    private val selectedItems = HashSet<String>() // Menyimpan ID item yang dipilih

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listFiltered[position]
        holder.tvNama.text = item.namaPegawai ?: ""
        holder.tvAlamat.text = item.alamatPegawai ?: ""
        holder.tvNoHP.text = item.noHPPegawai ?: ""
        holder.tvTerdaftar.text = "Bergabung pada ${item.tanggalTerdaftar ?: "-"}"
        holder.tvCabang.text = "Cabang ${item.cabangPegawai ?: "Tidak Terdaftar"}"

        // Show/hide checkbox based on edit mode
        if (isEditMode) {
            holder.checkBox.visibility = View.VISIBLE
            holder.ctaContainer.visibility = View.GONE

            // Set checkbox state
            val itemId = item.idPegawai ?: ""
            holder.checkBox.isChecked = selectedItems.contains(itemId)

            // Handle checkbox click
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(itemId)
                } else {
                    selectedItems.remove(itemId)
                }
                onSelectionChanged(selectedItems.size)
            }

            // Handle card click in edit mode
            holder.itemView.setOnClickListener {
                holder.checkBox.isChecked = !holder.checkBox.isChecked
            }
        } else {
            holder.checkBox.visibility = View.GONE
            holder.ctaContainer.visibility = View.VISIBLE

            holder.btHubungi.setOnClickListener { onHubungiClick(item) }
            holder.btLihat.setOnClickListener { onItemClick(item) }

            // Remove card click listener in normal mode
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = listFiltered.size

    fun filter(keyword: String) {
        this.filter.filter(keyword)
    }

    fun updateData(newList: ArrayList<ModelPegawai>) {
        listFull = ArrayList(newList)
        listFiltered = ArrayList(newList)
        notifyDataSetChanged()
    }

    // Method untuk mengubah mode edit
    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        if (!editMode) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    // Method untuk mendapatkan jumlah item yang dipilih
    fun getSelectedCount(): Int {
        return selectedItems.size
    }

    // Method untuk mendapatkan ID item yang dipilih
    fun getSelectedItemIds(): List<String> {
        return selectedItems.toList()
    }

    // Method untuk mendapatkan item yang dipilih
    fun getSelectedItems(): List<ModelPegawai> {
        return listFiltered.filter { selectedItems.contains(it.idPegawai) }
    }

    // Method untuk clear selection
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keyword = constraint.toString().lowercase(Locale.ROOT)
                val filtered = if (keyword.isEmpty()) {
                    listFull
                } else {
                    listFull.filter {
                        it.namaPegawai?.lowercase()?.contains(keyword) == true ||
                                it.alamatPegawai?.lowercase()?.contains(keyword) == true ||
                                it.noHPPegawai?.contains(keyword) == true ||
                                it.cabangPegawai?.lowercase()?.contains(keyword) == true ||
                                it.tanggalTerdaftar?.contains(keyword) == true
                    } as ArrayList<ModelPegawai>
                }

                val result = FilterResults()
                result.values = filtered
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listFiltered = results?.values as ArrayList<ModelPegawai>
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDataNamaPegawai)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvDataAlamatPegawai)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvDataNoHpPegawai)
        val tvCabang: TextView = itemView.findViewById(R.id.tvDataCabangPegawai)
        val tvTerdaftar: TextView = itemView.findViewById(R.id.tvDataTerdaftarPegawai)
        val btHubungi: TextView = itemView.findViewById(R.id.btnHubungi)
        val btLihat: TextView = itemView.findViewById(R.id.btnLihat)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxPegawai)
        val ctaContainer: LinearLayout = itemView.findViewById(R.id.ctaContainer)
    }
}