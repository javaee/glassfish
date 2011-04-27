sync
# unmount
umount $1
if [ $? -eq 0 ]; then
    exit 0
else
    exit -1
fi