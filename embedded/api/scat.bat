setlocal
set api="C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-all\3.0-Prelude-SNAPSHOT\glassfish-embedded-all-3.0-Prelude-SNAPSHOT.jar"

set classname=org.glassfish.embed.Main_old
set cp=target/test-classes
set cp=%cp%;%api%

java  %java_debug%    -cp %cp% %classname%
endlocal
