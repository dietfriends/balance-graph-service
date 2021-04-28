dependencies {

  api("com.graphql-java:graphql-java")
  api("com.jayway.jsonpath:json-path")
  api("com.graphql-java:graphql-java-extended-scalars")

  implementation("org.springframework:spring-web")
  implementation("org.springframework:spring-context")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  testImplementation("io.projectreactor:reactor-core")
  testImplementation("io.projectreactor:reactor-test")
}