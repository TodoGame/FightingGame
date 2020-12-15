package testgame.data

import android.service.autofill.UserData
import kotlin.properties.Delegates

object User {
    lateinit var username: String
    lateinit var authenticationToken: String
    lateinit var userData: user.UserData
}