#!/bin/sh
# invoke with path to the disk and iso file output location
echo "mkisofs.sh $1 $2"
rm -f $2
echo "running mkisofs"
mkisofs -l -allow-lowercase -allow-multidot -L -o $2 $1 




