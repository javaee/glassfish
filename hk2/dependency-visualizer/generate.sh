#!/bin/bash

# calls DotGenerator with the provided arguments to generate the DOT file

#  sample usage: 
# To generate a DOT file for bundles matching foo: ./generate.sh -i wires.xml -o foo.dot -m foo
VERSION=1.0
java -cp target/hk2-dependency-visualizer-$VERSION-jar-with-dependencies.jar com.sun.enterprise.tools.visualizer.hk2.DotGenerator $*
