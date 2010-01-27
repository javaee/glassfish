/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.util.*;
import java.net.URL;

import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import org.glassfish.api.embedded.Server;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

/**
 * Implementation for the CLI command verify-domain-xml
 * Verifies the content of the domain.xml file
 *
 * verify-domain-xml [--domaindir install_dir/domains] [domain_name]
 * 
 * @author Nandini Ektare
 */
@Service(name = "verify-domain-xml")
@Scoped(PerLookup.class)
public final class VerifyDomainXmlCommand extends LocalDomainCommand {

    private static final String DOMAINDIR = "domaindir";

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(VerifyDomainXmlCommand.class);

    /**
     * The prepare method must ensure that the commandOpts,
     * operandType, operandMin, and operandMax fields are set.
     */
    @Override
    protected void prepare()
            throws CommandException, CommandValidationException {
        Set<ValidOption> opts = new LinkedHashSet<ValidOption>();
        addOption(opts, DOMAINDIR, '\0', "STRING", false, null);
        addOption(opts, "help", '?', "BOOLEAN", false, "false");
        commandOpts = Collections.unmodifiableSet(opts);
        operandName = "domain_name";
        operandType = "STRING";
        operandMin = 0;
        operandMax = 1;

        //processProgramOptions();
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        File domainXMLFile = getDomainXml();
        logger.printDebugMessage("Domain XML file = " + domainXMLFile);
        try {
            Habitat habitat = Globals.getStaticHabitat();
            ConfigParser parser = new ConfigParser(habitat);
            URL domainURL = domainXMLFile.toURI().toURL();
            DomDocument doc = parser.parse(domainURL);
            Dom domDomain = doc.getRoot();
            Domain domain = domDomain.createProxy(Domain.class);            
            DomainXmlVerifier validator = new DomainXmlVerifier(domain);

            validator.invokeConfigValidator();
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
        return 0;
    }
}
