package tests

import match.CalculatedPlayerAction
import match.CalculatedPlayerDecision
import match.CalculatedSkipTurn
import match.MatchSnapshot
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
suspend fun onMatchStarted(players: Set<String>) {
    callInfo("Match started")
    callInfo("Players are: $players")
    enemyUsername = players.find { it != username } ?: "Unknown enemy"
}

suspend fun onTurnStarted(matchSnapshot: MatchSnapshot) {
    callInfo("TurnStarted. $matchSnapshot")
    val players = matchSnapshot.players
//    val playerSnapshot = players.find { it.username == username }
//            ?: throw GameApp.NullAppDataException("Null playerSnapshot")
//    val enemySnapshot = players.find { it.username != username }
//            ?: throw GameApp.NullAppDataException("Null enemySnapshot")
}

suspend fun onPlayerAction(message: CalculatedPlayerDecision) {
    callInfo("PlayerAction")
    when (message) {
        is CalculatedPlayerAction -> {
//            val attacker = match.findPlayerByUsername(message.attacker)
//            val target = match.findPlayerByUsername(message.target)
//            target.health -= message.damage
            callInfo("${message.attacker} hit ${message.target} \n " +
                    "Hitted ${message.damage} health")
        }
        is CalculatedSkipTurn -> {

        }
    }
}

suspend fun onMatchEnded(winner: String) {
    callInfo("Match ended")
//    match.state = Match.State.NO_MATCH
//    GlobalScope.launch {
//        match.webSocketSession?.close()
//                ?: throw GameApp.NullAppDataException("Null match webSocketSession")
//    }
//    match.winner.value = winner
    exitProcess(0)
}