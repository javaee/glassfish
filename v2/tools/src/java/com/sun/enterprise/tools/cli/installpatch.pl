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
#Usage: perl installpatch.pl <AS_INSTALL_ROOT> <PATCH_DIR> 

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
$PREPATCH="prepatch";
$POSTPATCH="postpatch";

#Attributes in the pkginfo file
$PATCHLIST="PATCHLIST";
$PATCH_OBSOLETES="PATCH_OBSOLETES";
$PATCH_INCOMPAT="PATCH_INCOMPAT";
$PATCH_REQUIRED="PATCH_REQUIRED";

if ($ARGV[1] eq "") {
  die "The patch directory should be provided as an argument for this script. Quitting !!!\nUsage: installpatch <patch directory>\n";
}
my $patchDir = $ARGV[1];
-d $patchDir or die "Argument provided is not a valid directory!!!\n";

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

$status = system("$CP ${patchDir}${slash}README.* README.txt");
die "Could not find the README file in the patch directory .. Quitting!!\n" unless $status == 0;
open(README, "README.txt");
while ($line = <README>)
{
  ($KEY, $VALUE) = split("=", $line);
  chomp($VALUE);
  $table{$KEY}= $VALUE;
}
close README;
system("$RM README.txt");

$INSTALLROOT=$ARGV[0];
#TODO: validate the user, product version
#make sure that all the packages involved in the patch are already installed

#We have decided to have one giant package and its pkginfo is under appserv/config
#@packages = split(",", $table{$PACKAGES});
#foreach $item (@packages)
#{
#  $dir = "$ARGV[0]\\$item";
  $dir = "$ARGV[0]${slash}config";
  $pkginfo = "$dir${slash}pkginfo";
#  -d $dir or die "Package $item is not installed. This package is needed for installing the patch. Please install it.";
  -d $dir or die "The config directory under AS_INSTALL_ROOT is not present. This is needed for installing the patch.";
  
#  -e $pkginfo or die "Package $item does not have a \"pkginfo\" file. Fatal Error !!!";
  -e $pkginfo or die "The config directory under AS_INSTALL_ROOT does not have a \"pkginfo\" file. Fatal Error !!!";
  open(PKGINFO, $pkginfo);
  $flag1=0;
  $flag2=0;
  $flag3=0;
  while ($line = <PKGINFO>)
  {
    if ($line =~ m/^($PATCHLIST=(.+))/s)
    {
      $flag1=1;
      # $2 is the patch list
      my $patchList = $2;
      $precense = checkPresence($patchList, $table{$PATCHID});
      if ($precense == 1)
      {
#        die "Patch $table{$PATCHID} has already been installed for Package $item\n";
        die "Patch $table{$PATCHID} has already been installed \n";
      }
      my $reqPatches = $table{$REQUIRED};
      my @reqPatchesArr = split(",", $reqPatches);
      my $error = 0;
      $presence = "";
      $presence = checkPresenceOfArray($patchList, $table{$REQUIRED});
#      $errMessage = "Patch(es) $presence is/are not installed on Package $item, and is/are needed before the installation of this patch. Quitting !!";
      $errMessage = "Patch(es) $presence is/are not installed, and is/are needed before the installation of this patch. Quitting !!";
      $errMessage =~ s/ ,/ /;
      $errMessage =~ s/\@1//;
      if ($presence ne "") {
        die "$errMessage";
      }

      $presence = checkPresenceOfMultipleItems($patchList, $table{$INCOMPATIBLES});
#      $errMessage = "Patch(es) $presence is/are installed on Package $item, it/they is/are incompatible with this patch. So this patch cannot be installed. Quitting !!";
      $errMessage = "Patch(es) $presence is/are installed, it/they is/are incompatible with this patch. So this patch cannot be installed. Quitting !!";
      $errMessage =~ s/ ,/ /;
      if ($presence ne "") {
        die "$errMessage";
      }

    } 
    if ($line =~ m/^($PATCH_OBSOLETES=(.+))/s)
    {
      $flag2=1;
      # $2 is the patch list
      $precense = checkPresence($2, $table{$PATCHID});
      if ($precense == 1)
      {
#        die "Patch $table{$PATCHID} is present in the $PATCH_OBSOLETES list of Package $item. Hence, cannot install this patch. Quitting.\n";
        die "Patch $table{$PATCHID} is present in the $PATCH_OBSOLETES list. Hence, cannot install this patch. Quitting.\n";
      }
    } 
    if ($line =~ m/^($PATCH_INCOMPAT=(.+))/s)
    {
      $flag3=1;
      # $2 is the patch list
      $precense = checkPresence($2, $table{$PATCHID});
      if ($precense == 1)
      {
#        die "Patch $table{$PATCHID} is present in the $PATCH_INCOMPAT list of Package $item. Hence, cannot install this patch. Quitting.\n";
        die "Patch $table{$PATCHID} is present in the $PATCH_INCOMPAT list. Hence, cannot install this patch. Quitting.\n";
      }
    } 
  }
  if ($flag1 == 0) {
#    die "There is no PATCHLIST attribute in the 'pkginfo' file in Package $item. Fatal Error !!!";
    die "There is no PATCHLIST attribute in the 'pkginfo' file. Fatal Error !!!";
  }
  if ($flag2 == 0) {
#    die "There is no PATCH_OBSOLETES attribute in the 'pkginfo' file in Package $item. Fatal Error !!!";
    die "There is no PATCH_OBSOLETES attribute in the 'pkginfo' file. Fatal Error !!!";
  }
  if ($flag3 == 0) {
#    die "There is no PATCH_INCOMPAT attribute in the 'pkginfo' file in Package $item. Fatal Error !!!";
    die "There is no PATCH_INCOMPAT attribute in the 'pkginfo' file. Fatal Error !!!";
  }
  close PKGINFO;
#}
#Check if the patch is already installed for each package involved
#you do this by parsing the 'pkginfo' file of each package involved, and verifying that the patchid is not present

#TODO: Make sure that there is enough disk space

$PATCHROOT = $INSTALLROOT;

#Make a backup of all the packages involved

#Here, we are going to create the create a directory for this patch under the patches directory, and set up all the files
if (debug eq "true") {
print "Execute Command : $MKDIR $PATCHROOT${slash}patches\n";
}
system("$MKDIR $PATCHROOT${slash}patches");
if (debug eq "true") {
print "Execute Command : $MKDIR $PATCHROOT${slash}patches${slash}$table{$PATCHID}\n";
}
system("$MKDIR $PATCHROOT${slash}patches${slash}$table{$PATCHID}");
if (debug eq "true") {
print "Execute Command : $MKDIR $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}patchpkg\n";
}
system("$MKDIR $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}patchpkg");
if (debug eq "true") {
print "Execute Command : $MKDIR $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg\n";
}
system("$MKDIR $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg");
if (debug eq "true") {
print "Execute Command : $CP ${patchDir}${slash}* $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}patchpkg\n";
}
system("$CP ${patchDir}${slash}* $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}patchpkg");
if (debug eq "true") {
print "Execute Command : $CP ${patchDir}${slash}*backout* $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg\n";
}
system("$CP ${patchDir}${slash}*backout* $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg");
if (debug eq "true") {
print("Execute Command : $CP ${patchDir}${slash}README.* $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg\n");
}
system("$CP ${patchDir}${slash}README.* $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg");
if (debug eq "true") {
print "Execute Command : $RM $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}patchpkg${slash}*backout*\n";
}
system("$RM $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}patchpkg${slash}*backout*");

if ($table{$PREPATCH} eq "yes")
{
if (debug eq "true") {
  print "Execute Command : ${INSTALLROOT}${slash}lib${slash}perl${slash}perl ${patchDir}${slash}prepatch.pl\n";
}
  system("${INSTALLROOT}${slash}lib${slash}perl${slash}perl ${patchDir}${slash}prepatch.pl");
}

@filesInc = split(",", $table{$FILESINC});
@filesAdded = split(",",$table{$FILESADDED});
@filesDeleted = split(",",$table{$FILESDELETED});
@filesModified = split(",", $table{$FILESMODIFIED});

foreach $filepath (@filesAdded)
{
  print "ADDING FILE ----\n";
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
    
if (debug eq "true") {
  print("Execute command : $CP ${patchDir}${slash}$actualFile $INSTALLROOT${slash}$filepath\n");
}
  system("$CP ${patchDir}${slash}$actualFile $INSTALLROOT${slash}$filepath"); 
}
foreach $filepath (@filesDeleted)
{
  print "DELETING FILE ----\n";
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
if (debug eq "true") {
  print("Execute command : $CP $INSTALLROOT${slash}$filepath $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg\n");
}
  system("$CP $INSTALLROOT${slash}$filepath $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg"); 
if (debug eq "true") {
  print("Execute command : $RM $INSTALLROOT${slash}$filepath\n");
}
  system("$RM $INSTALLROOT${slash}$filepath"); 
}
foreach $filepath (@filesModified)
{
  print "MODIFYING FILE ----\n";
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
if (debug eq "true") {
  print("Execute command : $CP $INSTALLROOT${slash}$filepath $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg\n");
}
  system("$CP $INSTALLROOT${slash}$filepath $PATCHROOT${slash}patches${slash}$table{$PATCHID}${slash}backoutpkg"); 
if (debug eq "true") {
  print("Execute command : $CP ${patchDir}${slash}$actualFile $INSTALLROOT${slash}$filepath\n");
}
  system("$CP ${patchDir}${slash}$actualFile $INSTALLROOT${slash}$filepath"); 
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
      chomp($line);
      $nline = "$line,$table{$PATCHID}";
      $nline =~ s/=,/=/;
      print NEWPKGINFO "$nline\n"; 
      next;
    } 
    if ($line =~ m/^($PATCH_OBSOLETES=(.+))/s)
    {
      chomp($line);
###      print("In patch_obsoletes\n");
      # $2 is the patch list
      $obList = $2;
###      print("oblist = '$obList'\n");
      chomp($obList);
      $presence = checkPresenceAndChange($obList, $table{$OBSOLETES});
###      print("presence = '$presence'\n");
      $presence1 = checkPresenceOfArray($presence, $table{$OBSOLETES});
###      print("presence1 = '$presence1'\n");
      $line = "$PATCH_OBSOLETES=$presence$presence1";
      $line =~ s/=,/=/;
###      print("line = '$line'\n");
      print NEWPKGINFO "$line"; 
      $value = "$presence$presence1";
      if ($value ne "\n")
      {
        print NEWPKGINFO "\n";
      }
      else {
###        print("GOTCHA !!!\n");
      }
      next;
    }
    if ($line =~ m/^($PATCH_REQUIRED=(.+))/s)
    {
      chomp($line);
###      print("In patch_required\n");
      # $2 is the patch list
      $requiredList = $2;
      chomp($requiredList);
###      print("requiredList = '$requiredList'\n");
      $presence = checkPresenceAndChange($requiredList, $table{$REQUIRED});
###      print("presence = '$presence'\n");
      $presence1 = checkPresenceOfArray($presence, $table{$REQUIRED});
###      print("presence1 = '$presence1'\n");
      $line = "$PATCH_REQUIRED=$presence$presence1";
      $line =~ s/=,/=/;
###      print("line = '$line'\n");
      print NEWPKGINFO "$line"; 
      $value = "$presence$presence1";
      if ($value ne "\n")
      {
        print NEWPKGINFO "\n";
      }
      else {
###        print("GOTCHA !!!\n");
      }
      next;
    }
    if ($line =~ m/^($PATCH_INCOMPAT=(.+))/s)
    {
      chomp($line);
###      print("In patch_incompat\n");
      # $2 is the patch list
      $incompatibleList = $2;
      chomp($incompatibleList);
###      print("imcompatibleList = '$imcompatibleList'\n");
      $presence = checkPresenceAndChange($incompatibleList, $table{$INCOMPATIBLES});
###      print("presence = '$presence'\n");
      $presence1 = checkPresenceOfArray($presence, $table{$INCOMPATIBLES});
###      print("presence1 = '$presence1'\n");
      $line = "$PATCH_INCOMPAT=$presence$presence1";
      $line =~ s/=,/=/;
###      print("line = '$line'\n");
      print NEWPKGINFO "$line";
      $value = "$presence$presence1";
      if ($value ne "\n")
      {
        print NEWPKGINFO "\n";
      }
      else {
###        print("GOTCHA !!!\n");
      }
      next;
    }
    print NEWPKGINFO $line; 
  }
  ($SEC, $MIN, $HOUR, $DAY, $MONTH, $YEAR) = (localtime)[0,1,2,3,4,5];
  $YEAR = $YEAR + 1900;
  $line = "PATCH_INFO_$table{$PATCHID}=Installed: $MONTH/$DAY/$YEAR $HOUR:$MIN:$SEC Obsoletes:$table{$OBSOLETES} Requires:$table{$REQUIRED} Incompatibles:$table{$INCOMPATIBLES}";
  print NEWPKGINFO "$line\n"; 
  close PKGINFO;
  close NEWPKGINFO;
if (debug eq "true") {
  print("Execute Command : $RM $pkginfo\n");
}
  system("$RM $pkginfo");
  if ($platform eq "windows")
  {
if (debug eq "true") {
    print("Execute Command : $MV $newpkginfo pkginfo\n");
}
    system("$MV $newpkginfo pkginfo");
  } else {
if (debug eq "true") {
    print("Execute Command : $MV $newpkginfo $pkginfo\n");
}
    system("$MV $newpkginfo $pkginfo");
  }
#}


if ($table{$POSTPATCH} eq "yes")
{
if (debug eq "true") {
  print("Execute Command : perl postpatch.pl\n");
}
  system("${INSTALLROOT}${slash}lib${slash}perl${slash}perl ${patchDir}${slash}postpatch.pl");
}
print("Patch installed successfully.\n");
    
#This function takes in an array, a single patch
#If the array contains that single patch, it returns 1 or else 0
sub checkPresence
{
  chomp($_[0]);
  chomp($_[1]);
  my @patchList = split(",",$_[0]);
  my $found = 0;
  foreach $item (@patchList)
  {
    chomp($item);
    if ( ($item =~ m/^$_[1]@/) || ($item eq $_[1]) )
    {
      $found = 1;
      last;
    }
  }
  return $found;
}

#This function takes in 1st array, 2nd array of patches
#It returns a list(string) of patches in 2nd array which ARE in 1st array
sub checkPresenceOfMultipleItems
{
  my @patchList = split(",",$_[0]);
  my @obsoleteList = split(",",$_[1]);
  my $returnList = "";
  foreach $item (@obsoleteList)
  {
    chomp($item);
###    print "Searching for --$item-- ....\n";
    my $found = 0;
    foreach $item1 (@patchList)
    {
      chomp($item1);
###      print "     Found --$item1--\n";
      if ($item eq $item1)
      {
###        print "FOUND\n";
        $found = 1;
        last;
      }
    }
    if ($found == 1) {
      $returnList = "$returnList,$item";
    }
  }
###  print "PRES = -$returnList-";
  return $returnList;
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
    chomp($item);
###    print "Searching for --$item-- ....\n";
    my $found = 0;
    foreach $item1 (@patchList)
    {
      chomp($item1);
###      print "     Found --$item1--\n";
      if ( ($item eq $item1) || ($item1 =~ m/^$item@/) )
      {
###        print "FOUND\n";
        $found = 1;
        last;
      } 
    }
    if ($found == 0) {
      $returnList = "$returnList,$item\@1";
    }
  }
###  print "PRES = -$returnList-";
  return $returnList;
}
  
#This function takes in 1st array, 2nd array of patches
#It returns a list(string) of patches in 2nd array which are in 1st array
sub retPresentElements
{
  my @patchList = split(",",$_[0]);
  my @obsoleteList = split(",",$_[1]);
  my $returnList="";
  foreach $item (@patchList)
  {
    foreach $item1 (@obsoleteList)
    {
      if ( ($item eq $item1) || ($item =~ m/^$item1@/) )
      {
        $returnList = "$returnList,$item1";
      } 
    }
  }
  return $returnList;
}

#This function takes in 1st array, 2nd array
#For each element in 2nd array, it checks if that element is present in the 1st array. If it is, then it increments the count of that element
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
        $elements[1]++;
        $item1 = join("@",@elements);
      }
    }
  }
  $returnList = join(",",@patchList);
  return $returnList;
}


  
#foreach $KEY (sort keys %table)
#{
#  print "$table{$KEY}"
#}
