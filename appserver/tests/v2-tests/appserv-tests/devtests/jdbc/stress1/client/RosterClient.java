package com.sun.s1asdev.jdbc.stress1.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.stress1.ejb.*;

public class RosterClient {
    
    // Invoke with args: #clients, #minutes
    public static void main(String[] args) throws Exception {

        int numClients = 10; //10 clients 
	int numMinutes = 10; // 10 minutes
	if (args.length == 2) {
            numClients = Integer.parseInt( args[0] );
	    numMinutes = Integer.parseInt( args[1] );
	}
        
        RosterClient client = new RosterClient();
	System.out.println("-=-=-=-=-=-=- Running for "+ numMinutes
	    +" minutes -=-=-=-=-=-=-=-");
	client.runTest( numClients, numMinutes );
    }	
    
    public void runTest(int numClients, int numMinutes ) throws Exception {

        RosterClientThread[] threads = new RosterClientThread[ numClients ];
	for (int i = 0 ; i < numClients; i++ ) {
	    try {
	        threads[i] = new RosterClientThread(i);
	    } catch( Exception e) {
	        System.out.println("Could not create thread : " + i);
		e.printStackTrace();
	    }
	    threads[i].start();
	}
        
	//Let it all run for few hours and then kill all threads
	System.out.println("Waiting for threads to do work now...");
	try {
	    //numMinutes min * 60 sec * 1000 = millis
	    Thread.sleep( numMinutes * 60 * 1000 );
	} catch(InterruptedException ie) {
	    ie.printStackTrace();
	}

	System.out.println("Interrupting threads now...");
	for (int i = 0 ; i < numClients; i++ ) {
	    threads[i].runFlag = false;
	}
    }	
}
