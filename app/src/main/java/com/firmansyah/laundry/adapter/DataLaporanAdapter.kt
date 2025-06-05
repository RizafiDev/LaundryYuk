package com.firmansyah.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firmansyah.laundry.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DataLaporanAdapter(
    private var transactionList: MutableList<ModelTransaksi>,
    private val onItemClick: (ModelTransaksi) -> Unit = {}
) : RecyclerView.Adapter<DataLaporanAdapter.ViewHolder>() {

    private var filteredList: MutableList<ModelTransaksi> = mutableListOf()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    init {
        filteredList.addAll(transactionList)
        numberFormat.currency = Currency.getInstance("IDR")
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdTransaksi: TextView = itemView.findViewById(R.id.tvDataIdTransaksi)
        val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvDataNamaPelanggan)
        val tvNoHpPelanggan: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvNamaPegawai: TextView = itemView.findViewById(R.id.tvDataNamaPegawai)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvDataLayanan)
        val tvMetodePembayaran: TextView = itemView.findViewById(R.id.tvDataMetodePembayaran)
        val tvTanggalTransaksi: TextView = itemView.findViewById(R.id.tvDataTerdaftarTransaksi)
        val tvTotalBayar: TextView = itemView.findViewById(R.id.tvTotalBayar)
        val tvNamaCabang: TextView = itemView.findViewById(R.id.tvDataCabang) // Added branch TextView
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxPelanggan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_laporan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = filteredList[position]

        holder.tvIdTransaksi.text = transaction.idTransaksi ?: "TRX-"
        holder.tvNamaPelanggan.text = transaction.namaPelanggan ?: "-"
        holder.tvNoHpPelanggan.text = transaction.noHpPelanggan ?: "-"
        holder.tvNamaPegawai.text = transaction.namaPegawai ?: "-"
        holder.tvLayanan.text = transaction.namaLayanan ?: "-"
        holder.tvMetodePembayaran.text = transaction.metodePembayaran ?: "-"
        holder.tvNamaCabang.text = transaction.namaCabang ?: "-" // Display branch name

        // Format tanggal
        val tanggal = transaction.tanggalTransaksi
        if (tanggal != null && tanggal.isNotEmpty()) {
            try {
                // Your date format is '05/06/2025 15:21:18" (day/month/year)
                val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                val date = inputFormat.parse(tanggal)
                holder.tvTanggalTransaksi.text = date?.let { outputFormat.format(it) } ?: tanggal
            } catch (e: Exception) {
                holder.tvTanggalTransaksi.text = tanggal
            }
        } else {
            holder.tvTanggalTransaksi.text = "-"
        }


        // Format total bayar
        val totalBayar = transaction.totalHarga?.toIntOrNull() ?: 0
        holder.tvTotalBayar.text = formatCurrency(totalBayar)

        // Hide checkbox for now
        holder.checkBox.visibility = View.GONE

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateData(newList: List<ModelTransaksi>) {
        transactionList.clear()
        transactionList.addAll(newList)
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(transactionList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            transactionList.forEach { transaction ->
                if (transaction.idTransaksi?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                    transaction.namaPelanggan?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                    transaction.noHpPelanggan?.contains(searchQuery) == true ||
                    transaction.namaPegawai?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                    transaction.namaLayanan?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                    transaction.metodePembayaran?.lowercase(Locale.getDefault())?.contains(searchQuery) == true ||
                    transaction.namaCabang?.lowercase(Locale.getDefault())?.contains(searchQuery) == true) {
                    filteredList.add(transaction)
                }
            }
        }
        notifyDataSetChanged()
    }

    private fun formatCurrency(amount: Int): String {
        return try {
            "Rp ${String.format("%,d", amount).replace(',', '.')}"
        } catch (e: Exception) {
            "Rp 0"
        }
    }
}