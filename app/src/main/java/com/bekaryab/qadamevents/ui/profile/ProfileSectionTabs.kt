package com.bekaryab.qadamevents.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

/** Secondary navigation inside the Profile screen (not the bottom bar). */
enum class ProfileSection(val label: String) {
    MY_EVENTS("MY EVENTS"),
    SAVES("SAVES"),
    LIKES("LIKES"),
    SETTINGS("SETTINGS")
}

private val TabsBackground = Color(0xFFE3E7EB)
private val TabIdleBackground = Color(0xFFF2F5F7)
private val TabNavy = Color(0xFF2E3768)
private val SelectedOutline = Color(0xFF2196F3)

/**
 * Reusable Profile tab selector. Pure UI: the caller owns [selected] state and
 * receives clicks via [onTabSelected]. Equal weights keep labels unclipped on
 * narrow screens.
 */
@Composable
fun ProfileSectionTabs(
    selected: ProfileSection,
    onTabSelected: (ProfileSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TabsBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileSection.values().forEach { section ->
                SectionTab(
                    section = section,
                    selected = section == selected,
                    onClick = { onTabSelected(section) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RowScope.SectionTab(
    section: ProfileSection,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Plain Box + clickable, NOT a clickable Surface: Material3's clickable
    // Surface enforces a 48dp minimum touch target, which was secretly forcing
    // every tab (and the container) to ~52dp regardless of padding. Box has no
    // such floor, so the true padding-driven height (28dp) renders.
    val tabShape = RoundedCornerShape(10.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(
                elevation = if (selected) 1.dp else 0.dp,
                shape = tabShape,
                clip = false,
                spotColor = Color.Black.copy(alpha = 0.12f),
                ambientColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(tabShape)
            .background(if (selected) Color.White else TabIdleBackground)
            .then(
                if (selected) Modifier.border(BorderStroke(1.5.dp, SelectedOutline), tabShape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 1.dp, vertical = 3.dp)
    ) {
        Text(
            text = section.label,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            // Explicit tight line height: without it the label inherits the
            // theme bodyLarge 24.sp line box (dead space taller than the tab).
            lineHeight = 12.sp,
            letterSpacing = 0.4.sp,
            color = TabNavy,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}
