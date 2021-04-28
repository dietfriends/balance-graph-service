dependencies {
  api(project(":graphql-bgs"))

  compileOnly("org.springframework.data:spring-data-mongodb")

  testImplementation("org.springframework.boot:spring-boot-actuator-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
}