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
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

public class ListTransformationRulesCommand extends BaseTransformationRuleCommand
{
    private static final String WEB_SERVICE_OPTION = "webservicename";
   
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
            //DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
            //final Set s = domainRoot.getQueryMgr().queryJ2EETypeSet(XTypes.WEB_SERVICE_ENDPOINT_CONFIG);
            String webServiceName = getOption(WEB_SERVICE_OPTION);
            validateWebServiceName(webServiceName, false);
            boolean nothingToList = listTransformationRules(mbsc);
            if (nothingToList)
            {
                CLILogger.getInstance().printMessage(getLocalizedString("NoElementsToList"));
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
     *  Iterates throught WSEP config to find the transformation rules.
     *  @throws CommandException
     */
    private boolean listTransformationRules(MBeanServerConnection mbsc) 
                    throws CommandException, CommandValidationException
    {
        String webServiceName = getOption(WEB_SERVICE_OPTION);
        if (webServiceName != null)
        {
            WebServiceEndpointConfig wsc = 
                    getWebServiceEndpointConfig(mbsc, webServiceName, false);
            if (wsc == null)
                throw new CommandException(getLocalizedString("CannotFindWebservice"));
            return displayTransformationRules(wsc);
        }
        DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
        final Set s = domainRoot.getQueryMgr().queryJ2EETypeSet(XTypes.WEB_SERVICE_ENDPOINT_CONFIG);
        final Iterator iter = s.iterator();
        boolean nothingToList = true;
        while (iter.hasNext() )
        {
            final WebServiceEndpointConfig wsc = (WebServiceEndpointConfig)iter.next();
            if (wsc.getTransformationRuleConfigMap().size() > 0)
            {
                CLILogger.getInstance().printMessage(wsc.getName());
                nothingToList = displayTransformationRules(wsc);
            }
        }
        return nothingToList;
    }


    /**
     *  Display the transformation rules.
     *  @throws CommandException
     */
    private boolean displayTransformationRules(WebServiceEndpointConfig wsc) 
                    throws CommandException
    {
        Map rules = wsc.getTransformationRuleConfigMap();
        for (Object key : rules.keySet())
        {
            CLILogger.getInstance().printMessage((String)key);
        }
        if (rules.size() > 0) 
            return false;
        else
            return true;
    }
}
