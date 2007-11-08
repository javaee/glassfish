/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.util;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * This class is a utility that takes as input a property file name and 
 * creates a file named <propertyFileName>.html which is an html document
 * containing a table consisting of the following columns:
 *
 *      MessageId, MessageSeverity, MessageText, Description
 *      
 * The MessageId and MessageText are pulled directly from the given property
 * file. The MessageSeverity is determined by searching through the source
 * code heuristically for the message key in the property file and parsing
 * the severity (FATAL, ALERT, SEVERE, WARNING, CONFIG, INFO, FINE, FINER,
 * FINEST) from the source files containing the message key. For now the 
 * description holds the file and line number where the message key was found.
 * 
 * The format of the property file is assumed to be as follows:
 * 
 *      Lines starting with # are considered comments and ignored
 *      
 *      Blank lines are ignored
 *
 *      Other lines are considered to be of the format:
 *          <messageKey>=<messageId><delimiter><messageText>
 *      The messageId is typically a string of the form <subsystem><id> such 
 *      as WEB0001. The delimiter is either the ':' character or a ' ' 
 *      (space). Message text can be anything.
 *
 * Notes:
 *      There are hardcoded values here about where to search for source 
 *      and what file types to search.
 *
 *      This has only been run / tested on Win2K against the 
 *      LogStrings.property files found in com/sun/logging.
 *
 *      The find utility is used to search for message key occurrences.
 **/

/**
 * The ThreadedReader is used to read stdout/stderr of the find command and 
 * return its results in an ArrayList buffer.
 **/
class ThreadedReader extends Thread 
{
    BufferedReader _reader = null;
    String _messageKey = null;
    ArrayList _result = null;

    public ThreadedReader(InputStream is, ArrayList result, String messageKey) {
        _reader = new BufferedReader(new InputStreamReader(is));
        _result = result;
        _messageKey = messageKey;
    }

    public void run() {
        try {
            String line = null;
            while (true) {
			    line = _reader.readLine();
			    if (line == null) {
				    break;
			    }
                _result.add(line);
            }
        } catch (Exception ex) {
            System.err.println("ThreadedReader " + _messageKey + " exception");
            ex.printStackTrace();
        }
    }
}


public class BuildLogMsgDoc {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: BuildLogMsgDoc <resource-file-name>");
            System.exit(1);
        }
        String inFile = args[0];
        String outFile = args[0] + ".html";
        try {
			createHtml(inFile, outFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
     }

    protected static void createHtml(String in, String out) 
	    throws FileNotFoundException, IOException
    {
	    BufferedReader reader = new BufferedReader(new FileReader(in));
	    BufferedWriter writer = new BufferedWriter(new FileWriter(out, false));
	    try {
            // output html table header
            writer.write(tableHeader(in));
		    writer.newLine();
            writer.write(tableRow("Message Id", "Severity", "Message Text", "Description"));
		    writer.newLine();
		    String line = null;
		    String newLine = null;
		    while (true) {
			    line = reader.readLine();
			    if (line == null) {
				    break;
			    }
			    parsePropertyEntry(writer, line);
		    } 
	    } finally {
            // output html table footer
            writer.write(tableFooter());
		    try {
			    reader.close();
		    } catch (Exception ex) {}
		    try {
			    writer.close();
		    } catch (Exception ex) {}
	    }	
    }

    protected static void parsePropertyEntry(BufferedWriter writer, String line)
    {
	    String result = line.trim();
	    //skip empty lines
	    if (result.length() == 0) {
		    return;
	    }
	    //skip comments
	    if (result.startsWith("#")) {
		    return;
	    }
        //parse the message key 
        String key = null;
        String messageId = null; 
        String message = null;
        int pos = -2;
        int pos2 = -2;
        int pos3 = -2;
       	pos = result.indexOf("=");
	    if (pos > 0) {
            // Attempt to parse a message of the form
            // <messageId>:<messageText> or 
            // <messageId> <messageText>
            // delegate to formatLine on success
		    key = result.substring(0, pos);
            result = result.substring(pos + 1);
            pos2 = result.indexOf(" ");
            pos3 = result.indexOf(":");
            if ((pos3 < 0 || pos2 < pos3) && pos2 > 0) {
                messageId = result.substring(0, pos2);
                message = result.substring(pos2 + 1).trim();
                formatLine(writer, key, messageId, message);
                return;

            } else if (pos3 > 0) {
                messageId = result.substring(0, pos3);
                message = result.substring(pos3 + 1).trim();
                formatLine(writer, key, messageId, message);
                return;
            }
        }
        // Malformed line in the property file
        System.err.println("Failed to parse: " + line + " pos=" + pos +
                " pos2=" + pos2 + " pos3=" + pos3);
    }

    protected static void formatLine(BufferedWriter writer, String key, 
            String messageId, String message)
    {
        // find occurrences of message key in the source
        ArrayList files = findSourceFiles (key);
        if (files == null) {
            // there are no occurrences of the message key found.
            try {
                System.err.println("message " + key + "is not found in any files");
                writer.write(tableRow(messageId, "UNKNOWN", message, 
                    "key=" + key + " found in NO FILES")); 
                writer.newLine();
            } catch (Exception ex) {
                System.err.println("formatLine id " +
                    key + " exception ");
                ex.printStackTrace();
            }
        } else {
            // for each source file in which the message key is found, we
            // find the location of the message key in the source file 
            // to determine its severity
            for (int i = 0; i < files.size(); i++) {
                //System.out.println("Searching file " + files.get(i) + " for " + key);
                findSourceOccurrence(writer, key, messageId, message, 
                    (String)files.get(i));
            }
        }
    }

    // Looks for a severity in a single line of source, by looking for very
    // specific keywords
    protected static String findLevel (String line) {
        String[] keywords = {"INFO", "WARNING", "SEVERE", "CONFIG", "ALERT", "FATAL", 
            "FINE", "FINER", "FINEST"};
        line = line.toUpperCase();
        for (int i = 0; i < keywords.length; i++) {
            int pos = line.indexOf(keywords[i]);
            if (pos >= 0) {
                return (keywords[i]);
            }
        }
        return null;
    }

    // Looks for an occurrence of the given messageKey in the file and
    // determines its severity. Upon success a row is written to the 
    // html table
    protected static void findSourceOccurrence(BufferedWriter writer,
            String messageKey, String messageId, String message, 
            String in)
    {
        BufferedReader reader = null;
	    try {
            reader = new BufferedReader(new FileReader(in));
            int lineno = 1;
            int foundOn = -1;
		    String line = null;
            // process each line of the source file sequentially looking for
            // the message key followed (somewhere) by its level
		    while (true) {
			    line = reader.readLine();
			    if (line == null) {
                    // end of file, check to see if we have found the message
                    // key, but no level
                    if (foundOn > 0) {
                        System.err.println("found " + messageKey + " but no level in " +
                                in);
                        writer.write(tableRow(messageId, "UNKNOWN", message, 
                            "key=" + messageKey + " found in " + in + 
                            " line " + foundOn));
                        writer.newLine();
                    }
				    break;
			    }
                int pos = line.indexOf(messageKey);
                if (pos >= 0) {
                    // we have found an occurrence of the message key in the 
                    // file.
                    if (foundOn > 0) {
                        // we have not yet found the level for the previous 
                        // occurrence of the message key
                        System.err.println("found next " + messageKey + 
                                " before finding previous level in " + in);
                        writer.write(tableRow(messageId, "UNKNOWN", message, 
                            "key=" + messageKey + " found in " + in + 
                            " line " + foundOn));
                        writer.newLine();
                    } 
                    foundOn = lineno; 
                    //System.out.println("Found " + messageKey + " on line " + foundOn +
                    //        " in " + in);
                }
                if (foundOn >= lineno) {
                    // start looking for a level only after the message key 
                    // has been found (i.e. foundOn > lineno)
                    String level = findLevel(line);
                    if (level != null) {
                        // we have successfully found the level. If the level
                        // was found within 5 lines of the key, then this is 
                        // considered "normal"; otherwise, we output ** next 
                        // to the level as a marker indicating that the level
                        // may not be accurate
                        //System.out.println(messageKey + " in " + in + " has level " + level);
                        if (foundOn < lineno + 5) {
                            writer.write(tableRow(messageId, level, message, 
                                "key=" + messageKey + " found in " + in + 
                                " line " + foundOn + " logged in line " + lineno));
                        } else { 
                            writer.write(tableRow(messageId, "**" + level, message, 
                                "key=" + messageKey + " found in " + in + 
                                " line " + foundOn + " logged in line " + lineno));
                        }
                        writer.newLine();
                        foundOn = -1;
                    }
                }
                lineno++;
		    } 
        } catch (Exception ex) {
            System.err.println("findSourceOccurrence id " +
                messageKey + " in " + in + " exception");
            ex.printStackTrace();
        } finally {
		    try {
			    if (reader != null) {
                    reader.close();
                }
		    } catch (Exception ex) {}
	    }	
    }

    protected static ArrayList findSourceFiles (String messageKey)
    {
        try {
            ArrayList stdoutList = new ArrayList();
            ArrayList stderrList = new ArrayList();
            // exec a find ... -exeec grep -l to search for the source files
            // containing the message key. Each match (source file) is returned
            // as one String entry in the resulting ArrayList
            Process p = Runtime.getRuntime().exec("/bin/find /ias/tip/iplanet/ias/server/src/java -name \"*.java\" -exec grep -l " + messageKey + " {} ;");
            ThreadedReader inReader = new ThreadedReader(p.getInputStream(), stdoutList, messageKey);
            ThreadedReader errReader = new ThreadedReader(p.getErrorStream(), stderrList, messageKey);
            inReader.start();
            errReader.start();
            inReader.join();
            errReader.join();
            p.waitFor();
            p.exitValue();
            if (!stderrList.isEmpty()) {
                System.err.println("findSourceFiles " + messageKey + " stderr");
                for (int i = 0; i < stderrList.size(); i++) {
                    System.err.println("  " + (String)stderrList.get(i));
                }
            } 
            if (!stdoutList.isEmpty()) {
                return stdoutList;
            } 
            return null;
        } catch (Exception ex) {
            System.err.println("findSourceFiles " + messageKey + " exception");
            ex.printStackTrace();
            return null;
        }
    }
   
    // Output the table header
    protected static String tableHeader(String inFile) {
        String r = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">";
        r += "\n" + "<html>";
        r += "\n" + "<head>";
        r += "\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">";
        r += "\n" + "<meta name=\"Author\" content=\"Ken Ebbs\">";
        r += "\n" + "<meta name=\"GENERATOR\" content=\"Mozilla/4.79 [en] (Windows NT 5.0; U) [Netscape]\">";
        r += "\n" + "<title>Logging Message Reference</title>";
        r += "\n" + "</head>";
        r += "\n" + "<body>";
        r += "\n" + "&nbsp;";
        r += "\n" + "<table BORDER WIDTH=\"100%\" >";
        r += "\n" + "<caption>Log Messages For " + inFile + "</caption>";
        return r;
    }

    // Output the table footer
    protected static String tableFooter() {
        String r = "</table>";
        r += "\n" + "</body>";
        r += "\n" + "</html>";
        return r;
    }

    // Output a single row in the table
    protected static String tableRow(String messageId, String severity, String messageText, String description) {
        String r = "<tr>";
        r += "\n" + "<td>" + messageId + "</td>";
        r += "\n" + "<td>" + severity + "</td>";
        r += "\n" + "<td>" + messageText + "</td>";
        r += "\n" + "<td>" + description + "</td>";
        r += "\n" + "</tr>";
        return r;
    }
}
