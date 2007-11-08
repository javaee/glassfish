#!/usr/bin/perl

#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#


#this script modifies the contents of CLI manpages
#ARGV[0] = base file
#ARGV[1] = output directory
#-prepend asadmin- to refentry, refentrytitle and index enties
#-change manvolnumm from 1 and 1M to 1as

$manpage_file = $ARGV[0];
#get the filename
$file_name = $manpage_file;
@arr_file = split(/\//, $manpage_file);
$file_name = pop(@arr_file);
$file_name =~ s/(.*\.\d+)[A-Za-z]?$/$1/g;
$prefix = "asadmin-";
$vol_suffix = "AS";
$output_file = $ARGV[1]."/asadmin-".$file_name.lc($vol_suffix);

if (-f $manpage_file)
{
    open (File1, "<$manpage_file") || die ("cannot open $manpage_file");
    open (File2, ">$output_file") || die ("cannot open $output_file");

    while (<File1>) 
    {
        if (m/(\<refentry +id= *")(.*)(-\d)[A-Za-z]?/gi) {
           $command_name = $2;
           my $lc_vol_suffix = lc($vol_suffix);
           $_ = "$1$prefix$2$3$lc_vol_suffix$'";
        }

        if (m/<refnamediv>/gi) {
           $line = $_;
           while ($line !~ m/<\/refnamediv>/gi) {
              $line = $line.<File1>;
              $line =~ s/\n//m;
           }
           $line =~ s/(<indexterm><primary sortas=")$command_name(">)$command_name(.*<\/indexterm><indexterm>.*<\/indexterm>)/$1$prefix$command_name$2$prefix$command_name$3$1$command_name$2$command_name$3/gi;
           $line =~s/(<refname>)$command_name(<\/refname>)/$1$prefix$command_name$2$1$command_name$2/gi;
           $_ = $line;
        }
       
        #s/(<primary sortas=")$command_name(">)$command_name/$1$prefix$command_name$2$prefix$command_name/g;

        s/(<manvolnum> *\d+)[A-Za-z]?/$1$vol_suffix/gi;
        s/(<refentrytitle> *)/$1$prefix/gi;
        s/(<link linkend=")([A-Za-z\-]*\d+)[A-Za-z]?(">)/$1$prefix$2$vol_suffix$3/gi;
        #print "$_";
        print File2 $_;
    }
    close(File1);
    close(File2);
}
