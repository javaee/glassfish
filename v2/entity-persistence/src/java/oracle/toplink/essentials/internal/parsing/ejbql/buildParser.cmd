cls
setlocal
set PATH=c:\jdk1.4\bin;%PATH%



cd antlr273
set CLASSPATH=C:\java\TopLink10-1.5\1013jlib\antlr273\antlr-2.7.3.zip;%CLASSPATH%
java -cp %CLASSPATH% persistence.antlr.Tool EJBQLParser.g

cd ..

pause
endlocal