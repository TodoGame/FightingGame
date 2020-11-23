package testgame.data

import kotlin.properties.Delegates

object User {
    lateinit var username: String
    lateinit var authenticationToken: String
    var level by Delegates.notNull<Int>()
    var currentLevelExperience by Delegates.notNull<Int>()
    var money by Delegates.notNull<Int>()
}