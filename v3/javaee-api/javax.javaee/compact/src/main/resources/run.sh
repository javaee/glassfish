#!/bin/sh
rm javax.javaee-api-compact.jar
$1 -XDprocess.packages -proc:only \
    -cp $2:. \
    -processor DIY \
    -Acom.sun.tools.javac.sym.Jar=$2 \
    -Acom.sun.tools.javac.sym.Dest=$3 \
    `jar tf $2 | grep '^javax/..*/$' | sed -e 's;/$;;' -e 's;/;.;g'`

