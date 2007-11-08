package Magnus;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

sub new {
    my $header = shift;
    my $self = {};
    my $source;
    my $line;
    my $x;

    $self->{'sourceDeleted'} = {};
    $self->{'file'} = shift;
    if (!open(FILE, $self->{'file'})) {
	$@ = $!;
	return undef;
    }
    while (<FILE>) {
	$source = $_;
	while (/\\$/) {
	    # continuation
	    $line = <FILE>;
	    $_ = $` . $line;
	    $source .= $line;
	    # in case of EOF:
	    last if $_ eq $`;
	}
	push( @{$self->{'source'}}, $source );
	next if /^\s*$/;
	next if /^\#/;
	chomp($_);
	($var, $val) = /(\w+)\s+(.*)/;
	$var = "\L$var";
	if ($var eq 'init') {
	    # init directives are no longer supported in magnus.conf
	    $self->{'modified'} = 1;
	} else {
	    if ($self->{'vars'}->{$var}) {
		# for duplicate directives, we're going to rename them
		# directiveX where X is a number starting from 1
		$x = 1;
		while ($self->{'vars'}->{"$var$x"}) {
		    $x++;
		}
		$self->{'vars'}->{"$var$x"} = $val;
		$self->{'index'}->{"\L$var$x"} = $#{$self->{'source'}};
	    } else {
		$self->{'vars'}->{"$var"} = $val;
		$self->{'index'}->{"\L$var"} = $#{$self->{'source'}};
	    }
	}
    }
    close(FILE);
    bless $self;
}

sub set {
    my	$self = shift;
    my	$var = shift;
    my	$val = shift;
    my	$index = defined( $self->{'vars'}->{"\L$var"} ) ?
	$self->{'index'}->{"\L$var"} : scalar( @{$self->{'source'}} );

    $self->{'vars'}->{"\L$var"} = $val;
    $self->{'source'}->[$index] = "$var $val\n";
    $self->{'index'}->{"\L$var"} = $index;
    $self->{'modified'} = 1;
}

sub value {
    my $self = shift;
    my $var = shift;

    return $self->{'vars'}->{"\L$var"};
}

sub deleteVar {
    my	$self = shift;
    my	$var = shift;

    $self->{'sourceDeleted'}->{$self->{'index'}->{"\L$var"}} = 1;
    $self->{'modified'} = 1;
}

sub flush {
    my $self = shift;

    if ($self->{'modified'}) {
	my $i;

	open(FILE, ">$self->{'file'}") or die "Cannot write to $self->{'file'}: $!.\n";
	for ($i = 0 ; $i < scalar(@{$self->{'source'}}) ; ++$i) {
	    print FILE $self->{'source'}->[$i]
		unless $self->{'sourceDeleted'}->{$i};
	}
	close(FILE);
    }
    $self->{'modified'} = 0;
}

1;
