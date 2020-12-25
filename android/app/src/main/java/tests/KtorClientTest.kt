package tests

import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import match.Message
import match.PlayerAction
import security.UserLoginInput
import security.UserRegisterInput
import testgame.data.GameApp
import testgame.data.Match
import testgame.data.User
import testgame.network.*
import testgame.network.NetworkService.Companion.AUTHORIZATION_HEADER_NAME
import kotlin.system.exitProcess

val match = Match
lateinit var username: String
var facultyId = 1
lateinit var token: String

@KtorExperimentalAPI
fun main() {
    runBlocking {
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

@KtorExperimentalAPI
private fun runCommand(command: String) {
    try {
        when (command) {
            "register" -> register()
            "login" -> login()
            "loginSuper" -> loginSuper()
            "user" -> getMe()
            "connectMain" -> connectMain()
            "subscribeUser" -> subscribeUser()
            "subscribeFaculty" -> subscribeLeadingFaculty()

            "myFaculty" -> getMyFaculty()
            "leadingFaculty" -> getLeadingFaculty()

            "shopItems" -> getShopItems()
            "buyItem" -> buyItem()

            "connectMatch" -> connectMatch()
            "attack" -> attack()
            else -> return
        }
    } catch (e: Exception) {
        throw e
    }
}

@KtorExperimentalAPI
private fun getShopItems() {
    runBlocking {
        val items = ShopApi.getAllNotOwnedItems(token)
        println("Items are")
        items.forEach {
            println(it)
        }
    }
}

@KtorExperimentalAPI
private fun buyItem() {
    print("Enter the itemId: ")
    val input = readLine() ?: ""
    runBlocking {
        try {
            val user = ShopApi.buyItem(token, input.toInt())
            println("User: $user")
        } catch (e: Exception) {
//            println(e.message)
            throw e
        }
    }
}

@KtorExperimentalAPI
private fun connectMain() {
    runBlocking {
        val ticket = MainApi.getWebSocketTicket(token)
        GlobalScope.launch {
            MainApi.connectToMainWebSocket(
                    ticket,
                    ::onUserMoneyUpdate,
                    ::onLeadingFacultyUpdate,
                    ::onFacultiesPointsUpdate
            )
        }
    }
}

@KtorExperimentalAPI
private fun subscribeUser() {
    runBlocking {
        MainApi.subscribeUser(username, true)
    }
}

@KtorExperimentalAPI
private fun subscribeLeadingFaculty() {
    runBlocking {
        MainApi.subscribeLeadingFaculty(true)
    }
}

@KtorExperimentalAPI
private fun connectMatch() {
    runBlocking {
        val ticket = MatchApi.getWebSocketTicket(token)
        GlobalScope.launch {
            MatchApi.connectToMatchWebSocket(
                    ticket,
                    ::onMatchStarted,
                    ::onTurnStarted,
                    ::onPlayerAction,
                    ::onMatchEnded
            )
        }
    }
}

private fun attack() {
    try {
        val enemyUsername = match.enemy.value!!.username
        val action = NetworkService.jsonFormat.encodeToString<Message>(
                PlayerAction(enemyUsername, match.player.value!!.username)
        )
        GlobalScope.launch {
            User.matchSession?.send(action)
                    ?: throw GameApp.NullAppDataException("Null match webSocketSession")
        }
    } catch (exception: java.lang.NullPointerException) {
        println("Null pointer exception")
    }
}

@KtorExperimentalAPI
private fun register(): HttpResponse? {
    var response: HttpResponse? = null
    runBlocking {
        username = "a"
        facultyId = 2
        val userRegisterInput = UserRegisterInput(
                username,
                "testPassword",
                "testUsername",
                facultyId
        )
        try {
            response = SecurityApi.register(userRegisterInput)
        } catch (e: NetworkService.NetworkException) {
            println(e.message)
        }
    }
    return response
}

@KtorExperimentalAPI
private fun login() : HttpResponse? {
    var response: HttpResponse? = null
    runBlocking {
        username = "a"
        val userLoginInput = UserLoginInput(
                username, "testPassword"
        )
        response = SecurityApi.login(userLoginInput)
        if (response!!.headers[AUTHORIZATION_HEADER_NAME] != null) {
            token = response!!.headers[AUTHORIZATION_HEADER_NAME]!!
            println("Token is: $token")
        } else {
            println("Wrong token response")
        }
    }
    return response
}

@KtorExperimentalAPI
private fun loginSuper(): HttpResponse? {
    var response: HttpResponse? = null
    runBlocking {
        username = "username"
        val userLoginInput = UserLoginInput(
                username, "password"
        )
        response = SecurityApi.login(userLoginInput)
        if (response!!.headers[AUTHORIZATION_HEADER_NAME] != null) {
            token = response!!.headers[AUTHORIZATION_HEADER_NAME]!!
            println("Token is: $token")
        } else {
            println("Wrong token response")
        }
    }
    return response
}

@KtorExperimentalAPI
private fun getMe() {
    runBlocking {
        val user = MainApi.getPlayerData(token)
        println("User: $user")
    }
}

@KtorExperimentalAPI
private fun getMyFaculty() {
    runBlocking {
        val faculty = MainApi.getConcreteFacultyData(token, facultyId)
        println("MyFaculty: $faculty")
    }
}

@KtorExperimentalAPI
private fun getLeadingFaculty() {
    runBlocking {
        val faculty = MainApi.getLeadingFacultyData(token)
        println("MyFaculty: $faculty")
    }
}
