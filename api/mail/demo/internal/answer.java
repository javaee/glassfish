/*
 *  @(#)answer.java	1.10 01/10/26
 *
 * Copyright (c) 1998-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 */

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.search.*;
import javax.mail.internet.*;

/**
 * Program to manage the javamail@Sun.COM mailing list by keeping track
 * of which messages have been answered.  Message that have been answered
 * have the ANSWERED flag set.  Messages that are replies to other messages
 * have the FLAGGED flag set. <p>
 *
 * Note that this program operates on the log file directly and thus all
 * access to the log file using this program must be through the same
 * IMAP server so that the flags are handled consistently. <p>
 *
 * When run with no message number arguments it will list the unanswered
 * messages.  The -u flag will cause it to first update the mailbox to
 * account for any recent replies. <p>
 *
 * When run with message number arguments it will mark those messages as
 * answered.  This is useful for marking spam and other random messages
 * that will never be replied to. <p>
 *
 * The -a flag will cause it to mark all messages as answered.  Useful for
 * flushing the ever accumulating spam. <p>
 *
 * @author Bill Shannon
 */

public class answer {

    static String protocol = "imap";
    static String host = "anybodys.sfbay";
    static String user = "javamail";
    static String password = "1javamail1";
    static String mbox =
	"/net/anybodys.sfbay/export6/javamail/logs/javamail.log";
    static String url = null;
    static boolean verbose = false;
    static boolean update = false;
    static boolean markAll = false;

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
	    } else if (argv[optind].equals("-u")) {
		update = true;
	    } else if (argv[optind].equals("-a")) {
		markAll = true;
		update = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: answer [-L url] [-T protocol] [-H host] [-U user] [-P password]\n" +
"\t[-f mailbox] [-v] [-u] [-a] [msgno ...]");
		System.exit(1);
	    } else {
		break;
	    }
	}

        try {
	    // Get a Properties object
	    Properties props = System.getProperties();

	    // Get a Session object
	    Session session = Session.getDefaultInstance(props,
				    new TtyAuthenticator());

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

	    if (optind < argv.length || update)
		folder.open(Folder.READ_WRITE);
	    else
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

	    if (optind >= argv.length) {
		// get all messages that aren't answered or flagged
		Flags f = new Flags();
		f.add(Flags.Flag.ANSWERED);
		f.add(Flags.Flag.FLAGGED);

		// Use a suitable FetchProfile
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfile.Item.FLAGS);

		Message[] msgs = folder.search(new FlagTerm(f, false));
		folder.fetch(msgs, fp);

		if (update) {
		    doUpdate(msgs);
		    // re-fetch messages
		    msgs = folder.search(new FlagTerm(f, false));
		    pv("");
		    pv("");
		}

		System.out.println("Unanswered messages:");
		for (int i = 0; i < msgs.length; i++) {
		    Message msg = msgs[i];
		    System.out.println("--------------------------");
		    System.out.println("MESSAGE #" +
			msg.getMessageNumber() + ":");
		    Question q = new Question(msg);
		    System.out.println(q);
		    if (q.isReply())
			System.out.println(
			    "A reply to a message we haven't seen");
		    if (markAll) {
			int msgno = msg.getMessageNumber();
			if (q.isReply()) {
			    pv("Flagging message #" + msgno);
			    msg.setFlag(Flags.Flag.FLAGGED, true);
			} else {
			    pv("Answering message #" + msgno);
			    msg.setFlag(Flags.Flag.ANSWERED, true);
			}
		    }
		}

	    } else {
		for (int i = optind; i < argv.length; i++) {
		    int msgno = Integer.parseInt(argv[i]);
		    Message msg = folder.getMessage(msgno);
		    Question q = new Question(msg);
		    if (q.isReply()) {
			pv("Flagging message #" + msgno);
			msg.setFlag(Flags.Flag.FLAGGED, true);
		    } else {
			pv("Answering message #" + msgno);
			msg.setFlag(Flags.Flag.ANSWERED, true);
		    }
		}
	    }

	    folder.close(false);
	    store.close();
	} catch (Exception ex) {
	    System.out.println("Oops, got exception! " + ex.getMessage());
	    ex.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    /**
     * Update the mailbox to account for any replies.
     */
    static void doUpdate(Message[] msgs) throws MessagingException {
	// a hash table of all the unanswered questions
	Hashtable qt = new Hashtable();

	Address javamailAddress = new InternetAddress("javamail@Sun.COM");
	SearchTerm javamail = new OrTerm(
		new RecipientTerm(Message.RecipientType.TO, javamailAddress),
		new RecipientTerm(Message.RecipientType.CC, javamailAddress));

	for (int i = 0; i < msgs.length; i++) {
	    Message msg = msgs[i];
	    pv("--------------------------");
	    pv("MESSAGE #" + msg.getMessageNumber() + ":");
	    Question q = new Question(msg);
	    pv(q.toString());
	    Question q0;
	    if ((q0 = (Question)qt.get(q)) != null) {
		pv("Found in table");
		if (q.isReply()) {
		    Message qm = q0.getMessage();
		    if (!qm.isSet(Flags.Flag.ANSWERED)) {
			pv("Answered:");
			pv(q0.toString());
			qm.setFlag(Flags.Flag.ANSWERED, true);
		    }
		    pv("is reply, flagging it");
		    msg.setFlag(Flags.Flag.FLAGGED, true);
		} else {
		    // a second copy of a message that's not a reply?
		    // shouldn't happen.
		}
	    } else {
		pv("NOT found in table");
		if (q.isReply()) {
		    // a reply to a message we haven't seen?
		    pv("flagging, but no original msg");
		    msg.setFlag(Flags.Flag.FLAGGED, true);
		} else {
		    // an original question, put it in the table
		    qt.put(q, q);
		    // if the message looks like spam, flag it
		    if (!javamail.match(msg)) {
			pv("SPAM!!!");
			msg.setFlag(Flags.Flag.FLAGGED, true);
		    }
		}
	    }
	}
    }

    /**
     * Print verbose.
     */
    static void pv(String s) {
	if (verbose)
	    System.out.println(s);
    }
}

/**
 * This class represents a single "question" sent to javamail@Sun.COM,
 * or possibly a reply to such a question.
 */
class Question {
    Message	msg;
    Address	sender;
    Address[]	recipients;
    String	subject;
    Date	date;
    boolean	reply = false;

    Question(Message msg) throws MessagingException {
	this.msg = msg;
	sender = msg.getReplyTo()[0];    // XXX - assume only one
	subject = msg.getSubject();
	if (subject == null)
	    subject = "";
	else if (subject.regionMatches(true, 0, "Re: ", 0, 4)) {
	    subject = subject.substring(4);
	    reply = true;
	    recipients = msg.getRecipients(Message.RecipientType.TO);
	}
	subject = subject.trim();
	date = msg.getSentDate();
	if (date == null)
	    date = msg.getReceivedDate();
    }

    public boolean isReply() {
	return reply;
    }

    public Message getMessage() {
	return msg;
    }

    public int hashCode() {
	return subject.hashCode();
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof Question))
	    return false;
	Question q = (Question)obj;
	if (this.reply == q.reply)
	    return this.sender.equals(q.sender) &&
		    this.subject.equals(q.subject);

	/*
	 * A reply is equal to a question if the sender of the question
	 * is one of the recipients of the reply.
	 */
	Question qq, qr;
	if (this.reply) {
	    qq = q;
	    qr = this;
	} else {
	    qq = this;
	    qr = q;
	}
	for (int i = 0; i < qr.recipients.length; i++) {
	    if (qq.sender.equals(qr.recipients[i]))
		return true;
	}
	return false;
    }

    public String toString() {
	return "Date: " + date + "\nFrom: " + sender + "\nSubject: " + subject;
    }
}
