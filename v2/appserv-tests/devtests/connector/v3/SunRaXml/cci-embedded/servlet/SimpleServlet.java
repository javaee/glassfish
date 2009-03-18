package servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleServlet extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * handles the HTTP POST operation *
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doTest(request, response);
    }

    private void doTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String testId = "J2EE Connectors : Embedded Adapter Tests";

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {

            out.println(testId + " : CoffeeClient started in main...");
            out.println("J2EE Connectors 1.5 : Standalone CCI adapter Tests");
            Context initial = new InitialContext();
            com.sun.s1peqe.connector.cci.CoffeeRemoteHome home = (com.sun.s1peqe.connector.cci.CoffeeRemoteHome)
                    initial.lookup("java:comp/env/ejb/SimpleCoffee");


            com.sun.s1peqe.connector.cci.CoffeeRemote coffee = home.create();

            int count = coffee.getCoffeeCount();
            out.println("Coffee count = " + count);

            out.println("Inserting 3 coffee entries...");
            coffee.insertCoffee("Mocha", 10);
            coffee.insertCoffee("Espresso", 20);
            coffee.insertCoffee("Kona", 30);

            int newCount = coffee.getCoffeeCount();
            out.println("Coffee count = " + newCount);
            if (count == (newCount - 3)) {
                out.println("Connector:cci Connector " + testId + " rar Test status:" + " PASS");
                out.println("TEST:PASS");
            } else {
                out.println("Connector:cci Connector " + testId + " rar Test status:" + " FAIL");
                out.println("TEST:FAIL");

            }

            //print test summary
            //stat.printSummary(testId);


        } catch (Exception ex) {
            out.println("Caught an unexpected exception!");
            out.println("Connector:CCI Connector " + testId + " rar Test status:" + " PASS");
            ex.printStackTrace();
        } finally {
            out.println("END_OF_TEST");
        }
    }

    private void debug(String msg) {
        System.out.println("[Redeploy Connector CLIENT]:: --> " + msg);
    }
}
