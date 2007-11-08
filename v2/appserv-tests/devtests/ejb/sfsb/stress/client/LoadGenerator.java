package com.sun.s1asdev.ejb.sfsb.stress.client;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSBHome;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class LoadGenerator {
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");    
    private static String propsFileName = 
	"/export/home/s1as/cts/ws/appserv-tests/devtests/ejb/sfsb/stress/client/jndi.properties";

    private Context ctx;
    private StressSFSBHome home;

    public LoadGenerator(String[] args)
	throws Exception
    {
	String jndiName = args[0];
	ctx = getContext(args[1]);    

	Object ref = ctx.lookup(jndiName);
	this.home = (StressSFSBHome) 
	    PortableRemoteObject.narrow(ref, StressSFSBHome.class);
	System.out.println("LoadGenerator got home: " + home.getClass());
    }

    private InitialContext getContext(String propsFileName)
	throws Exception
    {
        InitialContext ic;

        if( propsFileName == null ) {
            ic = new InitialContext();
        } else {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFileName);
            props.load(fis);
            ic = new InitialContext(props);
        }

        return ic;
    }


    public void doTest() {
	for (int i=0; i<10; i++) {
	    System.out.println("Creating StressSFSBClient[" + i + "]");
	    String clientName = "client-"+i;
	    StressSFSBClient client = new StressSFSBClient(clientName,
		    home, 10);
	}
    }


    public static void main(String[] args) {
        try {
	    stat.addDescription("ejb-sfsb-stress");
	    LoadGenerator generator = new LoadGenerator(args);
	    generator.doTest();
	    stat.addStatus("ejb-sfsb-stress main", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejb-sfsb-stress main", stat.FAIL);
        }
    }

}
