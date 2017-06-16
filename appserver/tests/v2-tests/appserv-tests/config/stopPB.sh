#!/bin/sh

SYS=`uname`
case $SYS in
   Windows*)
      OS=win32;;   #Windows environments
   Linux*)
      OS=linux;;   #Linux environments
   SunOS*)
      OS=solaris;; #Solaris environments
   *)
      OS=unknown;;    #All the rest
esac

if [ $OS = "win32" ]; then
    for x in `ps|grep "startPB"|cut -d' ' -f 2`
    do
        echo "killing startPB process with processid: $x"; 
        kill -9 $x
    done
    for x in `ps|grep "pointbase"|cut -d' ' -f 2`
    do
        echo "killing pointbase process with processid: $x"; 
        kill -9 $x
    done
else
    for x in `ps -ef|grep "java"|cut -d' ' -f 3`
    do
        echo "killing java process with processid: $x"; 
        kill -9 $x
    done
fi
