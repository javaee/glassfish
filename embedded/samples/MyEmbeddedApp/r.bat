setlocal
set api="C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-all\3.0-Prelude-SNAPSHOT\glassfish-embedded-all-3.0-Prelude-SNAPSHOT.jar"

set classname=com.elf.MyEmbeddedApp.MyApp
set cp=target/MyEmbeddedApp-1.0-SNAPSHOT.jar
set cp=%cp%;%api%

java -cp %cp% %classname%
endlocal
