package com.sun.s1asdev.ejb.sfsb.stress.client;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSBHome;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSB;

import java.util.ArrayList;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressSFSBClient
    implements Runnable
{

    String	    name;
    StressSFSBHome  home;
    ArrayList	    list;
    int		    maxActiveCount;
    boolean	    success = true;
    int		    maxIter = 5;
    Thread	    thread;

    public StressSFSBClient(String name,
	    StressSFSBHome home, int maxActiveCount)
    {
	thread = new Thread(this, name);
	this.name = name;
	this.home = home;
	this.maxActiveCount = maxActiveCount;
	this.list = new ArrayList(maxActiveCount);
	thread.start();
    }

    public void run() {
	System.out.println("StressSFSBClient: " + name + " started....");
	try {
	    for (int i=0; i<maxActiveCount; i++) {
		list.add(home.create(name+"-"+i));
	    }
	    for (int count = 0; count < maxIter; count++) {
		for (int i=0; i<maxActiveCount; i++) {
		    StressSFSB sfsb = (StressSFSB) list.get(i);
		    sfsb.ping();
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    success = false;
	}
    }

}
