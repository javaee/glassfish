/*
 *  @(#)msgsperweek.java	1.5 01/09/06
 *
 * Copyright (c) 1998-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 */

import java.util.*;
import java.text.*;
import java.io.*;
import javax.mail.*;
import javax.mail.search.*;
import javax.mail.internet.*;

/**
 * Program that generates stats about new messages received per week 
 * by the javamail@Sun.COM mailing list. It tracks *new* messages only,
 * the rule being that the message has a single addressee: 
 * "javamail@Sun.COM". Also searches each message body for the "pop3" 
 * string and counts its occurrence.
 * 
 * Note that this program operates on the log file directly and thus all
 * access to the log file using this program must be through the same
 * IMAP server so that the flags are handled consistently.
 *
 * @author Max Spivak
 */

public class msgsperweek {

    static String protocol = "imap";
    static String host = "anybodys.sfbay";
    static String user = "javamail";
    static String password = "1javamail1";
    static String mbox =
	"/net/anybodys.sfbay/export6/javamail/logs/javamail.log";
    static String url = null;
    static boolean verbose = false;
    static boolean doPop3 = false;
    static Calendar cal = null;

    public static void main(String argv[]) {
	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-T")) {
		protocol = argv[++optind];
	    } else if (argv[optind].equals("-H")) {
		host = argv[++optind];
	    } else if (argv[optind].equals("-U")) {
		user = argv[++optind];
	    } else if (argv[optind].equals("-P")) {
		password = argv[++optind];
	    } else if (argv[optind].equals("-v")) {
		verbose = true;
	    } else if (argv[optind].equals("-f")) {
		mbox = argv[++optind];
	    } else if (argv[optind].equals("-L")) {
		url = argv[++optind];
	    } else if (argv[optind].equals("-p")) {
		doPop3 = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: msgperweek [-L url] [-T protocol] [-H host] [-U user] [-P password]\n"+
"\t[-f mailbox] [-v] [-p]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	System.out.println("msgsperweek: Generating stats, please wait...");
	
	cal = Calendar.getInstance();
	
        try {
	    // Get a Properties object
	    Properties props = System.getProperties();

	    // Get a Session object
	    Session session = Session.getDefaultInstance(props,
				    new TtyAuthenticator());

	    //if (verbose)
	    //session.setDebug(true);

	    // Get a Store object
	    Store store = null;
	    if (url != null) {
		URLName urln = new URLName(url);
		store = session.getStore(urln);
		store.connect();
	    } else {
		if (protocol != null)		
		    store = session.getStore(protocol);
		else
		    store = session.getStore();

		// Connect
		if (host != null || user != null || password != null)
		    store.connect(host, user, password);
		else
		    store.connect();
	    }
	    

	    // Open the Folder

	    Folder folder = store.getDefaultFolder();
	    if (folder == null) {
	        System.out.println("No default folder");
	        System.exit(1);
	    }

	    folder = folder.getFolder(mbox);
	    if (folder == null) {
	        System.out.println("Invalid folder");
	        System.exit(1);
	    }

	    folder.open(Folder.READ_ONLY);
	    int totalMessages = folder.getMessageCount();

	    if (totalMessages == 0) {
		System.out.println("Empty folder");
		folder.close(false);
		store.close();
		System.exit(1);
	    }

	    if (verbose) {
		int newMessages = folder.getNewMessageCount();
		pv("Total messages = " + totalMessages);
		pv("New messages = " + newMessages);
		pv("-------------------------------");
	    }

	    // Use a suitable FetchProfile
	    FetchProfile fp = new FetchProfile();
	    fp.add(FetchProfile.Item.ENVELOPE);

	    SearchTerm term = new RecipientTerm(Message.RecipientType.TO,
				     new InternetAddress("javamail@sun.com"));
	    Message[] msgs = folder.search(term);
	    folder.fetch(msgs, fp);

	    Hashtable wks = new Hashtable();
	    int totalCount = 0;
	    BodyTerm pop3Search = new BodyTerm("pop3");
	    int pop3Requests = 0;

	    // go through each msg and count it if it's an incoming msg
	    for (int i = 0; i < msgs.length; i++) {
		// make sure we only have a single addressee:
		// javamail@sun.com, which means it's a new message
		// from outside of Sun
		Address[] recs=msgs[i].getRecipients(Message.RecipientType.TO);
		if (recs.length > 1)
		    continue;

		// get msgs date
		Date d = msgs[i].getSentDate();
		if (d == null)
		    d = msgs[i].getReceivedDate();
		cal.setTime(d);

		// figure out what week it is
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		int yr = cal.get(Calendar.YEAR);
		String wkInYr = week + "  " + yr;

		// increment that week's count
		String num = (String)wks.get(wkInYr);
		totalCount++;
		if (num == null)
		    wks.put(wkInYr, "1");
		else {
		    int count = Integer.parseInt(num);
		    count++;
		    Integer str = new Integer(count);
		    wks.put(wkInYr, str.toString());
		}

		// check for pop3 requests
		if (doPop3 && pop3Search.match(msgs[i]))
		    pop3Requests++;
	    }

	    // print out the statistics
	    int startWk = 40;
	    int startYr = 1997;
	    Calendar now = Calendar.getInstance();
	    now.setTime(new Date());
	    cal.set(startYr, 1, 1);
	    cal.set(Calendar.WEEK_OF_YEAR, startWk);
	    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	    DateFormat df = new SimpleDateFormat("E MMM dd yyyy");
	    int totalWks = 0;

	    System.out.println("\nNumber of new messages a week sent to javamail@Sun.COM by external JavaMail\nusers. This does not include our answers and follow-up mail.");
	    for (;;) {
		if (cal.after(now))
		    break;
		else {
		    totalWks++;
		    String wkInYr = (cal.get(Calendar.WEEK_OF_YEAR)) +
			"  " + 
			(cal.get(Calendar.YEAR));
		    Object o = wks.get(wkInYr);
		    if (o != null) {
			String i = (String)o;
			System.out.println("  week of " + 
					   df.format(cal.getTime()) + 
					   ": " + 
					   i + " messages");
		    }
		    		    
		    // increment to next week
		    cal.add(Calendar.WEEK_OF_YEAR, 1);
		}
	    }
	    System.out.println("------------------");
	    System.out.println("Total messages received: " + totalCount);
	    System.out.println("Average messages/week:    " + 
			       totalCount/totalWks);
	    if (doPop3)
		System.out.println("Total POP3 requests:      " + pop3Requests);
	    folder.close(false);
	    store.close();
	} catch (Exception ex) {
	    System.out.println("Oops, got exception! " + ex.getMessage());
	    ex.printStackTrace();
	}
	System.exit(1);
    }


    /**
     * Print verbose.
     */
    static void pv(String s) {
	if (verbose)
	    System.out.println(s);
    }
}
