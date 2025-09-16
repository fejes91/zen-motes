package hu.adamfejes.zenmotes.ui.theme

import androidx.compose.ui.graphics.Color

object ZenColors {
    
    object Light {
        internal val background = Color.White

        // Sand colors - more saturated, same brightness
        internal val sandColors = listOf(
            Color(0xFFFF80B3), // More saturated pink
            Color(0xFF80C0FF), // More saturated blue
            Color(0xFF80FF80), // More saturated green
            Color(0xFFFFD040), // More saturated yellow
            Color(0xFFD080FF), // More saturated purple
            Color(0xFFFF8040)  // More saturated orange
        )
        
        // Obstacle colors - more saturated with slight variations
        internal val obstacleColors = listOf(
            Color(0xFFFF6699), // More saturated pink
            Color(0xFF66B3FF), // More saturated blue
            Color(0xFF66FF66), // More saturated green
            Color(0xFFFFCC00), // More saturated yellow
            Color(0xFFB366FF), // More saturated purple
            Color(0xFFFF6600)  // More saturated orange
        )
        
        
        // UI element colors
        internal val paletteBorder = Color(0xFF333333) // Dark gray for palette border
        internal val pauseButtonBackground = Color(0xFFF5F5F5) // Light gray with pastel feel
        internal val pauseButtonIcon = Color(0xFF6B6B6B) // Soft gray for icons
        internal val pauseOverlayBackground = Color(0x80FFFFFF) // Semi-transparent white
        internal val pausedTitleText = Color(0xFF4A4A4A) // Darker gray for title visibility
        
        // Primary and secondary button colors
        internal val primaryButtonBackground = Color(0xFFE8F8E8) // Very light green pastel
        internal val primaryButtonText = Color(0xFF4CAF50) // Soft green
        internal val secondaryButtonBackground = Color(0xFFFFE8E8) // Very light pink pastel  
        internal val secondaryButtonText = Color(0xFFE57373) // Soft pink/red
        
        // Theme switch colors
        internal val themeSwitchBackground = Color(0xFFF0F0F0) // Light gray background
        internal val themeSwitchText = Color(0xFF6B6B6B) // Soft gray text

        internal val positiveBackground = Color(0xFFE8F8E8) // Light green pastel for positive actions

        internal val negativeBackground = Color(0xFFFFE8E8) // Light pink pastel for negative actions
    }
    
    object Dark {
        internal val background = Color.Black

        // Sand colors - brighter pastels for dark theme
        internal val sandColors = listOf(
            Color(0xFFFF85B5), // Bright pink
            Color(0xFF85CFFF), // Bright blue
            Color(0xFF85FF85), // Bright green
            Color(0xFFFFE666), // Bright yellow
            Color(0xFFCC85FF), // Bright purple
            Color(0xFFFF8566)  // Bright orange
        )
        
        // Obstacle colors - matching darker pastels
        internal val obstacleColors = listOf(
            Color(0xFFB85578), // Deeper muted pink
            Color(0xFF5291D1), // Deeper muted blue
            Color(0xFF52D152), // Deeper muted green
            Color(0xFFD1B820), // Deeper muted yellow
            Color(0xFF9F52D1), // Deeper muted purple
            Color(0xFFD15220)  // Deeper muted orange
        )
        
        
        // UI element colors - dark theme
        internal val paletteBorder = Color(0xFFCCCCCC) // Light gray for palette border
        internal val pauseButtonBackground = Color(0xFF2A2A2A) // Dark gray
        internal val pauseButtonIcon = Color(0xFFB0B0B0) // Light gray for icons
        internal val pauseOverlayBackground = Color(0x80000000) // Semi-transparent black
        internal val pausedTitleText = Color(0xFFE0E0E0) // Light gray for title visibility
        
        // Primary and secondary button colors - dark theme
        internal val primaryButtonBackground = Color(0xFF1A2D1A) // Dark green pastel
        internal val primaryButtonText = Color(0xFF6BE66B) // Muted green
        internal val secondaryButtonBackground = Color(0xFF2D1A1A) // Dark pink pastel
        internal val secondaryButtonText = Color(0xFFD1668A) // Muted pink
        
        // Theme switch colors - dark theme
        internal val themeSwitchBackground = Color(0xFF3A3A3A) // Dark gray background
        internal val themeSwitchText = Color(0xFFB0B0B0) // Light gray text

        internal val positiveBackground = Color(0xFF1A2D1A) // Dark green pastel for positive actions

        internal val negativeBackground = Color(0xFF2D1A1A) // Dark pink pastel for negative actions
    }
}

enum class Theme {
    LIGHT,
    DARK
}

fun Theme.toColorScheme(): ColorScheme {
    return getColorScheme(this)
}

data class ColorScheme(
    val background: Color,
    val sandColors: List<Color>,
    val obstacleColors: List<Color>,
    val paletteBorder: Color,
    val pauseButtonBackground: Color,
    val pauseButtonIcon: Color,
    val pauseOverlayBackground: Color,
    val pausedTitleText: Color,
    val primaryButtonBackground: Color,
    val primaryButtonText: Color,
    val secondaryButtonBackground: Color,
    val secondaryButtonText: Color,
    val textBackground: Color,
    val textColor: Color,
    val positiveBackground: Color,
    val negativeBackground: Color
)

fun getColorScheme(theme: Theme): ColorScheme {
    return when (theme) {
        Theme.LIGHT -> ColorScheme(
            background = ZenColors.Light.background,
            sandColors = ZenColors.Light.sandColors,
            obstacleColors = ZenColors.Light.obstacleColors,
            paletteBorder = ZenColors.Light.paletteBorder,
            pauseButtonBackground = ZenColors.Light.pauseButtonBackground,
            pauseButtonIcon = ZenColors.Light.pauseButtonIcon,
            pauseOverlayBackground = ZenColors.Light.pauseOverlayBackground,
            pausedTitleText = ZenColors.Light.pausedTitleText,
            primaryButtonBackground = ZenColors.Light.primaryButtonBackground,
            primaryButtonText = ZenColors.Light.primaryButtonText,
            secondaryButtonBackground = ZenColors.Light.secondaryButtonBackground,
            secondaryButtonText = ZenColors.Light.secondaryButtonText,
            textBackground = ZenColors.Light.themeSwitchBackground,
            textColor = ZenColors.Light.themeSwitchText,
            positiveBackground = ZenColors.Light.positiveBackground,
            negativeBackground = ZenColors.Light.negativeBackground
        )
        Theme.DARK -> ColorScheme(
            background = ZenColors.Dark.background,
            sandColors = ZenColors.Dark.sandColors,
            paletteBorder = ZenColors.Dark.paletteBorder,
            obstacleColors = ZenColors.Dark.obstacleColors,
            pauseButtonBackground = ZenColors.Dark.pauseButtonBackground,
            pauseButtonIcon = ZenColors.Dark.pauseButtonIcon,
            pauseOverlayBackground = ZenColors.Dark.pauseOverlayBackground,
            pausedTitleText = ZenColors.Dark.pausedTitleText,
            primaryButtonBackground = ZenColors.Dark.primaryButtonBackground,
            primaryButtonText = ZenColors.Dark.primaryButtonText,
            secondaryButtonBackground = ZenColors.Dark.secondaryButtonBackground,
            secondaryButtonText = ZenColors.Dark.secondaryButtonText,
            textBackground = ZenColors.Dark.themeSwitchBackground,
            textColor = ZenColors.Dark.themeSwitchText,
            positiveBackground = ZenColors.Dark.positiveBackground,
            negativeBackground = ZenColors.Dark.negativeBackground
        )
    }
}