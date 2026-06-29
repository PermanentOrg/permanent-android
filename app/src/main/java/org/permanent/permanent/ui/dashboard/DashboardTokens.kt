package org.permanent.permanent.ui.dashboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.permanent.permanent.R

/**
 * Typography + color tokens taken from the Figma Dashboard design.
 *
 * The design uses two type families:
 *  - **"Usual"** for all UI text (header, greeting, body, buttons, notes, chips). Bundled in the
 *    app as `usual_*` fonts, so it's reproduced exactly here.
 *  - **"Gyst"** — the serif display face used for the hero titles, provided by the designer
 *    (`res/font/gyst_*.otf`). Titles render in Gyst Medium / Medium Italic per the design.
 */
val UsualFontFamily = FontFamily(
    Font(R.font.usual_regular, FontWeight.Normal),
    Font(R.font.usual_regular, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.usual_medium, FontWeight.Medium),
    Font(R.font.usual_medium, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.usual_bold, FontWeight.Bold),
)

// "Gyst" serif display face — the hero titles use Medium / Medium Italic.
val DashboardDisplayFont = FontFamily(
    Font(R.font.gyst_regular, FontWeight.Normal),
    Font(R.font.gyst_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.gyst_medium, FontWeight.Medium),
    Font(R.font.gyst_medium_italic, FontWeight.Medium, FontStyle.Italic),
)

// Hero-title gradients (from the design's text fills).
val PurpleOrangeTitleGradient = listOf(Color(0xFF800080), Color(0xFFFF9933))
val NavyTitleGradient = listOf(Color(0xFF131B4A), Color(0xFF364493))

// "Chart your path" card fill + its selected-chip icon/text gradient (purple → magenta).
val PurpleMagentaGradient = listOf(Color(0xFF800080), Color(0xFFB843A6))
