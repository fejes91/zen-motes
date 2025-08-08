package hu.adamfejes.zenmotes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.pixeled

@Composable
fun getPixeledFontFamily(): FontFamily = FontFamily(
    Font(Res.font.pixeled),
)

@Composable
fun getTypography(): Typography {
    val pixeledFont = getPixeledFontFamily()
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = pixeledFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
        /* Other default text styles to override
        titleLarge = TextStyle(
            fontFamily = pixeledFont,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = pixeledFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
        */
    )
}