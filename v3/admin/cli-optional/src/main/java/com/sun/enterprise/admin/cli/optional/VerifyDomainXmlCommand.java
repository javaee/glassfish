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

package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.config.serverbeans.Domain;
import java.io.File;
import java.net.URL;
import org.glassfish.api.embedded.Server;
import org.jvnet.hk2.component.Habitat;

import com.sun.enterprise.cli.framework.CommandValidationException;
import com.sun.enterprise.cli.framework.CommandException;
import com.sun.enterprise.cli.framework.CLILogger;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

/**
 * Implementation for the CLI command verify-domain-xml
 * Verifies the content of the domain.xml file
 *
 * verify-domain-xml [--domaindir install_dir/domains] [domain_name]
 *                   [--help] [--verbose=false]
 * 
 * @author Nandini Ektare
 */
public class VerifyDomainXmlCommand extends BaseLifeCycleCommand {

    private static final String CONFIG = "config";
    private static final String DOMAIN_XML = "domain.xml";
    private String domainName;
    private String domainsDir;
    private boolean terse = false;
    private boolean verbose = false;

    @Override
    public void runCommand()
    throws CommandException, CommandValidationException {
        
        String domainXMLFile = getDomainXMLFile();
        File f = new File(domainXMLFile);
        if (!f.exists()) {
            throw new CommandException(
                getLocalizedString("verify.domainxml.DomainXMLDoesNotExist",
                                   new Object[]{getDomainName()}));
        }
        try {
            Server server = new Server.Builder("dummylaunch").build();
            server.start();
            Habitat habitat = server.getHabitat();
            ConfigParser parser = new ConfigParser(habitat);
            URL domainURL = (new File(domainXMLFile)).toURI().toURL();
            DomDocument doc = parser.parse(domainURL);
            Dom domDomain = doc.getRoot();
            Domain domain = domDomain.createProxy(Domain.class);            
            DomainXmlVerifier validator = new DomainXmlVerifier(domain);

            validator.invokeConfigValidator();
            CLILogger.getInstance().printDetailMessage(getLocalizedString(
                                                       "CommandSuccessful",
                                                       new Object[] {name}));
        } catch(Exception e) {
            throw new CommandException(e.getMessage());
        }
    }

    /**
     *  This method returns the xml file location for the given domain
     *  @return String returns the domainXMLFile
     */
    private String getDomainXMLFile() 
    throws CommandException, CommandValidationException {
        
        domainsDir = getDomainsRoot();
        domainName = getDomainName();
        checkOptions();
        String domainXMLFile = 
            domainsDir + File.separator + domainName + File.separator +
            CONFIG + File.separator + DOMAIN_XML;
        CLILogger.getInstance().printDebugMessage(
            "Domain XML file = " + domainXMLFile);
        return domainXMLFile;
    }

    /**
     * A method that checks the options and operand that the user supplied.
     * These tests are slightly different for different CLI commands
     */
    private void checkOptions() throws CommandValidationException {
        // make sure we have a domainsDir
        if(domainsDir == null || domainsDir.length() <= 0) {
            throw new CommandValidationException(getLocalizedString(
                "InvalidDomainPath", new String[] {domainsDir}) );
        }
        // make sure domainsDir exists and is a directory
        File domainsDirFile = new File(domainsDir);
        if(!domainsDirFile.isDirectory()) {
            throw new CommandValidationException(getLocalizedString(
                "InvalidDomainPath", new String[] {domainsDir}) );
        }
    }
}