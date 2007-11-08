#!/usr/bin/ksh -p
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

usage()
{
   cat <<-EOF
usage: bld_awk_pkginfo -p <prodver> -m <mach> -o <awk_script> [-v <version>]
EOF
}

#
# Awk strings
#
# two VERSION patterns: one for Dewey decimal, one for Dewey plus ,REV=n
# the first has one '=' the second has two or more '='
#
VERSION1="^VERSION=[^=]*$"
VERSION2="^VERSION=[^=]*=.*$"
PRODVERS="^SUNW_PRODVERS="
ARCH='ARCH=\"ISA\"'

#
# parse command line
#
mach=""
prodver=""
awk_script=""
version="IASVERS"

while getopts o:p:m:v: c
do
   case $c in
   o)
      awk_script=$OPTARG
      ;;
   m)
      mach=$OPTARG
      ;;
   p)
      prodver=$OPTARG
      ;;
   v)
      version=$OPTARG
      ;;
   \?)
      usage
      exit 1
      ;;
   esac
done

if [[ ( -z $prodver ) || ( -z $mach ) || ( -z $awk_script ) ]]
then
   usage
   exit 1
fi

if [[ -f $awk_script ]]
then
	rm -f $awk_script
fi

#
# Build REV= field based on date
#
rev=$(date "+%Y.%m.%d.%H.%M")

#
# Determine bundled and unbundled default install locations
#
if [[ $prodver = [0-9]*.0 ]]; then
	suffix=${prodver%.0}
else
	suffix=$prodver
fi
optconfdir=/etc/opt/SUNWappserver$suffix
optinstdir=/opt/SUNWappserver$suffix
usrconfdir=/etc/appserver
usrinstdir=/usr/appserver
javainstdir=/usr/j2se
locale=en_US
optdomainsdir=/var/opt/SUNWappserver$suffix/domains
vardomainsdir=/var/appserver/domains
origver="${version%%.0_*}"
hadbdir=/opt/SUNWhadb/4.4-0.8

#
# Build awk script which will process all the
# pkginfo.tmpl files.
#
# the first VERSION pattern is replaced with a leading quotation mark
#
rm -f $awk_script
cat << EOF > $awk_script
/$VERSION1/ {
      sub(/\=[^=]*$/,"=\"$rev\"")
      print
      next
   }
/$VERSION2/ {
      sub(/\=[^=]*$/,"=$rev\"")
      sub(/IASVERS/,"$origver")
      print
      next
   }
/$PRODVERS/ { 
      printf "SUNW_PRODVERS=\"%s\"\n", "$prodver" 
      next
   }
/$ARCH/ {
      printf "ARCH=\"%s\"\n", "$mach"
      next
   }
/ASCONFDIR=OPTCONFDIR/ {
      printf "ASCONFDIR=\"%s\"\n", "$optconfdir"
      next
   }
/ASINSTDIR=OPTINSTDIR/ {
      printf "ASINSTDIR=\"%s\"\n", "$optinstdir"
      next
   }
/ASJAVADIR=JAVAINSTDIR/ {
      printf "ASJAVADIR=\"%s\"\n", "$javainstdir"
      next
   }
/ASLOCALE=LOCALE/ {
      printf "ASLOCALE=\"%s\"\n", "$locale"
      next
   }
/ASCONFDIR=USRCONFDIR/ {
      printf "ASCONFDIR=\"%s\"\n", "$usrconfdir"
      next
   }
/ASINSTDIR=USRINSTDIR/ {
      printf "ASINSTDIR=\"%s\"\n", "$usrinstdir"
      next
   }
/ASDOMAINSDIR=OPTDOMAINSDIR/ {
      printf "ASDOMAINSDIR=\"%s\"\n", "$optdomainsdir"
      next
   }
/ASDOMAINSDIR=VARDOMAINSDIR/ {
      printf "ASDOMAINSDIR=\"%s\"\n", "$vardomainsdir"
      next
   }
/ASHADBDIR=OPTHADBDIR/ {
      printf "ASHADBDIR=\"%s\"\n", "$hadbdir"
      next
   }
/^STICKY_VERSION=.*$/ {
      printf "STICKY_VERSION=\"%s\"\n", sticky_version
      next
   }
{ print }
EOF
