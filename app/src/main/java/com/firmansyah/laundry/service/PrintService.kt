package com.firmansyah.laundry.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.core.content.ContextCompat
import com.firmansyah.laundry.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class PrintService(private val context: Context) {

    companion object {
        private const val TAG = "PrintService"
        private const val PRINTER_MAC_ADDRESS = "DC:0D:51:A7:FF:7A"
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // ESC/POS Commands
    private val ESC = 0x1B.toByte()
    private val GS = 0x1D.toByte()
    private val LF = 0x0A.toByte()
    private val CR = 0x0D.toByte()

    // Command arrays
    private val INIT_PRINTER = byteArrayOf(ESC, '@'.code.toByte())
    private val ALIGN_CENTER = byteArrayOf(ESC, 'a'.code.toByte(), 1)
    private val ALIGN_LEFT = byteArrayOf(ESC, 'a'.code.toByte(), 0)
    private val ALIGN_RIGHT = byteArrayOf(ESC, 'a'.code.toByte(), 2)
    private val BOLD_ON = byteArrayOf(ESC, 'E'.code.toByte(), 1)
    private val BOLD_OFF = byteArrayOf(ESC, 'E'.code.toByte(), 0)
    private val UNDERLINE_ON = byteArrayOf(ESC, '-'.code.toByte(), 1)
    private val UNDERLINE_OFF = byteArrayOf(ESC, '-'.code.toByte(), 0)
    private val SIZE_NORMAL = byteArrayOf(GS, '!'.code.toByte(), 0)
    private val SIZE_LARGE = byteArrayOf(GS, '!'.code.toByte(), 0x11)
    private val SIZE_MEDIUM = byteArrayOf(GS, '!'.code.toByte(), 0x01)
    private val CUT_PAPER = byteArrayOf(GS, 'V'.code.toByte(), 1)
    private val FEED_LINE = byteArrayOf(LF)
    private val FEED_LINES_3 = byteArrayOf(LF, LF, LF)

    data class InvoiceData(
        val transactionId: String = "",
        val namaPelanggan: String = "",
        val noHpPelanggan: String = "",
        val namaLayanan: String = "",
        val namaPegawai: String = "",
        val metodePembayaran: String = "",
        val kilogram: Int = 1,
        val subtotal: Int = 0,
        val pajak: Int = 0,
        val totalPembayaran: Int = 0,
        val baseServicePrice: Int = 0,
        val tambahanList: List<TambahanItem> = emptyList()
    )

    data class TambahanItem(
        val nama: String,
        val harga: Int
    )

    suspend fun printInvoice(invoiceData: InvoiceData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!connectToPrinter()) {
                    Log.e(TAG, "Failed to connect to printer")
                    return@withContext false
                }

                // Initialize printer
                sendCommand(INIT_PRINTER)

                // Print header
                printHeader()

                // Print invoice content
                printInvoiceContent(invoiceData)

                // Print footer
                printFooter()

                // Cut paper and disconnect
                sendCommand(CUT_PAPER)
                sendCommand(FEED_LINES_3)

                disconnect()
                true

            } catch (e: Exception) {
                Log.e(TAG, "Error printing invoice", e)
                disconnect()
                false
            }
        }
    }

    private fun connectToPrinter(): Boolean {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not supported")
                return false
            }

            if (!bluetoothAdapter!!.isEnabled) {
                Log.e(TAG, "Bluetooth not enabled")
                return false
            }

            val device: BluetoothDevice = bluetoothAdapter!!.getRemoteDevice(PRINTER_MAC_ADDRESS)
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket!!.connect()
            outputStream = bluetoothSocket!!.outputStream

            Log.i(TAG, "Connected to printer successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to printer", e)
            return false
        }
    }

    private fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            Log.i(TAG, "Disconnected from printer")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }

    private fun sendCommand(command: ByteArray) {
        try {
            outputStream?.write(command)
            outputStream?.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            throw e
        }
    }

    private fun printText(text: String, newLine: Boolean = true) {
        try {
            outputStream?.write(text.toByteArray(charset("UTF-8")))
            if (newLine) {
                outputStream?.write(FEED_LINE)
            }
            outputStream?.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error printing text", e)
            throw e
        }
    }

    private fun printHeader() {
        // Print logo (if available)
        printLogo()

        // Print business name
        sendCommand(ALIGN_CENTER)
        sendCommand(SIZE_LARGE)
        sendCommand(BOLD_ON)
        printText("LAUNDRY EXPRESS")
        sendCommand(BOLD_OFF)
        sendCommand(SIZE_NORMAL)

        // Print business info
        printText("Jl. Contoh No. 123, Surakarta")
        printText("Telp: 0271-123456")
        printText("Instagram: @laundryexpress")

        // Print separator
        sendCommand(ALIGN_LEFT)
        printText("================================")

        // Print invoice title
        sendCommand(ALIGN_CENTER)
        sendCommand(SIZE_MEDIUM)
        sendCommand(BOLD_ON)
        printText("INVOICE PEMBAYARAN")
        sendCommand(BOLD_OFF)
        sendCommand(SIZE_NORMAL)
        sendCommand(ALIGN_LEFT)
        printText("================================")
    }

    private fun printLogo() {
        try {
            // Create logo bitmap from drawable
            val logoBitmap = createLogoBitmap()
            if (logoBitmap != null) {
                printBitmap(logoBitmap)
                sendCommand(FEED_LINE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error printing logo", e)
        }
    }

    private fun createLogoBitmap(): Bitmap? {
        return try {
            // Create a simple text-based logo since we're using drawable
            val width = 200
            val height = 60
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Fill with white background
            canvas.drawColor(Color.WHITE)

            // Draw logo text
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            canvas.drawText("🧺 LAUNDRY", width / 2f, height / 2f, paint)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error creating logo bitmap", e)
            null
        }
    }

    private fun printBitmap(bitmap: Bitmap) {
        try {
            // Convert bitmap to monochrome and print
            val width = bitmap.width
            val height = bitmap.height

            sendCommand(ALIGN_CENTER)

            // ESC/POS bitmap printing command
            sendCommand(byteArrayOf(GS, 'v'.code.toByte(), '0'.code.toByte(), 0))

            // Send bitmap dimensions
            sendCommand(byteArrayOf((width / 8).toByte(), 0, height.toByte(), 0))

            // Convert and send bitmap data
            for (y in 0 until height) {
                for (x in 0 until width step 8) {
                    var byte = 0
                    for (bit in 0 until 8) {
                        if (x + bit < width) {
                            val pixel = bitmap.getPixel(x + bit, y)
                            val gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                            if (gray < 128) { // Black pixel
                                byte = byte or (1 shl (7 - bit))
                            }
                        }
                    }
                    outputStream?.write(byte)
                }
            }
            outputStream?.flush()

        } catch (e: Exception) {
            Log.e(TAG, "Error printing bitmap", e)
        }
    }

    private fun printInvoiceContent(invoiceData: InvoiceData) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID")).format(Date())

        // Transaction details
        printText("Tanggal  : $currentDate")
        printText("ID Trans : ${invoiceData.transactionId}")
        printText("")

        // Customer info
        sendCommand(BOLD_ON)
        printText("DATA PELANGGAN:")
        sendCommand(BOLD_OFF)
        printText("Nama     : ${invoiceData.namaPelanggan}")
        printText("No. HP   : ${invoiceData.noHpPelanggan}")
        printText("")

        // Service details
        sendCommand(BOLD_ON)
        printText("DETAIL LAYANAN:")
        sendCommand(BOLD_OFF)

        val servicePrice = if (invoiceData.subtotal > 0) {
            val tambahanTotal = invoiceData.tambahanList.sumOf { it.harga }
            invoiceData.subtotal - tambahanTotal
        } else {
            invoiceData.baseServicePrice * invoiceData.kilogram
        }

        printText("${invoiceData.namaLayanan} (${invoiceData.kilogram} Kg)")
        printText("  Rp ${formatRupiah(servicePrice)}")

        // Additional services
        if (invoiceData.tambahanList.isNotEmpty()) {
            printText("")
            sendCommand(BOLD_ON)
            printText("LAYANAN TAMBAHAN:")
            sendCommand(BOLD_OFF)
            invoiceData.tambahanList.forEach { tambahan ->
                printText("${tambahan.nama}")
                printText("  Rp ${formatRupiah(tambahan.harga)}")
            }
        }

        printText("--------------------------------")

        // Payment summary
        sendCommand(BOLD_ON)
        printText("RINCIAN PEMBAYARAN:")
        sendCommand(BOLD_OFF)
        printText("Subtotal   : Rp ${formatRupiah(invoiceData.subtotal)}")
        printText("Pajak (12%): Rp ${formatRupiah(invoiceData.pajak)}")
        printText("--------------------------------")

        sendCommand(SIZE_MEDIUM)
        sendCommand(BOLD_ON)
        printText("TOTAL      : Rp ${formatRupiah(invoiceData.totalPembayaran)}")
        sendCommand(BOLD_OFF)
        sendCommand(SIZE_NORMAL)

        printText("--------------------------------")
        printText("Pembayaran : ${invoiceData.metodePembayaran}")
        printText("Kasir      : ${invoiceData.namaPegawai}")
    }

    private fun printFooter() {
        printText("")
        sendCommand(ALIGN_CENTER)
        printText("Terima kasih atas kepercayaan Anda")
        printText("Barang yang sudah dicuci")
        printText("tidak dapat dikembalikan")
        printText("")
        printText("Follow IG: @laundryexpress")
        printText("WA: 0271-123456")
        printText("")

        // Print barcode/QR placeholder
        printText("Scan untuk review:")
        printText("*** QR CODE ***")
        printText("")

        sendCommand(ALIGN_LEFT)
    }

    private fun formatRupiah(amount: Int): String {
        return String.format(Locale("id", "ID"), "%,d", amount).replace(',', '.')
    }

    fun isBluetoothEnabled(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

    fun isPrinterPaired(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = bluetoothAdapter?.bondedDevices
        return pairedDevices?.any { it.address == PRINTER_MAC_ADDRESS } == true
    }
}