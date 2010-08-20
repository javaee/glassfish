The files in this directory are generated using schema2beans http://schema2beans.netbeans.org/

If the dtd file changes and you need to regenerate the sources, please use following command. 

Please point to a valid path to schema2beabs.jar and  schema2beansdev.jar before executing the command

java \
-classpath \
"schema2beans.jar;schema2beansdev.jar" \
org.netbeans.modules.schema2beansdev.GenBeans \
-f \
"../sun-cmp-mapping_1_2.dtd" \
-mdd \
"..//sun-cmp-mapping_1_2.mdd" \
-p \
com.sun.jdo.api.persistence.mapping.ejb.beans \
-r \
. \
-throw \
-dtd \
-validate

