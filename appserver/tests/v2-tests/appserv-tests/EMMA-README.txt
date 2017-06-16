
 1. Make sure ant version is 1.8+ (and emma version must be 2.1.5320 or higher)
 
 2. Copy emma.jar under appserv-tests/lib to java.home/jre/lib/ext before testing

 3. Build a GlassFish distribution (as emma will instrument jar files in this distribution):
    mvn clean install

 4. In addition to APS_HOME and S1AS_HOME variables, the following 2 variables have to be exported:
      export GLASSFISH_ZIP_FILE=any_path/glassfish.zip (a glssfish distribution, used for restore)
      export GLASSFISH_SRC_DIR=source_path

 5. Each component must change its build.xml in order to :
    (1). include appserv-tests/config/emma.xml.
        +<!ENTITY emma SYSTEM "../../config/emma.xml">
        ......
        +    &emma;

    (2). define its own jar file set that is to be instrumented.
        The jar files can be found under ${env.S1AS_HOME}/modules
          <patternset id="emma.instrument.jar.files">
            <include name="**/*.jar"/>
          </patternset>

 6. Each component can also define filters in a file with name 'emma-filters.txt'
    Reference : http://emma.sourceforge.net/reference/ch02s06s02.html

 7. Usage : (Take ejb component for example)
      ant all -> ant enable.emma all emma.gen.coverage.report -Demma.enabled=true

 8. Browse the result in 'report' folder
