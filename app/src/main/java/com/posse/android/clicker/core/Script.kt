package com.posse.android.clicker.core

interface Script {
    val game: Games
    val script: String
    val width: Int
    val height: Int
}

enum class Game : Script {

    Market {
        override val game: Games = Games.FifaMobile
        override val script: String = "Market"
        override val width: Int = 960
        override val height: Int = 480
    },

    EqualGame {
        override val game: Games = Games.FifaMobile
        override val script: String = "Equal game"
        override val width: Int = 960
        override val height: Int = 480
    },

    VSAttack {
        override val game: Games = Games.FifaMobile
        override val script: String = "VS Attack"
        override val width: Int = 960
        override val height: Int = 480
    },

    Ads {
        override val game: Games = Games.LooneyTunes
        override val script: String = "Ads"
        override val width: Int = 1280
        override val height: Int = 720
    },
}

enum class Games(val naming: String) {
    FifaMobile("Fifa Mobile"),
    LooneyTunes("Looney Tunes")
}