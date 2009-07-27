package client;

import java.net.*;
import java.io.*;
import com.example.hello.HelloService;
import com.example.hello.Hello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");


        public static void main(String[] args) {
	    stat.addDescription("ejbwebservicesinwar");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("ejbwebservicesinwar");
       }

       public void doTest(String[] args) {
            try {

                URL serviceInfo = new URL (args[0]);
                URLConnection con = serviceInfo.openConnection();
               BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); 

                String inputLine;
                int index=0; 
                while ((inputLine = in.readLine()) != null) {
                   if ((index= inputLine.indexOf("href="))>0){

                      String url = inputLine.substring(index+1);  
                      if (url.indexOf("http:")>0) {
                         stat.addStatus("ejbwebservicesinwar", stat.PASS);
                      } 
                      System.out.println(inputLine);
                   }
                }
                  in.close();
                stat.addStatus("ejbwebservicesinwar", stat.FAIL);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

