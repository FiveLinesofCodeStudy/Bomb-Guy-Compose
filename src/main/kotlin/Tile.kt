import androidx.compose.ui.graphics.Color

interface Tile {
    companion object {
        fun transform(i: Int): Tile {
            return when (i) {
                0 -> Air()
                1 -> Unbreakable()
                2 -> Stone()
                3 -> Bomb()
                4 -> BombClose()
                5 -> BombReallyClose()
                6 -> TmpFire()
                7 -> Fire()
                8 -> ExtraBomb()
                9 -> MonsterUp()
                10 -> MonsterRight()
                11 -> TmpMonsterRight()
                12 -> MonsterDown()
                13 -> TmpMonsterDown()
                14 -> MonsterLeft()
                else -> throw Exception("Unexpected tile: $i")
            }
        }
    }
    fun explode(y: Int, x: Int) {
        if (map[y][x].isNearBomb())
            bombsState.value++
        map[y][x] = Fire()
    }
    fun move(dy: Int, dx: Int)
    fun placeBomb()

    fun update(y: Int, x: Int)

    fun color(x: Int, y: Int): Color = Color.Transparent
    fun isStone(): Boolean {
        return this is Stone
    }

    fun isUnbreakable(): Boolean {
        return this is Unbreakable
    }

    fun isNearBomb(): Boolean {
        return this is Bomb || this is BombClose || this is BombReallyClose
    }

    fun isExtraBomb(): Boolean {
        return this is ExtraBomb
    }

    fun isDanger(): Boolean {
        return this is Fire ||
                this is MonsterDown ||
                this is MonsterLeft ||
                this is MonsterRight ||
                this is MonsterUp
    }

    fun isAir(): Boolean {
        return this is Air
    }

}
class Air : Tile {

    override fun move(dy: Int, dx: Int) {
        playerx.value += dx
        playery.value += dy
    }

    override fun placeBomb() {
        TODO("Not yet implemented")
    }

    override fun update(y: Int, x: Int) {

    }
}

class Unbreakable : Tile {
    override fun explode(y: Int, x: Int) {
        // do nothing
    }

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        // do nothing
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xff999999)
    }

}

class Stone : Tile {
    override fun explode(y: Int, x: Int) {
        if (Math.random() < 0.1) map[y][x] = ExtraBomb()
        else map[y][x] = Fire()
    }

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        // do nothing
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xff0000cc)
    }
}

class Bomb : Tile {
    override fun explode(y: Int, x: Int) {

    }

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y][x] = BombClose()
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xff770000)
    }
}

class BombClose : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y][x] = BombReallyClose()
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xffcc0000)
    }
}

class BombReallyClose : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y - 1][x].explode(y - 1, x)
        map[y + 1][x].explode(y + 1, x)
        map[y][x - 1].explode(y, x - 1)
        map[y][x + 1].explode(y, x + 1)
        map[y][x].explode(y, x)
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xff000000)
    }
}

class TmpFire : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y][x] = Fire()
    }
}

class Fire : Tile {

    override fun move(dy: Int, dx: Int) {
        playerx.value += dx
        playery.value += dy
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y][x] = Air()
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xffffcc00)
    }
}

class ExtraBomb : Tile {

    override fun move(dy: Int, dx: Int) {
        playerx.value += dx
        playery.value += dy
        bombsState.value += 1
        map[playery.value][playerx.value] = Air()
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        // do nothing
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xff00cc00)
    }
}

class MonsterUp : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        if (map[y - 1][x].isAir()) {
            map[y][x] = Air()
            map[y - 1][x] = MonsterUp()
        } else {
            map[y][x] = MonsterRight()
        }
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xffcc00cc)
    }
}

class MonsterRight : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        if (map[y][x + 1].isAir()) {
            map[y][x] = Air()
            map[y][x + 1] = TmpMonsterRight()
        } else {
            map[y][x] = MonsterDown()
        }
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xffcc00cc)
    }
}

class TmpMonsterRight : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y][x] = MonsterRight()
    }
}

class MonsterDown : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        if (map[y + 1][x].isAir()) {
            map[y][x] = Air()
            map[y][x - 1] = TmpMonsterDown()
        } else {
            map[y][x] = MonsterLeft()
        }
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xffcc00cc)
    }
}

class TmpMonsterDown : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        map[y][x] = MonsterDown()
    }
}

class MonsterLeft : Tile {

    override fun move(dy: Int, dx: Int) {
        // do nothing
    }

    override fun placeBomb() {
        // do nothing
    }

    override fun update(y: Int, x: Int) {
        if (map[y][x - 1].isAir()) {
            map[y][x] = Air()
            map[y][x - 1] = MonsterLeft()
        } else {
            map[y][x] = MonsterUp()
        }
    }

    override fun color(x: Int, y: Int): Color {
        return Color(0xffcc00cc)
    }
}

