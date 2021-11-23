package com.posse.android.clicker.core

sealed interface Script {
    val script: String
    val width: Int
    val height: Int

    sealed interface FifaMobile {
        data class Market(
            override val width: Int = 1280,
            override val height: Int = 720,
            override val script: String = "Market"
        ) : Script

        data class EventAttack(
            override val width: Int = 1280,
            override val height: Int = 720,
            override val script: String = "EventAttack"
        ) : Script
    }

    sealed interface LooneyTunes {
        data class Ads(
            override val width: Int = 1280,
            override val height: Int = 720,
            override val script: String = "Ads"
        ) : Script
    }
}