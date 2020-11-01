task("stage") {
    dependsOn("server:shadowJar")
}
