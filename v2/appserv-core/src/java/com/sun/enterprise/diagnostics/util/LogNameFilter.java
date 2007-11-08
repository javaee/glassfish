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
package com.sun.enterprise.diagnostics.util;

import java.io.File;

import java.io.FilenameFilter;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.diagnostics.DiagnosticException;
/**
 * Log file name is assumed to be in server.log_YYYY-MM-DDTHH-MM-SS format
 * eg.server.log_2005-03-04T12-21-53 format. Files are sorted in ascending order.
 * @author Manisha Umbarje, Jagadish Ramu
 */
public class LogNameFilter implements FilenameFilter {

    private Date startDate;
    private Date endDate;
    private String fileNamePrefix;
    
    private static final SimpleDateFormat dateFormat = 
    new SimpleDateFormat(Constants.DATE_PATTERN);

    public LogNameFilter (String fileName, Date startDate, Date endDate) {
	    this.startDate = startDate;
	    this.endDate = endDate;
	    fileNamePrefix = fileName;
    }

    public boolean accept(File aDir, String fileName) {
        if (aDir == null || fileName == null)
            return false;
        if (fileName.indexOf(fileNamePrefix) < 0)
            return false;
        int datePatternIndex = fileName.indexOf(Constants.FILENAME_DATE_SEPARATOR);

        if (datePatternIndex > 0) {
            try {
                // fileDate indicates date of last entry in the file
                Date fileDate = dateFormat.parse
                        (fileName.substring(datePatternIndex + 1, datePatternIndex + 11));

                if (isValidEntry(fileDate, startDate, endDate)) {
                    return true;
                }
                //handle the case where date_of_last entry is beyond endDate and
                // it may contain entries within start & end date.
                Date firstLogEntry = getDateOfFirstLogEntry(aDir, fileName);
                return  isValidEntry(firstLogEntry, startDate, endDate) ;
            } catch (Exception e) {
                return false;
            }
        } else {
            try {
                // server.log, check if date of first entry is before
                // the endDate, assuption here is every new log file start
                // with new logEntry and not partial entry
                Date date = getDateOfFirstLogEntry(aDir, fileName);

                return isValidEntry(date, startDate, endDate);
            } catch (DiagnosticException de) {
                return false;
            }
        }
    }

    private boolean isValidEntry(Date firstLogEntry, Date startDate, Date endDate){
        if(firstLogEntry == null){
            return false;
        }
        //assuming that startDate is not null.
        if( endDate != null )
            return firstLogEntry.compareTo(startDate) >=0 && firstLogEntry.compareTo(endDate) <=0 ;
        else
            return firstLogEntry.compareTo(startDate) >=0 ;
    }

    private Date getDateOfFirstLogEntry(File aDir, String fileName)
            throws DiagnosticException {
        if (aDir == null || fileName == null)
            return null;
            BufferedReader reader = null;
        try {
            File file = new File(aDir, fileName);
            reader = new BufferedReader(new FileReader(file));
            String firstEntry = null;
            while ((firstEntry = reader.readLine()) != null) {
                try {
                    return dateFormat.parse(firstEntry.substring
                            (Constants.ENTRY_DATE_BEGIN_INDEX, Constants.ENTRY_DATE_BEGIN_INDEX+Constants.ENTRY_DATE_LENGTH));
                } catch (ParseException pe) {
                    //ignore, it may be startup / shutdown log
                } catch(IndexOutOfBoundsException ie){
                    //ignore.
                }
                //continue with next log entry
                continue;
            }
        } catch (FileNotFoundException fnf) {
            throw new DiagnosticException(fnf.getMessage());
        } catch (IOException io) {
            throw new DiagnosticException(io.getMessage());
        }finally{
            try{
                if(reader!=null){
                    reader.close();
                }
            }catch(IOException ie){
                //ignore
            }
        }
        return null;
    }
}
