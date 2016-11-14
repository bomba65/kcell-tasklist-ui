name := """kcell-backend"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.8"

EclipseKeys.preTasks := Seq(compile in Compile)
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)

libraryDependencies += javaJdbc
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1208"

libraryDependencies += javaJpa
libraryDependencies += "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final"

libraryDependencies += javaWs

PlayKeys.externalizeResources := false

unmanagedBase <<= baseDirectory { base => base / "libs" }

libraryDependencies += "org.keycloak" % "keycloak-core" % "1.9.4.Final"
libraryDependencies += "org.keycloak" % "keycloak-adapter-core" % "1.9.4.Final"


libraryDependencies += "org.springframework" % "spring-context" % "4.2.4.RELEASE"

libraryDependencies += "org.apache.cxf" % "cxf-core" % "3.1.8"

libraryDependencies += "org.apache.cxf" % "cxf-rt-bindings-soap" % "3.1.8"

libraryDependencies += "org.apache.cxf" % "cxf-rt-frontend-jaxws" % "3.1.8"

libraryDependencies += "org.apache.xmlbeans" % "xmlbeans" % "2.6.0"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "5.0.0"

libraryDependencies += cache
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
