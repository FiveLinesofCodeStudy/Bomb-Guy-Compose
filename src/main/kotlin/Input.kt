interface Input {
    fun move()
}

class Up : Input {
    override fun move() {
        map[playery.value + -1][playerx.value + 0].move(-1, 0)
    }
}

class Down : Input {
    override fun move() {
        map[playery.value + 1][playerx.value + 0].move(1, 0)
    }
}

class Left : Input {
    override fun move() {
        map[playery.value + 0][playerx.value + -1].move(0, -1)
    }
}

class Right : Input {
    override fun move() {
        map[playery.value + 0][playerx.value + 1].move(0, 1)
    }
}

class Place : Input {
    override fun move() {
        placeBomb()
    }
}