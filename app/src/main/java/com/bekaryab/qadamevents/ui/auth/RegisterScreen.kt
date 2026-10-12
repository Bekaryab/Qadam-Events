
package com.bekaryab.qadamevents.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val AlumniSansSC = FontFamily(
    Font(R.font.alumni_sans_sc_black)
)

// Header-specific families using exact existing resources, per Figma spec.
private val FindYourFamily = FontFamily(
    Font(R.font.alumni_sans_sc_black, FontWeight.Black)
)
private val LifePathFamily = FontFamily(
    Font(R.font.alumni_sans_sc_extrabold, FontWeight.ExtraBold)
)

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onRegister: (String, String, String) -> Unit,
    onOpenLogin: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Full-screen cyan design background fills edge-to-edge; outer
            // horizontal margins removed so #BFE0E1 reaches screen edges.
            // Full-height parent (no scroll here) so the weighted Spacer below can
            // anchor the card toward the bottom responsively on any screen height.
            .background(OuterBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Cyan block fills the same screen area, rectangular, no rounded corners.
                .background(color = OuterBackground)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                RegistrationHeader(
                    modifier = Modifier.fillMaxWidth()
                )

                // Responsive push: consumes all free vertical space, so the whole
                // card sits near the bottom with only the 12dp block padding left
                // as a small balanced bottom gap. Collapses to 0 on short screens
                // instead of a hardcoded offset that would clip content.
                Spacer(modifier = Modifier.weight(1f))

                AuthCard {
                    Text(
                        text = "CREATE ACCOUNT",
                        fontFamily = AlumniSansSC,
                        fontSize = 25.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "SIGN IN TO CONTINUE YOUR JOURNEY",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.sp,
                        color = Color.Black.copy(alpha = 0.5f),
                        textAlign = TextAlign.Start
                    )

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        keyboardType = KeyboardType.Email
                    )

                    AuthTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        keyboardType = KeyboardType.Phone
                    )

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        password = true
                    )

                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        password = true
                    )

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            fontFamily = AlumniSansSc,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    SubmitButton(
                        // Phone stays UI-only: the ViewModel/repository take
                        // (email, password, confirmPassword); Firebase is
                        // email/password, and validation ignores the phone.
                        onClick = {
                            onRegister(email.trim(), password, confirmPassword)
                        },
                        isLoading = state.isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OrDivider()

                    Spacer(modifier = Modifier.height(8.dp))

                    SocialButtonsRow()

                    Row(
                        // Spacing to the social row comes from the card's uniform
                        // 10dp arrangement; no extra top padding (the old +6dp
                        // stacked to 16dp and pushed the row too far down).
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALREADY HAVE AN ACCOUNT?",
                            fontFamily = AlumniSansSc,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp,
                            color = Muted
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        androidx.compose.material3.TextButton(
                            onClick = onOpenLogin
                        ) {
                            Text(
                                text = "LOGIN",
                                fontFamily = AlumniSansSc,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = Purple
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationHeader(
    modifier: Modifier = Modifier
) {
    // Assumptions: Figma canvas origin is top-left of the cyan block; exact canvas
    // width is unknown, but layer widths (365 / 264) imply a ~365-400dp phone canvas.
    // We scale Figma 120/96sp by (availableWidth / 365) so oversized type is preserved
    // on wide screens yet never clips on narrow phones. Box is the positioning
    // container; each heading is placed independently with align + offset, and the
    // vertical overlap is intentional per the reference image.
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val referenceWidth = 365f
        val scale = (maxWidth.value / referenceWidth).coerceIn(0.72f, 1f)

        val findFontSize = (120f * scale).sp
        val lifeFontSize = (96f * scale).sp
        val findLineHeight = (112f * scale).sp
        val lifeLineHeight = (115f * scale).sp

        // LIFE PATH sits substantially right and slightly lower, overlapping FIND YOUR.
        // Offset tuned so overlap stays readable: FIND bottom at 112, LIFE top at
        // 56 (moved up ~20dp), preserving intent without burying LIFE in FIND.
        val lifeOffsetX = (92f * scale).dp
        val lifeOffsetY = (56f * scale).dp
        // Total height = offset + LIFE layer height (covers FIND layer + overlap).
        val headerHeight = ((56f + 115f) * scale).dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            Text(
                text = "FIND YOUR",
                modifier = Modifier.align(Alignment.TopStart),
                fontFamily = FindYourFamily,
                fontWeight = FontWeight.Black,
                fontSize = findFontSize,
                lineHeight = findLineHeight,
                color = Color.White,
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "LIFE PATH",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = lifeOffsetX, y = lifeOffsetY),
                fontFamily = LifePathFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = lifeFontSize,
                lineHeight = lifeLineHeight,
                color = Purple,
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
