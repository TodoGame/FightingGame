val serverJarLocation = "./server/build/libs/server-all.jar"

task("stage") {
    dependsOn("server:shadowJar")
}

task("checkStage") {
    dependsOn("stage")
    doLast {
        val jarFile = file(serverJarLocation)
        if (!jarFile.exists()) {
            throw GradleException("File $serverJarLocation does not exist")
        }
    }
}
