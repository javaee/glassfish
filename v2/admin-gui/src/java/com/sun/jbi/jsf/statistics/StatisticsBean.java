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

package com.sun.jbi.jsf.statistics;

import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.common.Util;
import com.sun.jbi.jsf.framework.common.resources.Messages;
import com.sun.jbi.jsf.framework.common.BaseBean;
import com.sun.jbi.jsf.statistics.Statistics;
import com.sun.jbi.jsf.framework.services.statistics.StatisticsService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * StatisticsBean.java
 *  a backing bean for providing message exchange statistics for endpoints,
 *  service units and components (SEs + BCs)
 * 
 * @author ylee
 */
public class StatisticsBean extends BaseBean implements Serializable {
    
    private StatisticsService statsService = null;
    
    private Logger logger = Logger.getLogger(StatisticsBean.class.getName());
    
    private List<Statistics> pStats;        // Provisioning Statistics
    private List<Statistics> cStats;        // Consuming Statistics
    
    /** Creates a new instance of StatisticsBean */
    public StatisticsBean() {
    }
    
    
    protected void getStatisticsService() {
        // setup request configuration data
        setup();
        statsService = serviceManager.getStatisticsService(tName);
    }
    
    public TableDataProvider getStatistics() {
        
        Statistics stats = getStatisticsInstance();
        
        //List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        List<DisplayStatistics> list = stats.generateDisplayStatistics();
        
        provider = new ObjectListDataProvider(list);
        
        return provider;
    }
    
    
    public TableDataProvider getProvisioningStatistics() {

        getStatisticsService();
        
        //String name = Util.mapComponentValue(cName,componentName);
        //String type = Util.mapComponentValue(cType,componentType);
        
        pStats = statsService.getProvisioningStatistics(componentName,componentType,cType,cName,pName);
        
        List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        
        if ( pStats!=null ) {
            for ( Iterator iter=pStats.iterator(); iter.hasNext(); ) {
                Statistics stats = (Statistics)iter.next();
                list.addAll(stats.generateDisplayStatistics());
            }
        }
        
        provider = new ObjectListDataProvider(list);
        
        return provider;
    }
    
    public TableDataProvider getConsumingStatistics() {
        getStatisticsService();
        
        cStats = statsService.getConsumingStatistics(componentName,componentType,cType,cName,pName);
        
        List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        
        if ( cStats!=null ) {
            for ( Iterator iter=cStats.iterator(); iter.hasNext(); ) {
                Statistics stats = (Statistics)iter.next();
                list.addAll(stats.generateDisplayStatistics());
            }
        }
        
        provider = new ObjectListDataProvider(list);
        
        return provider;
    }
    
    
    public TableDataProvider getTotalsStatistics() {
        getStatisticsService();
        
        //List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        
        Statistics stats = statsService.getTotalStatistics(pStats,cStats);
        
        // tally up statistics for all end points
        List<DisplayStatistics> list = stats.generateDisplayStatistics();
        
        provider = new ObjectListDataProvider(list);
        
        return provider;
    }
    
    
    public Statistics  getStatisticsInstance() {
        
        // setup request configuration data
        setup();
        
        statsService = serviceManager.getStatisticsService(tName);
        
        // todo - getting container statistics only
        String name = Util.mapComponentValue(cName,componentName);
        String type = Util.mapComponentValue(cType,componentType);
        Statistics stats = statsService.getStatistics(name,type);
        return stats;
        
    }
    
    
    public String getLabel() {
        String label = "";
        if ( GenericConstants.BC_TYPE.equals(componentType) ) {
            label = Messages.getString("jbi.statistics.bc.label");
        } else if ( GenericConstants.SE_TYPE.equals(componentType) ) {
            label = Messages.getString("jbi.statistics.se.label");
        } else if ( GenericConstants.SU_TYPE.equals(componentType) ) {
            label = Messages.getString("jbi.statistics.su.label");
        }
        return label;
    }
    
    public String getTitle() {
        //return getName()+" - " + Messages.getString("statistics_title");
        return getTitle("jbi.statistics.title");
    }
    
    public String getTableTitle() {
        return getTableTitle("jbi.statistics.bc.tabletitle","jbi.statistics.se.tabletitle","jbi.statistics.su.tabletitle");
    }
    
    public String showGraph() {
        // switch to graphical view
        //System.out.println(">>>> show graphical view");
        return GenericConstants.SUCCESS;        //$NON-NLS-1$
    }

    
    ///////////////////////////////////////////////////
    // Charting support - return raw DisplayStatistics
    ///////////////////////////////////////////////////
    public List<DisplayStatistics> getProvisioningStatisticsList() {
        getStatisticsService();
        //String name = Util.mapComponentValue(cName,componentName);
        //String type = Util.mapComponentValue(cType,componentType);
        pStats = statsService.getProvisioningStatistics(componentName,componentType,cType,cName,pName);
        List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        if ( pStats!=null ) {
            for ( Iterator iter=pStats.iterator(); iter.hasNext(); ) {
                Statistics stats = (Statistics)iter.next();
                list.addAll(stats.generateDisplayStatistics());
            }
        }
        return list;
    }
    public List<DisplayStatistics> getConsumingStatisticsList() {
        getStatisticsService();
        cStats = statsService.getConsumingStatistics(componentName,componentType,cType,cName,pName);
        List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        if ( cStats!=null ) {
            for ( Iterator iter=cStats.iterator(); iter.hasNext(); ) {
                Statistics stats = (Statistics)iter.next();
                list.addAll(stats.generateDisplayStatistics());
            }
        }
        return list;
    }
    
    public Map<String, Double> getConsumingTotalsList() {
        Map<String, Double> consumingTotalsMap = new HashMap<String, Double>();
        double totalReceivedRequests = 0D,
                totalReceivedReplies = 0D,
                totalReceivedErrors = 0D,
                totalReceivedDones = 0D,
                totalSentRequests = 0D,
                totalSentReplies = 0D,
                totalSentErrors = 0D,
                totalSentDones = 0D;
        getStatisticsService();
        cStats = statsService.getConsumingStatistics(componentName,componentType,cType,cName,pName);
        //List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        if ( cStats!=null ) {
            for ( Iterator iter=cStats.iterator(); iter.hasNext(); ) {
                Statistics stats = (Statistics)iter.next();
                totalReceivedRequests += stats.getReceivedRequests();
                totalReceivedReplies +=  stats.getReceivedReplies();
                totalReceivedErrors +=  stats.getReceivedErrors();
                totalReceivedDones +=  stats.getReceivedDones();
                totalSentRequests +=  stats.getSentRequests();
                totalSentReplies +=  stats.getSentReplies();
                totalSentErrors +=  stats.getSentErrors();
                totalSentDones +=  stats.getSentDones();
            }
        }
        consumingTotalsMap.put(Messages.getString("statistics_receivedRequests"), new Double(totalReceivedRequests));
        consumingTotalsMap.put(Messages.getString("statistics_receivedReplies"), new Double(totalReceivedReplies));
        consumingTotalsMap.put(Messages.getString("statistics_receivedErrors"), new Double(totalReceivedErrors));
        consumingTotalsMap.put(Messages.getString("statistics_receivedDones"), new Double(totalReceivedDones));
        consumingTotalsMap.put(Messages.getString("statistics_sentRequests"), new Double(totalSentRequests));
        consumingTotalsMap.put(Messages.getString("statistics_sentReplies"), new Double(totalSentReplies));
        consumingTotalsMap.put(Messages.getString("statistics_sentErrors"), new Double(totalSentErrors));
        consumingTotalsMap.put(Messages.getString("statistics_sentDones"), new Double(totalSentDones));
        
        return consumingTotalsMap;
    }
    
    public Map<String, Double> getProvisioningTotalsList() {
        Map<String, Double> provisioningTotalsMap = new HashMap<String, Double>();
        double totalReceivedRequests = 0D,
                totalReceivedReplies = 0D,
                totalReceivedErrors = 0D,
                totalReceivedDones = 0D,
                totalSentRequests = 0D,
                totalSentReplies = 0D,
                totalSentErrors = 0D,
                totalSentDones = 0D;
        getStatisticsService();
        cStats = statsService.getProvisioningStatistics(componentName,componentType,cType,cName,pName);
        //List<DisplayStatistics> list = new ArrayList<DisplayStatistics>();
        if ( cStats!=null ) {
            for ( Iterator iter=cStats.iterator(); iter.hasNext(); ) {
                Statistics stats = (Statistics)iter.next();
                totalReceivedRequests += stats.getReceivedRequests();
                totalReceivedReplies +=  stats.getReceivedReplies();
                totalReceivedErrors +=  stats.getReceivedErrors();
                totalReceivedDones +=  stats.getReceivedDones();
                totalSentRequests +=  stats.getSentRequests();
                totalSentReplies +=  stats.getSentReplies();
                totalSentErrors +=  stats.getSentErrors();
                totalSentDones +=  stats.getSentDones();
            }
        }
        provisioningTotalsMap.put(Messages.getString("statistics_receivedRequests"), new Double(totalReceivedRequests));
        provisioningTotalsMap.put(Messages.getString("statistics_receivedReplies"), new Double(totalReceivedReplies));
        provisioningTotalsMap.put(Messages.getString("statistics_receivedErrors"), new Double(totalReceivedErrors));
        provisioningTotalsMap.put(Messages.getString("statistics_receivedDones"), new Double(totalReceivedDones));
        provisioningTotalsMap.put(Messages.getString("statistics_sentRequests"), new Double(totalSentRequests));
        provisioningTotalsMap.put(Messages.getString("statistics_sentReplies"), new Double(totalSentReplies));
        provisioningTotalsMap.put(Messages.getString("statistics_sentErrors"), new Double(totalSentErrors));
        provisioningTotalsMap.put(Messages.getString("statistics_sentDones"), new Double(totalSentDones));
        
        return provisioningTotalsMap;
    }
    
    ///////////////////////////////////////////////////
    
    
}
