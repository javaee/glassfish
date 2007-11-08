package ES40lnf;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# Helper functions for Enterprise 4.0 Admin UI Look-n-Feel

# defaults
$dfltFontSize = 'SIZE="3"';
$dfltFontFace = 'FACE="PrimaSans BT, Verdana, sans-serif"';
$dfltFont = "<FONT $dfltFontSize $dfltFontFace>";

sub print_html {
    my $title = shift(@_);
    
    print "Content-type: text/html\n\n";
    print "<HTML>\n<HEAD><TITLE>$title</TITLE>\n</HEAD>\n\n";
}

sub print_body {

    print '<BODY BGCOLOR="#ffffff" LINK="#666699" VLINK="#666699" ALINK="#333366">', "\n";
    print "$dfltFont\n";
}

sub print_header {
    my $title = shift(@_);

    print '<TABLE BORDER="0" BGCOLOR="#9999cc" CELLPADDING="5" CELLSPACING="0" WIDTH="100%">', "\n";
    print "  <TR>\n";
    print '   <TD NOWRAP><BR><FONT SIZE="3" ', $dfltFontFace, ' COLOR="white">';
    print "<B>$title</B></FONT></TD>\n";
    print "  </TR>\n";
    print "</TABLE>\n";
    print "<P>\n";
}

sub print_subheader {
    my $title = shift(@_);

    print '<TABLE BORDER="0" BGCOLOR="#9999cc" CELLPADDING="5" CELLSPACING="0" WIDTH="100%">', "\n";
    print "  <TR>\n";
    print '   <TD NOWRAP><FONT SIZE="3" ', $dfltFontFace, ' COLOR="white">';
    print "$title</FONT></TD>\n";
    print "  </TR>\n";
    print "</TABLE>\n";
    print "<P>\n";
}

sub print_OpenTD {

	print "   <TD>$dfltFont";
}

sub print_CloseTD {

	print "</FONT></TD>\n";
}

# required to complete package:
1;
