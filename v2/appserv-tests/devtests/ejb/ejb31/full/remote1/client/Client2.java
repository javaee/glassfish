package com.acme;



import java.util.Properties;
import javax.naming.InitialContext;

public class Client2 {

    public static void main(String args[]) {

	String host;
	 String port;

	try {
	    Properties p = new Properties();
	    if( args.length > 0 ) {
		host = args[0];
		p.put("org.omg.CORBA.ORBInitialHost", host);
	    }

	    if( args.length > 1 ) {
		port = args[1];
		p.put("org.omg.CORBA.ORBInitialPort", port);
	    }

	    InitialContext ic = new InitialContext(p);
	    Hello h = (Hello) ic.lookup("HH#com.acme.Hello");
	    h.hello();


	} catch(Exception e) {
	    e.printStackTrace();
	}

    }


}
