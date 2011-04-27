#!/bin/sh
# invoke with path to the disk and mount point
echo "create-disk.sh $1 $2"
rm -rf $2

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

# mount.
echo "mkdir destination directory $2 \n"
mkdir $2
echo "Mounting custom disk /dev/loop$LOOPCOUNTER \n"
mount -t ext2 /dev/loop$LOOPCOUNTER $2

# add rights to everyone to write to the disk
chmod a+rwx $2

echo "Jerome Was Here with $1" >$2/jerome.txt

return $loopCounter



