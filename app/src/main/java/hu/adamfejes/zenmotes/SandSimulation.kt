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
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEBF0), // Darker light pink
                        Color(0xFFEBF0FF), // Darker light blue
                        Color(0xFFEBFFEB), // Darker light green
                        Color(0xFFFFF8EB), // Darker light yellow
                        Color(0xFFF0EBFF), // Darker light purple
                        Color(0xFFFFEBEB)  // Darker light coral
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Color picker - edge to edge with system bar padding
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
            }
            
            // Sand simulation view
            SandView(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .navigationBarsPadding(),
                sandColor = selectedColor,
                hasOwnBackground = false,
                sandGenerationAmount = 8
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