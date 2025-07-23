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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

@Composable
fun SandView(
    modifier: Modifier = Modifier,
    sandColor: Color = Color.Yellow,
    cellSize: Float = 6f,
    hasOwnBackground: Boolean = true,
    sandGenerationAmount: Int = 8, // Higher value for performance testing
    showPerformanceOverlay: Boolean, // Easy toggle for performance display
    isPaused: Boolean,
    resetTrigger: Int,
    currentTheme: Theme = Theme.LIGHT
) {
    var sandGrid by remember { mutableStateOf<SandGrid?>(null) }
    
    // Reset grid when resetTrigger changes
    LaunchedEffect(resetTrigger) {
        sandGrid?.reset()
    }
    var sandSourceX by remember { mutableStateOf(0f) }
    var isAddingSand by remember { mutableStateOf(false) }
    var frame by remember { mutableStateOf(0L) }
    
    // Clear sand adding state when paused and handle pause/resume
    LaunchedEffect(isPaused) {
        if (isPaused) {
            isAddingSand = false
            sandGrid?.onPause()
        } else {
            sandGrid?.onResume()
        }
    }

    // Track actual frame timing for real FPS calculation
    var lastFrameTime by remember { mutableStateOf(0L) }
    var frameCount by remember { mutableStateOf(0) }
    var actualFps by remember { mutableStateOf(0) }
    var totalDrawTime by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (hasOwnBackground) createBackgroundModifier(currentTheme) else Modifier)
            .clipToBounds()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isPaused) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (!isPaused) {
                                sandSourceX = offset.x
                                isAddingSand = true
                            }
                        },
                        onDragEnd = { 
                            isAddingSand = false 
                        }
                    ) { change, _ ->
                        if (!isPaused) {
                            sandSourceX = change.position.x
                            isAddingSand = true
                        }
                    }
                }
        ) {
            val frameStartTime = System.currentTimeMillis()
            val drawStartTime = System.nanoTime()

            // Initialize grid if needed using actual screen dimensions
            val gridDimensions = calculateGridDimensions(size, cellSize)
            sandGrid = initializeGridIfNeeded(sandGrid, gridDimensions)
            
            sandGrid?.let { grid ->
                // 2. Sand particle addition timing
                val sandAddTime = if (isAddingSand) {
                    val sandAddStartTime = System.nanoTime()
                    addSandParticles(
                        grid,
                        sandSourceX,
                        cellSize,
                        sandColor,
                        frame,
                        gridDimensions,
                        sandGenerationAmount
                    )
                    (System.nanoTime() - sandAddStartTime) / 1_000_000.0
                } else 0.0

                // 3. Draw grid timing (this will provide internal breakdown)
                val drawGridStartTime = System.nanoTime()
                drawSandGrid(
                    grid = grid,
                    cellSize = cellSize,
                    frame = frame,
                    showPerformanceOverlay = showPerformanceOverlay,
                    totalDrawTime = totalDrawTime,
                    actualFps = actualFps
                )
                val drawGridTime = (System.nanoTime() - drawGridStartTime) / 1_000_000.0

                val drawTime = ((System.nanoTime() - drawStartTime) / 1_000_000.0).roundToInt()

                // Calculate actual FPS including both physics and drawing
                if (lastFrameTime > 0) {
                    val totalFrameTime = frameStartTime - lastFrameTime
                    frameCount++
                    if (frameCount % 10 == 0) { // Update every 10 frames
                        actualFps = if (totalFrameTime > 0) (1000 / totalFrameTime).toInt() else 0
                        totalDrawTime = drawTime
                    }
                }
                lastFrameTime = frameStartTime

                val overallDrawTime = drawTime + grid.getPerformanceData().updateTime
                val fps = if (overallDrawTime > 0) {
                    1000 / (drawTime + grid.getPerformanceData().updateTime)
                } else {
                    0
                }

                timber.log.Timber.tag("DrawPerf").d(
                    "TOTAL: ${drawTime}ms + ${grid.getPerformanceData().updateTime}ms = ${drawTime + grid.getPerformanceData().updateTime}ms | FPS: $fps | AddSand: ${sandAddTime}ms | DrawGrid: ${drawGridTime}ms"
                )
            }
        }
    }


    SandAnimationLoop(isPaused = isPaused) { frameTime ->
        frame = frameTime
        sandGrid?.update(frameTime)
    }
}

@Composable
private fun SandAnimationLoop(isPaused: Boolean, onFrame: (Long) -> Unit) {
    LaunchedEffect(isPaused) {
        while (true) {
            withFrameMillis { frameTime ->
                if (!isPaused) {
                    onFrame(frameTime)
                }
            }
        }
    }
}

private fun createBackgroundModifier(currentTheme: Theme): Modifier {
    val colorScheme = getColorScheme(currentTheme)
    return Modifier.background(
        Brush.verticalGradient(
            colors = colorScheme.backgroundColors,
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    )
}

private fun calculateGridDimensions(
    size: androidx.compose.ui.geometry.Size,
    cellSize: Float
): Pair<Int, Int> {
    return Pair(
        (size.width / cellSize).roundToInt(),
        (size.height / cellSize).roundToInt()
    )
}

private fun initializeGridIfNeeded(
    currentGrid: SandGrid?,
    dimensions: Pair<Int, Int>
): SandGrid {
    val (width, height) = dimensions
    return if (currentGrid == null || currentGrid.getWidth() != width || currentGrid.getHeight() != height) {
        SandGrid(width = width, height = height)
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

private fun DrawScope.drawSandGrid(
    grid: SandGrid,
    cellSize: Float,
    @Suppress("UNUSED_PARAMETER") frame: Long,
    showPerformanceOverlay: Boolean,
    totalDrawTime: Int,
    actualFps: Int
) {
    val drawCellsStartTime = System.nanoTime()

    // Performance counters for detailed breakdown
    var sandParticlesDrawn = 0
    var settledParticlesDrawn = 0
    var obstaclesDrawn = 0
    var colorCalculationTime = 0.0
    var drawOperationTime = 0.0

    // 1. Get all cells timing - this might be expensive
    val getAllCellsStartTime = System.nanoTime()
    val allCells = grid.getAllCells()
    val getAllCellsTime = (System.nanoTime() - getAllCellsStartTime) / 1_000_000.0

    // 2. Break down the cell iteration
    val cellIterationStartTime = System.nanoTime()
    var sandDrawTime = 0.0
    var obstacleDrawTime = 0.0

    val cellIterationTime = measureTimeMillis {
        allCells.forEach { (x, y, cell) ->
            when (cell.type) {
                CellType.SAND -> {
                    val particle = cell.particle
                    if (particle != null) {
                        val colorStartTime = System.nanoTime()
                        colorCalculationTime += (System.nanoTime() - colorStartTime) / 1_000_000.0

                        if (particle.isSettled) {
                            settledParticlesDrawn++
                        } else {
                            sandParticlesDrawn++
                        }

                        val drawOpStartTime = System.nanoTime()
                        drawRect(
                            color = particle.displayColor,
                            topLeft = Offset(
                                x = x * cellSize,
                                y = y * cellSize
                            ),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                        )
                        val drawTime = (System.nanoTime() - drawOpStartTime) / 1_000_000.0
                        drawOperationTime += drawTime
                        sandDrawTime += drawTime
                    }
                }

                CellType.OBSTACLE -> {
                    obstaclesDrawn++
                    val drawOpStartTime = System.nanoTime()
                    drawRoundRect(
                        color = Color(0x80CCCCCC), // Light gray with transparency
                        topLeft = Offset(
                            x = x * cellSize,
                            y = y * cellSize
                        ),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
                    )
                    drawOperationTime += (System.nanoTime() - drawOpStartTime) / 1_000_000.0
                }

                CellType.SLIDING_OBSTACLE -> {
                    // Skip - sliding obstacles are drawn separately as single rectangles
                }

                CellType.EMPTY -> {
                    // Do nothing for empty cells
                }
            }
        }
    } / 1000.0 // Convert to ms

    // Draw sliding obstacles as single rectangles (much more efficient)
    val slidingObstacleTime = measureNanoTime {
        drawSlidingObstacles(grid, cellSize)
    } / 1_000_000.0 // Convert to ms

    val overlayTime = if (showPerformanceOverlay) {
        val overlayStartTime = System.nanoTime()
        drawPerformanceOverlay(grid = grid, totalDrawTime = totalDrawTime, actualFps = actualFps)
        (System.nanoTime() - overlayStartTime) / 1_000_000.0
    } else 0.0

    val totalCellDrawTime = (System.nanoTime() - drawCellsStartTime) / 1_000_000.0

    timber.log.Timber.tag("DrawDetail").d(
        "CELLS: Total=${allCells.size} | Sand=${sandParticlesDrawn} | Settled=${settledParticlesDrawn} | Obstacles=${obstaclesDrawn}"
    )

    timber.log.Timber.tag("DrawDetail").d(
        "BREAKDOWN: GetCells=${getAllCellsTime}ms | Iteration=${cellIterationTime}ms | SandDraw=${sandDrawTime}ms | SlidingObs=${slidingObstacleTime}ms | Overlay=${overlayTime}ms"
    )

    timber.log.Timber.tag("DrawDetail").d(
        "TIMING: Total=${totalCellDrawTime}ms | Color=${colorCalculationTime}ms | DrawOps=${drawOperationTime}ms"
    )
}

private fun DrawScope.drawSlidingObstacles(grid: SandGrid, cellSize: Float) {
    // Get sliding obstacles from grid and draw each as a single rectangle
    val slidingObstacles = grid.getSlidingObstacles()

    for (obstacle in slidingObstacles) {
        val size = obstacle.size.toFloat() * cellSize
        val x = obstacle.x * cellSize - size / 2
        val y = obstacle.y * cellSize - size / 2

        drawRoundRect(
            color = obstacle.color,
            topLeft = Offset(x, y),
            size = androidx.compose.ui.geometry.Size(size, size),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawPerformanceOverlay(grid: SandGrid, totalDrawTime: Int, actualFps: Int) {
    val perfData = grid.getPerformanceData()
    val textColor = Color.White
    val backgroundColor = Color.Black.copy(alpha = 0.6f)
    val textSizePx = 22f
    val padding = 16f
    val lineHeight = 26f

    // Background for text - make it taller for additional FPS line
    drawRect(
        color = backgroundColor,
        topLeft = Offset(padding, size.height - 220f),
        size = androidx.compose.ui.geometry.Size(600f, 180f)
    )

    // Use native Canvas for text drawing
    val canvas = drawContext.canvas.nativeCanvas
    val paint = android.graphics.Paint().apply {
        color = textColor.toArgb()
        textSize = textSizePx
        isAntiAlias = true
        typeface = android.graphics.Typeface.MONOSPACE
    }

    var yPos = size.height - 180f
    canvas.drawText("Real FPS: $actualFps", padding + 8f, yPos, paint)
    yPos += lineHeight
    canvas.drawText(
        "Update + Draw = Total: ${perfData.updateTime}ms + ${totalDrawTime}ms = ${perfData.updateTime + totalDrawTime}ms",
        padding + 8f,
        yPos,
        paint
    )
    yPos += lineHeight
    canvas.drawText("Avg: ${perfData.avgUpdateTime}ms", padding + 8f, yPos, paint)
    yPos += lineHeight
    canvas.drawText("Moving: ${perfData.movingParticles}", padding + 8f, yPos, paint)
    yPos += lineHeight
    canvas.drawText("Settled: ${perfData.settledParticles}", padding + 8f, yPos, paint)
}