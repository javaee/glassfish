use Socket;

package FSocket;
#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# Make a socket that has been bound to this host and port

$sockAddr = 'S n a4 x8';
$socketNumber = 0;
( $name, $aliases, $proto ) = getprotobyname( 'tcp' );

# new FSocket( hostname, port ) # port should be zero if outgoing
sub new {
    my $header = shift;
    my $port = shift;
    my $self = {};
    my $this;
    my $i;

    ( $name, $aliases, $port ) = getservbyname( $port, 'tcp' )
	unless $port =~ /^\d+$/;
    $this = pack( $sockAddr, &main::AF_INET, $port, "\0\0\0\0" );
    $self->{'socket'} = 'FSocket::S' . ++$socketNumber;
    socket( $self->{'socket'}, &main::PF_INET, &main::SOCK_STREAM, $proto ) ||
	die "socket: $!";
    for ( $i = 0 ; $i < 30 && ! bind( $self->{'socket'}, $this ) ; ++$i ) {
	warn "bind: $!";
	sleep 10;
    }
    die "Couldn't bind: $!" if $i == 30;
    listen( $self->{'socket'}, 5 ) || die "connect: $!" if ( $port );
    bless $self;
}

sub DESTROY {
    my $self = shift;

    close( $self->{'socket'} );
}

package Connection;

# InConnection and OutConnection supply the constructors
sub send {
    my $self = shift;
    my $data = shift;
    my $length = length( $data );

    syswrite( $self->{'socket'}, pack( 'L', $length ) . $data, $length + 4 );
}

sub get {
    local $self = shift;
    local $length;
    local $data;

    return ( 0, undef ) unless sysread( $self->{'socket'}, $data, 4 ) == 4;
    $length = sysread( $self->{'socket'}, $data, unpack( 'L', $data ) );
    ( $length, $data );		# return stuff
}

package OutConnection;

@ISA = ( 'Connection' );

sub new {
    my $header = shift;
    my $machine = shift;
    my $port = shift;
    my $self = {};
    my $socket = new FSocket( 0 );

    $self->{'fsocket'} = $socket;
    $self->{'socket'} = $socket->{'socket'};
    ( $name, $aliases, $type, $len, $thataddr ) = gethostbyname( $machine );
    $that = pack( $FSocket::sockAddr, &main::AF_INET, $port, $thataddr );
    connect( $socket->{'socket'}, $that ) || die "connect: $!";
    bless $self;
}

package InConnection;

@ISA = ( 'Connection' );

# An InConnection will wait for a someone to call us.

sub new {
    my $header = shift;
    my $socket = shift;
    my $self = {};

    $self->{'socket'} =  'FSocket::S' . ++$FSocket::socketNumber;
    $addr = accept( $self->{'socket'}, $socket->{'socket'} );
    bless $self;
}
1;
# Local Variables:
# mode: perl
# eval: (modify-syntax-entry ?' " " perl-mode-syntax-table)
# End:
