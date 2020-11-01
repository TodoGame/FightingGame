rootProject.name = "FightingGame"
include("server", "shared")

val notIncludeAndroid = startParameter.projectProperties["notIncludeAndroid"]

if (notIncludeAndroid != "true") {
    include("android", "android:app")
}
