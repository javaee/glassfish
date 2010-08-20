set wshell=WScript.CreateObject("WScript.Shell")

mycmd = "java -jar ..\lib\bootstrap.jar bootstrap.properties"

set oExec=wshell.exec(mycmd)
Do While oExec.Status = 0
     WScript.Sleep 500
Loop

WScript.StdErr.writeline "<resultReport xmlns=""http://openinstaller.org/config/resultreport/V1"">"
WScript.StdErr.writeline "<configStatus>SUCCESS</configStatus>"
WScript.StdErr.writeline "<platformError>No Platform Errors</platformError>"
WScript.StdErr.writeline "<productError>No Product Errors</productError>"
WScript.StdErr.writeline "<docReference>See http://www.sun.com</docReference>"
WScript.StdErr.writeline "<nextSteps>Next Steps</nextSteps>"
WScript.StdErr.writeline "</resultReport>"
