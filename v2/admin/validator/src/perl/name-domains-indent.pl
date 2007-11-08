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

    $indent_size = 3;

    if ($#ARGV != 2) {
      printUsage();
      exit;
    }
    $xml_file = shift(@ARGV);
    $out_file = shift(@ARGV);
    $indent_size = shift(@ARGV);
    
    print "input xml file: ", $xml_file , "\n";
    print "output file: ", $out_file, "\n";
    print "indent: ", $indent_size, "\n";

    $super_indent = "                                                                              ";

    open(RNG, $xml_file) || warn "Can't open file $xml_file: $!\n";
    open(OUTP, ">".$out_file) || warn "Can't open file $out_file: $!\n";

  
    while (<RNG>) {
        $nesting_level = 0;
        if((/^<name\-lists /) || (/^<\/name\-lists/)) {
           # just to distinguish from name list 
        } else {
            if((/^<name\-list /) || (/^<\/name\-list/)) {
                $nesting_level = 1;
            } else {
                if(/^<forms\-from/) {
                    $nesting_level = 2
                }
                else {
                    if(/^<referenced\-by /) {
                       $nesting_level = 3
                    }
                }    
            }
        }
        
        if(/^<name\-list /) {
                print OUTP "\n<!--**************************************************************-->\n";
        }
        if($nesting_level!=0) {
            print OUTP substr($super_indent,1,$indent_size*$nesting_level);
        }
        s/^\s*(<name\-list.*) (full\-name=\".*\") (scope=\".*\")(.*)$/$1\n           $2\n           $3$4/;
        print OUTP $_ ;
    }
    exit;
   
sub printUsage
{
      print "usage perl mergeRngWithNAmeDomains domain.rng names.xml out.xml";
   print ("Set idents in xml file.\n");
   print ("Usage: \n");
   print ("       generateNameDomains.pl <input xml file> <output file> <size-of-indent>");
}