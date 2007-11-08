package Cgi;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

sub parse {
    my	$line = shift;
    my	$assign;
    my	$var;
    my	$value;

    chomp( $line );
    if ( $raw ) {
	$raw .= '&' . $line;
    } else {
	$raw = $line;
    }
    foreach $assign ( split( /&/, $line ) ) {
	( $var, $value ) = split( /=/, $assign );
	$value = &decode( $value );
	$var = &decode( $var );
	$main::cgiVars{$var} = $value;
    }
}

sub decode {
    my	$string = shift;

    $string =~ s/\+/ /g;
    $string =~ s/%(\w\w)/chr(hex($1))/ge;

    return $string;
}

sub main::freakOut {
    my	$i;

    for ( $i = 0 ; $i < scalar( @_ ) ; ++$i ) {
	$_[$i] =~ s/'/\\'/g;
    }
    print "<SCRIPT language=JAVASCRIPT>\n";
    print "alert('@_');\n";
    print "location='index';\n</SCRIPT>\n";
    exit 0;
}

&parse( $ENV{'QUERY_STRING'} );
if ( $ENV{'CONTENT_LENGTH'} ) {
    while ( <STDIN> ) {
	&parse( $_ );
    }
}

1;
