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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;
import com.sun.appserv.management.ext.logging.LogAnalyzer;
import java.util.Map;
import java.text.DateFormat;
import java.util.Vector;

public class DisplayErrorStatisticsCommand extends BaseLoggingCommand 
{
    protected void handleReturnValue(Object retVal) 
    {
        final Map[] errorInfo = (Map[])retVal;
        
        if (errorInfo.length > 0)
        {
            String sTitle = String.format("%1$-39s %2$-9s %3$-8s",
                                          getLocalizedString("Timestamp"),
                                          getLocalizedString("Severity"),
                                          getLocalizedString("Warning"));
            
            CLILogger.getInstance().printDetailMessage(sTitle);
            
            CLILogger.getInstance().printDetailMessage("---------------------------------------------------------");
        }
        for (Map errorRecord : errorInfo)
        {
            final Long timeStamp = (Long) errorRecord.get(LogAnalyzer.TIMESTAMP_KEY);
            final java.util.Date date = new java.util.Date(timeStamp);

            final Integer severeCount = 
                    (Integer) errorRecord.get(LogAnalyzer.SEVERE_COUNT_KEY);
            final Integer warningCount = 
                    (Integer) errorRecord.get(LogAnalyzer.WARNING_COUNT_KEY);

            final String sDate = (getBooleanOption(TERSE))?timeStamp.toString():
                                 timeStamp.toString()+
                                 "("+DateFormat.getDateTimeInstance().format(date)+")";

            final String sErrorRecord = String.format("%1$-43s %2$-8s %3$-8s", sDate, 
                                                    (severeCount.intValue()<0)?"NA":severeCount, 
                                                    (warningCount.intValue()<0)?"NA":warningCount);
            CLILogger.getInstance().printMessage(sErrorRecord);
        }
        if (errorInfo.length > 0)
        {
            CLILogger.getInstance().printDetailMessage("---------------------------------------------------------");
        }
    }
}
