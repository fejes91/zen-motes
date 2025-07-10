package hu.adamfejes.zenmotes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@Composable
fun SandView(
    modifier: Modifier = Modifier,
    sandColor: Color = Color.Yellow,
    cellSize: Float = 6f,
    hasOwnBackground: Boolean = true
) {
    val density = LocalDensity.current
    var sandGrid by remember { mutableStateOf<SandGrid?>(null) }
    var sandSourceX by remember { mutableStateOf(0f) }
    var isAddingSand by remember { mutableStateOf(false) }
    var frame by remember { mutableStateOf(0L) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (hasOwnBackground) {
                    Modifier.background(
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
                } else {
                    Modifier
                }
            )
            .clipToBounds()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            sandSourceX = offset.x
                            isAddingSand = true
                        },
                        onDragEnd = {
                            isAddingSand = false
                        }
                    ) { change, _ ->
                        sandSourceX = change.position.x
                    }
                }
        ) {
            val width = (size.width / cellSize).roundToInt()
            val height = (size.height / cellSize).roundToInt()
            
            // Initialize grid if needed
            if (sandGrid == null || sandGrid!!.getWidth() != width || sandGrid!!.getHeight() != height) {
                sandGrid = SandGrid(width, height)
            }
            
            sandGrid?.let { grid ->
                // Add sand at source position with sprinkling effect
                if (isAddingSand) {
                    val centerX = (sandSourceX / cellSize).roundToInt().coerceIn(0, width - 1)
                    
                    // Generate multiple sand particles in a sprinkle pattern
                    repeat(3) {
                        val spreadX = centerX + (-2..2).random()
                        val spreadY = (0..1).random()
                        
                        if (spreadX in 0 until width && spreadY in 0 until height) {
                            grid.addSand(spreadX, spreadY, sandColor, frame)
                        }
                    }
                    
                    // Always add one at the center
                    grid.addSand(centerX, 0, sandColor, frame)
                }
                
                // Render sand particles (frame triggers recomposition)
                drawSandGrid(grid, cellSize, frame)
            }
        }
    }
    
    // Animation loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frameTime ->
                frame = frameTime
                sandGrid?.update(frameTime)
            }
        }
    }
}

private fun DrawScope.drawSandGrid(grid: SandGrid, cellSize: Float, frame: Long) {
    grid.getAllCells().forEach { (x, y, cell) ->
        when (cell.type) {
            CellType.SAND -> {
                val particle = cell.particle
                if (particle != null) {
                    // Apply noise variation to make some particles darker
                    val noisyColor = particle.color.copy(
                        red = (particle.color.red * particle.noiseVariation).coerceIn(0f, 1f),
                        green = (particle.color.green * particle.noiseVariation).coerceIn(0f, 1f),
                        blue = (particle.color.blue * particle.noiseVariation).coerceIn(0f, 1f)
                    )
                    
                    drawRect(
                        color = noisyColor,
                        topLeft = Offset(
                            x = x * cellSize,
                            y = y * cellSize
                        ),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
            CellType.OBSTACLE -> {
                drawRoundRect(
                    color = Color(0x80CCCCCC), // Light gray with transparency
                    topLeft = Offset(
                        x = x * cellSize,
                        y = y * cellSize
                    ),
                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
                )
            }
            CellType.ROTATING_OBSTACLE -> {
                drawRoundRect(
                    color = Color(0x80CCCCCC), // Same color as regular obstacles
                    topLeft = Offset(
                        x = x * cellSize,
                        y = y * cellSize
                    ),
                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
                )
            }
            CellType.EMPTY -> {
                // Do nothing for empty cells
            }
        }
    }
}