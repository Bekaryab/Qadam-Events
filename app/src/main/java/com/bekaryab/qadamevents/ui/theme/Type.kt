package com.bekaryab.qadamevents.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bekaryab.qadamevents.R

// Shared Alumni Sans SC family. No medium file exists in res/font, so Medium
// reuses the regular file (same visual as Regular, correct selection behavior).
val AlumniSansSc = FontFamily(
    Font(R.font.alumni_sans_sc_regular, FontWeight.Normal),
    Font(R.font.alumni_sans_sc_regular, FontWeight.Medium),
    Font(R.font.alumni_sans_sc_semibold, FontWeight.SemiBold),
    Font(R.font.alumni_sans_sc_bold, FontWeight.Bold),
    Font(R.font.alumni_sans_sc_extrabold, FontWeight.ExtraBold),
    Font(R.font.alumni_sans_sc_black, FontWeight.Black)
)

// Shared typography: every Material 3 text style keeps its default size, weight,
// line height, and letter spacing — only the font family is swapped to Alumni
// Sans SC. Applied app-wide via QadamEventsTheme, so screens using default or
// MaterialTheme styles (Login, AuthSuccess, fields, buttons, dialogs) inherit it
// with no per-screen edits.
private val BaseTypography = Typography()

val Typography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = AlumniSansSc),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AlumniSansSc),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AlumniSansSc),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = AlumniSansSc),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = AlumniSansSc),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AlumniSansSc),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = AlumniSansSc),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AlumniSansSc),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AlumniSansSc),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = AlumniSansSc),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = AlumniSansSc),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AlumniSansSc),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AlumniSansSc),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = AlumniSansSc),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = AlumniSansSc)
)