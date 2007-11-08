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
import com.sun.enterprise.admin.common.JMXFileTransfer;
import javax.management.MBeanServerConnection;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import com.sun.appserv.management.base.UploadDownloadMgr;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class CreateTransformationRuleCommand extends BaseTransformationRuleCommand
{
    private static final String RULE_LOCATION_OPTION = "rulefilelocation";
    private static final String APPLY_TO_OPTION = "applyto";
    private static final String ENABLED_OPTION = "enabled";
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
            DomainRoot domainRoot = ProxyFactory.getInstance(mbsc).getDomainRoot();
            //final Set s = domainRoot.getQueryMgr().queryJ2EETypeSet(XTypes.WEB_SERVICE_ENDPOINT_CONFIG);
            String webServiceName = getOption(WEB_SERVICE_OPTION);
            validateWebServiceName(webServiceName, true);
            WebServiceEndpointConfig wsc = 
                    getWebServiceEndpointConfig(mbsc, webServiceName, true);
            if (wsc == null)
                throw new CommandException(getLocalizedString("CannotFindWebservice"));
            String ruleName = (String) getOperands().get(0);
            String ruleLocation = getOption(RULE_LOCATION_OPTION);
            boolean enabled = getBooleanOption(ENABLED_OPTION);
            String applyTo = getOption(APPLY_TO_OPTION);
            // please remember the file needs to uploaded to the location mentioned, then only
            // transformation rule can be active.
			File ruleFile = new File(ruleLocation);
			String msg = null;
			
			if (! ruleFile.exists()) 
                msg = getLocalizedString("FileDoesNotExist", new Object[] {ruleLocation});
			else if(ruleFile.length() <= 0)
                msg = getLocalizedString("FileIsEmpty", new Object[] {ruleLocation});
			else if(! ruleFile.canRead())
                msg = getLocalizedString("FileNotReadable", new Object[] {ruleLocation});

			if(msg != null)
				throw new CommandValidationException(msg);
			
            String remoteLocation = uploadFileToServer(mbsc);
            //File uploadedFile = uploadFile(domainRoot, ruleLocation);
			
            CLILogger.getInstance().printDebugMessage("uploadedFile = " + 
                                            remoteLocation);
            wsc.createTransformationRuleConfig(ruleName,
                    remoteLocation, enabled, applyTo, null);

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
     *Uploads file to temp location on server.
     *@throws CommandException
     */
    private String uploadFileToServer(MBeanServerConnection mbsc) 
                        throws CommandException, IOException
    {
        String ruleLocation = getOption(RULE_LOCATION_OPTION);
        return new JMXFileTransfer(mbsc).uploadFile(ruleLocation);
    }
}
