# Script that takes the catman(1) output and modifies it to really
# clean up the output man pages.

# Largely this means deleting unwanted lines, but we also replace the
# catman processing with our own processing, which cleans the man
# pages up even more.

# get rid of extraneous noise from catman
/^Building.*/d
/^sman.*/d
/^mandir.*/d
/.*: search the sections lexicographically$/d
/.*CVS.*/d

# next two lines convert the use of temp files into a pipeline. Don't
# know why catman doesn't do this itself!
/cd/s/>.*/|/
/ cat /s/^[^|][^|]*|//

# Replace catman's subsequent processing by a call to our own function
# (defined elsewhere)
s/col -x/clean_man_pages/
