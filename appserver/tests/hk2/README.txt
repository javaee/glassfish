This is a set of integrations tests for hk2 and GlassFish.

They currently require two parameters to be specified:
glassfish.home= The directory where glassfish is installed
source.home= The directory where the glassfish source is located

To run the tests something like this line should be run:

mvn -Dglassfish.home=/scratch/jwells/bg/installs/gf/glassfish5/glassfish \
    -Dsource.home=/scratch/jwells/bg/all/main \
	clean install 2>&1 | tee t.out

UNDER CONSTRUCTION:  We would like to get rid of the need for source.home

