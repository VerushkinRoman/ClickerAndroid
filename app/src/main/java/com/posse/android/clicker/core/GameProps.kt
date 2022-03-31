package com.posse.android.clicker.core

interface GameProps {
    val gameScripts: List<ScriptProps>
    val gameName: String
}

interface ScriptProps {
    val width: Int
    val height: Int
    val scriptName: String
}

enum class FifaGameScript : ScriptProps {
    Market {
        override val width = 960
        override val height = 480
        override val scriptName = "Market"
    },
    EqualGame {
        override val width = 960
        override val height = 480
        override val scriptName = "Equal game"
    },
    VSAttack {
        override val width = 960
        override val height = 480
        override val scriptName = "VS Attack"
    },
}

enum class LooneyGameScript : ScriptProps {
    Ads {
        override val width = 1280
        override val height = 720
        override val scriptName = "Ads"
    }
}

enum class Games : GameProps {

    FifaMobile {
        override val gameScripts = listOf(
            FifaGameScript.Market,
            FifaGameScript.VSAttack,
            FifaGameScript.EqualGame
        )
        override val gameName = "Fifa Mobile"
    },

    LooneyTunes {
        override val gameScripts = listOf(LooneyGameScript.Ads)
        override val gameName = "Looney Tunes"
    }
}