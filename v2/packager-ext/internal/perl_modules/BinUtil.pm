#!perl
package BinUtil;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# Generic useful functions.

$isNT = -d '\\';
# if NT, make regexp case insensitive
$checkCase = $isNT ? '(?i)' : '';


sub getPkgList {
    my $svrRoot = shift;
    my %pkgList = ();
    my $line;
    my @pkgs;

    open(PKGLIST, "$svrRoot/setup/installed.pkg") or die
	"<FONT COLOR=\"red\">[Error]</FONT> Cannot open $svrRoot/setup/installed.pkg: $!\n";
    $line = <PKGLIST>;
    @pkgs = split /:/, $line;
    $pkgList{'core'} = shift @pkgs;
    $pkgList{'jre'} = shift @pkgs;
    $pkgList{'jdk'} = shift @pkgs;
    $pkgList{'java'} = shift @pkgs;
    $pkgList{'ssjsdb'} = shift @pkgs;
    $pkgList{'webpub'} = shift @pkgs;
    $pkgList{'snmp'} = shift @pkgs;
    $pkgList{'wai'} = shift @pkgs;
    $pkgList{'ssjs'} = shift @pkgs;
    close PKGLIST;
    return %pkgList;
}

sub getVersion
{
    my $svrRoot = shift;
    my @fields;
    my $major_version;
    my $minor_version;
    my $sp_version = 0;

    open(PKGLIST, "$svrRoot/setup/installed.pkg") or die
    "<FONT COLOR=\"red\">[Error]</FONT> Cannot open $svrRoot/setup/installed.pkg
: $!\n";
    while (<PKGLIST>)
    {
        chomp;
        @fields = split(/=/, $_);
        if (/^Major Version/)
        {
            ($major_version = $fields[1]) =~ s/\s+//g;
        }
        elsif (/^Minor Version/)
        {
            ($minor_version = $fields[1]) =~ s/\s+//g;
        }
        elsif (/^SP Version/)
        {
            ($sp_version = $fields[1]) =~ s/\s+//g;
        }
    }

    my $version = "$major_version.$minor_version";
    $version .= "SP$sp_version" if ($sp_version > 0);
    return $version;
}

sub copyFile {
    my $src = shift;
    my $dest = shift;

    if ($src eq $dest) {
	print "Source and destination are the same!\n";
	return;
    }
    if (-f $src) {
	if ($isNT) {
	    grep { s@/@\\@g } $src;
	    grep { s@/@\\@g } $dest;
	    `copy $src $dest`;
            if (!(-f $dest)) {
                local *SRC;
                local *DEST;
                my @data = ();
                open(SRC, $src) or 
                    die "<FONT COLOR=\"red\">[Error]</FONT> Cannot open $src: $!\n";
                binmode(SRC);
                open(DEST, "> $dest") or
                    die "<FONT COLOR=\"red\">[Error]</FONT> Cannot open $dest: $!\n";
                binmode(DEST);
                @data = <SRC>;
                print DEST @data;
                close(SRC);
                close(DEST);
            }
	} else {
	    `cp $src $dest`;
	}
    } else {
	print "File not found: $src\n";
    }
}


# This should work like a "cp -r" on unix.
# However, if you're copying over an existing directory,
# you need to specify the directory to get around some NT idiocy.
#
# eg.  To copy from /tmp/foo to /tmp/bar where /tmp/bar is new:
#        copyDir("/tmp/foo", "/tmp/bar");
#      To copy from /tmp/foo to /tmp/bar where /tmp/bar already exists:
#        copyDir("/tmp/foo", "/tmp", "bar");
#      Of course, the latter also works if /tmp/bar is new
sub copyDir {
    my $src = shift;
    my $dest = shift;
    my $dir = shift;
    my $entry;
    local *SRCDIR;

    if ($src eq $dest) {
	print "Source and destination are the same!\n";
	return;
    }
    if ($isNT) {
	if ($dir) {
	    $dest .= "/$dir";
	}
	if (!-d $dest) {
	    mkdir("$dest", 0755);
	}
    }
    if (-d $src) {
	if ($isNT) {
	    opendir(SRCDIR, "$src") or 
		die "<FONT COLOR=\"red\">[Error]</FONT> Cannot open $src: $!\n";
	    while ($entry = readdir(SRCDIR)) {
		next if ($entry =~ /^\.$/ || $entry =~ /^\.\.$/);
		if (-d "$src/$entry") {
		    mkdir("$dest/$entry", 0755);
		    copyDir("$src/$entry", "$dest/$entry");
		} else {
		    copyFile("$src/$entry", "$dest/$entry");
		}
	    }
	    closedir(SRCDIR);
	} else {
	    `cp -r $src $dest`;
	}
    } else {
	print "Directory not found: $src\n";
    }
}


sub recursiveChown {
    my $uid = shift;
    my $gid = shift;
    my @files = @_;
    my $name;
    my $entry;
    local *DIR;

    foreach $name (@files) {
	chown($uid, $gid, $name);
	if (-d $name) {
	    opendir(DIR, "$name") or
		die "<FONT COLOR=\"red\">[Error]</FONT> Cannot open $name: $!\n";
	    while ($entry = readdir(DIR)) {
		next if ($entry =~ /^\.$/ || $entry =~ /^\.\.$/);
		recursiveChown($uid, $gid, "$name/$entry");
	    }
	    closedir(DIR);
	}
    }
}


sub makeScript {
    my $param;
    my @files = ();
    local *IN;
    local *OUT;

    foreach $param ( @_ ) {
	if ( $param =~ /=/ ) {
	    $vars{$`} = $';
	} else {
	    push( @files, $param );
	}
    }
    die "Usage: $0 [var=val...] srcFile destFile\n" unless scalar( @files ) == 2;

    open( IN, $files[0] ) or die "Can't read $files[0]: $!\n";
    open( OUT, ">$files[1]" ) or die "Can't create $files[1]: $!\n";
    while ( <IN> ) {
	while ( /%%%([^%]+)%%%/ ) {
	    print OUT $`;
	    if ( exists $vars{$1} ) {
		print OUT $vars{$1};
	    } else {
		print OUT $&;
	    }
	    $_ = $';
	}
	print OUT $_;
    }
    close( IN );
    close( OUT );
    # make executable
    chmod( 0755, $files[1] );
}


sub backupFile {
    my $newHome = shift;
    my $rootDir;
    my $sname = shift;
    my @files = @_;
    my $bkFileName = "$newHome/conf_bk/backups.conf";
    local *CONF;
    my @fileSect = ();
    my @tagSect = ();
    my $new = 1;
    my $line;
    my @fields = ();
    my $fileName;
    my $nickName;
    my @fileNames = ();
    my $x;
    my $y;
    my $version;
    my $ctime;
    my $curTag;

    if (-e $bkFileName) {
	$new = 0;
	open(CONF, "$newHome/conf_bk/backups.conf") or 
	    die "<FONT COLOR=\"red\">[Error]</FONT> Cannot read $bkFileName: $!\n";
	while (<CONF>) {
	    last if (/\.acl:httpacl/);
	}
	push(@fileSect, $_);
	while (<CONF>) {
	    last if ($_ eq "\n");
	    push(@fileSect, $_);
	}
	@tagSect = <CONF>;
	$curTag = $tagSect[$#tagSect];
	close CONF;
	$x = 0;
	$y = 0;
	while (($x = index($newHome, "/", $x)) > -1) {
	    $y = $x;
	    $x++;
	}
	$rootDir = substr($newHome, 0, $y);
	$x = 0;
	foreach $line (@fileSect) {
	    (@fields) = split /:/, $line;
	    foreach $fileName (@files) {
		if ($fields[0] eq $fileName) {
		    chomp ${fields[4]};
		    $nickName = &_getNickname("$newHome/conf_bk", $fileName);
		    BinUtil::copyFile("$rootDir/${fields[4]}", "$rootDir/${fields[1]}");
		    BinUtil::copyFile("$rootDir/${fields[1]}", "$newHome/conf_bk/$nickName");
		    $ctime = (stat("$rootDir/${fields[1]}"))[10];
		    $version = $fields[2] + 1;
		    $fileSect[$x] = "$fileName:$fields[1]:$version:$ctime:$fields[4]\n";
		    $curTag =~ s/(.*:$fileName\/)(\d+)(:.*)/$1$version$3/;
		    push(@tagSect, "$ctime".substr($curTag, 9));
		} else {
		    push(@fileNames, $fileName);
		}
	    }
	    @files = @fileNames;
	    @fileNames = ();
	    $x++;
	}
    }
    open(CONF, "> $newHome/conf_bk/backups.conf") or 
	die "<FONT COLOR=\"red\">[Error]</FONT> Cannot create $newHome/conf_bk/backups.conf: $!\n";
    print CONF "backups.conf:Version 4.0\n";
    print CONF "\n";
    if ($new) {
	# add ACL files, which have a different syntax
	$ctime = (stat("$newHome/../httpacl/genwork.$sname.acl"))[10];
	print CONF "$sname.acl:httpacl/genwork.$sname.acl:1:$ctime:httpacl/generated.$sname.acl\n";
	$curTag = "$ctime:$sname.acl/1:";
	push(@tagSect, "$curTag:\n");
    } else {
	print CONF @fileSect;
	chop($curTag);
	chop($curTag);
    }
    # add new files
    foreach $file (@files) {
        BinUtil::copyFile("$newHome/config/$file", "$newHome/conf_bk/$file");
        BinUtil::copyFile("$newHome/config/$file", "$newHome/conf_bk/$file.1");
	$ctime = (stat("$newHome/conf_bk/$file.1"))[10];
	print CONF "$file:$sname/conf_bk/$file:1:$ctime:$sname/config/$file\n";
	$curTag .= "$file/1:";
	push(@tagSect, "$ctime".substr($curTag, 9).":\n");
    }
    # print tag section
    print CONF "\n";
    print CONF @tagSect;
    close(CONF);
}

# internal method
sub _getNickname {
    my	$dir = shift;
    my	$file = shift;
    my	$index;

    $file =~ s@.*/@@;	# trim path info
    $file =~ s/:/_/;	# reaganify
    if (-e "$dir/$file") {
	$index = 1;
	while (-e "$dir/$file.$index") {
	    ++$index;
	}
	$file .= ".$index";
    }
    return $file;
}



# Usage: fixpaths <filename> <# of paths to fix>
#                 <list of old paths> <list of new paths>
#                 <list of allowed paths>
#                 
# The list of allowed paths will be translated regardless of
# whether or not they exist.
sub fixPaths {
    my $src = shift;
    my $numXs = shift;
    my @srcPaths = splice( @_, 0, $numXs );
    my @destPaths = splice( @_, 0, $numXs );
    my @allowedEmpty = @_;
    my $x;
    my @data = ();
    $errCount = 0;
    $errDir = "";

    open(SRC, $src) or die "<FONT COLOR=\"red\">[Error]</FONT> Cannot read $src: $!\n";
    grep { s/\\/\//g } @srcPaths if $isNT;
    while ($line = <SRC>) {
	for ($x = 0; $x < $numXs; $x++) {
	    if ($line =~ /$checkCase$srcPaths[$x]/) {
		$line = _xlatePaths($line, $srcPaths[$x], $destPaths[$x], @allowedEmpty);
	    }
	}
	push(@data, $line);
    }
    close(SRC);
    open(DEST, "> $src") or die "<FONT COLOR=\"red\">[Error]</FONT> Cannot edit $src: $!\n";
    print DEST @data;
    close(DEST);
}

# internal method for fixPaths
sub _xlatePaths {
    my $line = shift;
    my @otherParams = @_;
    my $srcPath = shift;
    my $destPath = shift;
    my @allowedEmpty = @_;
    my $pre;
    my $match;
    my $post;
    my $allowed;
    my $path;
    my $fullPath;

    $line =~ /(.*)($checkCase$srcPath)(.*)/;
    $pre = $1;
    $match = $2;
    $post = $3;

    $path = $match . $post;
    # only want the path -- strip out everything else
    if ($isNT) {
	$path =~ s/[;\"\'\`\s>\*].*//s;
    } else {
	$path =~ s/[:\"\'\`\s>\*].*//s;
    }
    ($fullPath = $path) =~ s/$checkCase$srcPath/$destPath/;
    if (-e $fullPath) {
	if ($pre =~ /$checkCase$srcPath/) {
	    $pre = &_xlatePaths($pre, @otherParams);
	    if (substr($pre, length($pre)-1, length($pre)) eq "\n") {
		$pre = substr($pre, 0, length($pre)-1);
	    }
	}
	return "$pre$destPath$post\n";
    } else {
	$isAllowed = 0;
	foreach $allowed (@allowedEmpty) {
	    if ($post =~ /$allowed/) {
		$isAllowed = 1;
	    }
	}
	if ($isAllowed) {
	    # xlate the name anyway
	    if ($pre =~ /$checkCase$srcPath/) {
		$pre = &_xlatePaths($pre, @otherParams);
		if (substr($pre, length($pre)-1, length($pre)) eq "\n") {
		    $pre = substr($pre, 0, length($pre)-1);
		}
	    }
	    return "$pre$destPath$post\n";
	} else {
	    if ($errCount == 0) {
		print "&nbsp;&nbsp;<FONT COLOR=\"red\">Warning:</FONT>  The following paths do not exist in the new server and must be dealt with manually:\n";
	    }
	    if ($errDir ne $path) {
		print "&nbsp;&nbsp;&nbsp;&nbsp;<CODE>$path</CODE>\n";
		$errDir = $path;
	    }
	    $errCount++;
	    if ($pre =~ /$checkCase$srcPath/) {
		$pre = &_xlatePaths($pre, @otherParams);
		if (substr($pre, length($pre)-1, length($pre)) eq "\n") {
		    $pre = substr($pre, 0, length($pre)-1);
		}
		$line = "$pre$match$post\n";
	    }
	}
    }
    return $line;
}


sub uname {
    local (@CommandLine) = @_;

    local($getall) = 0;
    local($getproc) = 0;
    local($getosrel) = 0;
    local($getosname) = 0;
    local($getosver) = 0;

    while ($_ = $CommandLine[0]) {
      PARSE_SWITCH: {
	  if (/^-a\b/i) {
              # show all information
	      $getall=1;
	      shift(@CommandLine);
	      last PARSE_SWITCH
	  }
	  if (/^-n\b/i) {
	      # show node name
	      $getnodename=1;
	      shift(@CommandLine);
	      last PARSE_SWITCH
	  }
	  if (/^-p\b/i) {
	      # show processor
	      $getproc=1;
	      shift(@CommandLine);
	      last PARSE_SWITCH
	  }
	  if (/^-r\b/i) {
	      # show os release
	      $getosrel=1;
	      shift(@CommandLine);
	      last PARSE_SWITCH
	  }
	  if (/^-s\b/i) {
	      # show os name
	      $getosname=1;
	      shift(@CommandLine);
	      last PARSE_SWITCH
	  }
	  if (/^-v\b/i) {
	      # show os version
	      $getosver=1;
	      shift(@CommandLine);
	      last PARSE_SWITCH
	  }
	  print "   ERROR: Unknown parameter: $_\n";
	  shift(@CommandLine);
      }
    }

    chomp(local($os) = `uname -s`);
    local($nodename) = "";
    local($proc) = "";
    local($osrel) = "";
    local($osname) = "";
    local($osver) = "";
    local($osrel1) = "";
    local($osrel2) = "";
    local($nodename1) = "";
    local($retval) = "";
    local($ret) = "";

    if ($os eq "Windows_NT") {
	chomp($nodename = `uname -n`); 
	chomp($proc = lc(`uname -m`));
	if ($proc =~ /^[0-9]86.*/) {
	    $proc = "i386";
	} else {
	    $proc = "?";
	}
	chomp($osrel1 = `uname -r`);
	chomp($osrel2 = `uname -v`);
	$osrel = $osrel1.".".$osrel2;
	$osname = "WINNT";
	$osver = "???";
    }
    if ($os eq "WINNT") {
	chomp($nodename = `uname -n`);
	chomp($proc = lc(`uname -p`));
	chomp($osrel = `uname -r`);
	$osname = "WINNT";
	chomp($osver = `uname -v`);
    } 

    if ($os eq "SunOS") {
	chomp($nodename = `uname -n`);
	chomp($proc = `uname -p`);
	chomp($osrel = `uname -r`);
	$osname = $os;
	chomp($osver = `uname -v`);
    }
    if ($os eq "IRIX" || $os eq "IRIX64") {
	chomp($nodename = `uname -n`);
	chomp($proc = `uname -p`);
	chomp($osrel = `uname -r`);
	$osname = "IRIX";
	chomp($osver = `uname -v`);
    }

    if ($os eq "HP-UX") {
	chomp($nodename = `uname -n`);
#        $proc = "hppa1.1";
	chomp($proc = `uname -m`);
	chomp($osrel = `uname -r`);
	$osname = $os;
	chomp($osver = `uname -v`);
    }

    if ($os eq "OSF1") {
	chomp($nodename1 = `uname -n`);
	($nodename) = ($nodename1 =~ /(\w+)\..*/);
	chomp($proc = `uname -m`);
	chomp($osrel = `uname -r`);
	$osname = $os;
	chomp($osver = `uname -v`);
    }

    if ($os eq "AIX") {
	chomp($nodename = `uname -n`);
	$proc = "rs6000";
	chomp($osrel1 = `uname -v`);
	chomp($osrel2 = `uname -r`);
	$osrel = $osrel1.".".$osrel2;
	$osname = $os;
	$osver = "???";
    }

    if ($getall) {
	$getosname = 1;
	$getnodename = 1;
	$getosrel = 1;
	$getosver = 1;
	$getproc = 1;
    }

    $retval = "";
    $retval = $retval.($getosname ?	$osname :		"");
    $retval = $retval.($getnodename ?	" ".$nodename :		"");
    $retval = $retval.($getosrel ?	" ".$osrel :		"");
    $retval = $retval.($getosver ?	" ".$osver :		"");
    $retval = $retval.($getproc ?	" ".$proc :		"");

    if ($retval eq "") {
	$retval = $nodename;
    }

($ret) = ($retval =~ /\s*(.*)/);

    return "$ret";
}


# required to complete package:
1;
