package ProdInfo;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

%knownNicknames = ( 'httpd' => 'Netscape FastTrack Server',
		    'https' => 'Netscape Enterprise Server' );

sub new {
    my	$header = shift;
    my	$file = shift;
    my	$nickname = shift;
    my	$self = {};
    my	$var;
    my	$val;

    if ( $file ) {
	open( FILE, $file ) || ( $@ = $!, return undef );
	while ( <FILE> ) {
	    next if /^\s*$/ || /^#/;
	    if ( /^\[(.*)\]/ ) {
		last if $self->{'when'};
		$self->{'when'} = $1;
		next;
	    }
	    s/\s*$//;	# trim trailing whitespace
	    ( $var ) = /^([^\s=:\?]+)\s*/;	# get name and any whitespace
	    $_ = $';
	    $var = "\L$var";
	    if ( /^:\s*/ ) {	# list var
		my	$element;

		foreach $element ( split( /,/, $' ) ) {
		    $self->{'lists'}{$var}{$element} = 1;
		}
	    } elsif ( /^\?\s*/ ) {	# boolean
		$self->{'bools'}{$var} = $' =~ /^true$/i;
	    } else {		# string var
		s/^=\s*//;	# trim optional = sign
		$self->{'vars'}{$var} = $_;
	    }
	}
	close( FILE );
    } else {	# no file, so wing it
	$self->{'vars'}{'serverversion'} = '2.0';
	$self->{'vars'}{'servervendor'} =
	    "Netscape Communications Corporation";
	$self->{'vars'}{'servername'} = $knownNicknames{$nickname};
    }

    bless $self;
}

sub value {
    my	$self = shift;
    my	$var = shift;

    return $self->{'vars'}{"\L$var"};
}

sub when {
    my	$self = shift;

    return $self->{'when'};
}

sub canDo {
    my	$self = shift;
    my	$var = shift;
    my	$element = shift;

    if ( $element ) {	# list
	return $self->{'lists'}{"\L$var"}{$element};
    } else {
	return $self->{'bools'}{"\L$var"};
    }
}

1;
