# delete leading blank lines

BEGIN { anonblank = 0; }

/^$/ { if (0 == anonblank) { next; } }

{ anonblank = 1; print $0; }
