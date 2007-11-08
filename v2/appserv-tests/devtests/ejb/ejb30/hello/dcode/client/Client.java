package com.sun.s1asdev.ejb.ejb30.hello.dcode.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.Handle;
import com.sun.s1asdev.ejb.ejb30.hello.dcode.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-dcode");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-dcodeID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB SfulHome sfulHome;
    
    private static final String HANDLE_FILE_NAME = "sfulhandle";

    private Sful[] sfuls = new Sful[20];

    public void doTest() {



        try {

            File handleFile = new File(HANDLE_FILE_NAME);

            if( handleFile.exists() ) {

                System.out.println("handle file already exists.  reconstituting stateful reference");

                FileInputStream fis = new FileInputStream(handleFile);
                
                ObjectInputStream ois = new ObjectInputStream(fis);

                for(int i = 0; i < sfuls.length; i++) {
                    
                    Handle sfulHandle = (Handle) ois.readObject();

                    sfuls[i] = (Sful) sfulHandle.getEJBObject();

                    System.out.println("invoking stateful" + i);

                    sfuls[i].hello();

                    System.out.println("successfully invoked stateful" + i);
                }

                ois.close();

                stat.addStatus("dcodewithhandles main", stat.PASS);

            } else {
                
                System.out.println("handle file doesn't exist.  Creating sful" +
                                   " bean");
                
                FileOutputStream fos = new FileOutputStream(handleFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                for(int i = 0; i < sfuls.length; i++) {

                    sfuls[i] = sfulHome.create();

                    System.out.println("invoking stateful" + i);
                    sfuls[i].hello();

                    Handle handle = sfuls[i].getHandle();
                    oos.writeObject(handle);

                }

                oos.close();

                System.out.println("test complete");
                
                stat.addStatus("dcodenohandles main", stat.PASS);

            }

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

