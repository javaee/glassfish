#!/bin/sh

destination=$1

for file in `ls .`; do
   if [ $file != "debian" -a $file != "build-stamp" -a $file != "configure-stamp" -a  $file != "Makefile" ]; then
      echo "Copying $file from ./$file to $destination"
      cp -R ./$file $destination    
   fi
done

exit 0
