setlocal

set api=target/glassfish-embedded-all-3.0-Prelude-Embedded-SNAPSHOT.jar
set cp=target/SimpleMaven-1.0-SNAPSHOT.jar
set cp=%cp%;%api%

set classname=mygfe.HelloGFE

java -cp %cp% %classname%
endlocal





