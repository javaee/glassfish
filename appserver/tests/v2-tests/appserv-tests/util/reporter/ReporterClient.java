package com.sun.ejte.ccl.reporter;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class ReporterClient{

    private static SimpleReporterAdapter stat = 
            new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]){
        if(args.length<1){
            usage();
        }
        echo(args[0]+" is the test name");

        String default_desc=args[0]+"_default_description";
        if(args.length>1 && !((args[1].trim()).equals(""))){
            echo(args[1]+" is the test description");
            default_desc = args[1];
        }
	int numTests = 1;
	if (args.length>=3) {
	    numTests = Integer.parseInt(args[2]);
	}


        echo("adding description...");
        stat.addDescription(default_desc);
        echo("adding status...");
	if (numTests==1) {
		 stat.addStatus(args[0], stat.DID_NOT_RUN);
	} else {
	     for (int i=0;i<numTests; i++) {
        	  stat.addStatus(args[0]+"-"+(i+1), stat.DID_NOT_RUN);
	     }
	}

        echo("printing summary...");
        stat.printSummary();
    }
    public static void usage(){
       String usg="Usage:"+
           "\tReporterClient <test name> [<test description>]"+
           "\tNote:Test description is not required but recommended"; 
       echo(usg);
    }
    public static void echo(String msg){
        System.out.println(msg);
    }
}
