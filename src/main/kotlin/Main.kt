@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay


const val TITLE_SIZE = 100
const val FPS = 30
const val SLEEP = 1000 / FPS
const val TPS = 2
const val DELAY = FPS / TPS

enum class Tile {
    AIR,
    UNBREAKABLE,
    STONE,
    BOMB,
    BOMB_CLOSE,
    BOMB_REALLY_CLOSE,
    TMP_FIRE,
    FIRE,
    EXTRA_BOMB,
    MONSTER_UP,
    MONSTER_RIGHT,
    TMP_MONSTER_RIGHT,
    MONSTER_DOWN,
    TMP_MONSTER_DOWN,
    MONSTER_LEFT,
}

enum class Input {
    UP, DOWN, LEFT, RIGHT, PLACE
}

val map = arrayOf(
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

fun explode(x: Int, y: Int, type: Tile, mapState: MutableState<Array<Array<Int>>>, bombsState: MutableState<Int>) {
    val map = mapState.value
    if (map[y][x] == Tile.STONE.ordinal) {
        if (Math.random() < 0.1) map[y][x] = Tile.EXTRA_BOMB.ordinal
        else map[y][x] = type.ordinal
    } else if (map[y][x] != Tile.UNBREAKABLE.ordinal) {
        if (
            map[y][x] == Tile.BOMB.ordinal ||
            map[y][x] == Tile.BOMB_CLOSE.ordinal ||
            map[y][x] == Tile.BOMB_REALLY_CLOSE.ordinal
        )
            bombsState.value++
        map[y][x] = type.ordinal
    }
}

fun move(
    dx: Int,
    dy: Int,
    mapState: MutableState<Array<Array<Int>>>,
    playerxState: MutableState<Int>,
    playeryState: MutableState<Int>,
    bombsState: MutableState<Int>
) {
    val map = mapState.value
    val playerx = playerxState.value
    val playery = playeryState.value

    if (
        map[playery + dy][playerx + dx] == Tile.AIR.ordinal ||
        map[playery + dy][playerx + dx] == Tile.FIRE.ordinal
    ) {
        playerxState.value += dx
        playeryState.value += dy
    } else if (map[playery + dy][playerx + dx] == Tile.EXTRA_BOMB.ordinal) {
        playerxState.value += dx
        playeryState.value += dy
        bombsState.value += 1
        map[playeryState.value][playerxState.value] = Tile.AIR.ordinal
    }
}

fun placeBomb(mapState: MutableState<Array<Array<Int>>>, bombsState: MutableState<Int>, playerx: Int, playery: Int) {
    if (bombsState.value > 0) {
        val map = mapState.value
        map[playery][playerx] = Tile.BOMB.ordinal
        bombsState.value--
    }
}

fun update(
    inputs: SnapshotStateList<Input>,
    mapState: MutableState<Array<Array<Int>>>,
    playerxState: MutableState<Int>,
    playeryState: MutableState<Int>,
    delayState: MutableState<Int>,
    bombsState: MutableState<Int>,
    gameOverState: MutableState<Boolean>
) {
    while (!gameOverState.value && inputs.size > 0) {
        val current = inputs.removeLast()
        when (current) {
            Input.LEFT -> {
                move(-1, 0, mapState, playerxState, playeryState, bombsState)
            }

            Input.RIGHT -> {
                move(1, 0, mapState, playerxState, playeryState, bombsState)
            }

            Input.UP -> {
                move(0, -1, mapState, playerxState, playeryState, bombsState)
            }

            Input.DOWN -> {
                move(0, 1, mapState, playerxState, playeryState, bombsState)
            }

            Input.PLACE -> {
                placeBomb(mapState, bombsState, playerxState.value, playeryState.value)
            }
        }

    }

    val map = mapState.value
    val playery = playeryState.value
    val playerx = playerxState.value

    if (
        map[playery][playerx] == Tile.FIRE.ordinal ||
        map[playery][playerx] == Tile.MONSTER_DOWN.ordinal ||
        map[playery][playerx] == Tile.MONSTER_LEFT.ordinal ||
        map[playery][playerx] == Tile.MONSTER_RIGHT.ordinal ||
        map[playery][playerx] == Tile.MONSTER_UP.ordinal
    )
        gameOverState.value = true

    if (--delayState.value > 0) return
    delayState.value = DELAY

    for (y in 1 until map.size) {
        for (x in 1 until map[y].size) {
            if (map[y][x] == Tile.BOMB.ordinal) {
                map[y][x] = Tile.BOMB_CLOSE.ordinal
            } else if (map[y][x] == Tile.BOMB_CLOSE.ordinal) {
                map[y][x] = Tile.BOMB_REALLY_CLOSE.ordinal
            } else if (map[y][x] == Tile.BOMB_REALLY_CLOSE.ordinal) {
                explode(x + 0, y - 1, Tile.FIRE, mapState, bombsState)
                explode(x + 0, y + 1, Tile.FIRE, mapState, bombsState)
                explode(x - 1, y + 0, Tile.FIRE, mapState, bombsState)
                explode(x + 1, y + 0, Tile.FIRE, mapState, bombsState)
                map[y][x] = Tile.FIRE.ordinal
                bombsState.value++
            } else if (map[y][x] == Tile.TMP_FIRE.ordinal) {
                map[y][x] = Tile.FIRE.ordinal
            } else if (map[y][x] == Tile.FIRE.ordinal) {
                map[y][x] = Tile.AIR.ordinal
            } else if (map[y][x] == Tile.TMP_MONSTER_DOWN.ordinal) {
                map[y][x] = Tile.MONSTER_DOWN.ordinal
            } else if (map[y][x] == Tile.TMP_MONSTER_RIGHT.ordinal) {
                map[y][x] = Tile.MONSTER_RIGHT.ordinal
            } else if (map[y][x] == Tile.MONSTER_RIGHT.ordinal) {
                if (map[y][x + 1] == Tile.AIR.ordinal) {
                    map[y][x] = Tile.AIR.ordinal
                    map[y][x + 1] = Tile.TMP_MONSTER_RIGHT.ordinal
                } else {
                    map[y][x] = Tile.MONSTER_DOWN.ordinal
                }
            } else if (map[y][x] == Tile.MONSTER_DOWN.ordinal) {
                if (map[y + 1][x] == Tile.AIR.ordinal) {
                    map[y][x] = Tile.AIR.ordinal
                    map[y][x - 1] = Tile.TMP_MONSTER_DOWN.ordinal
                } else {
                    map[y][x] = Tile.MONSTER_LEFT.ordinal
                }
            } else if (map[y][x] == Tile.MONSTER_LEFT.ordinal) {
                if (map[y][x - 1] == Tile.AIR.ordinal) {
                    map[y][x] = Tile.AIR.ordinal
                    map[y][x - 1] = Tile.MONSTER_LEFT.ordinal
                } else {
                    map[y][x] = Tile.MONSTER_UP.ordinal
                }
            } else if (map[y][x] == Tile.MONSTER_UP.ordinal) {
                if (map[y - 1][x] == Tile.AIR.ordinal) {
                    map[y][x] = Tile.AIR.ordinal
                    map[y - 1][x] = Tile.MONSTER_UP.ordinal
                } else {
                    map[y][x] = Tile.MONSTER_RIGHT.ordinal
                }
            }
        }
    }
}

@Composable
fun draw(
    mapState: MutableState<Array<Array<Int>>>,
    playerxState: MutableState<Int>,
    playeryState: MutableState<Int>,
    gameOverState: MutableState<Boolean>,
    updateState: MutableState<Boolean>
) {
    val before = System.currentTimeMillis()

    Canvas(modifier = Modifier.width(1200.dp).height(800.dp)) {
        val map = mapState.value
        for (y in map.indices) {
            for (x in map[y].indices) {
                val rect = Rect(
                    (x * TITLE_SIZE).toFloat(),
                    (y * TITLE_SIZE).toFloat(),
                    TITLE_SIZE.toFloat(),
                    TITLE_SIZE.toFloat()
                )
                val color = if (map[y][x] == Tile.UNBREAKABLE.ordinal) {
                    Color(0xff999999)
                } else if (map[y][x] == Tile.STONE.ordinal) {
                    Color(0xff0000cc)
                } else if (map[y][x] == Tile.EXTRA_BOMB.ordinal) {
                    Color(0xff00cc00)
                } else {
                    Color.Transparent
                }
                if (map[y][x] != Tile.AIR.ordinal)
                    drawRect(
                        color = color,
                        topLeft = rect.topLeft,
                        size = Size(TITLE_SIZE.toFloat(), TITLE_SIZE.toFloat())
                    )
            }
        }
    }

    if (updateState.value) {
        updateState.value = false
        RealTimeCanvas(mapState, playerxState, playeryState, gameOverState)
    } else {
        RealTimeCanvas(mapState, playerxState, playeryState, gameOverState)
    }

    val after = System.currentTimeMillis()
    val frameTime = after - before
    val sleep = SLEEP - frameTime

    LaunchedEffect(Unit) {
        while (true) {
            delay(sleep)
            updateState.value = true
        }
    }
}

@Composable
private fun RealTimeCanvas(
    mapState: MutableState<Array<Array<Int>>>,
    playerxState: MutableState<Int>,
    playeryState: MutableState<Int>,
    gameOverState: MutableState<Boolean>
) {
    Canvas(modifier = Modifier.width(1200.dp).height(800.dp)) {
        val map = mapState.value
        val playerx = playerxState.value
        val playery = playeryState.value

        for (y in map.indices) {
            for (x in map[y].indices) {
                val rect = Rect(
                    (x * TITLE_SIZE).toFloat(),
                    (y * TITLE_SIZE).toFloat(),
                    TITLE_SIZE.toFloat(),
                    TITLE_SIZE.toFloat()
                )
                val color = if (map[y][x] == Tile.UNBREAKABLE.ordinal) {
                    Color.Transparent
                } else if (map[y][x] == Tile.STONE.ordinal) {
                    Color.Transparent
                } else if (map[y][x] == Tile.EXTRA_BOMB.ordinal) {
                    Color.Transparent
                } else if (map[y][x] == Tile.FIRE.ordinal) {
                    Color(0xffffcc00)
                } else if (
                    map[y][x] == Tile.MONSTER_UP.ordinal ||
                    map[y][x] == Tile.MONSTER_LEFT.ordinal ||
                    map[y][x] == Tile.MONSTER_RIGHT.ordinal ||
                    map[y][x] == Tile.MONSTER_DOWN.ordinal
                ) {
                    Color(0xffcc00cc)
                } else if (map[y][x] == Tile.BOMB.ordinal) {
                    Color(0xff770000)
                } else if (map[y][x] == Tile.BOMB_CLOSE.ordinal) {
                    Color(0xffcc0000)
                } else if (map[y][x] == Tile.BOMB_REALLY_CLOSE.ordinal) {
                    Color(0xff000000)
                } else {
                    Color.Transparent
                }
                if (map[y][x] != Tile.AIR.ordinal)
                    drawRect(
                        color = color,
                        topLeft = rect.topLeft,
                        size = Size(TITLE_SIZE.toFloat(), TITLE_SIZE.toFloat())
                    )
            }
        }

        val player = Rect(
            playerx * TITLE_SIZE.toFloat(),
            playery * TITLE_SIZE.toFloat(),
            TITLE_SIZE.toFloat(),
            TITLE_SIZE.toFloat()
        )
        if (!gameOverState.value)
            drawRect(color = Color(0xff00ff00), player.topLeft, Size(TITLE_SIZE.toFloat(), TITLE_SIZE.toFloat()))
    }
}

@Composable
fun gameLoop(
    updateState: MutableState<Boolean>,
    inputs: SnapshotStateList<Input>,
    playerxState: MutableState<Int>,
    playeryState: MutableState<Int>,
    mapState: MutableState<Array<Array<Int>>>,
    delayState: MutableState<Int>,
    bombsState: MutableState<Int>,
    gameOverState: MutableState<Boolean>
) {
    update(inputs, mapState, playerxState, playeryState, delayState, bombsState, gameOverState)
    draw(mapState, playerxState, playeryState, gameOverState, updateState)
}

@Composable
@Preview
fun App(inputs: SnapshotStateList<Input>) {

    val playerx = remember { mutableStateOf(1) }
    val playery = remember { mutableStateOf(1) }
    val mapState = remember { mutableStateOf(map) }

    val delay = remember { mutableStateOf(0) }
    val bombs = remember { mutableStateOf(1) }
    val gameOver = remember { mutableStateOf(false) }

    val updateState = remember { mutableStateOf(false) }

    MaterialTheme {
        gameLoop(
            updateState = updateState,
            inputs = inputs,
            mapState = mapState,
            playerxState = playerx,
            playeryState = playery,
            delayState = delay,
            bombsState = bombs,
            gameOverState = gameOver
        )
    }
}

fun main() = application {

    val inputs = remember { mutableStateListOf<Input>() }

    Window(onCloseRequest = ::exitApplication, onKeyEvent = {
        if (it.key == Key.DirectionUp && it.type == KeyEventType.KeyDown) {
            inputs.add(Input.UP)
            true
        } else if (it.key == Key.DirectionDown && it.type == KeyEventType.KeyDown) {
            inputs.add(Input.DOWN)
            true
        } else if (it.key == Key.DirectionLeft && it.type == KeyEventType.KeyDown) {
            inputs.add(Input.LEFT)
            true
        } else if (it.key == Key.DirectionRight && it.type == KeyEventType.KeyDown) {
            inputs.add(Input.RIGHT)
            true
        } else if (it.key == Key.Spacebar && it.type == KeyEventType.KeyDown) {
            inputs.add(Input.PLACE)
            true
        } else {
            false
        }
    }) {
        App(inputs)
    }
}
