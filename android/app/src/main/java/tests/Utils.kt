package tests

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import match.MatchSnapshot
import testgame.data.GameApp
import testgame.data.Match
import testgame.data.MatchPlayer
import user.Username
import kotlin.system.exitProcess

fun callInfo(info: String) {
    println(info)
}


/**
 * Main
 */
fun onUserMoneyUpdate(username: Username, money: Int) {
    println("$username get $money money")
}

fun onLeadingFacultyUpdate(facultyId: Int, points: Int) {
    println("$facultyId get $points points")
}

fun onFacultiesPointsUpdate(facultyId: Int, points: Int, winnerUsername: String) {
    println("$winnerUsername get $points points for faculty with $facultyId id")
}


/**
 * Match
 */
fun onMatchStarted(players: Set<String>) {
    callInfo("Match started")
    callInfo("Players are: $players")
}

fun onTurnStarted(matchSnapshot: MatchSnapshot) {
    callInfo("TurnStarted")
    val players = matchSnapshot.players
    val playerSnapshot = players.find { it.username == username }
            ?: throw GameApp.NullAppDataException("Null playerSnapshot")
    val enemySnapshot = players.find { it.username != username }
            ?: throw GameApp.NullAppDataException("Null enemySnapshot")
    if (match.player == null || match.enemy == null) {
        match.player = MatchPlayer(playerSnapshot.username, playerSnapshot.health, playerSnapshot.health)
        match.enemy = MatchPlayer(enemySnapshot.username, enemySnapshot.health, enemySnapshot.health)
    } else {
        match.player?.let { it.currentHealth = playerSnapshot.health }
        match.enemy?.let { it.currentHealth = enemySnapshot.health }
    }
    if (playerSnapshot.isActive) {
        match.state = Match.State.MY_TURN
    } else {
        match.state = Match.State.ENEMY_TURN
    }
}

fun onPlayerAction(attackerUsername: String, targetUsername: String) {
    callInfo("PlayerAction")
    val attacker = match.findPlayerByUsername(attackerUsername)
    val target = match.findPlayerByUsername(targetUsername)
    target.currentHealth -= GameApp.PLAYER_ACTION_DAMAGE
    callInfo("${attacker.username} hit ${target.username} \n " +
            "Hitted ${GameApp.PLAYER_ACTION_DAMAGE} health")
}

fun onMatchEnded(winner: String) {
    callInfo("Match ended")
    match.state = Match.State.NO_MATCH
    GlobalScope.launch {
        match.webSocketSession?.close()
                ?: throw GameApp.NullAppDataException("Null match webSocketSession")
    }
    match.winner = winner
    exitProcess(0)
}