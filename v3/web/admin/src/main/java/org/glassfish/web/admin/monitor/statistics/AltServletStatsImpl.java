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

package org.glassfish.web.admin.monitor.statistics;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;

import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.TimeStatistic;

import org.glassfish.admin.monitor.cli.MonitorContract;

//import com.sun.appserv.management.monitor.statistics.AltServletStats;

/** 
	Defines additional Application Server specific statistics 
	ServletStats interface.
 */

@Service
@Scoped(PerLookup.class)
public class AltServletStatsImpl implements /*AltServletStats,*/ MonitorContract {

    private final String name = "servlet";

    public String getName() {
        return name;
    }

    public ActionReport process(final ActionReport report, final String filter) {

        /*
        StringBuffer sb = new StringBuffer();
        sb.append("MSR: test message from AltServletStatsImpl ..." + System.getProperty("line.separator"));
        String str = String.format("%1$-10s %2$-10s %3$-10s", "ActSess", "SessTtl", "SrvltLdC");
        sb.append(str + System.getProperty("line.separator"));
        str = String.format("%1$-10s %2$-10s %3$-10s", 10, 20, 30);
        sb.append(str + System.getProperty("line.separator"));
        report.setMessage("MSR: test message from AltServletStatsImpl ...");
        report.setMessage(sb.toString());
        */

        String str = null;
        ActionReport.MessagePart part = null;

/*
        str = String.format("%1$-10s %2$-10s %3$-10s", "ActSess", "SessTtl", "SrvltLdC");
        part = report.getTopMessagePart().addChild();
        part.setChildrenType("monitor_header");
        part.setMessage(str);
*/
        
        str = String.format("%1$-10s %2$-10s %3$-10s", 10, 20, 30);
        part = report.getTopMessagePart().addChild();
        part.setChildrenType("monitor_values");
        part.setMessage(str);


        report.setActionExitCode(ExitCode.SUCCESS);
        return report;
    }
    
    /**
     * @return CountStatistic
     */
    public CountStatistic getErrorCount() {
    	return null;
    }
    
    /**
     * @return CountStatistic
     */
    public CountStatistic getRequestCount() {
    	return null;
    }
    
    /**
     * @return CountStatistic
     */
    public CountStatistic getProcessingTime() {
    	return null;
    }
    
    /**
     * @return CountStatistic
     */
    public CountStatistic getMaxTime() {
    	return null;
    }
    
    /**
     * @return TimeStatistic
     */
    public TimeStatistic getServiceTime() {
    	return null;
    }

    public Statistic[] getStatistics() {
    	return null;
    }

    public String[] getStatisticNames() {
    	return null;
    }

    public Statistic getStatistic(String statisticName) {
    	return null;
    }
}
