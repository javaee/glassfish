/*
 * @(#)fpopulate.java	1.1 02/04/05
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;

/*
 * Copy folder hierarchies between files and a Store. This is a useful 
 * utility to populate new (and possibly empty) mail stores. Specify
 * the source as a directory name and the destination folders as a URL.
 *	
 * @author John Mani
 * @author Bill Shannon
 */

public class fpopulate {

    static boolean force = false;
    static boolean skipSCCS = false;
    static boolean clear = false;

    static Session session;

    public static void main(String argv[]) {
    	String srcdir = null;
    	String dstURL = null;
	boolean debug = false;

	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-s")) {
		srcdir = argv[++optind];
	    } else if (argv[optind].equals("-d")) {
		dstURL = argv[++optind];
	    } else if (argv[optind].equals("-D")) {
		debug = true;
	    } else if (argv[optind].equals("-f")) {
		force = true;
	    } else if (argv[optind].equals("-S")) {
		skipSCCS = true;
	    } else if (argv[optind].equals("-c")) {
		clear = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		printUsage();
		System.exit(1);
	    } else {
		break;
	    }
	}

	try {

	    if (srcdir == null || dstURL == null) {
		printUsage();
		System.exit(1);
	    }

	    session = Session.getDefaultInstance(
				System.getProperties(), null);
	    session.setDebug(debug);

	    // Get source folder
	    File srcFolder = new File(srcdir);
	    if (!srcFolder.exists()) {
		System.out.println("source folder does not exist");
		System.exit(1);
	    }

	    // Set up destination folder
	    URLName dstURLName = new URLName(dstURL);
	    Folder dstFolder;
	    // Check if the destination URL has a folder specified. If
	    // not, we use the source folder name
	    if (dstURLName.getFile() == null) {
		Store s = session.getStore(dstURLName);
		s.connect();
		dstFolder = s.getFolder(srcFolder.getName());
	    } else
		dstFolder = session.getFolder(new URLName(dstURL));

	    if (clear && dstFolder.exists()) {
		if (!dstFolder.delete(true)) {
		    System.out.println("couldn't delete " +
						dstFolder.getFullName());
		    return;
		}
	    }
	    copy(srcFolder, dstFolder);

	    // Close the respective stores.
	    dstFolder.getStore().close();

	} catch (MessagingException mex) {
	    System.out.println(mex.getMessage());
	    mex.printStackTrace();
	} catch (IOException ioex) {
	    System.out.println(ioex.getMessage());
	    ioex.printStackTrace();
	}
    }

    private static void copy(File src, Folder dst)
		throws MessagingException, IOException {
	System.out.println("Populating " + dst.getFullName());

	if (!dst.exists()) {
	    // Create it.
	    int type = holdsMessages(src) ?
		    Folder.HOLDS_MESSAGES : Folder.HOLDS_FOLDERS;
	    if (!dst.create(type)) {
		System.out.println("couldn't create " + dst.getFullName());
		return;
	    }

	    // Copy over any messages from src to dst
	    if (holdsMessages(src))
		copyMessages(src, dst);
	} else  {
	    System.out.println(dst.getFullName() + " already exists");
	    // Copy over any messges from src to dst
	    if (force && holdsMessages(src))
		copyMessages(src, dst);
	}

	// Copy over subfolders
	if (holdsFolders(src)) {
	    String[] sf = src.list();
	    for (int  i = 0; sf != null && i < sf.length; i++) {
		// skip SCCS directories?
		if (skipSCCS && sf[i].equals("SCCS"))
		    continue;
		File f = new File(src, sf[i]);
		if (f.isDirectory())
		    copy(f, dst.getFolder(sf[i]));
	    }
    	}
    }

    /**
     * Does this directory hold messages?
     * Return true if there's at least one message.
     */
    private static boolean holdsMessages(File f) {
	File msg = new File(f, "1");
	return msg.exists();
    }

    private static boolean holdsFolders(File f) {
	return !holdsMessages(f);	// XXX - hack for now
    }

    /**
     * Copy message files from the source directory to the
     * destination folder.  Message files must be named "1",
     * "2", etc.  The first missing number terminates the
     * copy.
     */
    private static void copyMessages(File src, Folder dst)
				throws MessagingException, IOException {
	System.out.println("  Copy from " + src + " to " + dst);
	int msgnum = 1;
	Message[] msgs = new Message[1];
	for (;;) {
	    File f = new File(src, String.valueOf(msgnum));
	    if (!f.exists())	// break when we find a message missing
		break;
	    FileInputStream fis = new FileInputStream(f);
	    BufferedInputStream is = new BufferedInputStream(fis);
	    is.mark(1024);
	    DataInputStream dis = new DataInputStream(is);
	    String line = dis.readLine();
	    /*
	     * If it's in UNIX mbox format, we skip the first line,
	     * otherwise we start reading at the beginning.
	     */
	    if (!line.startsWith("From "))
		is.reset();
	    MimeMessage msg = new MimeMessage(session, is);
	    fis.close();
	    msgs[0] = msg;
	    dst.appendMessages(msgs);
	    msgnum++;
	}
	System.out.println("  Copied " + (msgnum - 1) + " messages");
    }

    private static void printUsage() {
	System.out.println("fpopulate [-D] [-f] [-S] [-c] " +
			   "-s source_dir -d dest_url");
	System.out.println("URLs are of the form: " +
		  	   "protocol://username:password@hostname/foldername");
	System.out.println("The destination URL does not need a foldername," +
		  	   " in which case, the source foldername is used");
    }
}
