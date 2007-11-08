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
package com.sun.enterprise.diagnostics.collect;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.ServiceConfig;
import com.sun.enterprise.diagnostics.CLIOptions;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.util.FileUtils;
import com.sun.enterprise.diagnostics.util.LogNameFilter;
import com.sun.enterprise.diagnostics.util.LogNameComparator;
import com.sun.enterprise.diagnostics.collect.*;
/**
 * Visits one or more server.log files to collect log entries
 * satisfying the log filters - startDate, endDate, minLogLevel and
 * max number of log entries collected. Collects log entries
 * pertaingin to ULF only.
 * @author Manisha Umbarje, Jagadish Ramu
 */
public class LogCollector implements Collector {
    private static final SimpleDateFormat dateFormat =
    new SimpleDateFormat(Constants.DATE_PATTERN);;
    private ServiceConfig config;
    private Date startDate;
    private Date endDate;
    private String destFolder;
    private boolean partialPrevEntry = false;
    private boolean prevEntryCopied = false;
    private String logFileName ;
    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    public LogCollector(Date startDate, Date endDate,
            String destFolder,ServiceConfig config) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.destFolder = destFolder;
	this.config = config;
    }

    public LogCollector(String destFolder, String logFile) {
        this.destFolder = destFolder;
        this.logFileName = logFile;
    }

   /**
    * Captures contents for server.log
    */
    public Data capture() throws DiagnosticException {
	int noOfCapturedEntries = 0;
        int maxNoOfEntries = Defaults.MAX_NO_OF_ENTRIES;
        int minLogLevel = Defaults.MIN_LOG_LEVEL;

        if(config != null) {
            maxNoOfEntries = config.getMaxNoOfEntries();
            minLogLevel = config.getMinLogLevel();
            logFileName = config.getLogFile();
            if(logFileName.indexOf(config.getRepositoryDir()) == -1)
                logFileName = config.getRepositoryDir() + logFileName;
        }
	String destLogFile = destFolder + Defaults.DEST_LOG_FILE;

	// Start reading server.log*, may have to traverse mulitple
	// log files when start date and/or end date is specified.
	//If startDate != null
	// if startDate is not specified, read from server.log only.

	List logFiles;

    try {
	    if (startDate != null) {
		File logFile = new File(logFileName);
		File logDir = new File(logFile.getParent());
		String fileNamePrefix = logFile.getName();
		logFiles = FileUtils.getFileListing
		    (logDir, false,                                                                                             
		    new LogNameFilter
		    (fileNamePrefix, startDate, endDate),
		    new LogNameComparator());
            if(logFiles != null && logFiles.size() == 0)
                logFiles = Arrays.asList(new File[] {new File(logFileName)});
        } else
            logFiles = Arrays.asList(new File[] {new File(logFileName)});

	    logger.log(Level.FINE, "diagnostic-service.dest_log_file",
                    new Object[] {destLogFile} );
	    Iterator filesIterator = logFiles.iterator();

	    PrintWriter out ;
	    String logEntry;
        BufferedReader inFile = null;
        try {
		out = new PrintWriter
		(new BufferedWriter(new FileWriter(destLogFile)));
	    }catch(IOException ioe1) {
		File parent = (new File(destLogFile)).getParentFile();
		parent.mkdirs();
		out = new PrintWriter(new BufferedWriter(new FileWriter(destLogFile)));
	    }

        while (filesIterator.hasNext()) {
            if (endDate != null || noOfCapturedEntries < maxNoOfEntries) {
                try {
                    inFile = new BufferedReader
                            (new FileReader((File) filesIterator.next()));

                    while ((logEntry = inFile.readLine()) != null) {
                        try {
                            if (isValid
                                    (logEntry, startDate, endDate, minLogLevel)) {
                                out.println(logEntry);
                                if (endDate == null) {
                                    if (!partialPrevEntry)
                                        noOfCapturedEntries++;
                                    if (noOfCapturedEntries >= maxNoOfEntries) {
                                        break;
                                    }
                                }
                            }//if(isValid)
                        } catch (Exception pe) {
                        }//catch
                    }//while

                    out.flush();
                } catch (IOException ioe) {
                        throw new DiagnosticException(ioe.getMessage());
                }
                finally {
                    try {
                        if (inFile != null) {
                            inFile.close();
                        }
                    } catch (IOException ioe) {
                        //ignore
                    }
                    if(out != null){
                        out.flush();
                        out.close();
                    }
                }
            }//if (noOfCapturedEntries < maxNoOfEntris)
            else
                break;
        }
            return new FileData(destLogFile, DataType.LOG_INFO);
	}catch (FileNotFoundException fnfe) {
	    throw new DiagnosticException(fnfe.getMessage());
	}
	catch (IOException ioe) {
	    throw new DiagnosticException(ioe.getMessage());
	}
    }//captureLog

    /**
     * Entries are in the following format
     * [#|yyyy-mm-ddThh:mm:ss.SSS-Z|Log Level|ProductName_Version|LoggerName|
     * Key Value Pairs|Message|#]
     * Entries may span multiple lines
     */
    private boolean isValid(String entry, Date startDate,
                            Date endDate, int minLogLevel)
            throws ParseException {

        //entryDate >= startDate, entryDate <= endDate ,
        //entryLogLevel >= minLogLevel

        // Blank line
        if (entry.length() <= 0)
            return false;

        //Previous Partial Line was copied, copy blindly
        if (partialPrevEntry) {
            if (hasEndOfEntry(entry))
                partialPrevEntry = false;
            if (prevEntryCopied)
                return true;
            else
                return false;
        }

        // New log entry
        int logLevelSepBeginIndex = entry.indexOf
                (Constants.FIELD_SEPARATOR, Constants.ENTRY_DATE_BEGIN_INDEX) + 1;

        String entryLogLevelStr = entry.substring
                (logLevelSepBeginIndex, entry.indexOf(Constants.FIELD_SEPARATOR,
                        logLevelSepBeginIndex));
        int entryLogLevel = Level.parse(entryLogLevelStr).intValue();

        // Entry log level > min Log Level
        if (entryLogLevel >= minLogLevel) {
            Date entryDate = dateFormat.parse
                    (entry.substring(Constants.ENTRY_DATE_BEGIN_INDEX,
                            Constants.ENTRY_DATE_BEGIN_INDEX + Constants.ENTRY_DATE_LENGTH));

            if (startDate != null && endDate != null) {

                if (entryDate.compareTo(startDate) >= 0 &&
                        entryDate.compareTo(endDate) <= 0)
                    prevEntryCopied = true;
                else
                    prevEntryCopied = false; // Date comparison fails
            } else if (startDate != null && entryDate.compareTo(startDate) < 0) {
                prevEntryCopied = false;
            } else {
                prevEntryCopied = true; //entry is not restricted to date
            }
        } else
            prevEntryCopied = false;

        //Determine whether the entry has end indicator
        if (!hasEndOfEntry(entry))
            partialPrevEntry = true;
        return prevEntryCopied;
    }

    private boolean hasEndOfEntry(String entry) {
	return (entry.indexOf(Constants.ENTRY_END_INDICATOR) > -1);
    }

}
