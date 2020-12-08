package tests

import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import match.MatchSnapshot
import match.Message
import match.PlayerAction
import testgame.data.GameApp
import testgame.data.Match
import testgame.data.MatchPlayer
import testgame.network.MatchApi
import testgame.network.NetworkService
import testgame.network.SecurityApi
import kotlin.system.exitProcess

const val AUTHORIZATION_HEADER_NAME = "Authorization"
val match = Match
lateinit var username: String

@KtorExperimentalAPI
fun main() {
    runBlocking {
        var globalToken = ""
//        val myUsername = kotlin.random.Random.nextInt(0, 1000).toString()
        val myUsername = "TestUsername5"
        val myFacultyId = 1
        username = myUsername
        try {
//            val userRegisterInput = security.UserRegisterInput(
//                    myUsername,
//                    "testPassword",
//                    "testUsername",
//                    myFacultyId
//            )
//            var response: HttpResponse? = null
//            try {
//                response = SecurityApi.register(userRegisterInput)
//            } catch (e: NetworkService.UnknownNetworkException) {
//                println(e.message)
//            }
            val userLoginInput = security.UserLoginInput(
                    myUsername, "testPassword"
            )
            val response = SecurityApi.login(userLoginInput)
            val token = response!!.headers[AUTHORIZATION_HEADER_NAME]
            if (token != null) {
                globalToken = token
                println("Token is: $token")
            } else {
                println("Wrong token response")
            }
        } catch (exception: NullPointerException) {
            println("Some data missed")
        }

        val ticket = MatchApi.getWebSocketTicket(globalToken)
        GlobalScope.launch {
            MatchApi.connectMatchWebSocket(
                    match,
                    ticket,
                    ::onMatchStarted,
                    ::onTurnStarted,
                    ::onPlayerAction,
                    ::onMatchEnded
            )
        }
        var command: String
        while (true) {
            print("Enter the command: ")
            command = readLine() ?: ""
            when (command) {
                "exit" -> exitProcess(0)
                else -> runCommand(command)
            }
        }
    }
}

private fun runCommand(command: String) {
    when (command) {
        "attack" -> attack()
    }
}

private fun attack() {
    try {
        val enemyUsername = match.enemy!!.username
        val action = NetworkService.jsonFormat.encodeToString<Message>(
                PlayerAction(enemyUsername, match.player!!.username)
        )
        GlobalScope.launch {
            match.webSocketSession?.send(action)
                    ?: throw GameApp.NullAppDataException("Null match webSocketSession")
        }
    } catch (exception: java.lang.NullPointerException) {
        println("Null pointer exception")
    }
}

private fun onMatchStarted(players: Set<String>) {
//    callInfo("Match started")
}

private fun onTurnStarted(matchSnapshot: MatchSnapshot) {
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

private fun onPlayerAction(attackerUsername: String, targetUsername: String) {
    val attacker = match.findPlayerByUsername(attackerUsername)
    val target = match.findPlayerByUsername(targetUsername)
    target.currentHealth -= GameApp.PLAYER_ACTION_DAMAGE
    callInfo("${attacker.username} hit ${target.username} \n " +
            "Hitted ${GameApp.PLAYER_ACTION_DAMAGE} health")
}

private fun onMatchEnded(winner: String) {
    callInfo("Match ended")
    match.state = Match.State.NO_MATCH
    GlobalScope.launch {
        match.webSocketSession?.close()
                ?: throw GameApp.NullAppDataException("Null match webSocketSession")
    }
    match.winner = winner
    exitProcess(0)
}

private fun callInfo(info: String) {
    println(info)
}