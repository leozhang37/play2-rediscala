resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")

resolvers ++= Seq(
  Resolver.url("sbt-plugin-releases2", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  "coda" at "http://repo.codahale.com"
)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")

resolvers += "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"

addSbtPlugin("reaktor" % "sbt-scct" % "0.2-SNAPSHOT")

addSbtPlugin("com.github.theon" %% "xsbt-coveralls-plugin" % "0.0.3")