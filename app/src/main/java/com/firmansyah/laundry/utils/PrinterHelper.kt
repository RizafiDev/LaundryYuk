package com.firmansyah.laundry.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log

object PrinterHelper {

    private const val TAG = "PrinterHelper"
    private const val PRINTER_MAC_ADDRESS = "DC:0D:51:A7:FF:7A"

    fun findPrinterDevice(): BluetoothDevice? {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth not available or not enabled")
            return null
        }

        val pairedDevices = bluetoothAdapter.bondedDevices

        return pairedDevices?.find { device ->
            device.address.equals(PRINTER_MAC_ADDRESS, ignoreCase = true)
        }
    }

    fun isPrinterAvailable(): Boolean {
        return findPrinterDevice() != null
    }

    fun getPrinterInfo(): String? {
        val device = findPrinterDevice()
        return device?.let {
            "Name: ${it.name ?: "Unknown"}\nMAC: ${it.address}\nBonded: ${it.bondState == BluetoothDevice.BOND_BONDED}"
        }
    }

    fun getAllPairedPrinters(): List<BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return emptyList()
        }

        val pairedDevices = bluetoothAdapter.bondedDevices ?: return emptyList()

        // Filter devices that might be printers (based on common printer names)
        return pairedDevices.filter { device ->
            val deviceName = device.name?.lowercase() ?: ""
            deviceName.contains("printer") ||
                    deviceName.contains("pos") ||
                    deviceName.contains("thermal") ||
                    deviceName.contains("receipt") ||
                    device.address == PRINTER_MAC_ADDRESS
        }
    }

    /**
     * ESC/POS Command Builder Helper
     */
    object EscPosCommands {

        // Control characters
        const val ESC = 0x1B
        const val GS = 0x1D
        const val LF = 0x0A
        const val CR = 0x0D
        const val HT = 0x09
        const val FF = 0x0C

        // Initialize printer
        val INIT = byteArrayOf(ESC.toByte(), '@'.code.toByte())

        // Text alignment
        val ALIGN_LEFT = byteArrayOf(ESC.toByte(), 'a'.code.toByte(), 0)
        val ALIGN_CENTER = byteArrayOf(ESC.toByte(), 'a'.code.toByte(), 1)
        val ALIGN_RIGHT = byteArrayOf(ESC.toByte(), 'a'.code.toByte(), 2)

        // Text formatting
        val BOLD_ON = byteArrayOf(ESC.toByte(), 'E'.code.toByte(), 1)
        val BOLD_OFF = byteArrayOf(ESC.toByte(), 'E'.code.toByte(), 0)
        val UNDERLINE_ON = byteArrayOf(ESC.toByte(), '-'.code.toByte(), 1)
        val UNDERLINE_OFF = byteArrayOf(ESC.toByte(), '-'.code.toByte(), 0)
        val ITALIC_ON = byteArrayOf(ESC.toByte(), '4'.code.toByte(), 1)
        val ITALIC_OFF = byteArrayOf(ESC.toByte(), '4'.code.toByte(), 0)

        // Text size
        val SIZE_NORMAL = byteArrayOf(GS.toByte(), '!'.code.toByte(), 0)
        val SIZE_WIDE = byteArrayOf(GS.toByte(), '!'.code.toByte(), 0x10)
        val SIZE_TALL = byteArrayOf(GS.toByte(), '!'.code.toByte(), 0x01)
        val SIZE_LARGE = byteArrayOf(GS.toByte(), '!'.code.toByte(), 0x11)

        // Line spacing
        val LINE_SPACING_DEFAULT = byteArrayOf(ESC.toByte(), '2'.code.toByte())
        val LINE_SPACING_NARROW = byteArrayOf(ESC.toByte(), '3'.code.toByte(), 0)

        // Paper control
        val FEED_LINE = byteArrayOf(LF.toByte())
        val FEED_LINES_2 = byteArrayOf(LF.toByte(), LF.toByte())
        val FEED_LINES_3 = byteArrayOf(LF.toByte(), LF.toByte(), LF.toByte())
        val CUT_PAPER = byteArrayOf(GS.toByte(), 'V'.code.toByte(), 1)
        val CUT_PAPER_FULL = byteArrayOf(GS.toByte(), 'V'.code.toByte(), 0)

        // Drawer control (if supported)
        val OPEN_DRAWER = byteArrayOf(ESC.toByte(), 'p'.code.toByte(), 0, 60, 120)

        fun createTextSizeCommand(width: Int, height: Int): ByteArray {
            val size = ((width - 1) shl 4) or (height - 1)
            return byteArrayOf(GS.toByte(), '!'.code.toByte(), size.toByte())
        }

        fun createLineSpacingCommand(spacing: Int): ByteArray {
            return byteArrayOf(ESC.toByte(), '3'.code.toByte(), spacing.toByte())
        }

        fun createLeftMarginCommand(margin: Int): ByteArray {
            return byteArrayOf(GS.toByte(), 'L'.code.toByte(),
                (margin and 0xFF).toByte(),
                ((margin shr 8) and 0xFF).toByte())
        }
    }

    /**
     * Text formatting utilities
     */
    object TextFormatter {

        fun centerText(text: String, lineWidth: Int = 32): String {
            if (text.length >= lineWidth) return text
            val padding = (lineWidth - text.length) / 2
            return " ".repeat(padding) + text
        }

        fun rightAlignText(text: String, lineWidth: Int = 32): String {
            if (text.length >= lineWidth) return text
            val padding = lineWidth - text.length
            return " ".repeat(padding) + text
        }

        fun createSeparatorLine(char: Char = '-', length: Int = 32): String {
            return char.toString().repeat(length)
        }

        fun formatTwoColumnText(left: String, right: String, lineWidth: Int = 32): String {
            val maxLeftLength = lineWidth - right.length - 1
            val leftText = if (left.length > maxLeftLength) {
                left.substring(0, maxLeftLength - 3) + "..."
            } else {
                left
            }

            val padding = lineWidth - leftText.length - right.length
            return leftText + " ".repeat(maxOf(1, padding)) + right
        }

        fun wrapText(text: String, lineWidth: Int = 32): List<String> {
            if (text.length <= lineWidth) return listOf(text)

            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = ""

            for (word in words) {
                if ((currentLine + word).length <= lineWidth) {
                    currentLine += if (currentLine.isEmpty()) word else " $word"
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        // Word is longer than line width, split it
                        lines.add(word.substring(0, lineWidth))
                        currentLine = word.substring(lineWidth)
                    }
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }

            return lines
        }
    }
}