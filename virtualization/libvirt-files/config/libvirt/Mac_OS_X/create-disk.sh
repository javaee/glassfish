#!/bin/sh
# invoke with path to the disk and size and mount point
echo "create-disk.sh $1 $2 $3"
rm -rf $1
rm -rf $3
echo "Generating Disk"
dd if=/dev/zero of=$1 bs=1024 count=$2

# formating.
echo "Formating custom disk $1"
newfs_ext2 -F $1

# mount.
mkdir $3
fuse-ext2 $1 $3 -o force

# add rights to everyone to write to the disk
chmod a+rwx $3

echo "Done with the script !"
echo "Jerome Was Here with $1" >$3/jerome.txt




