package com.firmansyah.laundry.adapter

import android.content.Intent
import android.net.Uri
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

class DataTransaksiAdapter(
    private val transaksiList: MutableList<Map<String, Any>>
) : RecyclerView.Adapter<DataTransaksiAdapter.TransaksiViewHolder>() {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private val outputDateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataIdTransaksi: TextView = itemView.findViewById(R.id.tvDataIdTransaksi)
        val tvDataNamaPelanggan: TextView = itemView.findViewById(R.id.tvDataNamaPelanggan)
        val tvDataNoHpPelanggan: TextView = itemView.findViewById(R.id.tvDataNoHpPelanggan)
        val tvDataCabang: TextView = itemView.findViewById(R.id.tvDataCabang)
        val tvDataNamaPegawai: TextView = itemView.findViewById(R.id.tvDataNamaPegawai)
        val tvDataLayanan: TextView = itemView.findViewById(R.id.tvDataLayanan)
        val tvDataMetodePembayaran: TextView = itemView.findViewById(R.id.tvDataMetodePembayaran)
        val tvDataTerdaftarTransaksi: TextView = itemView.findViewById(R.id.tvDataTerdaftarTransaksi)
        val tvTotalBayar: TextView = itemView.findViewById(R.id.tvTotalBayar)
        val checkBoxPelanggan: CheckBox = itemView.findViewById(R.id.checkBoxPelanggan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_laporan, parent, false)
        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val transaksi = transaksiList[position]

        // Transaction ID
        val transactionId = transaksi["transactionId"] as? String ?: "N/A"
        holder.tvDataIdTransaksi.text = transactionId

        // Customer data
        val pelangganData = transaksi["pelanggan"] as? Map<String, Any>
        val namaPelanggan = pelangganData?.get("nama") as? String ?: "N/A"
        val noHpPelanggan = pelangganData?.get("noHp") as? String ?: "N/A"
        holder.tvDataNamaPelanggan.text = "Pelanggan $namaPelanggan"
        holder.tvDataNoHpPelanggan.text = noHpPelanggan

        // Set click listener for phone number to open WhatsApp
        holder.tvDataNoHpPelanggan.setOnClickListener {
            openWhatsApp(it, noHpPelanggan)
        }

        // Branch
        val cabang = transaksi["cabang"] as? String ?: "N/A"
        holder.tvDataCabang.text = "Cabang $cabang"

        // Employee data
        val karyawanData = transaksi["karyawan"] as? Map<String, Any>
        val namaKaryawan = karyawanData?.get("nama") as? String ?: "N/A"
        holder.tvDataNamaPegawai.text = "Dilayani Oleh $namaKaryawan"

        // Service data
        val layananData = transaksi["layanan"] as? Map<String, Any>
        val namaLayanan = layananData?.get("nama") as? String ?: "N/A"
        val kilogram = when (val kg = layananData?.get("kilogram")) {
            is Number -> kg.toInt() // Convert to integer
            else -> 0
        }

        // Show service name with kilogram
        val layananText = if (kilogram > 0) {
            "$namaLayanan (${kilogram}kg)"
        } else {
            namaLayanan
        }
        holder.tvDataLayanan.text = layananText

        // Payment method
        val pembayaranData = transaksi["pembayaran"] as? Map<String, Any>
        val metodePembayaran = pembayaranData?.get("metodePembayaran") as? String ?: "N/A"
        holder.tvDataMetodePembayaran.text = "Via $metodePembayaran"

        // Transaction date
        val tanggalTransaksi = transaksi["tanggalTransaksi"] as? String ?: "N/A"
        // Format the date for better display
        val formattedDate = try {
            val date = inputDateFormat.parse(tanggalTransaksi)
            date?.let { outputDateFormat.format(it) } ?: tanggalTransaksi
        } catch (e: Exception) {
            tanggalTransaksi
        }
        holder.tvDataTerdaftarTransaksi.text = formattedDate

        // Total payment
        val totalPembayaran = when (val total = pembayaranData?.get("totalPembayaran")) {
            is Number -> total.toLong()
            else -> 0L
        }

        // Format currency without decimal places
        val formattedTotal = "Rp ${formatRupiah(totalPembayaran)}"
        holder.tvTotalBayar.text = formattedTotal

        // Hide checkbox for now (can be used for future bulk operations)
        holder.checkBoxPelanggan.visibility = View.GONE
    }

    override fun getItemCount(): Int = transaksiList.size

    private fun formatRupiah(amount: Long): String {
        return String.format(Locale("id", "ID"), "%,d", amount)
    }

    private fun openWhatsApp(view: View, phoneNumber: String) {
        val context = view.context

        // Clean phone number (remove spaces, dashes, etc.)
        val cleanNumber = phoneNumber.replace("[^\\d+]".toRegex(), "")

        // Convert to international format if needed
        val internationalNumber = if (cleanNumber.startsWith("0")) {
            "+62${cleanNumber.substring(1)}"
        } else if (!cleanNumber.startsWith("+")) {
            "+62$cleanNumber"
        } else {
            cleanNumber
        }

        try {
            // Try to open WhatsApp directly
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$internationalNumber")
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, open in browser
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://web.whatsapp.com/send?phone=$internationalNumber")
            context.startActivity(intent)
        }
    }
}