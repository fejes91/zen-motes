package hu.adamfejes.zenmotes.ui.theme

import androidx.compose.ui.graphics.Color

object ZenColors {
    
    object Light {
        internal val background = Color.White
        
        // Obstacle colors - more saturated with slight variations
        internal val obstacleColors = listOf(
            Color(0xFF9760EA), // purple
            Color(0xFF4EEB60), // green
            Color(0xFFEBE660), // yellow
            Color(0xFFEB8560), // red
        )
        
        
        // UI element colors
        internal val paletteBorder = Color(0xFF333333) // Dark gray for palette border
        internal val pauseButtonBackground = Color(0xFFF5F5F5) // Light gray with pastel feel
        internal val pauseButtonIcon = Color(0xFF6B6B6B) // Soft gray for icons
        internal val pauseOverlayBackground = Color(0x80FFFFFF) // Semi-transparent white
        internal val pausedTitleText = Color(0xFFE0E0E0) // Darker gray for title visibility
        
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

        internal val positiveText = Color(0xFF4CAF50) // Soft green for positive text

        internal val negativeText = Color(0xFFE57373) // Soft pink/red for negative text
    }
    
    object Dark {
        internal val background = Color.Black
        
        // Obstacle colors - matching darker pastels
        internal val obstacleColors = listOf(
            Color(0xFF8F93EA), // blue
            Color(0xFF8FEBAD), // green
            Color(0xFFEBD57C), // yellow
            Color(0xFFEB8F8D), // red
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

        internal val positiveText = Color(0xFF6BE66B) // Muted green for positive text

        internal val negativeText = Color(0xFFD1668A) // Muted pink for negative text
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
    val textColorOnBackground: Color,
    val positiveBackground: Color,
    val negativeBackground: Color,
    val positiveText: Color,
    val negativeText: Color
)

fun getColorScheme(theme: Theme): ColorScheme {
    return when (theme) {
        Theme.LIGHT -> ColorScheme(
            background = ZenColors.Light.background,
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
            textColorOnBackground = ZenColors.Light.themeSwitchText,
            positiveBackground = ZenColors.Light.positiveBackground,
            negativeBackground = ZenColors.Light.negativeBackground,
            positiveText = ZenColors.Light.positiveText,
            negativeText = ZenColors.Light.negativeText
        )
        Theme.DARK -> ColorScheme(
            background = ZenColors.Dark.background,
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
            textColorOnBackground = ZenColors.Dark.themeSwitchText,
            positiveBackground = ZenColors.Dark.positiveBackground,
            negativeBackground = ZenColors.Dark.negativeBackground,
            positiveText = ZenColors.Dark.positiveText,
            negativeText = ZenColors.Dark.negativeText
        )
    }
}