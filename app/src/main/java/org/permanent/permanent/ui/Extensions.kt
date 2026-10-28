package org.permanent.permanent.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.OpenableColumns
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.WindowMetricsCalculator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.models.TwoFAVO
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale


fun Context.hideKeyboardFrom(windowToken: IBinder) {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            ).hideSoftInputFromWindow(windowToken, 0)
}

fun Context.showKeyboardFor(view: View) {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            ).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Context.assetSize(resourceUri: Uri): Long {
    try {
        val descriptor = contentResolver.openAssetFileDescriptor(resourceUri, "r")
        val size = descriptor?.length ?: return 0
        descriptor.close()
        return size
    } catch (e: Resources.NotFoundException) {
        return 0
    }
}

fun Context.openLink(uriString: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(uriString)
    )
    startActivity(intent)
}

fun Activity.computeWindowSizeClasses(): WindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    val width = metrics.bounds.width()
    val height = metrics.bounds.height()
    val density = resources.displayMetrics.density
    return WindowSizeClass.compute(width / density, height / density)
}

fun gbToBytes(gb: Int): Long = gb.toLong() * 1024 * 1024 * 1024

fun mbToBytes(mb: Int): Long = mb.toLong() * 1024 * 1024

// Must match iOS readableFileSize: 1000-based units, at most one decimal, always a period
fun bytesToHumanReadableString(bytes: Long): String {
    if (bytes <= 0) return ""
    val units = listOf("bytes", "KB", "MB", "GB", "TB", "PB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1000 && unitIndex < units.lastIndex) {
        value /= 1000
        unitIndex++
    }
    val number = if (unitIndex == 0) bytes.toString()
    else DecimalFormat("0.#", DecimalFormatSymbols(Locale.US)).format(value)
    val unit = if (bytes == 1L) "byte" else units[unitIndex]
    return "$number $unit"
}

fun bytesToCustomHumanReadableString(bytes: Long, showDecimal: Boolean): String {
    val unit = 1024.0
    var result = bytes.toDouble()
    if (result < unit)
        return "$result MB"
    result /= unit
    val unitsToUse = "MGTPE"
    val unitsCount = unitsToUse.length
    val conversionLimit = 100
    var i = 0

    while (true) {
        result /= unit
        if (result < conversionLimit || i == unitsCount - 1)
            break
        ++i
    }

    val resultString =
        if (showDecimal) String.format(Locale.US, "%.1f", result) else result.toInt().toString()

    return with(StringBuilder(9)) {
        append(resultString)
        append(" ")
        append(unitsToUse[i])
        append("B")
    }.toString()
}

internal val BACKEND_DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

private val EDTF_DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx", Locale.US)

/**
 * Convert a LocalDate to a backend datetime string.
 *
 * @param useEndOfDay if true, result will be at 23:59:59 of the date (default = true).
 *                    if false, result will be at 00:00:00 of the date.
 */
fun LocalDate.toBackendDateTimeString(useEndOfDay: Boolean = true): String? {
    val time = if (useEndOfDay) LocalTime.of(23, 59, 59) else LocalTime.MIDNIGHT
    val dt = LocalDateTime.of(this, time)
    return dt.format(BACKEND_DATE_TIME_FORMATTER)
}

fun String.toLocalDateUtc(): LocalDate =
    Instant.parse(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

// "yyyy-MM-dd" (start of day) or "yyyy-MM-dd HH:mm:ss" (trailing fraction/zone ignored), else null.
fun String.toLocalDateTimeOrNull(): LocalDateTime? = try {
    if (length == 10) LocalDate.parse(this).atStartOfDay()
    else LocalDateTime.parse(take(19), BACKEND_DATE_TIME_FORMATTER)
} catch (e: DateTimeParseException) {
    null
}

fun String?.toDisplayDate(): String =
    this?.toLocalDateTimeOrNull()?.format(DateTimeFormatter.ofPattern("MMM. d, yyyy", Locale.US)) ?: ""

private val EDTF_LEVEL_0 =
    Regex("""\d{4}(-\d{2}(-\d{2}(T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:\d{2})?)?)?)?""")

// Level 0 EDTF -> the app's "yyyy-MM-dd HH:mm:ss" shape. Times with a zone are shown in device
// local time, dates and zone-less times as written. Anything else (qualifiers, ranges,
// unspecified digits, invalid dates) is shown verbatim.
fun String.edtfToDisplayDate(): String {
    val zone = EDTF_LEVEL_0.matchEntire(this)?.groupValues?.get(5) ?: return this
    return try {
        when {
            length == 4 -> this
            length == 7 -> this.also { YearMonth.parse(it) }
            length == 10 -> this.also { LocalDate.parse(it) }
            zone.isNotEmpty() -> toDateTimeIn(ZoneId.systemDefault()) ?: this
            else -> LocalDateTime.parse(this).format(BACKEND_DATE_TIME_FORMATTER)
        }
    } catch (e: DateTimeParseException) {
        this
    }
}

// An ISO date-time with "Z" or an offset -> "yyyy-MM-dd HH:mm:ss" in [zone], else null.
fun String.toDateTimeIn(zone: ZoneId): String? = try {
    OffsetDateTime.parse(this).atZoneSameInstant(zone).format(BACKEND_DATE_TIME_FORMATTER)
} catch (e: DateTimeParseException) {
    null
}

// Legacy dates are UTC "yyyy-MM-dd HH:mm:ss" on the wire; the app shows and edits them in local
// time. Dates without a time, and anything unparseable, pass through unchanged.
fun String.utcToLocalDateTime(): String = shiftDateTime(ZoneOffset.UTC, ZoneId.systemDefault())

fun String.localToUtcDateTime(): String = shiftDateTime(ZoneId.systemDefault(), ZoneOffset.UTC)

private fun String.shiftDateTime(from: ZoneId, to: ZoneId): String = if (length != 19) this else try {
    LocalDateTime.parse(this, BACKEND_DATE_TIME_FORMATTER).atZone(from)
        .withZoneSameInstant(to).format(BACKEND_DATE_TIME_FORMATTER)
} catch (e: DateTimeParseException) {
    this
}

// The editors' "yyyy-MM-dd" or local "yyyy-MM-dd HH:mm:ss" -> EDTF Level 0. Times carry the
// device offset at that date, as Web writes them: never "Z", never offset-less.
fun String.toEdtfLevel0(): String =
    if (length == 10) this
    else LocalDateTime.parse(this, BACKEND_DATE_TIME_FORMATTER)
        .atZone(ZoneId.systemDefault()).format(EDTF_DATE_TIME_FORMATTER)

@SuppressLint("SimpleDateFormat")
fun Uri.getDisplayName(context: Context): String {
    var displayName = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
    val cursor = context.contentResolver.query(this, null, null, null, null)
    try {
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        nameIndex?.let { displayName = cursor.getString(it) }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return displayName
}

fun Uri.getSize(context: Context): String {
    var displaySize = -1L
    val cursor = context.contentResolver.query(
        this, null, null, null, null
    )
    try {
        val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
        cursor?.moveToFirst()
        sizeIndex?.let { displaySize = cursor.getLong(it) }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return bytesToHumanReadableString(displaySize)
}

fun Uri.getMimeType(context: Context): String? {
    return context.contentResolver.getType(this)
}

fun Uri.getFile(context: Context, displayName: String): File? {
    var output: OutputStream? = null
    try {
        val inputStream = context.contentResolver.openInputStream(this)
        if (inputStream != null) {
            val file = File(context.cacheDir, displayName)
            output = FileOutputStream(file)

            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
            return file
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        output?.close()
    }
    return null
}

// Serialize a list of TwoFAVO to JSON
fun List<TwoFAVO>.toJson(): String {
    return Gson().toJson(this)
}

// Deserialize JSON to a list of TwoFAVO
fun String.toTwoFAVOList(): List<TwoFAVO> {
    val type = object : TypeToken<List<TwoFAVO>>() {}.type
    return Gson().fromJson(this, type)
}

val Record.pendingInvitationCount: Int
    get() = shares?.count { it.status.value == Status.PENDING } ?: 0

fun formatPendingInvitationCount(count: Int): String =
    if (count > 9) "9+" else count.toString()

fun View.boundsOnScreen(): Rect {
    val location = IntArray(2).also { getLocationOnScreen(it) }
    return Rect(location[0], location[1], location[0] + width, location[1] + height)
}

// A full-screen popup window: the status and navigation bars keep the screen's own look.
fun Window.drawBehindSystemBars(screenWindow: Window?) {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = Color.TRANSPARENT
    navigationBarColor = Color.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isNavigationBarContrastEnforced = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        attributes = attributes.also {
            it.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
    screenWindow?.let {
        val screenBars = WindowCompat.getInsetsController(it, it.decorView)
        WindowCompat.getInsetsController(this, decorView).apply {
            isAppearanceLightStatusBars = screenBars.isAppearanceLightStatusBars
            isAppearanceLightNavigationBars = screenBars.isAppearanceLightNavigationBars
        }
    }
}

const val SKELETON_PULSE_MILLIS = 800L
const val SKELETON_PULSE_MIN_ALPHA = 0.5f

fun View.startSkeletonPulse() {
    startAnimation(AlphaAnimation(1f, SKELETON_PULSE_MIN_ALPHA).apply {
        duration = SKELETON_PULSE_MILLIS
        repeatCount = Animation.INFINITE
        repeatMode = Animation.REVERSE
    })
}

// The fade under a pinned header appears once the list scrolls beneath it.
fun RecyclerView.fadeInOnScroll(fade: View) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0 && fade.alpha >= 1f) return
            val offset = recyclerView.computeVerticalScrollOffset()
            fade.alpha = (offset.toFloat() / fade.height.coerceAtLeast(1)).coerceAtMost(1f)
        }
    })
}
