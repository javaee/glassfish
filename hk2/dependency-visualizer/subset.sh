#!/bin/bash
# script that generates the wiring dependency diagram for a subset of bundles in
# glassfish

# Sample usage: to generate "admin" wiring dependency diagram: invoke: ./subset admin. This generate admin.jpg
./generate.sh -i wires.xml -o $1.dot -m $1  && ./rundot.sh $1.dot $1.jpg
