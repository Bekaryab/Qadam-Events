
package com.bekaryab.qadamevents.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bekaryab.qadamevents.R

/** Bottom-tab destinations, in reference order. Screens are placeholders for now. */
enum class QadamTab(
    val label: String,
    @DrawableRes val iconRes: Int
) {
    Create("Create", R.drawable.tab_create_plus),
    Explore("Explore", R.drawable.tab_explore_search),
    Reels("Reels", R.drawable.tab_reels_video),
    Profile("Profile", R.drawable.tab_profile_person)
}

private val BarBackground = Color(0xFFF8FCFC)
private val SelectedPurple = Color(0xFF6840E8)
private val NavyIcon = Color(0xFF2E3768)
private val CreateSquare = Color(0xFF2F2A5C)

/**
 * Reusable bottom TabBar matching the Figma reference.
 *
 * Pure UI: the caller owns [selectedTab] state (or wires it to navigation) and
 * receives clicks via [onTabSelected]. Tabs share the row with equal [weight],
 * so no hardcoded widths are needed.
 */
@Composable
fun QadamBottomBar(
    selectedTab: QadamTab,
    onTabSelected: (QadamTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false,
                spotColor = Color.Black.copy(alpha = 0.10f),
                ambientColor = Color.Black.copy(alpha = 0.10f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = BarBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QadamTab.values().forEach { tab ->
                TabItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: QadamTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp)
        ) {
            when {
                // Create keeps its dark-indigo rounded square in every state.
                tab == QadamTab.Create -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(CreateSquare, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.label,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Selected tab: white icon on a purple circle with a subtle glow.
                selected -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            clip = false,
                            spotColor = SelectedPurple.copy(alpha = 0.35f),
                            ambientColor = SelectedPurple.copy(alpha = 0.35f)
                        )
                        .background(SelectedPurple, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.label,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                else -> Icon(
                    painter = painterResource(id = tab.iconRes),
                    contentDescription = tab.label,
                    tint = NavyIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun QadamBottomBarPreview() {
    Box(
        modifier = Modifier
            .background(Color(0xFFDDF3F5))
            .padding(16.dp)
    ) {
        QadamBottomBar(
            selectedTab = QadamTab.Profile,
            onTabSelected = { }
        )
    }
}
