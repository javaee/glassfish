setlocal
set api="C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\embedded\glassfish-embedded-all\3.0-Prelude-SNAPSHOT\glassfish-embedded-all-3.0-Prelude-SNAPSHOT.jar"

set jsp="C:\Documents and Settings\bnevins\.m2\repository\org\glassfish\web\jsp-impl\2.1\jsp-impl-2.1.jar"


set classname=com.elf.MyEmbeddedApp.MyApp
set cp=target/MyEmbeddedApp-1.0-SNAPSHOT.jar
set cp=%cp%;%jsp%
set cp=%cp%;%api%


java %JAVA_DEBUGX% -cp %cp% %classname%
endlocal
