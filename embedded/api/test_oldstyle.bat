rem for ideas on how to run an embedded app...



set CP=target/test-classes
set CP=%CP%;target/glassfish-embedded-api-10.0-SNAPSHOT-with-full-v3.jar
set CLASS=org.glassfish.embed.Main

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1323 -cp %CP% %CLASS%

