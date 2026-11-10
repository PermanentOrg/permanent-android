package org.permanent.permanent.ui.bulkEditMetadata

import android.icu.text.TimeZoneNames
import android.icu.util.TimeZone
import android.icu.util.ULocale
import org.permanent.permanent.edtf.EdtfConverter
import org.permanent.permanent.edtf.decodeWrittenTime
import org.permanent.permanent.models.DisplayDated
import java.text.Normalizer
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TimeZoneOption(
    val id: String,
    val zone: ZoneId,
    val region: String,
    val city: String,
    val country: String?,
) {
    val placeName: String
        get() = country?.let { "$it, $city" } ?: city

    // The search text that doesn't move with the date; read only by a search.
    private val foldedWords: String by lazy {
        listOfNotNull(id.replace('_', ' ').replace('/', ' '), TimeZoneCatalog.genericName(id), country).joinToString(" ").folded()
    }

    // Gap: moves forward. Overlap: [preferred] when it fits, else the earlier offset (iOS).
    fun offsetAt(clock: LocalDateTime, preferred: ZoneOffset? = null): ZoneOffset =
        ZonedDateTime.ofLocal(clock, zone, preferred).offset.withoutSeconds()

    fun offsetLabel(clock: LocalDateTime): String = offsetLabel(offsetAt(clock))

    // English letters ("EDT", "GMT") when the platform has them, else the offset ("+02", "+0530").
    fun shortName(clock: LocalDateTime, preferred: ZoneOffset? = null): String {
        val zoned = ZonedDateTime.ofLocal(clock, zone, preferred)
        val daylight = zone.rules.isDaylightSavings(zoned.toInstant())
        val name = java.util.TimeZone.getTimeZone(id).getDisplayName(daylight, java.util.TimeZone.SHORT, Locale.US)
        return name.takeIf { it.isNotEmpty() && it.all(Char::isLetter) } ?: tzdataOffset(zoned.offset)
    }

    internal fun matches(foldedNeedle: String, label: String): Boolean =
        foldedNeedle in foldedWords || foldedNeedle in label.folded()
}

data class ListedZone(val option: TimeZoneOption, val offsetLabel: String)

data class TimeZoneRegion(val name: String, val zones: List<ListedZone>)

object TimeZoneCatalog {
    private val names: TimeZoneNames by lazy { TimeZoneNames.getInstance(ULocale.US) }
    private val options = HashMap<String, TimeZoneOption?>()

    // zone.tab ids, as iOS and Web write them; ids this device's tz data lacks are left out.
    val listed: List<TimeZoneOption> by lazy { TIME_ZONE_COUNTRY_CODES.keys.sorted().mapNotNull(::option) }

    // Any id the platform knows, listed or not (an old alias or "UTC" a record may carry).
    fun option(id: String): TimeZoneOption? = synchronized(options) { options.getOrPut(id) { create(id) } }

    fun regions(clock: LocalDateTime, query: String = ""): List<TimeZoneRegion> {
        val needle = query.trim().let { (offsetNeedle(it) ?: it).folded() }
        return listed
            .map { option -> option.offsetAt(clock).let { Triple(option, it.totalSeconds, offsetLabel(it)) } }
            .filter { (option, _, label) -> needle.isEmpty() || option.matches(needle, label) }
            .groupBy { it.first.region }
            .toSortedMap()
            .map { (name, group) ->
                val sorted = group.sortedWith(
                    compareBy<Triple<TimeZoneOption, Int, String>> { it.second }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.first.city }
                )
                TimeZoneRegion(name, sorted.map { ListedZone(it.first, it.third) })
            }
    }

    internal fun genericName(id: String): String? =
        names.getDisplayName(TimeZone.getCanonicalID(id) ?: id, TimeZoneNames.NameType.LONG_GENERIC, System.currentTimeMillis())

    private fun create(id: String): TimeZoneOption? {
        val zone = try {
            ZoneId.of(id)
        } catch (e: DateTimeException) {
            return null
        }
        val parts = id.split('/')
        return TimeZoneOption(
            id = id,
            zone = zone,
            region = parts.first(),
            city = parts.last().replace('_', ' '),
            country = TIME_ZONE_COUNTRY_CODES[id]?.let { Locale("", it).getDisplayCountry(Locale.US) }?.takeIf { it.isNotEmpty() },
        )
    }
}

// After the Info row's time: the zone's short name, else the offset the string carries ("+03").
val DisplayDated.shownZone: String?
    get() {
        val written = displayTime?.let { EdtfConverter.decodeWrittenTime(it, rangeStart = false) } ?: return null
        val clock = written.clock ?: return null
        timezone?.let(TimeZoneCatalog::option)?.let { return it.shortName(clock, written.storedOffset) }
        return written.storedOffset?.let(::tzdataOffset)
    }

private val TZDATA_OFFSET: DateTimeFormatter = DateTimeFormatter.ofPattern("x", Locale.US)
private val COMBINING_MARKS = Regex("\\p{Mn}+")

// "GMT+01:00", the row label.
private fun offsetLabel(offset: ZoneOffset): String = "GMT" + EdtfConverter.formatOffset(offset)

// "+02", "+0530": an offset without letters, as the tz database abbreviates it.
private fun tzdataOffset(offset: ZoneOffset): String = TZDATA_OFFSET.format(offset.withoutSeconds())

private fun ZoneOffset.withoutSeconds(): ZoneOffset = ZoneOffset.ofTotalSeconds(totalSeconds / 60 * 60)

private fun String.folded(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(COMBINING_MARKS, "").lowercase(Locale.US)

// "+5", "GMT-3", "utc +5:30" or "+0530" as the labels write them: "+05", "-03", "+05:30".
private fun offsetNeedle(text: String): String? {
    var rest = text.trim().lowercase(Locale.US)
    listOf("gmt", "utc").firstOrNull { rest.startsWith(it) }?.let { rest = rest.removePrefix(it).trim() }
    val sign = rest.firstOrNull()?.takeIf { it == '+' || it == '-' } ?: return null
    val digits = rest.drop(1).replace(":", "")
    if (digits.isEmpty() || digits.length > 4 || !digits.all(Char::isDigit)) return null
    val hours = if (digits.length <= 2) digits else digits.dropLast(2)
    val minutes = if (digits.length <= 2) null else digits.takeLast(2)
    if (hours.toInt() >= 24) return null
    return String.format(Locale.US, "%s%02d", sign, hours.toInt()) + (minutes?.let { ":$it" } ?: "")
}
