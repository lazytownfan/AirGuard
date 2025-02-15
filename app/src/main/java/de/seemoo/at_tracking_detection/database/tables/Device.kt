package de.seemoo.at_tracking_detection.database.tables

import android.bluetooth.le.ScanResult
import androidx.room.*
import de.seemoo.at_tracking_detection.ATTrackingDetectionApplication
import de.seemoo.at_tracking_detection.R
import de.seemoo.at_tracking_detection.util.DeviceType
import de.seemoo.at_tracking_detection.util.converter.DateTimeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.experimental.and

@Entity(tableName = "device", indices = [Index(value = ["address"], unique = true)])
@TypeConverters(DateTimeConverter::class)
data class Device(
    @PrimaryKey(autoGenerate = true) var deviceId: Int,
    @ColumnInfo(name = "address") var address: String,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "ignore") val ignore: Boolean,
    @ColumnInfo(name = "connectable") val connectable: Boolean,
    @ColumnInfo(name = "payloadData") val payloadData: Byte?,
    @ColumnInfo(name = "firstDiscovery") val firstDiscovery: LocalDateTime,
    @ColumnInfo(name = "lastSeen") var lastSeen: LocalDateTime,
    @ColumnInfo(name = "notificationSent") var notificationSent: Boolean,
    @ColumnInfo(name = "lastNotificationSent") var lastNotificationSent: LocalDateTime?
) {

    constructor(
        address: String,
        ignore: Boolean,
        connectable: Boolean,
        payloadData: Byte?,
        firstDiscovery: LocalDateTime,
        lastSeen: LocalDateTime,
    ) : this(
        0,
        address,
        null,
        ignore,
        connectable,
        payloadData,
        firstDiscovery,
        lastSeen,
        false,
        null
    )

    constructor(scanResult: ScanResult) : this(
        0,
        scanResult.device.address,
        scanResult.scanRecord?.deviceName,
        false,
        scanResult.isConnectable,
        scanResult.scanRecord?.getManufacturerSpecificData(76)?.get(2),
        LocalDateTime.now(), LocalDateTime.now(), false, null
    )

    private fun getDateTimeFormatter(): DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

    fun getFormattedDiscoveryDate(): String =
        firstDiscovery.format(getDateTimeFormatter())

    fun getFormattedLastSeenDate(): String =
        lastSeen.format(getDateTimeFormatter())

    fun getType(): Int {
        return when {
            payloadData == null -> {
                DeviceType.UNKNOWN
            }
            payloadData.and(0x10).toInt() != 0 && connectable -> {
                DeviceType.AIRTAG
            }
            else -> {
                DeviceType.UNKNOWN
            }
        }
    }

    fun getImage(): Int {
        return when (this.getType()) {
            DeviceType.AIRTAG -> R.drawable.ic_airtag
            DeviceType.UNKNOWN -> R.drawable.ic_baseline_device_unknown_24
            else -> R.drawable.ic_baseline_device_unknown_24
        }
    }

    fun isAirTag(): Boolean {
        return this.getType() == DeviceType.AIRTAG
    }

    fun getDeviceName(): String {
        return when (this.getType()) {
            DeviceType.AIRTAG -> "AirTag"
            else -> "FindMy Device"
        }
    }

    fun getDeviceNameWithId(): String {
        val resources = ATTrackingDetectionApplication.getAppContext().resources
        return when (this.getType()) {
            DeviceType.AIRTAG -> resources.getString(R.string.device_name_airtag).format(deviceId)
            else -> resources.getString(R.string.device_name_find_my_device).format(deviceId)
        }
    }
}