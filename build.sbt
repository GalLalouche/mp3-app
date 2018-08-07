name := "mp3-app"

version := "0.1"

val scalaVersionStr = "2.12.6"
scalaVersion := scalaVersionStr

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Maven Repository" at "http://repo1.maven.org/maven2/",
  Resolver.mavenLocal,
)

val monocleVersion = "1.5.0"
val scalazVersion = "7.2.15"
libraryDependencies ++= Seq(
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "com.github.mpilquist" %% "simulacrum" % "0.10.0",
  "io.reactivex" %% "rxscala" % "0.26.4",
  "org.me" %% "scalacommon" % "1.0" changing(),
  "org.scalamacros" % ("paradise_" + scalaVersionStr) % "2.1.0", // For some reason, it uses the full binary version
  "org.scala-lang.modules" %% "scala-swing" % "2.0.0",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
  "org.scalatest" %% "scalatest" % "3.0.4",
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "com.github.goxr3plus" % "java-stream-player" % "1.1.1", // 1.1.1 is my fixed version
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "io.spray" %%  "spray-json" % "1.3.4",
  "com.google.inject" % "guice" % "4.2.0",
  "net.codingwell" %% "scala-guice" % "4.2.1",
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
