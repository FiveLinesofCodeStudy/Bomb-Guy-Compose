@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay


const val TITLE_SIZE = 100
const val FPS = 30
const val SLEEP = 1000 / FPS
const val TPS = 2
const val DELAY = FPS / TPS

val numberMap = arrayOf(
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(1, 0, 0, 2, 2, 2, 2, 2, 1),
    arrayOf(1, 0, 1, 2, 1, 2, 1, 2, 1),
    arrayOf(1, 2, 2, 2, 2, 2, 2, 2, 1),
    arrayOf(1, 2, 1, 2, 1, 2, 1, 2, 1),
    arrayOf(1, 2, 2, 2, 2, 0, 0, 0, 1),
    arrayOf(1, 2, 1, 2, 1, 0, 1, 0, 1),
    arrayOf(1, 2, 2, 2, 2, 0, 0, 10, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
)

val map = MutableList(numberMap.size) { y -> MutableList(numberMap[0].size) { x -> Tile.transform(numberMap[y][x]) } }

val playerx = mutableStateOf(1)
val playery = mutableStateOf(1)
val delayState = mutableStateOf(0)
val bombsState = mutableStateOf(1)

fun placeBomb() {
    if (bombsState.value > 0) {
        map[playery.value][playerx.value] = Bomb()
        bombsState.value--
    }
}

fun update() {
    moveUntilLast()
    gameOverIfDanger()
    updateIfNotDelayState()
}

private fun updateIfNotDelayState() {
    if (isNotDelayState()) {
        delayState.value = DELAY
        for (y in 1 until map.size) {
            for (x in 1 until map[y].size) {
                map[y][x].update(y, x)
            }
        }
    }
}

private fun isNotDelayState() = --delayState.value <= 0

private fun gameOverIfDanger() {
    if (map[playery.value][playerx.value].isDanger())
        gameOverState.value = true
}

private fun moveUntilLast() {
    while (!gameOverState.value && inputs.size > 0) {
        inputs.removeLast().move()
    }
}

val gameOverState = mutableStateOf(false)
val updateState = mutableStateOf(false)

@Composable
fun draw() {
    val before = System.currentTimeMillis()
    drawMapEndlessly()
    val sleep = getSleepTime(before)
    setUpdateStateInfinitely(sleep)
}

private fun getSleepTime(before: Long): Long {
    val after = System.currentTimeMillis()
    val frameTime = after - before
    return SLEEP - frameTime
}

private fun setUpdateStateInfinitely(sleep: Long) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(sleep)
            updateState.value = true
        }
    }
}

private fun drawMapEndlessly() {
    if (updateState.value) {
        updateState.value = false
        drawMap()
    } else {
        drawMap()
    }
}

private fun DrawScope.drawMap() {
    for (y in map.indices) {
        for (x in map[y].indices) {
            drawRect(
                color = map[y][x].color(x, y),
                topLeft = drawBox(x, y).topLeft,
                size = Size(TITLE_SIZE.toFloat(), TITLE_SIZE.toFloat())
            )
        }
    }
}

@Composable
private fun drawMap() {
    Canvas(modifier = Modifier.width(1200.dp).height(800.dp)) {
        drawMap()
        drawPlayerIfNotGameOver()
    }
}

private fun DrawScope.drawPlayerIfNotGameOver() {
    if (!gameOverState.value)
        drawRect(color = Color(0xff00ff00), getPlayerOffset(), Size(TITLE_SIZE.toFloat(), TITLE_SIZE.toFloat()))
}

private fun getPlayerOffset(): Offset {
    return Offset(
        playerx.value * TITLE_SIZE.toFloat(),
        playery.value * TITLE_SIZE.toFloat(),
    )
}

fun drawBox(x: Int, y: Int): Rect {
    return Rect(
        (x * TITLE_SIZE).toFloat(),
        (y * TITLE_SIZE).toFloat(),
        TITLE_SIZE.toFloat(),
        TITLE_SIZE.toFloat()
    )
}

@Composable
fun gameLoop() {
    update()
    draw()
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        gameLoop()
    }
}

val inputs = mutableStateListOf<Input>()

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        onKeyEvent = { it.processKeyEvent() }
    ) {
        App()
    }
}
