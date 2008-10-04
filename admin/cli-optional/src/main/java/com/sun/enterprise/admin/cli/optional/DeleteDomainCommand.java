/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

/*
 *  $Id: DeleteDomainCommand.java,v 1.4 2006/02/02 00:21:45 pa100654 Exp $
 */

package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.DomainConfig;

import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;

/**
 * Deletes a domain of the Application server
 */
public class DeleteDomainCommand extends BaseLifeCycleCommand 
{


    /** Creates new DeleteDomainCommand */
    public DeleteDomainCommand() 
    {
    }

    /**
     * Validates the Options for correctness
     * @return boolean returns true if validation is succesful else false
     */
    public boolean validateOptions() throws CommandValidationException 
    {
        return super.validateOptions();
    }
    
    /**
     * Executes the command
     * @throws CommandException
     */
    public void runCommand() throws CommandException, CommandValidationException 
    {
        validateOptions();
	
	String domainName = null;
        try
        {            
	    domainName = (String)operands.firstElement();
            DomainConfig domainConfig = getDomainConfig(domainName);
            DomainsManager manager = new PEDomainsManager();
            manager.deleteDomain(domainConfig);
            deleteLoginInfo();
        }
        catch (Exception e)
        {
	    CLILogger.getInstance().printDetailMessage(e.getLocalizedMessage());
	    throw new CommandException(getLocalizedString("CouldNotDeleteDomain",
							  new Object[] {domainName}));
        }

	CLILogger.getInstance().printDetailMessage(getLocalizedString("DomainDeleted",
                                             new Object[] {domainName}));
    }


    /**
     * This method will delete the entry in the .asadminpass file if exists
     */
    private void deleteLoginInfo() throws CommandValidationException 
    {
        return;
    }
    
}
