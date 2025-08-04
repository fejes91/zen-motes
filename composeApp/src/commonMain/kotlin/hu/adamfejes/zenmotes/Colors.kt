package hu.adamfejes.zenmotes

import androidx.compose.ui.graphics.Color

object ZenColors {
    
    object Light {
        internal val background = Color.White

        // Sand colors - soft pastels
        internal val sandColors = listOf(
            Color(0xFFFF9BB5), // Saturated Pink
            Color(0xFF9BCFFF), // Saturated Blue
            Color(0xFF9BFF9B), // Saturated Green
            Color(0xFFFFE066), // Saturated Yellow
            Color(0xFFD99BFF), // Saturated Purple
            Color(0xFFFF9B66)  // Saturated Orange
        )
        
        // Obstacle colors - matching pastels with slight variations
        internal val obstacleColors = listOf(
            Color(0xFFFF85A3), // Slightly deeper pink
            Color(0xFF85BFFF), // Slightly deeper blue
            Color(0xFF85FF85), // Slightly deeper green
            Color(0xFFFFD033), // Slightly deeper yellow
            Color(0xFFCC85FF), // Slightly deeper purple
            Color(0xFFFF8533)  // Slightly deeper orange
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
    val themeSwitchBackground: Color,
    val themeSwitchText: Color
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
            themeSwitchBackground = ZenColors.Light.themeSwitchBackground,
            themeSwitchText = ZenColors.Light.themeSwitchText
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
            themeSwitchBackground = ZenColors.Dark.themeSwitchBackground,
            themeSwitchText = ZenColors.Dark.themeSwitchText
        )
    }
}