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
    hasOwnBackground: Boolean = true,
    sandGenerationAmount: Int = 8, // Higher value for performance testing
    allowSandBuildup: Boolean // Control whether sand builds up or falls through
) {
    var sandGrid by remember { mutableStateOf<SandGrid?>(null) }
    var sandSourceX by remember { mutableStateOf(0f) }
    var isAddingSand by remember { mutableStateOf(false) }
    var frame by remember { mutableStateOf(0L) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (hasOwnBackground) createBackgroundModifier() else Modifier)
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
                        onDragEnd = { isAddingSand = false }
                    ) { change, _ ->
                        sandSourceX = change.position.x
                        isAddingSand = true
                    }
                }
        ) {
            val gridDimensions = calculateGridDimensions(size, cellSize)
            sandGrid = initializeGridIfNeeded(sandGrid, gridDimensions, allowSandBuildup)
            
            sandGrid?.let { grid ->
                if (isAddingSand) {
                    addSandParticles(grid, sandSourceX, cellSize, sandColor, frame, gridDimensions, sandGenerationAmount)
                }
                drawSandGrid(grid, cellSize, frame)
            }
        }
    }
    
    
    SandAnimationLoop { frameTime ->
        frame = frameTime
        sandGrid?.update(frameTime)
    }
}

@Composable
private fun SandAnimationLoop(onFrame: (Long) -> Unit) {
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis(onFrame)
        }
    }
}

private fun createBackgroundModifier(): Modifier {
    return Modifier.background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFEBF0), Color(0xFFEBF0FF), Color(0xFFEBFFEB),
                Color(0xFFFFF8EB), Color(0xFFF0EBFF), Color(0xFFFFEBEB)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    )
}

private fun calculateGridDimensions(size: androidx.compose.ui.geometry.Size, cellSize: Float): Pair<Int, Int> {
    return Pair(
        (size.width / cellSize).roundToInt(),
        (size.height / cellSize).roundToInt()
    )
}

private fun initializeGridIfNeeded(
    currentGrid: SandGrid?,
    dimensions: Pair<Int, Int>,
    allowSandBuildup: Boolean
): SandGrid {
    val (width, height) = dimensions
    return if (currentGrid == null || currentGrid.getWidth() != width || currentGrid.getHeight() != height) {
        SandGrid(width = width, height = height, allowSandBuildup = allowSandBuildup)
    } else {
        currentGrid
    }
}

private fun addSandParticles(
    grid: SandGrid,
    sourceX: Float,
    cellSize: Float,
    color: Color,
    frame: Long,
    dimensions: Pair<Int, Int>,
    sandGenerationAmount: Int
) {
    val (width, _) = dimensions
    val centerX = (sourceX / cellSize).roundToInt().coerceIn(0, width - 1)
    
    // Generate multiple sand particles in a sprinkle pattern at the top of the screen
    repeat(sandGenerationAmount) {
        val spreadX = centerX + (-2..2).random()
        val spreadY = 0 // Always spawn at the top of the screen
        
        if (spreadX in 0 until width) {
            grid.addSand(spreadX, spreadY, color, frame)
        }
    }
    
    // Always add one at the center of the top
    grid.addSand(centerX, 0, color, frame)
}

private fun DrawScope.drawSandGrid(grid: SandGrid, cellSize: Float, @Suppress("UNUSED_PARAMETER") frame: Long) {
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
            CellType.SLIDING_OBSTACLE -> {
                val obstacleColor = cell.slidingObstacle?.color ?: Color(0xFFFF6B6B)
                drawRoundRect(
                    color = obstacleColor, // Use the sliding obstacle's color
                    topLeft = Offset(
                        x = x * cellSize,
                        y = y * cellSize
                    ),
                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }
            CellType.EMPTY -> {
                // Do nothing for empty cells
            }
        }
    }
}