# Script to remove the appropriate number of blank lines before and
# after headers and footers.

# Note that the original catman(1) process throws some blank lines
# away when it adds headers and footers. We attempt to put these back 
# when we can - so far the only processing is for bulleted items

# A header in the input file matches the pattern /^HEADER DELETE$/
# It is preceded by 2 blank lines, and followed by 3 blank lines

# A footer in the input file matches the pattern /^FOOTER DELETE$/
# It is preceded by 3 blank lines, and followed by 4 blank lines

# A bullet in the input file matches the pattern /^        o /
# The line before must be a blank line. This blank is to be added if
# its not otherwise there.

BEGIN {
	blankline = 0;
}

# blank line? count it, then skip outputting it

/^$/ {
	blankline++;
    next;
}

# header - delete preceding blanks, the line itself, and following blanks
# skip further processing
/^HEADER DELETE$/ {
	#for (blankline -= 2; blankline > 0; blankline --){
	#	print "";
	#}
    if (blankline > 0) {
        print "";
    }
	blankline = 0;
	for (i = 0; i < 3; i++){
		getline;
	}
	next;
}

# footer - delete preceding blanks, the line itself, and following blanks
# skip further processing
/^FOOTER DELETE$/ {
	#for (blankline -= 3; blankline > 0; blankline--) {
	#	print "";
	#}
    if (blankline > 0) {
        print "";
    }
	blankline = 0;
	for (i = 0; i < 4; i++){
		getline;
	}
	next;
}

# bullet item - occaisonally the header/footer processing deletes blank
# space before bulleted items - this operation ensures that each
# bullet item has at least one blank line before it.
/        o / {
	if (0 == blankline){
		blankline = 1;
	}
}

	
# nonblank - print preceding blanks (if any), and the line itself
{
#	while (blankline > 0){
#		print "";
#		blankline--;
#	}
    #only print blankline onces to eliminate extra blank lines
    if (blankline > 0) {
        print "";
        blankline = 0;
    }
	print $0;
}

