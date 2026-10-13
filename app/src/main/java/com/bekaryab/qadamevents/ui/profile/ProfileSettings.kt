package com.bekaryab.qadamevents.ui.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val SettingsNavy = Color(0xFF2E3768)
private val SettingsMuted = Color(0xFF7B8AA0)
private val SettingsDanger = Color(0xFFB3261E)

private data class SettingItem(
    val title: String,
    @DrawableRes val iconRes: Int,
    val action: SettingAction
)

private sealed interface SettingAction {
    data class Placeholder(val message: String) : SettingAction
    data object Logout : SettingAction
}

/**
 * Settings content for the Profile SETTINGS tab. Only Log Out is wired to the
 * real authentication flow; every other row reports "coming soon" instead of
 * pretending its destination exists.
 */
@Composable
fun ProfileSettings(
    email: String,
    onLogout: () -> Unit,
    onPlaceholder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        SettingItem("Edit Profile", R.drawable.ic_pencil, SettingAction.Placeholder("Edit Profile is coming soon")),
        SettingItem("Notifications", R.drawable.ic_notifications, SettingAction.Placeholder("Notifications are coming soon")),
        SettingItem("Privacy", R.drawable.ic_lock, SettingAction.Placeholder("Privacy is coming soon")),
        SettingItem("Language", R.drawable.ic_language, SettingAction.Placeholder("Language is coming soon")),
        SettingItem("About Qadam Events", R.drawable.ic_info, SettingAction.Placeholder("About is coming soon")),
        SettingItem("Log Out", R.drawable.ic_logout, SettingAction.Logout)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "account") {
            Text(
                text = "Signed in as $email",
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = SettingsMuted,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        items(rows, key = { it.title }) { row ->
            val isLogout = row.action is SettingAction.Logout
            val accent = if (isLogout) SettingsDanger else SettingsNavy
            Surface(
                onClick = {
                    when (val action = row.action) {
                        is SettingAction.Logout -> onLogout()
                        is SettingAction.Placeholder -> onPlaceholder(action.message)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(14.dp),
                        clip = false,
                        spotColor = Color.Black.copy(alpha = 0.08f),
                        ambientColor = Color.Black.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = row.iconRes),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = row.title,
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = accent,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isLogout) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = SettingsMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
