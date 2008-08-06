set CP=target/test-classes;target/gf-embedded-api-1.0-alpha-5-SNAPSHOT.jar;all.jar
set CLASS=org.glassfish.embed.Main


if "%1"=="debug" goto debug

java -cp %CP% %CLASS%
goto end


:debug
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1323 -cp %CP% %CLASS%

:end

