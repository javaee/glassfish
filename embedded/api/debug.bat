@echo off
setlocal

set CP=target\glassfish-embedded-api-3.0-Prelude-SNAPSHOT.jar
set CP=%CP%;C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-all\3.0-Prelude-SNAPSHOT\glassfish-embedded-all-3.0-Prelude-SNAPSHOT.jar

set DEB=%java_debugx%
java %DEB% -cp "%CP%" org.glassfish.embed.EmbeddedMain %*

endlocal
