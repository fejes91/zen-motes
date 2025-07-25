package hu.adamfejes.zenmotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun SandSimulation(
    modifier: Modifier = Modifier
) {
    // TODO store it in shared preferences
    var currentTheme by remember { mutableStateOf(Theme.LIGHT) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF9BB5)) }
    var isPaused by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableIntStateOf(0) }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> isPaused = true
                Lifecycle.Event.ON_RESUME -> {}
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Use current theme colors
    // TODO distribute selected colorscheme via local composition
    val colorScheme = getColorScheme(currentTheme)
    val sandColors = colorScheme.sandColors
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Everything that should be blurred when paused  
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isPaused) Modifier.blur(20.dp) else Modifier)
        ) {
            // Sand simulation view - full edge-to-edge behind everything
            SandView(
                modifier = Modifier.fillMaxSize(),
                sandColor = selectedColor,
                hasOwnBackground = true,
                sandGenerationAmount = 60,
                showPerformanceOverlay = true, // Toggle performance overlay for testing
                isPaused = isPaused,
                resetTrigger = resetTrigger,
                currentTheme = currentTheme
            )
            
            // Top UI overlay - color picker and reset button
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(sandColors) { color ->
                    ColorButton(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { selectedColor = color }
                    )
                }
                
                item {
                    // Pause button - circular and same size as color buttons
                    PauseButton(
                        isPaused = isPaused,
                        onClick = { isPaused = !isPaused },
                        currentTheme = currentTheme
                    )
                }
            }
        }
        
        // Pause overlay with blur and menu
        if (isPaused) {
            PauseOverlay(
                onResume = { isPaused = false },
                onRestart = { 
                    resetTrigger++
                    isPaused = false
                },
                currentTheme = currentTheme,
                onThemeChange = { newTheme -> currentTheme = newTheme }
            )
        }
    }
}

@Composable
private fun ColorButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.background(Color.White.copy(alpha = 0.3f))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {}
    }
}

@Composable
private fun PauseButton(
    isPaused: Boolean,
    onClick: () -> Unit,
    currentTheme: Theme
) {
    val colorScheme = getColorScheme(currentTheme)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(colorScheme.pauseButtonBackground),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.pauseButtonIcon
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (isPaused) "▶" else "⏸",
                fontSize = 16.sp,
                color = colorScheme.pauseButtonIcon
            )
        }
    }
}