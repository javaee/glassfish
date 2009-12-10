#!/bin/sh
$1 -XDprocess.packages -proc:only \
    -cp $2:. \
    -processor DIY \
    -Acom.sun.tools.javac.sym.Jar=$2 \
    -Acom.sun.tools.javac.sym.Dest=$3 \
    `jar tf $2 | grep '^javax/..*/$' | sed -e 's;/$;;' -e 's;/;.;g'`

# extract tld files
cd $3
for i in `jar tf $2 | grep "**/*.tld$"`
do
    echo $i
    jar xvf $2 $i
done
# adding license file in the build area so it appears in the jar
mkdir -p META-INF
cp ../../../../../packager/CDDL+GPL.txt META-INF/LICENSE.txt

