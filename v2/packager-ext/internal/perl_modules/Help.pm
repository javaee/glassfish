package Help;

package main;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

sub helpJavaScriptForTopic {
    my	$topic = shift;
    my	$server = $ENV{'SERVER_NAMES'};
    my	$type;
    my	$tutor;

    $server =~ /-/;
    $type = $`;
    $tutor = "$ENV{'SERVER_URL'}/$server/admin/tutor";

    return "if ( top.helpwin ) { top.helpwin.focus(); " .
	"top.helpwin.infotopic.location='$tutor?!$topic';" .
	    "} else {" .
		"window.open('$tutor?$topic', 'infowin_$type', " .
		    "'resizable=1,width=500,height=500'); }";
}

sub helpJavaScript {
    my	$topic = $ENV{'PATH_INFO'};

    $topic =~ s@.*/@@;
    $topic =~ s@\?.*@@;

    return helpJavaScriptForTopic( $topic );
}

1;
