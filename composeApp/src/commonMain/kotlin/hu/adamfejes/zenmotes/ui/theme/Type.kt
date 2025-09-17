package hu.adamfejes.zenmotes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.Minecraft

@Composable
fun getFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Minecraft),
)

@Composable
fun getTypography(): Typography {
    val pixeledFont = getFontFamily()
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = pixeledFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = pixeledFont,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            lineHeight = 56.sp,
            letterSpacing = 0.sp
        ),
        labelLarge = TextStyle(
            fontFamily = pixeledFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )
}