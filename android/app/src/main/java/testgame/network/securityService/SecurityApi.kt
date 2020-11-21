package testgame.network.securityService

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.KtorExperimentalAPI
import security.LOGIN_ENDPOINT
import security.REGISTER_ENDPOINT
import security.UserLoginInput
import security.UserRegisterInput
import testgame.network.NetworkService
import java.lang.Exception
import java.net.UnknownHostException

object SecurityApi : NetworkService() {

    @KtorExperimentalAPI
    suspend fun login(userLoginInput: UserLoginInput): HttpResponse {
        return getSuccessfulResponseOrException {
            client.post() {
                url("$BASE_HTTP_URL$LOGIN_ENDPOINT")
                body = userLoginInput
                contentType(ContentType.Application.Json)
            }
        }
    }

    @KtorExperimentalAPI
    suspend fun register(userRegisterInput: UserRegisterInput): HttpResponse {
        return getSuccessfulResponseOrException {
            client.post() {
                url("$BASE_HTTP_URL$REGISTER_ENDPOINT")
                body = userRegisterInput
                contentType(ContentType.Application.Json)
            }
        }
    }
}
