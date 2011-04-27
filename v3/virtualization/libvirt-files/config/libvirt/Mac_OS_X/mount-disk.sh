#!/bin/sh
# invoke with path to the disk and mount point
echo "mount-disk.sh $1 $2"
rm -rf $2

# mount.
echo "mkdir destination directory $2 \n"
mkdir $2
# mount.
fuse-ext2 $1 $2 -o force



