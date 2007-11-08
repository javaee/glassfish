#!/usr/bin/perl

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


    if ($#ARGV != 2) {
      printUsage();
      exit;
    }
    $rng_file = shift(@ARGV);
    $names_file = shift(@ARGV);
    $out_file = shift(@ARGV);
    print "rng file: ", $rng_file , "\n";
    print "names description file: ", $names_file, "\n";
    print "output file: ", $out_file, "\n";
    open(RNG, $rng_file) || warn "Can't open file $rng_file: $!\n";
    open(NAMES, $names_file) || warn "Can't open file $names_file: $!\n";
    open(OUTP, ">".$out_file) || warn "Can't open file $out_file: $!\n";
    while (<RNG>) {
        if(/<rng:grammar/) {
            print OUTP "<foo>  \n";
        }
        print OUTP $_ ;
    }
    while (<NAMES>) {
        print OUTP $_ ;
    }
    print OUTP "</foo>  \n" ;
    exit;
   
    $user = /%REPLACE_BY_DB_USER%/;
    $pass = /%REPLACE_BY_DB_PASS%/;
    $sid  = /%REPLACE_BY_DB_SID%/;
#    $user = 'alexkrav';
#    $pass = 'alexkrav';
#    $sid  = 'bell';
    $db_prefix  = 'BX40_';


    # arguments
    $rule_name = "";
    $inher_crs = "";
    $dup_crs   = "";
    $dag_crs   = "";

    if(!($rule_name = shift(@ARGV)))
      {
         printUsage();
         exit;
      }
    if(!($inher_crs = shift(@ARGV)))
      {
         printUsage();
         exit;
      }
    if(($dup_crs = shift(@ARGV)))
      {
         $dag_crs = shift(@ARGV)
      }

    print ("Change rules CRSs\n");
    print ("Rule name: $rule_name\n");
    print ("New inheritance CRS: $inher_crs\n");
    if($dup_crs)
       {
          print ("New DUP CRS: $dup_crs\n");
       }
    if($dag_crs)
       {
          print ("New DAG CRS: $dag_crs\n");
       }
    print ("\n");

    open(TEMPFILE, ">tempsql.sql")||die "Can't open output file 'tempsql.sql': $!\n";


    print TEMPFILE ("set linesize 999\n");
    print TEMPFILE ("set pagesize 999\n");
    $stmt = "UPDATE  $db_prefix"."assignment set inher_crs=$inher_crs WHERE attr_id=(SELECT attr_id FROM  $db_prefix"."attrs WHERE ATTR_NAME= '$rule_name');";
    print TEMPFILE ("$stmt\n");
    if($dup_crs)
       {
         if($dag_crs)
            {
               $stmt = "UPDATE  $db_prefix"."attrs set dup_crs=$dup_crs,dag_crs=$dag_crs WHERE ATTR_NAME= '$rule_name';";
            }
         else
            {
               $stmt = "UPDATE  $db_prefix"."attrs set dup_crs=$dup_crs WHERE ATTR_NAME= '$rule_name';";
            }
         print TEMPFILE ("$stmt\n");
       }
    print TEMPFILE ("exit;\n");
    close(TEMPFILE);

    $logon = $user.'/'.$pass.'@'.$sid;
    $lines = `sqlplus -S $logon  \@tempsql.sql`;
print ("lines=$lines\n");

exit;

sub printUsage
{
      print "usage perl mergeRngWithNAmeDomains domain.rng names.xml out.xml";
   print ("Merges domain rng file with name-domains descriptions.\n");
   print ("Usage: \n");
   print ("       generateNameDomains.pl <domain rng file> <names domains descriptions> <output file>");
}