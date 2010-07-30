package client;

import javax.ejb.EJB;
import ejb.Hello; 

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @EJB(mappedName="ejb.Hello")
	static Hello hello;

        public static void main(String[] args) {
	    stat.addDescription("localremoteejbwebservice-issue");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("localremoteejbwebservice-issue");
       }

       public void doTest(String[] args) {
            try {
                for (int i=0;i<10;i++) {
                    String ret = hello.invoke("Hello Tester !");
		    if(ret.indexOf("Hello Tester") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("localremoteejbwebservice-issue", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("localremoteejbwebservice-issue", stat.FAIL);
            }
       }
}

