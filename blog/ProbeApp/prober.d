#!/usr/sbin/dtrace -Zs

dtrace:::BEGIN {
	printf("%-Y     Starting DTrace of fooblog", walltimestamp);
}

fooblog*::: {
argNumber0 = copyinstr(arg0);



	printf("fooblog TRIGGERED!!!%s, %d", argNumber0);
}
dtrace:::END {
	printf("%-Y     Finished DTrace of fooblog.\n", walltimestamp);
}




