package com.firmansyah.laundry.adapter

import com.firmansyah.laundry.model.ModelCabang
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import android.widget.Filter
import android.widget.Filterable
import java.util.*
import kotlin.collections.ArrayList

class DataCabangAdapter(
    private var listFull: ArrayList<ModelCabang>,
    private val onItemClick: (ModelCabang) -> Unit,
    private val onHubungiClick: (ModelCabang) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<DataCabangAdapter.CabangViewHolder>(), Filterable {

    private var listFiltered = ArrayList<ModelCabang>(listFull)
    private var isEditMode = false
    private val selectedItems = HashSet<String>() // Menyimpan ID item yang dipilih

    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataNamaCabang: TextView = itemView.findViewById(R.id.tvDataNamaCabang)
        val tvDataAlamatCabang: TextView = itemView.findViewById(R.id.tvDataAlamatCabang)
        val tvDataKabKotaCabang: TextView = itemView.findViewById(R.id.tvDataKabKotaCabang)
        val tvDataNoHpCabang: TextView = itemView.findViewById(R.id.tvDataNoHpCabang)
        val tvDataTerdaftarPelanggan: TextView = itemView.findViewById(R.id.tvDataTerdaftarPelanggan)
        val checkBoxPelanggan: CheckBox = itemView.findViewById(R.id.checkBoxPelanggan)
        val btnHubungi: TextView = itemView.findViewById(R.id.btnHubungi)
        val btnLihat: TextView = itemView.findViewById(R.id.btnLihat)
        val ctaContainer: LinearLayout = itemView.findViewById(R.id.ctaContainer)

        fun bind(cabang: ModelCabang, position: Int) {
            tvDataNamaCabang.text = cabang.namaCabang ?: ""
            tvDataAlamatCabang.text = cabang.alamatCabang ?: ""
            tvDataKabKotaCabang.text = "${cabang.kabKotaCabang ?: ""}, ${cabang.provinsiCabang ?: ""}"
            tvDataNoHpCabang.text = cabang.noHpCabang ?: ""
            tvDataTerdaftarPelanggan.text = "Terdaftar ${cabang.tanggalTerdaftar ?: "-"}"

            // Show/hide checkbox and buttons based on edit mode
            if (isEditMode) {
                checkBoxPelanggan.visibility = View.VISIBLE
                ctaContainer.visibility = View.GONE

                // Set checkbox state
                val itemId = cabang.idCabang ?: ""
                checkBoxPelanggan.isChecked = selectedItems.contains(itemId)

                // Handle checkbox click
                checkBoxPelanggan.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedItems.add(itemId)
                    } else {
                        selectedItems.remove(itemId)
                    }
                    onSelectionChanged(selectedItems.size)
                }

                // Handle card click in edit mode
                itemView.setOnClickListener {
                    checkBoxPelanggan.isChecked = !checkBoxPelanggan.isChecked
                }
            } else {
                checkBoxPelanggan.visibility = View.GONE
                ctaContainer.visibility = View.VISIBLE

                // Handle button clicks in normal mode
                btnHubungi.setOnClickListener { onHubungiClick(cabang) }
                btnLihat.setOnClickListener { onItemClick(cabang) }

                // Remove card click listener in normal mode
                itemView.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_cabang, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        holder.bind(listFiltered[position], position)
    }

    override fun getItemCount(): Int = listFiltered.size

    fun filter(keyword: String) {
        this.filter.filter(keyword)
    }

    fun updateData(newList: ArrayList<ModelCabang>) {
        listFull = ArrayList(newList)
        listFiltered = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        if (!editMode) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int {
        return selectedItems.size
    }

    fun getSelectedItemIds(): List<String> {
        return selectedItems.toList()
    }

    fun getSelectedCabang(): List<ModelCabang> {
        return listFiltered.filter { selectedItems.contains(it.idCabang) }
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun hasSelectedItems(): Boolean = selectedItems.isNotEmpty()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val keyword = constraint.toString().lowercase(Locale.ROOT)
                val filtered = if (keyword.isEmpty()) {
                    listFull
                } else {
                    listFull.filter {
                        it.namaCabang?.lowercase()?.contains(keyword) == true ||
                                it.alamatCabang?.lowercase()?.contains(keyword) == true ||
                                it.kabKotaCabang?.lowercase()?.contains(keyword) == true ||
                                it.provinsiCabang?.lowercase()?.contains(keyword) == true ||
                                it.noHpCabang?.contains(keyword) == true
                    } as ArrayList<ModelCabang>
                }

                val result = FilterResults()
                result.values = filtered
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listFiltered = results?.values as ArrayList<ModelCabang>
                notifyDataSetChanged()
            }
        }
    }
}