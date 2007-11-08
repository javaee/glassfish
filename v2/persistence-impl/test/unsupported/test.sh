export S1AS_HOME=H:/olsen/sjsas9/as9
export CLASSPATH="./build/classes;../deployment/test/acme/build/enhanced;C:/Programs/Oracle/oracle10.1.0/Client_1/jdbc/lib/ojdbc14.jar;"
./run.sh $CLASSPATH com.sun.persistence.test.AcmeTest1 "jdbc:oracle:thin:@keroppi:1521:ora92" "oracle.jdbc.driver.OracleDriver" "mz" "mz"