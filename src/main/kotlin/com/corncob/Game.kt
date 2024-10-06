package com.corncob

import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable

data class Game (val leader: User,
                 val players: MutableMap<User, Int>,
                 val id: String,
                 var inProgress: Boolean,
                 var startTime: GMTDate,
                 var endTime: GMTDate,
                 var timeLimit: Int = 0,
                 var location: String,
                 ) {
    fun addPlayer(player: User) {
        players[player] = 0
    }

    fun removePlayer(player: User) {
        players.remove(player)
    }

    fun setScore(player: User, score: Int): Boolean {
        if(!players.containsKey(player)) {
            return false
        }
        players[player] = score
        return true
    }

    fun getScore(player: User): Int {
        return players[player]!!
    }

    fun getGameState(user: User): Map<User, Int> {
        return players
    }

    fun startGame() {
        inProgress = true
        startTime = GMTDate()
    }

    fun endGame() {
        inProgress = false
        endTime = GMTDate()
    }
}

@Serializable
data class GameDTO(
    var startTime: GMTDate,
    var endTime: GMTDate,
    var timeLimit: Int = 0,
    var location: String,
) {

}