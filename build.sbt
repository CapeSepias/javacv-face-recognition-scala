
name := "javacv-scala"

version := "1.0"

classpathTypes += "maven-plugin"

libraryDependencies ++= Seq(
  "org.bytedeco" % "javacv" % "1.1",
  "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1",
  "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1" classifier "macosx-x86_64",
  "org.scalaz" %% "scalaz-core" % "7.2.0"
)
