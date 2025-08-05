package hu.adamfejes.zenmotes.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import zenmotescmp.composeapp.generated.resources.background_night
import zenmotescmp.composeapp.generated.resources.background_daylight
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import hu.adamfejes.zenmotes.logic.CellType
import hu.adamfejes.zenmotes.logic.ColorType
import hu.adamfejes.zenmotes.ui.theme.ColorScheme
import hu.adamfejes.zenmotes.ui.theme.Theme
import hu.adamfejes.zenmotes.ui.theme.toColorScheme
import hu.adamfejes.zenmotes.utils.Logger
import hu.adamfejes.zenmotes.utils.TimeUtils
import org.jetbrains.compose.resources.imageResource
import zenmotescmp.composeapp.generated.resources.Res
import zenmotescmp.composeapp.generated.resources.tower
import zenmotescmp.composeapp.generated.resources.wider_tower
import kotlin.math.roundToInt
import kotlin.time.measureTime

@Composable
fun SandView(
    modifier: Modifier = Modifier,
    sandColorType: ColorType = ColorType.OBSTACLE_COLOR_1,
    cellSize: Float = 6f,
    sandGenerationAmount: Int = 8, // Higher value for performance testing
    showPerformanceOverlay: Boolean, // Easy toggle for performance display
    isPaused: Boolean,
    resetTrigger: Int
) {
    val colorScheme = LocalTheme.current.toColorScheme()

    var sandGrid by remember { mutableStateOf<SandGrid?>(null) }

    // Reset grid when resetTrigger changes
    LaunchedEffect(resetTrigger) {
        sandGrid?.reset()
    }
    var sandSourceX by remember { mutableFloatStateOf(0f) }
    var isAddingSand by remember { mutableStateOf(false) }
    var frame by remember { mutableLongStateOf(0L) }

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
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    var frameCount by remember { mutableIntStateOf(0) }
    var actualFps by remember { mutableIntStateOf(0) }
    var totalDrawTime by remember { mutableIntStateOf(0) }

    // Load sample image for obstacles and testing
    val images = listOf(
        imageResource(Res.drawable.tower),
        imageResource(Res.drawable.wider_tower)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        BackgroundImage()

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
                .pointerInput(isPaused) {
                    detectTapGestures(onPress = { offset ->
                        if (!isPaused) {
                            sandSourceX = offset.x
                            isAddingSand = true

                            awaitRelease()
                            isAddingSand = false
                        }
                    })
                }
        ) {
            val frameStartTime = TimeUtils.currentTimeMillis()
            val drawStartTime = TimeUtils.nanoTime()

            // Initialize grid if needed using actual screen dimensions
            val gridDimensions = calculateGridDimensions(size, cellSize)
            sandGrid = initializeGridIfNeeded(sandGrid, gridDimensions, images)

            sandGrid?.let { grid ->
                // 2. Sand particle addition timing
                val sandAddTime = if (isAddingSand) {
                    val sandAddStartTime = TimeUtils.nanoTime()
                    addSandParticles(
                        grid,
                        sandSourceX,
                        cellSize,
                        sandColorType,
                        frame,
                        gridDimensions,
                        sandGenerationAmount
                    )
                    (TimeUtils.nanoTime() - sandAddStartTime) / 1_000_000.0
                } else 0.0

                // 3. Draw grid timing (this will provide internal breakdown)
                val drawGridStartTime = TimeUtils.nanoTime()
                drawSandGrid(
                    grid = grid,
                    cellSize = cellSize,
                    frame = frame,
                    showPerformanceOverlay = showPerformanceOverlay,
                    totalDrawTime = totalDrawTime,
                    actualFps = actualFps,
                    colorScheme = colorScheme
                )
                val drawGridTime = (TimeUtils.nanoTime() - drawGridStartTime) / 1_000_000.0

                val drawTime = ((TimeUtils.nanoTime() - drawStartTime) / 1_000_000.0).roundToInt()

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

                Logger.d(
                    "DrawPerf",
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

@Composable
private fun BackgroundImage() {
    val currentTheme = LocalTheme.current
    val colorScheme = currentTheme.toColorScheme()

    val backgroundImageRes = if (currentTheme == Theme.DARK) {
        Res.drawable.background_night
    } else {
        Res.drawable.background_daylight
    }

    Image(
        painter = painterResource(backgroundImageRes),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .alpha(0.8f),
        contentScale = ContentScale.Crop
    )
}

private fun calculateGridDimensions(
    size: Size,
    cellSize: Float
): Pair<Int, Int> {
    return Pair(
        (size.width / cellSize).roundToInt(),
        (size.height / cellSize).roundToInt()
    )
}

private fun initializeGridIfNeeded(
    currentGrid: SandGrid?,
    dimensions: Pair<Int, Int>,
    images: List<ImageBitmap>
): SandGrid {
    val (width, height) = dimensions
    return if (currentGrid == null || currentGrid.getWidth() != width || currentGrid.getHeight() != height) {
        val newGrid = SandGrid(width = width, height = height)
        newGrid.setImages(images)
        newGrid
    } else {
        // Update bitmap if it's changed
        currentGrid.setImages(images)
        currentGrid
    }
}

private fun addSandParticles(
    grid: SandGrid,
    sourceX: Float,
    cellSize: Float,
    colorType: ColorType,
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
            grid.addSand(spreadX, spreadY, colorType, frame)
        }
    }

    // Always add one at the center of the top
    grid.addSand(centerX, 0, colorType, frame)
}

private fun DrawScope.drawSandGrid(
    grid: SandGrid,
    cellSize: Float,
    @Suppress("UNUSED_PARAMETER") frame: Long,
    showPerformanceOverlay: Boolean,
    totalDrawTime: Int,
    actualFps: Int,
    colorScheme: ColorScheme
) {
    val drawCellsStartTime = TimeUtils.nanoTime()

    // Performance counters for detailed breakdown
    var sandParticlesDrawn = 0
    var settledParticlesDrawn = 0
    var obstaclesDrawn = 0
    var colorCalculationTime = 0.0
    var drawOperationTime = 0.0

    // 1. Get all cells timing - this might be expensive
    val getAllCellsStartTime = TimeUtils.nanoTime()
    val allCells = grid.getAllCells()
    val getAllCellsTime = (TimeUtils.nanoTime() - getAllCellsStartTime) / 1_000_000.0

    // 2. Break down the cell iteration
    val cellIterationStartTime = TimeUtils.nanoTime()
    var sandDrawTime = 0.0

    val cellIterationTime = measureTime {
        allCells.forEach { (x, y, cell) ->
            when (cell.type) {
                CellType.SAND -> {
                    val particle = cell.particle
                    if (particle != null) {
                        val colorStartTime = TimeUtils.nanoTime()
                        colorCalculationTime += (TimeUtils.nanoTime() - colorStartTime) / 1_000_000.0

                        if (particle.isSettled) {
                            settledParticlesDrawn++
                        } else {
                            sandParticlesDrawn++
                        }

                        val drawOpStartTime = TimeUtils.nanoTime()
                        val baseColor = mapObstacleColorToTheme(particle.colorType, colorScheme)
                        val displayColor = baseColor.copy(
                            red = (baseColor.red * particle.noiseVariation).coerceIn(0f, 1f),
                            green = (baseColor.green * particle.noiseVariation).coerceIn(0f, 1f),
                            blue = (baseColor.blue * particle.noiseVariation).coerceIn(0f, 1f)
                        )
                        drawRect(
                            color = displayColor,
                            topLeft = Offset(
                                x = x * cellSize,
                                y = y * cellSize
                            ),
                            size = Size(cellSize, cellSize)
                        )
                        val drawTime = (TimeUtils.nanoTime() - drawOpStartTime) / 1_000_000.0
                        drawOperationTime += drawTime
                        sandDrawTime += drawTime
                    }
                }

                CellType.OBSTACLE -> {
                    obstaclesDrawn++
                    val drawOpStartTime = TimeUtils.nanoTime()
                    drawRoundRect(
                        color = Color(0x80CCCCCC), // Light gray with transparency
                        topLeft = Offset(
                            x = x * cellSize,
                            y = y * cellSize
                        ),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(1f, 1f)
                    )
                    drawOperationTime += (TimeUtils.nanoTime() - drawOpStartTime) / 1_000_000.0
                }

                CellType.SLIDING_OBSTACLE -> {
                    // Skip - sliding obstacles are drawn separately as single rectangles
                }

                CellType.EMPTY -> {
                    // Do nothing for empty cells
                }
            }
        }
    }.inWholeMilliseconds.toDouble() / 1000.0 // Convert to ms

    // Draw sliding obstacles as single rectangles (much more efficient)
    val slidingObstacleTime = measureTime {
        drawSlidingObstacles(grid, cellSize, colorScheme)
    }.inWholeNanoseconds.toDouble() / 1_000_000.0 // Convert to ms

    val overlayTime = if (showPerformanceOverlay) {
        val overlayStartTime = TimeUtils.nanoTime()
        drawPerformanceOverlay(grid = grid, totalDrawTime = totalDrawTime, actualFps = actualFps)
        (TimeUtils.nanoTime() - overlayStartTime) / 1_000_000.0
    } else 0.0

    val totalCellDrawTime = (TimeUtils.nanoTime() - drawCellsStartTime) / 1_000_000.0

    Logger.d(
        "DrawDetail",
        "CELLS: Total=${allCells.size} | Sand=${sandParticlesDrawn} | Settled=${settledParticlesDrawn} | Obstacles=${obstaclesDrawn}"
    )
    Logger.d(
        "DrawDetail",
        "BREAKDOWN: GetCells=${getAllCellsTime}ms | Iteration=${cellIterationTime}ms | SandDraw=${sandDrawTime}ms | SlidingObs=${slidingObstacleTime}ms | Overlay=${overlayTime}ms"
    )
    Logger.d(
        "DrawDetail",
        "TIMING: Total=${totalCellDrawTime}ms | Color=${colorCalculationTime}ms | DrawOps=${drawOperationTime}ms"
    )
}

private fun DrawScope.drawSlidingObstacles(
    grid: SandGrid,
    cellSize: Float,
    colorScheme: ColorScheme
) {
    // Get sliding obstacles from grid and draw each as a single rectangle
    val slidingObstacles = grid.getSlidingObstacles()

    for (obstacle in slidingObstacles) {
        val width = obstacle.width * cellSize.toInt()
        val height = obstacle.height * cellSize.toInt()
        val x = obstacle.x * cellSize - width / 2
        val y = obstacle.y * cellSize - height / 2

        drawImage(
            image = obstacle.bitmapShape,
            dstOffset = IntOffset(x.toInt(), y.toInt()),
            dstSize = IntSize(width, height),
            filterQuality = FilterQuality.None, // Pixelated scaling
            colorFilter = ColorFilter.tint(
                color = mapObstacleColorToTheme(obstacle.colorType, colorScheme),
                blendMode = BlendMode.Modulate
            ),
        )
    }
}

fun mapObstacleColorToTheme(colorType: ColorType, colorScheme: ColorScheme): Color {
    return when (colorType) {
        ColorType.OBSTACLE_COLOR_1 -> colorScheme.obstacleColors[0]
        ColorType.OBSTACLE_COLOR_2 -> colorScheme.obstacleColors[1]
        ColorType.OBSTACLE_COLOR_3 -> colorScheme.obstacleColors[2]
        ColorType.OBSTACLE_COLOR_4 -> colorScheme.obstacleColors[3]
        // ColorType.OBSTACLE_COLOR_5 -> colorScheme.obstacleColors[4]
        // ColorType.OBSTACLE_COLOR_6 -> colorScheme.obstacleColors[5]
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
        size = Size(600f, 180f)
    )

    // Use platform-specific text drawing
    val lines = listOf(
        "Real FPS: $actualFps",
        "Update + Draw = Total: ${perfData.updateTime}ms + ${totalDrawTime}ms = ${perfData.updateTime + totalDrawTime}ms",
        "Avg: ${perfData.avgUpdateTime}ms",
        "Moving: ${perfData.movingParticles}",
        "Settled: ${perfData.settledParticles}"
    )

    drawTextLines(
        lines = lines,
        color = textColor,
        textSize = textSizePx,
        startOffset = Offset(padding + 8f, size.height - 180f),
        lineHeight = lineHeight
    )
}