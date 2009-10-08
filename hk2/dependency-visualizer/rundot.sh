#!/bin/bash
# runs the dot program against a DOT file to generate directed graphs

# $1 = dot file
# $2 = output JPG file

# sample usage: to create a jpeg image of foo.dot: ./rundot foo.dot foo.jpg

dot -Goverlap=false -Tjpg $1 -o $2 

#open the generated image
#eog $2 &
