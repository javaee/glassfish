/**
 * @(#)foldersplit.java	1.1	99/05/21
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 */

import java.util.*;
import java.text.*;
import javax.mail.*;

/**
 *
 * Split mail folders according to date of messages.
 *
 * @author Bill Shannon
 */

public class foldersplit {

    static String protocol;
    static String host = null;
    static String user = null;
    static String password = null;
    static String dst = null;
    static String url = null;
    static int port = -1;
    static boolean verbose = false;
    static boolean debug = false;
    static boolean nop = false;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy.MM");

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
	    } else if (argv[optind].equals("-d")) {
		dst = argv[++optind];
	    } else if (argv[optind].equals("-v")) {
		verbose = true;
	    } else if (argv[optind].equals("-n")) {
		nop = true;
	    } else if (argv[optind].equals("-D")) {
		debug = true;
	    } else if (argv[optind].equals("-L")) {
		url = argv[++optind];
	    } else if (argv[optind].equals("-p")) {
		port = Integer.parseInt(argv[++optind]);
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: foldersplit [-L url] [-T protocol] [-H host] [-p port] [-U user]");
		System.out.println(
"\t[-P password] [-v] [-D] folder ...");
		System.exit(1);
	    } else {
		break;
	    }
	}

        try {
	    // Get a Properties object
	    Properties props = System.getProperties();

	    // Get a Session object
	    Session session = Session.getDefaultInstance(props, null);
	    session.setDebug(debug);

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
		    store.connect(host, port, user, password);
		else
		    store.connect();
	    }
	    

	    // Open the Folder

	    Folder deffolder = store.getDefaultFolder();
	    if (deffolder == null) {
	        System.out.println("No default folder");
	        System.exit(1);
	    }
	    Folder dstfolder;
	    if (dst == null) {
		dstfolder = deffolder;
	    } else {
		dstfolder = deffolder.getFolder(dst);
		if (dstfolder == null) {
		    System.out.println("No destination folder");
		    System.exit(1);
		}
	    }

	    Folder outf = null;
	    for (; optind < argv.length; optind++) {
		Folder folder = deffolder.getFolder(argv[optind]);
		if (folder == null) {
		    System.out.println("Invalid folder: " + argv[optind]);
		    continue;
		}
		System.out.println("Folder: " + folder.getFullName());

		folder.open(Folder.READ_ONLY);
		int totalMessages = folder.getMessageCount();

		if (totalMessages == 0) {
		    System.out.println("Empty folder");
		    folder.close(false);
		    continue;
		}

		if (verbose) {
		    int newMessages = folder.getNewMessageCount();
		    System.out.println("Total messages = " + totalMessages);
		    System.out.println("New messages = " + newMessages);
		    System.out.println("-------------------------------");
		}

		// Attributes & Flags for all messages ..
		Message[] msgs = folder.getMessages();

		// Use a suitable FetchProfile
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfile.Item.FLAGS);
		folder.fetch(msgs, fp);

		for (int i = 0; i < msgs.length; i++) {
		    if (verbose) {
			System.out.println("--------------------------");
			System.out.println("MESSAGE #" + (i + 1) + ":");
		    }
		    Date d = msgs[i].getReceivedDate();
		    if (d != null) {
			String n = df.format(d);
			if (verbose)
			    System.out.println(n);
			if (outf == null || !n.equals(outf.getName())) {
			    outf = dstfolder.getFolder(n);
			    if (!outf.exists()) {
				System.out.println("Creating: " +
						    outf.getFullName());
				if (!nop)
				    outf.create(Folder.HOLDS_MESSAGES);
			    }
			}
		    }
		    if (!nop)
			outf.appendMessages(new Message[] { msgs[i] });
		}
		folder.close(false);
	    }

	    store.close();
	} catch (Exception ex) {
	    System.out.println("Oops, got exception! " + ex.getMessage());
	    ex.printStackTrace();
	}
	System.exit(1);
    }
}
