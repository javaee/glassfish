#!/usr/sbin/dtrace -Zs

dtrace:::BEGIN {
	printf("%-Y     Starting DTrace of fooblog", walltimestamp);
}

fooblog*::: {
	printf("fooblog TRIGGERED!!!");
}
dtrace:::END {
	printf("%-Y     Finished DTrace of fooblog.\n", walltimestamp);
}




