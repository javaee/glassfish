# Delete blank lines at end of file

BEGIN { blankline = 0; }

/^$/ {
	blankline++;
	result = getline;
	if (-1 == result) { # Error
		exit(result);
	}
	if (1 == result) { # !EOF
		if (0 == length($0)) { #blankline
			blankline++;
		} else { #non blankline
			while (blankline > 0){
				print "";
				blankline--;
			}
			print $0;
		}
	}
	next;
}

{
	#while (blankline > 0){
	#	print "";
	#	blankline--;
	#}
    # do not display more than one blank line at a time
    if (blankline > 0) {
        print "";
        blankline = 0;
        
    }
	
	print $0;
}

	

