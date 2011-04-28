#!/bin/sh
# invoke with path to the disk and iso file output location
echo "mkisofs.sh $1 $2"
rm -f $2
echo "running mkisofs via mac hdiutil"
hdiutil makehybrid $1  -iso -joliet -o $2



