name := "phonebook"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.5",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "org.flywaydb" %% "flyway-play" % "3.0.1",
  "org.postgresql" % "postgresql" % "9.4.1209",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test",
  "org.mockito" % "mockito-core" % "2.2.9" % "test"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-deprecation"
)

lazy val `phonebook` = (project in file(".")).enablePlugins(PlayScala)
