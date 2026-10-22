package org.permanent.permanent.edtf

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

private val CASES: JsonObject = EdtfConverterTest::class.java
    .getResourceAsStream("/edtf/edtf-cases.json")!!
    .reader()
    .use { JsonParser.parseReader(it).asJsonObject }

private val OFFSET: ZoneOffset = ZoneOffset.of(CASES["defaultOffset"].asString)

@RunWith(Parameterized::class)
class EdtfConverterCasesTest(@Suppress("unused") private val name: String, private val case: JsonObject) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = CASES["cases"].asJsonArray.map { element ->
            val case = element.asJsonObject
            val source = case["source"].asString
            val name = if (case.has("decode")) "$source decode \"${case["decode"].asString}\""
            else "$source encode ${case["id"].asString}"
            arrayOf(name, case)
        }
    }

    @Test
    fun matchesCase() {
        if (case.has("decode")) checkDecode(case["decode"].asString) else checkEncode()
    }

    private fun checkDecode(input: String) {
        val result = EdtfConverter.decode(input)
        if (case.has("unsupported")) {
            val reason = EdtfDecodeResult.Reason.valueOf(case["unsupported"].asString)
            assertThat(result).isEqualTo(EdtfDecodeResult.Unsupported(input, reason))
            return
        }
        assertThat(result).isInstanceOf(EdtfDecodeResult.Success::class.java)
        val value = (result as EdtfDecodeResult.Success).value
        case["expect"]?.let { assertThat(value).isEqualTo(valueOf(it)) }
        val reencoded = case["reencode"]?.asString ?: input
        assertThat(EdtfConverter.encode(value, OFFSET)).isEqualTo(EdtfEncodeResult.Success(reencoded))
    }

    private fun checkEncode() {
        val result = EdtfConverter.encode(valueOf(case["encode"]), OFFSET)
        when {
            case.has("expect") -> {
                val expected = case["expect"].asString
                assertThat(result).isEqualTo(EdtfEncodeResult.Success(expected))
                val decoded = EdtfConverter.decode(expected)
                assertThat(decoded).isInstanceOf(EdtfDecodeResult.Success::class.java)
                assertThat(EdtfConverter.encode((decoded as EdtfDecodeResult.Success).value, OFFSET))
                    .isEqualTo(result)
            }
            case.has("errors") -> assertThat(result).isEqualTo(
                EdtfEncodeResult.Invalid(case["errors"].asJsonArray.map { errorOf(it.asString) })
            )
            else -> assertThat(result).isEqualTo(EdtfEncodeResult.Empty)
        }
    }

    private fun valueOf(json: JsonElement): EdtfValue = when {
        json.isJsonPrimitive -> EdtfValue.UnknownDate
        json.asJsonObject.has("single") -> EdtfValue.Single(partsOf(json.asJsonObject["single"].asJsonObject))
        else -> json.asJsonObject["range"].asJsonArray.let { EdtfValue.Range(sideOf(it[0]), sideOf(it[1])) }
    }

    private fun sideOf(json: JsonElement): EdtfSide = when {
        !json.isJsonPrimitive -> EdtfSide.Dated(partsOf(json.asJsonObject))
        json.asString == "open" -> EdtfSide.Open
        else -> EdtfSide.Unknown
    }

    private fun partsOf(json: JsonObject) = EdtfDateParts(
        year = json["year"]?.asString.orEmpty(),
        month = json["month"]?.asString.orEmpty(),
        day = json["day"]?.asString.orEmpty(),
        time = json["time"]?.asString?.split(":")?.map(String::toInt)?.let {
            EdtfTime(it[0], it[1], it.getOrElse(2) { 0 }, json["offset"]?.asString?.let(ZoneOffset::of))
        },
        uncertain = json["uncertain"]?.asBoolean == true,
        approximate = json["approximate"]?.asBoolean == true,
    )

    private fun errorOf(text: String) = text.split(":").let { (side, code) ->
        EdtfError(EdtfError.Side.valueOf(side), EdtfError.Code.valueOf(code))
    }
}

class EdtfConverterTest {

    @Test
    fun encodeUsesTodaysDeviceOffsetInBothDstSeasons() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            val today = ZoneId.of("America/New_York").rules.getOffset(Instant.now())
            val january = EdtfValue.Single(EdtfDateParts("2020", "01", "15", EdtfTime(10, 0, 0)))
            val july = EdtfValue.Single(EdtfDateParts("2020", "07", "15", EdtfTime(10, 0, 0)))

            assertThat(EdtfConverter.deviceOffsetNow()).isEqualTo(today)
            assertThat(EdtfConverter.encode(january)).isEqualTo(EdtfEncodeResult.Success("2020-01-15T10:00:00${today.id}"))
            assertThat(EdtfConverter.encode(july)).isEqualTo(EdtfEncodeResult.Success("2020-07-15T10:00:00${today.id}"))
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun decodeNeverThrows() {
        listOf(
            "\u0000", "////", "T", "2024-06-15T", "9".repeat(1000), "XXXX-XX-XX-XX", "2024-06-15T10:00:00+99:00",
            "..", "?", "2024??", "２０２４", " 2024", "2024 ", "2024-6-1", "x", "19xx",
        ).forEach { input ->
            assertThat(EdtfConverter.decode(input)).isInstanceOf(EdtfDecodeResult.Unsupported::class.java)
        }
    }
}
