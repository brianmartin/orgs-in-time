name := "bd-project"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

resolvers ++= Seq(
    "Maven Repository Switchboard" at "http://repo1.maven.org/maven2",
    "OpenNLP Maven Repository" at "http://opennlp.sourceforge.net/maven2",
    "Codahale Repo" at "http://repo.codahale.com",
    "Twitter repo" at "http://maven.twttr.com"
  )

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies  ++=  Seq(
    // SQL
    "org.squeryl" %% "squeryl" % "0.9.5-2",
    "mysql" % "mysql-connector-java" % "5.1.10",
    // openNLP
    "org.apache.opennlp" % "opennlp-maxent" % "3.0.1-incubating",
    "org.apache.opennlp" % "opennlp-tools" % "1.5.1-incubating",
    // Count-min
    "com.twitter" %% "algebird" % "0.1.4",
    // Logging
    "com.codahale" % "logula_2.9.1" % "2.1.3",
    // REST server
    "com.twitter" % "finagle-http" % "5.3.7",
    "com.codahale" % "jerkson_2.9.1" % "0.5.0"
  )

scalacOptions ++= Seq("-deprecation", "-Xlint")

