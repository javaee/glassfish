package ObjConf;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# An ObjConf object represents the data in the file passed in
# objects is an array of objects in the order they were in the file
# names is a hash indexed by object name
# ppaths is a hash indexed by the ppaths
# directives is an array of extra-object information
sub new {
    my	$header = shift;
    my	$self = {};
    my	$curObj = undef;
    my	$nextLine;

    $self->{'file'} = shift;

    open( FILE, $self->{'file'} ) || ( $@ = $!, return undef );
    $nextLine = <FILE>;
    while ( $nextLine ) {
	$_ = $nextLine;
	$nextLine = <FILE>;
	while ( $nextLine && $nextLine =~ /^\s/ ) {
            $_ .= $nextLine;
            $nextLine = <FILE>;
        }
	push( @{$self->{'source'}}, $_ );
	next if ( m'^(#|$)' );	# skip comments and blank lines
	if ( /^<Object\s+/i ) {
	    if ( defined( $curObj ) ) {
		$@ = "recursive Object definition in $self->{'file'}, line $.";
		return undef;
	    } else {
		$curObj = new ConfObject( $', $#{$self->{'source'}} );
		return undef unless $curObj;
	    }
	} elsif ( defined( $curObj ) ) {
	    if ( m'^</Object>'i ) {
		push( @{$self->{'objects'}}, $curObj );
		if ( $curObj->{'type'} eq 'name' ) {
		    $self->{'names'}->{$curObj->{'name'}} = $curObj;
		} elsif ( $curObj->{'type'} eq 'ppath' ) {
		    $self->{'ppaths'}->{$curObj->{'ppath'}} = $curObj;
		} else {
		    $@ = "Unknown object type: $curObj->{'type'}";
		    return undef;
		}
		undef( $curObj );
	    } else {
		$curObj->sourceLine( $_, $#{$self->{'source'}} )
		    || return undef;
	    }
	} else {
	    chomp( $_ );
	    push( @{$self->{'directives'}},
		  new ConfDirective( $_, $#{$self->{'source'}} ) );
	}
    }
    close( FILE );
    bless $self;
}

sub write {
    my	$self = shift;
    my	$backupPolicy = shift;
    my	$i;

    &main::makeBackup( $self->{'file'}, $backupPolicy ) if $backupPolicy;
    open( FILE, ">$self->{'file'}" ) || ( $@ = $!, return undef );
    for ( $i = 0 ; $i < scalar( @{$self->{'source'}} ) ; ++$i ) {
	print FILE $self->{'source'}[$i] unless $self->{'deletedSource'}->{$i};
    }
    close( FILE ) || ( $@ = $! );
}

sub removeDirective {
    my	$self = shift;
    my	$object = shift;
    my	$directive = shift;

    $self->{'deletedSource'}->{$directive->{'sourceIndex'}} = 1;
    $object->remove( $directive );
}

package ConfDirective;

# conf directive has a type, name, and params
sub new {
    my	$header = shift;
    my	$line = shift;
    my	$self = {};
    my	@params;
    my	$param;
    my	$name;
    my	$value;

    $self->{'sourceIndex'} = shift;
    $self->{'client'} = shift;
    $line =~ /\s+/;
    $self->{'type'} = $`;
    $line = $';
    $self->{'params'} = {};
    while ( $param = &nextExp( \$line ) ) {
	( $name, $value ) = split( /\s*=\s*/, $param );
	$value =~ s/^"//;	# Trim "s;
	$value =~ s/"$//;	# Trim "s;
	$name = "\L$name";
	if ( $name =~ /^fn$/i ) {
	    $self->{'name'} = $value;
	} else {
	    $self->{'params'}->{$name} = $value;
	}
    }
    bless $self;
}

sub nextExp {
    my	$string = shift;	# reference, so we can hack it
    my	$result;

    return undef if $$string =~ /^$/;
    if ( $$string =~ /\s+/ ) {
	my	$space = $&;

	$result = $`;
	$$string = $';
	while ( $result =~ m'^\w+\s*=\s*"' &&
	        substr( $result, length( $result ) - 1, 1 ) ne '"' ) {
	    $result .= $space;
	    if ( $$string =~ /\s+/ ) {
		$result .= $`;
		$space = $&;
		$$string = $';
	    } else {
		$result .= $$string;
		$$string = '';
	    }
	}
    } else {	# last token
	$result = $$string;
	$$string = '';
    }
    return $result;
}

package ConfObject;

sub new {
    my	$header = shift;
    my	$id = shift;
    my	$self = {};

    $self->{'sourceIndex'} = shift;
    chomp( $id );
    if ( $id =~ /^name\s*=\s*/i ) {	# named object
	$self->{'type'} = 'name';
	( $id = $' ) =~ s/^"//;	# "; Clean off quotes
	$id =~ s/"?\s*>\s*$//;	# "; Clean off quotes and >
	$self->{'name'} = $id;
    } elsif ( $id =~ /^ppath\s*=\s*/i ) {
	$self->{'type'} = 'ppath';
	( $id = $' ) =~ s/^"//;	# "; Clean off quotes
	$id =~ s/"?\s*>\s*$//;	# "; Clean off quotes and >
	$self->{'ppath'} = $id;
    } else {
	$@ = "Syntax error in Object definition: $id";
	return undef;
    }
    bless $self;
}

sub sourceLine {
    my	$self = shift;
    my	$line = shift;
    my	$sourceIndex = shift;

    chomp( $line );
    if ( $line =~ /<client\s*/i ) {	# new Client
	if ( $self->{'curClient'} ) {
	    $@ = "Recursive Client: $.";
	    return undef;
	} else {
	    ( $self->{'curClient'} = $' ) =~ s/\s*>\s*$//;
	}
    } elsif ( $line =~ m'</client>'i ) {
	delete $self->{'curClient'};
    } else {
	push( @{$self->{'directives'}},
	      new ConfDirective( $line, $sourceIndex, $self->{'curClient'} ) );
    }
    1;
}

sub remove {
    my	$self = shift;
    my	$directive = shift;
    my	$i;

    for ( $i = 0 ; $i < scalar( @{$self->{'directives'}} ) ; ++$i ) {
	if ( $self->{'directives'}->[$i] == $directive ) {
	    splice( @{$self->{'directives'}}, $i, 1 );
	    last;
	}
    }
}

1;
