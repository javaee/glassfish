package servlet;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.s1asdev.jdbc.maxconnectionusage.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.maxconnectionusage.ejb.SimpleBMP;

public class SimpleServlet extends HttpServlet {


    public void doGet (HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      doPost(request, response);
    }

    /** handles the HTTP POST operation **/
    public void doPost (HttpServletRequest request,HttpServletResponse response)
          throws ServletException, IOException {
        doTest(request, response);
    }

    public void doTest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        PrintWriter out = response.getWriter();

        try{
        System.out.println("Max connection usage test");

        InitialContext ic = new InitialContext();
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome) ic.lookup("java:comp/env/ejb/SimpleBMPEJB");
        out.println("Running Max connection usage test ");

        SimpleBMP simpleBMP = simpleBMPHome.create();

        if (simpleBMP.test1(false)) {
            System.out.println(" Max Connection Usage -  (local-TxNotSupported): PASS");
	    out.println("TEST:PASS");
        } else {
            System.out.println(" Max Connection Usage -  (local-TxNotSupported): FAIL");
            out.println("TEST:FAIL");
        }

        boolean useXA = false;
        boolean status = connectionSharingTest(simpleBMP, useXA, 21112);
        if (status) {
            System.out.println(" Max Connection Usage - (local-Tx-Sharing) : PASS");
	    out.println("TEST:PASS");
        } else {
            System.out.println(" Max Connection Usage - (local-Tx-Sharing) : FAIL");
            out.println("TEST:FAIL");
        }


        if (simpleBMP.test1(true)) {
            System.out.println(" Max Connection Usage -  (XA-TxNotSupported) : PASS");
	    out.println("TEST:PASS");
        } else {
            System.out.println(" Max Connection Usage -  (XA-TxNotSupported) : FAIL");
            out.println("TEST:FAIL");
        }

	} catch(NamingException ne) {
	    ne.printStackTrace();
	} catch(CreateException e) {
	    e.printStackTrace();
        } finally {
	    out.println("END_OF_TEST");
	    out.flush();
	}
    }

    private boolean connectionSharingTest(SimpleBMP simpleBMP, boolean useXA, int value) {//throws RemoteException {
        String results[] = new String[10];
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                results[i] = simpleBMP.test2(useXA, value);
            } else {
                results[i] = simpleBMP.test3((i / 2) + 1, useXA, value);
            }
        }
        boolean status = true;
        String result = results[0];
        for (int i = 0; i < results.length; i++) {
            if (!results[i].equalsIgnoreCase(result)) {
                System.out.println("Result 0 : " + result);
                System.out.println("Result " + i + " : " + results[i]);
                status = false;
                break;
            }
        }

        String result2 = simpleBMP.test2(useXA, value);

        if (!result2.equalsIgnoreCase(result) && status ) {
            status = true;
        } else {
            System.out.println("Marking status as false during verification");
            System.out.println("is XA : " + useXA);
            System.out.println("Value : " + value);
            System.out.println("Result 1 : " + result);
            System.out.println("Result 2 : " + result2);
            status = false;
        }
        return status;
    }
}
