/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
       
        try {
            System.out.println("START");
            stat.addDescription("fieldtest");

            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/FT2");
            fieldtest.A2Home a2home = (fieldtest.A2Home)PortableRemoteObject.narrow(objref, fieldtest.A2Home.class);

            fieldtest.A2 a2bean = a2home.create("A2");
            fieldtest.A2PK pk2 = (fieldtest.A2PK)a2bean.getPrimaryKey();
            System.out.println("CREATED 2.x WITH PK: " + pk2.id1 + " " + pk2.iddate);

            System.out.println("CREATED 2.x: " + a2bean.getName());
            if (!a2bean.getName().equals("A2")) throw new RuntimeException("Wrong result after CREATE 2.x");

            System.out.println("CREATED 2.x: " + a2bean.getMyDate());
            if(a2bean.getMyDate().getYear() != new java.util.Date().getYear())
                throw new RuntimeException("Wrong result after CREATE 2.x");

            System.out.println("CREATED 2.x: " + a2bean.getSqlDate());
            if(a2bean.getSqlDate() != null) throw new RuntimeException("Wrong result after CREATE 2.x");

            System.out.println("CREATED 2.x: " + a2bean.getBlb()[0] + " " + a2bean.getBlb()[1]);
            if(a2bean.getBlb().length != 2) throw new RuntimeException("Wrong result after CREATE 2.x");

            System.out.println("CREATED 2.x: " + a2bean.getList());
            if (a2bean.getList().size() != 0) throw new RuntimeException("Wrong result after CREATE 2.x");

            a2bean.update();

            System.out.println("UPDATED 2.x: " + a2bean.getName());
            if (!a2bean.getName().equals("A2")) throw new RuntimeException("Wrong result after UPDATE 2.x");

            System.out.println("UPDATED 2.x: " + a2bean.getMyDate());
            if(a2bean.getMyDate().getYear() != new java.util.Date(0).getYear())
                throw new RuntimeException("Wrong result after UPDATE 2.x");

            System.out.println("UPDATED 2.x: " + a2bean.getSqlDate());
            if(a2bean.getSqlDate().getYear() != new java.util.Date().getYear()) 
                throw new RuntimeException("Wrong result after UPDATE 2.x");

            System.out.println("UPDATED 2.x: " + a2bean.getBlb()[0] + " " + a2bean.getBlb()[1]);
            if(a2bean.getBlb().length != 2) throw new RuntimeException("Wrong result after UPDATE 2.x");
 
            System.out.println("UPDATED 2.x: " + a2bean.getList());
            if(a2bean.getList().size() != 1 || !a2bean.getList().get(0).equals("A2"))
                throw new RuntimeException("Wrong result after UPDATE 2.x");

            objref = initial.lookup("java:comp/env/ejb/FT1");
            fieldtest.A1Home a1home = (fieldtest.A1Home)PortableRemoteObject.narrow(objref, fieldtest.A1Home.class);

            fieldtest.A1 a1bean = a1home.create("A1");
            fieldtest.A1PK pk1 = (fieldtest.A1PK)a1bean.getPrimaryKey();
            System.out.println("CREATED 1.1 WITH PK: " + pk1.id1 + " " + pk1.iddate);
            System.out.println("CREATED 1.1: " + a1bean.getName());
            if (!a1bean.getName().equals("A1")) throw new RuntimeException("Wrong result after CREATE 1.x");

            System.out.println("CREATED 1.1: " + a1bean.getMyDate());
            if(a1bean.getMyDate().getYear() != new java.util.Date().getYear())
                throw new RuntimeException("Wrong result after CREATE 1.x");

            System.out.println("CREATED 1.1: " + a1bean.getSqlDate());
            if(a1bean.getSqlDate() != null) throw new RuntimeException("Wrong result after CREATE 1.x");

            System.out.println("CREATED 1.1: " + a1bean.getBlb()[0] + " " + a1bean.getBlb()[1]);
            if(a1bean.getBlb().length != 2) throw new RuntimeException("Wrong result after CREATE 1.x");

            System.out.println("CREATED 1.1: " + a1bean.getList());
            if (a1bean.getList().size() != 0) throw new RuntimeException("Wrong result after CREATE 1.x");

            a1bean.update();

            System.out.println("UPDATED 1.1: " + a1bean.getName());
            if (!a1bean.getName().equals("A1")) throw new RuntimeException("Wrong result after UPDATE 1.x");

            System.out.println("UPDATED 1.1: " + a1bean.getMyDate());
            if(a1bean.getMyDate().getYear() != new java.util.Date(0).getYear())
                throw new RuntimeException("Wrong result after UPDATE 1.x");

            System.out.println("UPDATED 1.1: " + a1bean.getSqlDate());
            if(a1bean.getSqlDate().getYear() != new java.util.Date().getYear()) 
                throw new RuntimeException("Wrong result after UPDATE 1.x");

            System.out.println("UPDATED 1.1: " + a1bean.getBlb()[0] + " " + a1bean.getBlb()[1]);
            if(a1bean.getBlb().length != 2) throw new RuntimeException("Wrong result after UPDATE 1.x");
 
            System.out.println("UPDATED 1.1: " + a1bean.getList());
            if(a1bean.getList().size() != 1 || !a1bean.getList().get(0).equals("A1"))
                throw new RuntimeException("Wrong result after UPDATE 1.x");

            a1bean = a1home.findByPrimaryKey(pk1);
            System.out.println("FOUND 1.1: " + a1bean.getName());

	    stat.addStatus("ejbclient fieldtest", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient fieldtest", stat.FAIL);

        }
          stat.printSummary("fieldtest");
    }
    
}
