dependencies {
  api(project(":graphql-bgs"))

  implementation("org.springframework.boot:spring-boot-starter")
  compileOnly("org.springframework.data:spring-data-mongodb")

  testImplementation("org.springframework.boot:spring-boot-actuator-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
}