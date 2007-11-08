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

import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import com.sun.enterprise.admin.pluggable.ClientPluggableFeatureFactory;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.InstancesManager;
import com.sun.enterprise.admin.common.Status;
import java.io.File;

/**
 *  This command class stops the domain
 *  @version  $Revision: 1.7 $
 */
public class StopDomainCommand extends BaseLifeCycleCommand {
    
    /**
     *  An abstract method that validates the options
     *  on the specification in the xml properties file
     *  This method verifies for the correctness of number of
     *  operands and if all the required options are supplied by the client.
     *  @return boolean returns true if success else returns false
     */
    public boolean validateOptions() throws CommandValidationException {
        return super.validateOptions();
    }
    
    public void stopDomain (String domainName) throws CommandException, CommandValidationException 
    {
        try {            
            final ClientPluggableFeatureFactory fac     = getFeatureFactory();
            final DomainsManager                mgr     = fac.getDomainsManager();
            final DomainConfig                  cfg     = getDomainConfig(domainName);
            final InstancesManager              im      = mgr.getInstancesManager(cfg);
            final int                           state   = im.getInstanceStatus();
            final String[]			domains = mgr.listDomains(cfg);
            boolean				exists	= false;
			
            for(int i = 0; domains != null && i < domains.length; i++)
     	    {
                if(domains[i].equals(domainName))
                {
                    exists = true;
                    break;
                }
            }

            if(!exists)
            {
                // it doesn't exist -- let the existing code throw an Exception with
		// the correct error message
               stopDomain(mgr, cfg);
            }

            // check if the Domain is running
            else if (state == Status.kInstanceRunningCode)
            {
                stopDomain(mgr, cfg);
            
                CLILogger.getInstance().printDetailMessage(getLocalizedString("DomainStopped",
                                                           new Object[] {domainName}));
            }
            else if (state == Status.kInstanceStartingCode)
            {
                stopDomain(mgr, cfg);
            
                CLILogger.getInstance().printDetailMessage(getLocalizedString("DomainStoppedWasStarting",
                                                           new Object[] {domainName}));
            }
            else
            {
                //print the message if domain already stopped
                CLILogger.getInstance().printDetailMessage(getLocalizedString("CannotStopDomainAlreadyStopped",
                                                                              new Object[] {domainName}));
            }
            unconfigureAddons();
        }
        catch(Exception e) {
            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());         
            throw new CommandException(getLocalizedString("CannotStopDomain",
                                                           new Object[] {domainName} ), e);
        }
    }
    
    /**
     *  An abstract method that Executes the command
     *  @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException {
        validateOptions();
        String domainName = null;
        try {
            domainName = getDomainName();
        } catch(Exception e) {
            CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
            domainName = domainName==null?getLocalizedString("Undefined"):domainName;
            throw new CommandException(getLocalizedString("CannotStopDomain",
                                                          new Object[] {domainName} ), e);
        }
        stopDomain(domainName);
    }

    private void unconfigureAddons() {
        try {
            AddonControl ac = new AddonControl();
            String domainInstanceRoot = getDomainsRoot() + File.separator + getDomainName();
            ac.unconfigureDAS(new File(domainInstanceRoot));
        }catch(Throwable t) {
            CLILogger.getInstance().printDetailMessage(t.getLocalizedMessage());
        }

    }
    
    private void stopDomain(DomainsManager mgr, DomainConfig cfg) 
    throws DomainException {
        String forceOption = getCLOption(KILL);
        if (forceOption == null) {
            mgr.stopDomain(cfg);
        } else {
            int timeout = getIntegerOption(KILL);
            mgr.stopDomainForcibly(cfg, timeout);
        }
    }
}
