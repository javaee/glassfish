#!/usr/bin/perl

#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#


#================================================================
#								=
# This script will install/uninstall the Solaris	 	=
# packages of Sun ONE Application Server. The packages names   	=
# are defined in bundled_pkg.txt. Configuration params should 	=
# be put in conf.txt.						=
#================================================================

use Getopt::Long;
use English;
use FileHandle;
require "ctime.pl";

&GetOptions("install!", "uninstall!", "help!");
$usage = "
Usage :
	$0 [options]
	Command-line options and arguments:
	-install | -uninstall | -help

	-help       	Print this usage message.
	-install 	Install the solaris bundled build specified in conf.txt.
	-uninstall 	Uninstall the solaris bundled app server.

";
die "\nAction is not defined \n $usage" unless defined($opt_install || $opt_uninstall || $opt_help);
if ($opt_help)
{
                print "$usage";
                exit 1;
}

# Definitions
use Env qw(HOST);
$rm = "rm -rf";
$s_HOME = `pwd`;
chomp $s_HOME;
$pkgDir = "$s_HOME/pkg";
$pkgaddLog = "/tmp/pkgaddLog.$$";
$asadmin = "/usr/appserver/bin/asadmin";
$confRoot = "/usr/appserver/config";
$sampleDir = "/usr/appserver/samples";

# Reading info.
sub read_inf($) {
    my ($file) = @_;
    open F, "< $file" or die "$!, reading from $file";
    # m/^\s*\[[^\[\]]+\]\s*$/;
    my @vals = map { m/^\s*([^= \t]+)\s*[= \t]\s*(.+)/ ? (lc $1,$2) : () } <F>;
    close F;
    return @vals;
}


$inf_file = "conf.txt";
my %NVP = read_inf $inf_file;
$domainRoot = $NVP{domainroot};
$adminUser = $NVP{adminuser};
$adminPort = $NVP{adminport};
$adminPasswd = $NVP{adminpasswd};
$instanceport = $NVP{serverport};

# main.
print "\n";
print "__________________________________________________________\n";
print "Start processing at    ", &ctime(time), "\n";

chdir "$s_HOME";
$osver = `uname -r`;
chomp $osver;
	open(bp, "bundled_pkg.txt");

@runList = <bp>;
print "@runList\n";
@revList = reverse(@runList);

if ($opt_uninstall)
{
  $retCode = system("$asadmin stop-domain");
  
  for (my($i) = 0; $i <= $#revList; $i++)
  {
        chomp($revList[$i]);
        $retCode = system("pkgrm -a $s_HOME/admin.conf -n $revList[$i]");
  }
  $retCode = system("$rm $confRoot");
  $retCode = system("$rm $domainRoot");
  $retCode = system("$rm /usr/appserver");
}

if ($opt_install)
{
  chdir "$pkgDir";
  for (my($i) = 0; $i <= $#runList; $i++)
  {
        chomp($runList[$i]);
	$retCode = system("pkgadd -a $s_HOME/admin.conf -d . $runList[$i] >> $pkgaddLog");
#	$retCode = system("pkgadd -d . $runList[$i]");
	if ($retCode ne "0")
	{ 
	    print "Failed to add package $runList[$i].\n";
	}
  }

  rename("$sampleDir/common.properties", "$sampleDir/common.properties.orig");
  open(F1, "<$sampleDir/common.properties.orig");
  open(F2, ">$sampleDir/common.properties") || die ("common.properties cannot be opened\n");
  while (<F1>)
  {
     if (m/^com.sun.aas.pointbaseRoot=/) {s/=.*$/=\/usr\/appserver\/pointbase/;}
     if (m/^com.sun.aas.webServicesLib=/) {s/=.*$/=\/usr\/appserver\/lib/;}
     if (m/^com.sun.aas.imqLib=/) {s/=.*$/=\/usr\/share\/lib\/imq/;}
     if (m/^com.sun.aas.installRoot=/) {s/=.*$/=\/usr\/appserver/;}
     if (m/^com.sun.aas.javaRoot=/) {s/=.*$/=\/usr\/j2se/;}
	 if (m/^#admin.password=/) {s/#admin.password=.*$/admin.password=$adminPasswd/;}
     if (m/^admin.host=/) {s/=.*$/=localhost/;}
     if (m/^appserver.instance.port=/) {s/=.*$/=$instanceport/;}
     if (m/^admin.user=/) {s/=.*$/=$adminUser/;}
     if (m/^admin.port=/) {s/=.*$/=$adminPort/;}
	 if (m/^appserver.instance=/) {s/=.*$/=server/;}
     if (m/^pointbase.port=/) {s/=.*$/=9092/;}
     print F2 ("$_");
  }
  close(F1);
  close(F2);

  $SUN_acc = "$domainRoot/domain1/config/sun-acc.xml";
  rename("$confRoot/asenv.conf", "$confRoot/asenv.conf.orig");
  open(F3, "<$confRoot/asenv.conf.orig");
  open(F4, ">$confRoot/asenv.conf") || die ("asenv.conf cannot be opened\n");
  while (<F3>)
  {
     if (m/^AS_ACC_CONFIG=/) {s/=.*$/=$SUN_acc/;}
     if (m/^AS_DEF_DOMAINS_PATH=/) {s/=.*$/=$domainRoot/;}
     print F4 ("$_");
  }
  close(F3);
  close(F4);
  $retCode = system("$asadmin create-domain --domaindir $domainRoot --adminport $adminPort --adminuser $adminUser --adminpassword $adminPasswd --instanceport $instanceport domain1");
  if ($retCode ne "0")
  {
	print "Failed to create domain.\n";
	exit 1;
  }

  $retCode = system("$asadmin start-domain --user $adminUser --password $adminPasswd");
  if ($retCode ne "0")
  {
	print "Failed to start the domain1.\n";
	exit 1;
  }

  $docRoot = "$domainRoot/domain1/docroot";
  $retCode = system("cd $sampleDir; find . -name docs > docslist;");
  $retCode = system("./xcp $docRoot/samples $sampleDir/docslist $sampleDir");
  $retCode = system("cp -r $sampleDir/index.html $docRoot/samples/");
  $retCode = system("cp -r $sampleDir/indexSamplesDomain.html $docRoot/samples/");
  $retCode = system("rm $sampleDir/docslist");
  $retCode = system("cp $s_HOME/3RD-PARTY-LICENSE.txt /usr/appserver/.");
  $retCode = system("cp $s_HOME/LICENSE.txt /usr/appserver/.");
}


print "__________________________________________________________\n";
print "Finished processing at ", &ctime(time), "\n";  


