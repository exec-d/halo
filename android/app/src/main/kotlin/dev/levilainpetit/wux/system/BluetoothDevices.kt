package dev.levilainpetit.wux.system

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Un appareil Bluetooth connecté et le niveau de sa batterie. */
data class ConnectedDevice(
    val name: String,
    val kind: Kind,
    /** 0–100, ou `null` si l'appareil ne le communique pas. */
    val level: Int?,
) {
    enum class Kind { HEADPHONES, SPEAKER, WATCH, INPUT, CAR, OTHER }
}

/** Les appareils Bluetooth connectés, pour le widget Appareils. */
object BluetoothDevices {

    /** Faux tant que l'autorisation « Appareils à proximité » manque (Android 12+). */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

    /**
     * Les appareils appairés et connectés, les mieux renseignés d'abord ;
     * `null` si l'autorisation manque, liste vide si le Bluetooth est coupé.
     */
    @SuppressLint("MissingPermission")
    fun connected(context: Context): List<ConnectedDevice>? {
        if (!hasPermission(context)) return null
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return runCatching {
            adapter.bondedDevices.orEmpty()
                .filter { isConnected(it) }
                .map { device ->
                    ConnectedDevice(
                        name = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) device.alias else null)
                            ?: device.name ?: "Appareil",
                        kind = kind(device.bluetoothClass),
                        level = batteryLevel(device),
                    )
                }
                .sortedWith(compareBy<ConnectedDevice> { it.level == null }.thenBy { it.name })
        }.getOrDefault(emptyList())
    }

    fun sample() = listOf(
        ConnectedDevice("Pixel Buds Pro", ConnectedDevice.Kind.HEADPHONES, 70),
        ConnectedDevice("Pixel Watch", ConnectedDevice.Kind.WATCH, 45),
        ConnectedDevice("Voiture", ConnectedDevice.Kind.CAR, null),
    )

    // Ni la connexion ni la batterie d'un appareil ne sont publiques ; les
    // réglages Bluetooth d'Android les lisent ainsi.
    private fun isConnected(device: BluetoothDevice): Boolean =
        runCatching { device.javaClass.getMethod("isConnected").invoke(device) as Boolean }.getOrDefault(false)

    private fun batteryLevel(device: BluetoothDevice): Int? =
        runCatching { device.javaClass.getMethod("getBatteryLevel").invoke(device) as Int }
            .getOrNull()?.takeIf { it in 0..100 }

    private fun kind(bluetoothClass: BluetoothClass?): ConnectedDevice.Kind {
        val device = bluetoothClass?.deviceClass ?: return ConnectedDevice.Kind.OTHER
        return when {
            device == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> ConnectedDevice.Kind.SPEAKER
            device == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> ConnectedDevice.Kind.CAR
            bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO -> ConnectedDevice.Kind.HEADPHONES
            bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.WEARABLE -> ConnectedDevice.Kind.WATCH
            bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.PERIPHERAL -> ConnectedDevice.Kind.INPUT
            else -> ConnectedDevice.Kind.OTHER
        }
    }
}
