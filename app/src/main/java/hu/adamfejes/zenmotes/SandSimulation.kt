package hu.adamfejes.zenmotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SandSimulation(
    modifier: Modifier = Modifier
) {
    var selectedColor by remember { mutableStateOf(Color(0xFFFF9BB5)) }
    
    val sandColors = listOf(
        Color(0xFFFF9BB5), // Saturated Pink
        Color(0xFF9BCFFF), // Saturated Blue
        Color(0xFF9BFF9B), // Saturated Green
        Color(0xFFFFE066), // Saturated Yellow
        Color(0xFFD99BFF), // Saturated Purple
        Color(0xFFFF9B66)  // Saturated Orange
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Sand simulation view - full edge-to-edge behind everything
        SandView(
            modifier = Modifier.fillMaxSize(),
            sandColor = selectedColor,
            hasOwnBackground = true,
            sandGenerationAmount = 60,
            showPerformanceOverlay = true // Toggle performance overlay for testing
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
                // Reset button - circular and same size as color buttons
                ResetButton(
                    onClick = { /* Reset functionality removed since destroyable obstacles are gone */ }
                )
            }
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
private fun ResetButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "↻",
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}