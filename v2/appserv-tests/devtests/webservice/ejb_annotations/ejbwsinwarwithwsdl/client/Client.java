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
	    stat.addDescription("ejbwsinwarwithwsdl");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("ejbwsinwarwithwsdl");
       }

       public void doTest(String[] args) {
            try {
                HelloService service = new HelloService();
                Hello client = service.getHelloPort();
                String result = client.sayHello("Duke");
                if(result.equals("WebSvcTest-Hello Duke" )) {
                   stat.addStatus("ejbwsinwarwithwsdl", stat.PASS);
                }  else {
                    stat.addStatus("ejbwsinwarwithwsdl", stat.FAIL);
                }

                /*
                URL serviceInfo = new URL (args[0]);
                URLConnection con = serviceInfo.openConnection();
               BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); 

                String inputLine;
                int index=0; 
                while ((inputLine = in.readLine()) != null) {
                   if ((index= inputLine.indexOf("href="))>0){

                      String url = inputLine.substring(index+1);  
                      if (url.indexOf("http:")>0) {
                         stat.addStatus("ejbwsinwarwithwsdl", stat.PASS);
                      } 
                      System.out.println(inputLine);
                   }
                }
                  in.close();
                stat.addStatus("ejbwsinwarwithwsdl", stat.FAIL);
                */
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus(args[0], stat.FAIL);
            }
       }
}

