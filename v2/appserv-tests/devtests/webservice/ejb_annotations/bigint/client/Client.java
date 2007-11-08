package client;

import javax.xml.ws.WebServiceRef;
import java.math.BigInteger;
import endpoint.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://localhost:8080/CustomerManagerService/CustomerManager?WSDL")
        static CustomerManagerService service;

        public static void main(String[] args) {
            stat.addDescription("webservices-bigint");
            Client client = new Client();
            client.doTest(args);
            stat.printSummary("webservices-bigint");
       }

       public void doTest(String[] args) {
            try {
                CustomerManager port = service.getCustomerManagerPort();
                Customer ret = port.createCustomer(new BigInteger("1212"), "vijay");
                boolean found = false;
                List<Object> retList = port.getCustomerList();
                Iterator it = retList.iterator();
                while(it.hasNext()) {
                    Customer c = (Customer)it.next();
                    String name = c.getName();
                    BigInteger bi = c.getBigInteger();
                    System.out.println("Name -> " + name +
                        "; BigInt = " + bi.intValue());
                    if(("vijay".equals(name)) && (bi.intValue() == 1212)) {
                        found=true;
                    }
                }
                if(!found) {
                    System.out.println("Entity not persisted as expected");
                    stat.addStatus("ejb-bigint-test", stat.FAIL);
                } else {
                    port.removeCustomer("vijay");
                    stat.addStatus("ejb-bigint-test", stat.PASS);
                }
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("ejb-bigint-test", stat.FAIL);
            }
       }
}

