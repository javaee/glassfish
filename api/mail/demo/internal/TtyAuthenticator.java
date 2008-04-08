/*
 * @(#)TtyAuthenticator.java	1.2 04/01/08
 *
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
 */

import java.io.*;
import java.net.*;
import javax.mail.*;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;

/**
 * A simple Authenticator that prompts for the user name and password on stdin.
 * Puts up a dialog something like:
 * <p> <pre>
 * Connecting to &lt;protocol&gt; mail service on host &lt;addr&gt;, port &lt;port&gt;.
 * &lt;prompt&gt;
 *
 * User Name: [defaultUserName]
 * Password:
 * </pre> <p>
 *
 * @author Bill Shannon
 */

public class TtyAuthenticator extends Authenticator {

    /**
     * @return The PasswordAuthentication collected from the
     *		user, or null if none is provided.
     */
    protected PasswordAuthentication getPasswordAuthentication() {
	BufferedReader in = new BufferedReader(
				new InputStreamReader((System.in)));
	StringBuffer sb = new StringBuffer();
	sb.append("Connecting to ");
	sb.append(getRequestingProtocol());
	sb.append(" mail service on host ");
	sb.append(getRequestingSite().getHostName());
	int port = getRequestingPort();
	if (port > 0) {
	    sb.append(", port ");
	    sb.append(port);
	}
	sb.append(".");
	System.out.println(sb.toString());
	String prompt = getRequestingPrompt();
	if (prompt != null)
	    System.out.println(prompt);
	System.out.println();
	String userName = get(in, "User Name", getDefaultUserName());
	String password = get(in, "Password", null);
	if (userName == null)
	    return null;
	else
	    return new PasswordAuthentication(userName, password);
    }

    private static final String get(BufferedReader in,
				String name, String value) {
	PrintStream p = System.out;

	p.print(name + ": ");
	if (value != null)
	    p.print("[" + value + "] ");
	p.flush();

	try {
	    String s = in.readLine();
	    if (s.length() == 0)
		return value;
	    else
		return s;
	} catch (IOException e) {
	    return value;
	}
    }

    // main program, for debugging.
    // Usage: java TtyAuthenticator host port protocol prompt defaultUser
    public static void main(String argv[]) throws Exception {
	Session sess = Session.getInstance(System.getProperties(),
					new TtyAuthenticator());
	PasswordAuthentication pw = sess.requestPasswordAuthentication(
		InetAddress.getByName(argv[0]),
		Integer.parseInt(argv[1]), argv[2], z(argv[3]), z(argv[4]));
	System.out.println("User: " + n(pw.getUserName()));
	System.out.println("Password: " + n(pw.getPassword()));
    }

    private static final String n(String s) {
	return s == null ? "<null>" : s;
    }

    private static final String z(String s) {
	return s.length() > 0 ? s : null;
    }
}
