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
import javax.management.MBeanServerConnection;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import com.sun.appserv.management.config.WebServiceEndpointConfigKeys;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ConfigureWebServiceCommand extends BaseTransformationRuleCommand
{
    private static final String MONITORING_OPTION = "monitoring";
    private static final String MAX_HISTORY_SIZE_OPTION = "maxhistorysize";
   
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException
    {
        validateOptions();
        try
        { 
            MBeanServerConnection mbsc = getMBeanServerConnection(getHost(), getPort(), 
                                                                  getUser(), getPassword());
            DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
            //final Set s = domainRoot.getQueryMgr().queryJ2EETypeSet(XTypes.WEB_SERVICE_ENDPOINT_CONFIG);
            String webServiceName = (String) getOperands().get(0);
            validateWebServiceName(webServiceName, true);
            WebServiceEndpointConfig wsc = 
                    getWebServiceEndpointConfig(mbsc, webServiceName, true);
            if (wsc == null)
                throw new CommandException(getLocalizedString("CannotFindWebservice"));

            String maxHistorySize = getOption(MAX_HISTORY_SIZE_OPTION);
            String monitoring = getOption(MONITORING_OPTION);
            if (maxHistorySize != null)
            {
                wsc.setMaxHistorySize(maxHistorySize);
            }
            if (monitoring != null)
            {
                wsc.setMonitoringLevel(monitoring);
            }
	    CLILogger.getInstance().printDetailMessage(getLocalizedString(
						       "CommandSuccessful",
						       new Object[] {name}));
        }
        catch(Exception e)
        {
            displayExceptionMessage(e);
        }        
    
    }


    /**
     *  parse the operand to get the web service name
     *  Also validate if the operand is well formed.
     *  @throws CommandValidationException
     */
    private String getWebServiceName() throws CommandException
    {
        String operand = (String) getOperands().get(0);
        StringTokenizer paramsTokenizer = new StringTokenizer(operand, "#");
        int size = paramsTokenizer.countTokens();
        if (size != 3)
            throw new CommandException(getLocalizedString("InvalidWebServiceEndpoint"));
        return paramsTokenizer.nextToken();
    }


    /**
     *  parse the operand to get the webservice endpoint
     *  @throws CommandValidationException
     */
    private String getWebServiceEndPoint() throws CommandException
    {
        String operand = (String) getOperands().get(0);
        int index = operand.indexOf("#");
        return operand.substring(index+1);
    }


    /**
     *  return the map containing the monitoring & maxhistorysize attrs.
     *  @throws CommandValidationException
     */
    private Map getOptionMap() throws CommandException
    {
        HashMap map = new HashMap();
        String maxHistorySize = getOption(MAX_HISTORY_SIZE_OPTION);
        String monitoring = getOption(MONITORING_OPTION);
        map.put(WebServiceEndpointConfigKeys.MONITORING_LEVEL_KEY, monitoring);
        map.put(WebServiceEndpointConfigKeys.MAX_HISTORY_SIZE_KEY, maxHistorySize);
        return map;
    }
}
