package com.sun.s1asdev.ejb.ejb30.interceptors.session.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.interceptors.session.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-interceptors-inheritance");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-inheritanceID");
    }

    public Client (String[] args) {
    }

    private static @EJB Sful sful;

    private static @EJB Sful sful0;
    private static @EJB Sful sful1;
    private static @EJB Sful sful2;
    private static @EJB Sful sful3;
    private static @EJB Sful sful4;
    private static @EJB Sful sful5;
    private static @EJB Sful sful6;
    private static @EJB Sful sful7;
    private static @EJB Sful sful8;
    private static @EJB Sful sful9;

    public void doTest() {
	doTest1();
	doTest2();
	doTest3();
	doTest4();
	doTest6();
	/**	doTest7_8(); **/ // enable when we have passivation
	doTest9();
	doTest10();
    }

    private void doTest1() {
        try {

            System.out.println("invoking stateful");
            sful.hello();
	    System.out.println("+++++++++++++++++++++++++++++++++++++++");
	    System.out.println("+++++ InterceptorCallCount: " + sful.getCount());
	    System.out.println("+++++++++++++++++++++++++++++++++++++++");
            stat.addStatus("local test1" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test1" , stat.FAIL);
        }
    }

    private void doTest2() {
        try {
            sful.throwAppException("XYZ");
            stat.addStatus("local test2" , stat.FAIL);
        } catch (AppException appEx) {
		System.out.println("Got expected AppException: " + appEx);
        	stat.addStatus("local test2" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test2" , stat.FAIL);
        }
    }

    private void doTest3() {
        try {
            String result = sful.computeMid(4, 10);
	    System.out.println("[Test3]: Got: " + result);
            stat.addStatus("local test3" , stat.PASS);
        } catch (SwapArgumentsException swapEx) {
	    System.out.println("Got unexpected Exception: " + swapEx);
            stat.addStatus("local test3" , stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test3" , stat.FAIL);
        }
    }

    private void doTest4() {
        try {
            String result = sful.computeMid(23, 10);
	    System.out.println("[Test4]: Got: " + result
			    + " INSTEAD of SwapArgumentsException");
            stat.addStatus("local test4" , stat.FAIL);
        } catch (SwapArgumentsException swapEx) {
	    System.out.println("Got expected Exception: " + swapEx);
            stat.addStatus("local test4" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test4" , stat.FAIL);
        }
    }

    private void doTest6() {
        try {
            sful.eatException();
            stat.addStatus("local test6" , stat.PASS);
        } catch(Exception e) {
	    System.out.println("Got unexpected Exception: " + e);
            e.printStackTrace();
            stat.addStatus("local test6" , stat.FAIL);
        }
    }

    private void doTest7_8() {
        try {
	    int sz = 20;
        Context ctx = new InitialContext();
	    sful.resetLifecycleCallbackCounters();
	    Sful[] sfuls = new Sful[sz];
	    for (int i=0; i<sz; i++) {
    		sfuls[i] = (Sful) ctx.lookup("com.sun.s1asdev.ejb.ejb30.interceptors.session.Sful");
    		int prevIndex = (i ==0) ? 0 : i-1;
    		System.out.println("Created sful["+i+"]: " + sfuls[i] + " ==> "
    		        + sfuls[prevIndex].equals(sfuls[i]));
		    sfuls[i].setID(i);
            sfuls[i].getCount();
	    }
	    sleepFor(10);
	    for (int i=0; i<sz; i++) {
	        sfuls[i].getCount();
	    }
	    int passivationCount = sful.getPrePassivateCallbackCount();
	    int activationCount = sful.getPostActivateCallbackCount();

        boolean status = (passivationCount > 0) && (activationCount> 0);
        System.out.println("passivation: " + passivationCount + "; "
		    + "activation: " + activationCount);
        stat.addStatus("local test7" ,
		    (status == true) ? stat.PASS : stat.FAIL);

        boolean stateRestored = true;
        for (int i=0; i<sz; i++) {
            boolean ok = sfuls[i].isStateRestored();
            System.out.println("stateRestored[" + i + "]:" + ok);
            stateRestored = ok && status;
        }
        stat.addStatus("local test8" ,
                (stateRestored == true) ? stat.PASS : stat.FAIL);

        } catch(Exception e) {
            System.out.println("Got unexpected Exception: " + e);
                e.printStackTrace();
                stat.addStatus("local test7" , stat.FAIL);
        }
    }

    public void doTest9() {
		try {
			sful.isInterceptorCallCounOK();
			sful.isInterceptorCallCounOK();
			sful.isInterceptorCallCounOK();
			sful.isInterceptorCallCounOK();
			String resultStr = sful.isInterceptorCallCounOK();
			boolean result = "true true true true".equals(resultStr);
			stat.addStatus("local test9 "  + resultStr ,
		                (result) ? stat.PASS : stat.FAIL);
		} catch (Exception ex) {
			stat.addStatus("local test9" , stat.FAIL);
		}
	}

    public void doTest10() {
		try {
			String resultStr = sful.isPostConstructCallCounOK();
			boolean result = "true true true true".equals(resultStr);
			stat.addStatus("local test10 "  + resultStr ,
		                (result) ? stat.PASS : stat.FAIL);
		} catch (Exception ex) {
			stat.addStatus("local test10" , stat.FAIL);
		}
	}


    private static void sleepFor(int seconds) {
	while (seconds-- > 0) {
	    try {
		System.out.println("" + seconds + " left...");
		Thread.currentThread().sleep(1000);
	    } catch (InterruptedException inEx) {
	    }
	}
    }

}

