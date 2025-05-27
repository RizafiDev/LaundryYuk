package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import com.firmansyah.laundry.model.ModelTambahan

class LayananTambahanAdapter(
    private val list: MutableList<ModelTambahan>,
    private val onRemoveClick: (ModelTambahan, Int) -> Unit
) : RecyclerView.Adapter<LayananTambahanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaLayananTambahan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHargaLayananTambahan)
        val btnRemove: View = itemView.findViewById(R.id.btnRemoveTambahan)

        fun bind(tambahan: ModelTambahan, position: Int) {
            tambahan.apply {
                tvNama.text = namaLayanan ?: "-"
                tvHarga.text = "Rp ${hargaLayanan ?: "0"}"
            }

            btnRemove.setOnClickListener {
                if (position in 0 until list.size) {
                    onRemoveClick(tambahan, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layanan_tambahan_selected, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position in 0 until list.size) {
            holder.bind(list[position], position)
        }
    }

    fun addItem(tambahan: ModelTambahan) {
        if (!list.contains(tambahan)) {
            list.add(tambahan)
            notifyItemInserted(list.size - 1)
        }
    }

    fun removeItem(position: Int) {
        if (position in 0 until list.size) {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size)
        }
    }
}