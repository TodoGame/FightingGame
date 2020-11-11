rootProject.name = "FightingGame"
include("shared")

val localProperties = java.util.Properties()
val localPropertiesFile = file("./local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val notIncludeAndroid =
    startParameter.projectProperties["notIncludeAndroid"] == "true" || localProperties["notIncludeAndroid"] == "true"
if (!notIncludeAndroid) {
    include("android", "android:app")
}

val notIncludeServer = localProperties["notIncludeServer"] == "true"
if (!notIncludeServer) {
    include("server")
}
