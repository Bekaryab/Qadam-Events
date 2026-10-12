
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

// WELCOME uses the genuine static alumni_sans_sc_blackitalic.ttf face
// (verified: "Alumni Sans SC Black Italic", usWeightClass = 900, non-variable),
// registered exactly as Black + Italic. No synthesis, skew, or substitution.
private val WelcomeFamily = FontFamily(
    Font(R.font.alumni_sans_sc_blackitalic, FontWeight.Black, FontStyle.Italic)
)

private val LoginRed = Color(0xFFB12629)

@Composable
fun LoginScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onOpenRegister: () -> Unit
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Same full-screen cyan system as the Register page: edge-to-edge
            // #BFE0E1 background, safe-drawing insets, and a full-height parent
            // so the weighted Spacer anchors the card toward the bottom.
            .background(OuterBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = OuterBackground)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LoginHeader(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                AuthCard {
                    Text(
                        text = "HI, HOW ARE YOU?",
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
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
                        textAlign = TextAlign.Center
                    )

                    AuthTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "PHONE NUMBER",
                        iconRes = R.drawable.ic_phone,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                        enabled = !state.isLoading
                    )

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "PASSWORD",
                        iconRes = R.drawable.ic_lock,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        password = true,
                        enabled = !state.isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(
                            // No forgot-password flow exists in the backend;
                            // visual entry point only.
                            onClick = { },
                            enabled = !state.isLoading
                        ) {
                            Text(
                                text = "FORGOT PASSWORD?",
                                fontFamily = AlumniSansSc,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = Purple
                            )
                        }
                    }

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    SubmitButton(
                        onClick = { onLogin(phone.trim(), password) },
                        isLoading = state.isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OrDivider()

                    Spacer(modifier = Modifier.height(8.dp))

                    SocialButtonsRow()

                    Row(
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
                            // Preserves the existing navigation callback: the graph
                            // exposes only onOpenRegister here. NOTE: the Figma row
                            // labels this action LOGIN; rename to REGISTER if the
                            // design is ever reconciled with its destination.
                            onClick = onOpenRegister
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
private fun LoginHeader(
    modifier: Modifier = Modifier
) {
    // Same responsive approach as the Register header: Figma sizes (96 / 70)
    // scaled by (availableWidth / 365) so the oversized type is preserved on wide
    // screens yet never clips on narrow phones. WELCOME is top-start; COMING SOON
    // is right-aligned beneath it with an intentional slight overlap. Right edge
    // alignment (instead of a fixed X offset) guarantees no horizontal overflow.
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val referenceWidth = 365f
        val scale = (maxWidth.value / referenceWidth).coerceIn(0.72f, 1f)

        val welcomeFontSize = (96f * scale).sp
        val comingFontSize = (70f * scale).sp
        val welcomeLineHeight = (89f * scale).sp
        val comingLineHeight = (65f * scale).sp

        // COMING SOON starts slightly above WELCOME's bottom edge (14dp overlap
        // at scale 1) for the intended stacked overlap while staying readable.
        val comingOffsetY = ((89f - 14f) * scale).dp
        val headerHeight = ((89f - 14f + 65f) * scale).dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            Text(
                text = "WELCOME",
                modifier = Modifier.align(Alignment.TopStart),
                fontFamily = WelcomeFamily,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = welcomeFontSize,
                lineHeight = welcomeLineHeight,
                color = LoginRed,
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "COMING SOON",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .offset(y = comingOffsetY),
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = comingFontSize,
                lineHeight = comingLineHeight,
                color = Color.White,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun LoginScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        LoginScreen(
            state = AuthUiState(),
            onLogin = { _, _ -> },
            onOpenRegister = {}
        )
    }
}
