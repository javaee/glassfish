copy "C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-nucleus\10.0-SNAPSHOT\glassfish-embedded-nucleus-10.0-SNAPSHOT.jar"
copy "C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-web\10.0-SNAPSHOT\glassfish-embedded-web-10.0-SNAPSHOT.jar"
copy "C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-all\10.0-SNAPSHOT\glassfish-embedded-all-10.0-SNAPSHOT.jar"
set CP=target/test-classes
rem set CP=%CP%;target/glassfish-embedded-api-10.0-SNAPSHOT.jar
set CP=%CP%;glassfish-embedded-nucleus-10.0-SNAPSHOT.jar
set CP=%CP%;glassfish-embedded-web-10.0-SNAPSHOT.jar

set CLASS=org.glassfish.embed.Main

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1323 -cp %CP% %CLASS%

