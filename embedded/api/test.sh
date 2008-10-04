#!/bin/sh -x

XYZ="-cp target/test-classes:target/glassfish-embedded-api-10.0-SNAPSHOT-with-full-v3.jar org.glassfish.embed.Main"
export XYZ

java ${XYZ}



