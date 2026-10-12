
package com.bekaryab.qadamevents.ui.auth

import android.graphics.BlurMaskFilter
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

// Shared auth design-system values (moved verbatim from RegisterScreen so both
// auth pages render identically).
val OuterBackground = Color(0xFFBFE0E1)
val CardBackground = Color(0xFFE9F5F4)
val Purple = Color(0xFF6F49BF)
val Green = Color(0xFF3E6B31)
val Muted = Color(0xFF858585)
val BorderGray = Color(0xFFD4D4D4)
val SocialGray = Color(0xFFD9D9D9)
val DarkGray = Color(0xFF424242)
val GrayShadow = Color(0xFF9E9E9E).copy(alpha = 0.45f)

/**
 * Reusable Figma "Inner Shadow" effect for rounded rectangles, implemented with
 * native Android blur ([BlurMaskFilter]) inside [drawWithCache].
 *
 * A blurred copy of the shape is drawn on an offscreen layer and its interior is
 * punched out, leaving only a soft inset rim along the inner edges. The center
 * stays untouched, and the effect is fully independent from any outer drop
 * shadow applied via [shadow].
 */
fun Modifier.roundedInnerShadow(
    radius: Dp,
    color: Color = Color.Black.copy(alpha = 0.22f),
    blur: Dp = 6.dp,
    spread: Dp = 2.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 2.dp
): Modifier = this.drawWithCache {
    val radiusPx = radius.toPx()
    val blurPx = blur.toPx()
    val spreadPx = spread.toPx()
    val dx = offsetX.toPx()
    val dy = offsetY.toPx()
    val shadowPaint = Paint().apply {
        this.color = color
        asFrameworkPaint().maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
    }
    val erasePaint = Paint().apply { blendMode = BlendMode.Clear }
    val layerPaint = Paint()
    onDrawWithContent {
        drawContent()
        drawIntoCanvas { canvas ->
            canvas.saveLayer(Rect(Offset.Zero, size), layerPaint)
            canvas.drawRoundRect(
                left = -spreadPx + dx,
                top = -spreadPx + dy,
                right = size.width + spreadPx + dx,
                bottom = size.height + spreadPx + dy,
                radiusX = radiusPx,
                radiusY = radiusPx,
                paint = shadowPaint
            )
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = radiusPx,
                radiusY = radiusPx,
                paint = erasePaint
            )
            canvas.restore()
        }
    }
}

/** Light-cyan rounded auth card shared by the Login and Register pages. */
@Composable
fun AuthCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

/** White auth input field shared by the Login and Register pages. */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    @DrawableRes iconRes: Int? = null,
    imeAction: ImeAction = ImeAction.Default,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            )
        },
        leadingIcon = iconRes?.let { res ->
            {
                Icon(
                    painter = painterResource(id = res),
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        singleLine = true,
        enabled = enabled,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = AlumniSansSc
        ),
        visualTransformation = if (password) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Purple,
            unfocusedBorderColor = BorderGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedLabelColor = Purple,
            unfocusedLabelColor = Muted
        )
    )
}

/** Green Submit button with Figma drop shadow + inner shadow, shared by auth pages. */
@Composable
fun SubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            // Figma drop shadow: black 25%, X=0, Y=4, blur=4, spread=0.
            // Replaces the default Material elevation, zeroed below.
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
            // Figma inner shadow: subtle inset rim, clipped to the same
            // rounded shape; independent from the drop shadow above.
            .roundedInnerShadow(
                radius = 20.dp,
                color = Color.Black.copy(alpha = 0.22f),
                blur = 6.dp,
                spread = 2.dp,
                offsetY = 2.dp
            ),
        shape = RoundedCornerShape(20.dp),
        // Figma stroke: same green, weight 1, inside alignment.
        border = BorderStroke(1.dp, Green),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green,
            contentColor = Color.White,
            disabledContainerColor = Green,
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "SUBMIT",
                fontFamily = AlumniSansSc,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 6.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = BorderGray
        )
        Text(
            text = "OR",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            letterSpacing = 1.sp,
            color = Muted,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = BorderGray
        )
    }
}

@Composable
fun SocialButtonsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SocialButton(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.google_logo,
            contentDescription = "Google",
            label = "CONTINUE WITH GOOGLE"
        )
        SocialButton(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.facebook_logo,
            contentDescription = "Facebook",
            label = "CONTINUE WITH FACEBOOK"
        )
    }
}

@Composable
private fun SocialButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    contentDescription: String,
    label: String
) {
    // Visual only: no Google/Facebook auth flow exists in the backend
    // (AuthRepository is email/password), so this is a non-clickable Surface
    // rather than a working login button.
    Surface(
        modifier = modifier
            .height(48.dp)
            // Single subtle neutral-gray outer shadow. No inner shadow: Figma
            // specifies none for these buttons.
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                spotColor = GrayShadow,
                ambientColor = GrayShadow
            ),
        shape = RoundedCornerShape(24.dp),
        color = SocialGray
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                // Weighted so the text wraps inside the remaining button width
                // instead of overflowing past the button edges.
                modifier = Modifier.weight(1f, fill = false),
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.3.sp,
                color = DarkGray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true
            )
        }
    }
}
