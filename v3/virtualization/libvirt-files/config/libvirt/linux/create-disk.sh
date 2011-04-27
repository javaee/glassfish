#!/bin/sh
# invoke with path to the disk and size and mount point
echo "create-disk.sh $1 $2 $3"
rm -rf $1
rm -rf $3
echo "Generating Disk"
dd if=/dev/zero of=$1 bs=1024 count=$2

# find a free loopback
LOOPCOUNTER=0
losetup /dev/loop$LOOPCOUNTER
while [ $? -eq 0 ]
do
	LOOPCOUNTER=`expr $LOOPCOUNTER + 1`
	losetup /dev/loop$LOOPCOUNTER
done

echo "Using /dev/loop$LOOPCOUNTER"

losetup /dev/loop$LOOPCOUNTER $1
if [ $? -ne 0 ]; then
	echo "Error while attaching to loopback device"
	return
fi

# formating.
echo "Formating custom disk /dev/loop$LOOPCOUNTER"
mkfs -t ext2 -m 1 -v /dev/loop$LOOPCOUNTER


# mount.
echo "mkdir destination directory $3 \n"
mkdir $3
echo "Mounting custom disk /dev/loop$LOOPCOUNTER \n"
mount -t ext2 /dev/loop$LOOPCOUNTER $3

# add rights to everyone to write to the disk
chmod a+rwx $3

echo "Jerome Was Here with $1" >$3/jerome.txt

return $loopCounter



