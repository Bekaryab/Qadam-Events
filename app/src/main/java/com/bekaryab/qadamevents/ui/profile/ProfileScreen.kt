package com.bekaryab.qadamevents.ui.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val ProfileNavy = Color(0xFF2E3768)
private val ProfileGrayBlue = Color(0xFF7B8AA0)
private val ProfileDivider = Color(0xFFB9C8D4)
private val LavenderText = Color(0xFF5B4BA0)
private val AvatarPlaceholder = Color(0xFFC7E3EC)
private val AvatarIconTint = Color(0xFF5B7A99)

/** Presentation-only statistic; replace [stats] with repository data later. */
data class ProfileStat(
    val value: String,
    val label: String
)

/**
 * Upper profile-header section. All content is parameterised with presentation
 * defaults so real user data / stats can be connected later without layout work.
 *
 * @param avatarRes real photo drawable, or null for the pale-blue placeholder.
 */
@Composable
fun ProfileHeader(
    name: String = "Abiyev Bekarys",
    status: String = "STUDENT",
    stats: List<ProfileStat> = listOf(
        ProfileStat("12", "EVENTS"),
        ProfileStat("5", "SAVES"),
        ProfileStat("23", "LIKES"),
        ProfileStat("2", "GROUPS")
    ),
    @DrawableRes avatarRes: Int? = null,
    onEditProfile: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Clean uniform pale-cyan background (inherited from the parent);
    // decorations removed for a calmer header.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(avatarRes = avatarRes)

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = name,
                                fontFamily = AlumniSansSc,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                lineHeight = 24.sp,
                                color = ProfileNavy,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = status,
                                fontFamily = AlumniSansSc,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                letterSpacing = 1.5.sp,
                                color = ProfileGrayBlue,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        MenuButton(onClick = onMenuClick)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    StatsRow(stats = stats)

                    Spacer(modifier = Modifier.height(4.dp))

                    EditProfileText(onClick = onEditProfile)
                }
            }
        }
}

@Composable
private fun AvatarImage(
    @DrawableRes avatarRes: Int?
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(96.dp)
            .background(AvatarPlaceholder, CircleShape)
            .border(3.dp, Color.White, CircleShape)
            .clip(CircleShape)
    ) {
        if (avatarRes != null) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Placeholder: no user photo asset exists yet. Pass avatarRes to
            // swap in a real image without touching this layout.
            Icon(
                painter = painterResource(id = R.drawable.tab_profile_person),
                contentDescription = null,
                tint = AvatarIconTint,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(stats: List<ProfileStat>) {
    // Compact cluster: wrap-content columns with 8dp gaps (also 8dp on each
    // side of the bare 1dp dividers). No weights, no full-width stretching.
    // Measured worst case (320dp screen): ~131dp content vs ~134dp available,
    // so GROUPS stays fully visible on one line.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(ProfileDivider)
                )
            }
            StatItem(
                value = stat.value,
                label = stat.label
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 19.sp,
            color = ProfileNavy,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = label,
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Normal,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.5.sp,
            color = ProfileGrayBlue,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun EditProfileText(onClick: () -> Unit) {
    // No edit-profile destination exists yet; kept ready for integration.
    // Text-only: no background, border, icon, or shadow.
    Text(
        text = "EDIT PROFILE",
        fontFamily = AlumniSansSc,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        letterSpacing = 1.sp,
        color = LavenderText,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun MenuButton(onClick: () -> Unit) {
    // No menu destination exists yet; kept ready for integration.
    // Bare three-line glyph only: no container, background, or shadow.
    // The 48dp touch target is invisible; the visible lines span 24x20dp.
    Canvas(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        val lineWidth = 24.dp.toPx()
        val totalHeight = 20.dp.toPx()
        val thickness = 2.5.dp.toPx()
        val startX = (size.width - lineWidth) / 2f
        for (i in 0..2) {
            val centerY = totalHeight * (1f + 2f * i) / 6f
            drawLine(
                color = ProfileNavy,
                start = Offset(startX, centerY),
                end = Offset(startX + lineWidth, centerY),
                strokeWidth = thickness,
                cap = StrokeCap.Round
            )
        }
    }
}
