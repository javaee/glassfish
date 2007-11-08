#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License. You can obtain
# a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
# or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
# 
# When distributing the software, include this License Header Notice in each
# file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
# Sun designates this particular file as subject to the "Classpath" exception
# as provided by Sun in the GPL Version 2 section of the License file that
# accompanied this code.  If applicable, add the following below the License
# Header, with the fields enclosed by brackets [] replaced by your own
# identifying information: "Portions Copyrighted [year]
# [name of copyright owner]"
# 
# Contributor(s):
# 
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#!/tools/ns/bin/perl
#Usage: perl backoutpatch.pl <AS_INSTALL_ROOT> <PATCH_ID>

my $debug="false";

#Attributes in the README.patchid file
$PACKAGES="Packages";
$PATCHID="Patch-ID#";
$FILESINC="Files included with this patch";
$FILESADDED="FilesAdded";
$FILESDELETED="FilesDeleted";
$FILESMODIFIED="FilesModified";
$REQUIRED="PatchesRequired";
$OBSOLETES="Obsoletes";
$INCOMPATIBLES="IncompatiblePatches";
$PREBACKOUT="prebackout";
$POSTBACKOUT="postbackout";

#Attributes in the pkginfo file
$PATCHLIST="PATCHLIST";
$PATCH_OBSOLETES="PATCH_OBSOLETES";
$PATCH_INCOMPAT="PATCH_INCOMPAT";
$PATCH_REQUIRED="PATCH_REQUIRED";

if ($ARGV[1] eq "") {
  die "The patch to be backed out should be provided as an argument for this script. Quitting !!!\nUsage: backoutpatch <ID of patch to be backed out>\n";
}

my $ostype = $^O;
if ($ostype =~m/Win/)
{
  $platform = "windows";
  $CP = "copy";
  $MV = "ren";
  $MKDIR = "md";
  $RM = "del";
  $slash = "\\";
} else {
  $platform = "linux";
  $CP = "cp";
  $MV = "mv";
  $MKDIR = "mkdir";
  $RM = "rm";
  $slash = "/";
}

$INSTALLROOT = $ARGV[0];
$PATCHROOT = $INSTALLROOT;
$BKPATCHDIR = "${PATCHROOT}${slash}patches${slash}${ARGV[1]}${slash}backoutpkg";

$status = system("$CP ${BKPATCHDIR}${slash}README.* README.txt");
die "Could not find the README file in the backout patch directory .. Quitting!!\n" unless $status == 0;
open(README, "README.txt");
while ($line = <README>)
{
  ($KEY, $VALUE) = split("=", $line);
  chomp($VALUE);
  $table{$KEY}= $VALUE;
}
close README;
system("$RM README.txt");

#TODO: validate the user, product version
#make sure that all the packages involved in the patch are already installed
@packages = split(",", $table{$PACKAGES});
#foreach $item (@packages)
#{
#  $dir = "$ARGV[0]\\$item";
  $dir = "$ARGV[0]${slash}config";
  $pkginfo = "$dir${slash}pkginfo";
  -d $dir or die "The config directory in AS_INSTALL_ROOT is missing. Fatal error !!!!";

  -e $pkginfo or die "The config directory under AS_INSTALL_ROOT does not have a \"pkginfo\" file. Fatal Error !!!";
  open(PKGINFO, $pkginfo);
  while ($line = <PKGINFO>)
  {
    if ($line =~ m/^($PATCH_OBSOLETES=(.+))/s)
    {
      $flag=1;
      # $2 is the patch list
      $presence = checkPresence($2, $table{$PATCHID});
      if ($presence == 1)
      {
#        die "Patch $table{$PATCHID} is present in the $PATCH_OBSOLETES list of Package $item. Hence, cannot remove this patch. Quitting.\n";
        die "Patch $table{$PATCHID} is present in the $PATCH_OBSOLETES list. Hence, cannot remove this patch. Quitting.\n";
      }
    }
    if ($line =~ m/^($PATCH_REQUIRED=(.+))/s)
    {
      $flag=1;
      # $2 is the patch list
      $presence = checkPresence($2, $table{$PATCHID});
      if ($presence == 1)
      {
#        die "Patch $table{$PATCHID} is present in the $PATCH_REQUIRED list of Package $item. Hence, cannot remove this patch. Quitting.\n";
        die "Patch $table{$PATCHID} is present in the $PATCH_REQUIRED list. Hence, cannot remove this patch. Quitting.\n";
      }
    }
  }
#}

if ($table{$PREBACKOUT} eq "yes")
{
if ($debug eq "true") {
  print("Execute Command : ${INSTALLROOT}${slash}lib${slash}perl${slash}perl  ${BKPATCHDIR}${slash}prebackout.pl\n");
}
  system("${INSTALLROOT}${slash}lib${slash}perl${slash}perl ${BKPATCHDIR}${slash}prebackout.pl");
}

@filesInc = split(",", $table{$FILESINC});
@filesAdded = split(",",$table{$FILESADDED});
@filesDeleted = split(",",$table{$FILESDELETED});
@filesModified = split(",", $table{$FILESMODIFIED});


foreach $filepath (@filesAdded)
{
  print "RESTORING ADDED FILE ---\n";
  if ($platform eq "windows")
  {
    $filepath =~ s/\/|\\/\\/g;
    $filepath =~ m/.*\\(.+)/;
    $actualFile = $1;
  } else {
    $filepath =~ s/\/|\\/\//g;
    $filepath =~ m/.*\/(.+)/;
    $actualFile = $1;
  }
if ($debug eq "true") {
  print("Execute command : $RM $INSTALLROOT${slash}$filepath\n");
}
  system("$RM $INSTALLROOT${slash}$filepath"); 
}
foreach $filepath (@filesDeleted)
{
  print "RESTORING DELETED FILE ---\n";
  if ($platform eq "windows")
  {
    $filepath =~ s/\/|\\/\\/g;
    $filepath =~ m/.*\\(.+)/;
    $actualFile = $1;
  } else {
    $filepath =~ s/\/|\\/\//g;
    $filepath =~ m/.*\/(.+)/;
    $actualFile = $1;
  }
if ($debug eq "true") {
  print("Execute command : $CP ${BKPATCHDIR}${slash}$actualFile $INSTALLROOT${slash}$filepath\n");
}
  system("$CP ${BKPATCHDIR}${slash}$actualFile $INSTALLROOT${slash}$filepath"); 
}
foreach $filepath (@filesModified)
{
  print "RESTORING MODIFIED FILE ---\n";
  if ($platform eq "windows")
  {
    $filepath =~ s/\/|\\/\\/g;
    $filepath =~ m/.*\\(.+)/;
    $actualFile = $1;
  } else {
    $filepath =~ s/\/|\\/\//g;
    $filepath =~ m/.*\/(.+)/;
    $actualFile = $1;
  }
if ($debug eq "true") {
  print("Execute command : $RM $INSTALLROOT${slash}$filepath\n");
}
  system("$RM $INSTALLROOT${slash}$filepath"); 
if ($debug eq "true") {
  print("Execute command : $CP ${BKPATCHDIR}${slash}$actualFile $INSTALLROOT${slash}$filepath\n");
}
  system("copy ${BKPATCHDIR}${slash}$actualFile $INSTALLROOT${slash}$filepath"); 
}

#It's time to update the pkginfo file of each package involved
#foreach $item (@packages)
#{
#  print "Updating pkginfo file for package :: $item\n";
  print "Updating pkginfo file \n";
#  $dir = "$ARGV[0]\\$item";
  $dir = "$ARGV[0]${slash}config";
  $pkginfo = "$dir${slash}pkginfo";
  $newpkginfo = "$pkginfo.new";
  open(PKGINFO, "$pkginfo");
  open(NEWPKGINFO, "> $newpkginfo");
  my $obList = "";
  my $requiredList = "";
  my $imcompatibleList = "";
  while ($line = <PKGINFO>)
  {
    if ($line =~ m/^($PATCHLIST=(.+))/s)
    {
      $line =~ s/$table{$PATCHID}//;
      $line =~ s/,,/,/;
      $line =~ s/=,/=/;
      $line =~ s/(,)$//;
      print NEWPKGINFO "$line"; 
      next;
    }
    if ($line =~ m/^($PATCH_OBSOLETES=(.+))/s)
    {
      # $2 is the patch list
      $obList = $2;
      chomp($obList);
      $presence = checkPresenceAndChange($obList, $table{$OBSOLETES});
      $line = "$PATCH_OBSOLETES=$presence";
      $line =~ s/=,/=/;
      $line =~ s/,,/,/g;
      $line =~ s/(,)$//;
      print NEWPKGINFO "$line"; 
      $value = $presence;
      if ($value ne "\n")
      {
        print NEWPKGINFO "\n";
      }
      else
      {
###        print "GOTCHA!!!\n";
      }
      next;
    }
    if ($line =~ m/^($PATCH_REQUIRED=(.+))/)
    {
      chomp($line);
      # $2 is the patch list
      $requiredList = $2;
      $presence = checkPresenceAndChange($requiredList, $table{$REQUIRED});
      $line = "$PATCH_REQUIRED=$presence";
      $line =~ s/=,/=/;
      $line =~ s/,,/,/g;
      $line =~ s/(,)$//;
      print NEWPKGINFO "$line"; 
      $value = $presence;
      if ($value ne "\n")
      {
        print NEWPKGINFO "\n";
      }
      else
      {
###        print "GOTCHA!!!\n";
      }
      next;
    }
    if ($line =~ m/^($PATCH_INCOMPAT=(.+))/)
    {
      chomp($line);
      # $2 is the patch list
      $incompatibleList = $2;
      $presence = checkPresenceAndChange($incompatibleList, $table{$INCOMPATIBLES});
      $line = "$PATCH_INCOMPAT=$presence";
      $line =~ s/=,/=/;
      $line =~ s/,,/,/g;
      $line =~ s/(,)$//;
      print NEWPKGINFO "$line"; 
      $value = $presence;
      if ($value ne "\n")
      {
        print NEWPKGINFO "\n";
      }
      else
      {
###        print "GOTCHA!!!\n";
      }
      next;
    }
    if ($line =~ m/^(PATCH_INFO_$table{$PATCHID}=Installed:)/)
    {
      next;
    }
    print NEWPKGINFO $line; 
  }
  close PKGINFO;
  close NEWPKGINFO;
if ($debug eq "true") {
  print("Execute Command : $RM $pkginfo\n");
}
  system("$RM $pkginfo");
  if ($platform eq "windows")
  {
if ($debug eq "true") {
    print("Execute Command : $MV $newpkginfo pkginfo\n");
}
    system("$MV $newpkginfo pkginfo");
  } else {
if ($debug eq "true") {
    print("Execute Command : $MV $newpkginfo $pkginfo\n");
}
    system("$MV $newpkginfo $pkginfo");
  }
#}

if ($table{$POSTBACKOUT} eq "yes")
{
if ($debug eq "true") {
  print("Execute Command : perl postbackout.pl\n");
}
  system("perl postbackout.pl");
}
print "Patch removed successfully\n";

#This function takes in an array, a single patch
#If the array contains that single patch, it returns 1 or else 0
sub checkPresence
{
  my @patchList = split(",",$_[0]);
  my $found = 0;
  foreach $item (@patchList)
  {
    if ( ($item =~ m/^$_[1]@/) || ($item eq $_[1]) )
    {
      $found = 1;
      last;
    }
  }
  return $found;
}

#This function takes in 1st array, 2nd array of patches
#It returns a list(string) of patches in 2nd array which are not in 1st array
sub checkPresenceOfArray
{
  my @patchList = split(",",$_[0]);
  my @obsoleteList = split(",",$_[1]);
  my $returnList="";
  foreach $item (@obsoleteList)
  {
    my $found = 0;
    foreach $item1 (@patchList)
    {
      if ( ($item eq $item1) || ($item1 =~ m/^$item@/) )
      {
        $found = 1;
        last;
      }
    }
    if ($found == 0) {
      $returnList = "$returnList,$item\@1";
    }
  }
  return $returnList;
}

#This function takes in 1st array, 2nd array
#For each element in 2nd array, it checks if that element is present in the 1st array. If it is, then it decrements the count of that element, and returns the joined list of 2nd array elements.
sub checkPresenceAndChange
{
  my @patchList = split(",",$_[0]);
  my @obsoleteList = split(",",$_[1]);
  foreach $item (@obsoleteList)
  {
    foreach $item1 (@patchList)
    {
      if ($item1 =~ m/^$item@/)
      {
        @elements = split("@",$item1);
        $elements[1]--;
        $item1 = join("@",@elements);
        $item1 =~ s/.+\@0//;
      }
    }
  }
  $returnList = join(",",@patchList);
  return $returnList;
}
