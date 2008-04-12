/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.cli.RemoteCommand;
import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.xml.MiniXmlParser;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * General purpose methods for CLI remote commands
 * There are many overloaded methods as a convenience so you can get an admin port or 
 * ping DAS with whatever information you happen to have lying around.
 * @author bnevins
 */
class RemoteUtils {
    /**
     * @param domainXml The main configuration file for the domain
     * @return an array of admin ports - there might be more than one
     * @throws com.sun.enterprise.cli.framework.CommandValidationException if there is
     * a parser error or if there are no admin ports
     */
    static int[] getAdminPorts(File domainXml) throws CommandValidationException
    {
        try {
            MiniXmlParser parser = new MiniXmlParser(domainXml);
            Set<Integer> portsSet = parser.getAdminPorts();
            
            if(portsSet.size() <= 0) {
                throw new CommandValidationException(strings.get("CLIUtils.parserError", 
                            strings.get("CLIUtils.parserErrorNoAdminPort")));
            }

            int[] ports = new int[portsSet.size()];
            int i = 0;

            for(Integer port : portsSet) {
                ports[i++] = port;
            }
            return ports;
        }
        catch (MiniXmlParserException ex) {
            throw new CommandValidationException(strings.get("CLIUtils.parserError", ex), ex);
        }
    }

    /**
     * @param domainXml The main configuration file for the domain
     * @return an admin port - there might be more than one, we return the first one
     * @throws com.sun.enterprise.cli.framework.CommandValidationException if there is
     * a parser error or if there are no admin ports
     */
    static int getAdminPort(File domainXml) throws CommandValidationException
    {
       return getAdminPorts(domainXml)[0]; 
    }

    
    static File getDomainXml(String domainsDir, String domainName) throws CommandValidationException {
        File domainRootDir = new File(new File(domainsDir), domainName);
        return getDomainXml(domainRootDir);
    }

    static File getDomainXml(File domainRootDir) throws CommandValidationException {
        File domainXml = new File(domainRootDir, "config/domain.xml");
        
        if (!domainXml.canRead()) {
            throw new CommandValidationException(
                    strings.get("CLIUtils.noDomainXml", domainXml));
        }

        return SmartFile.sanitize(domainXml);
    }
    
    static boolean pingDAS(File domainXml) throws CommandValidationException {
        return pingDAS(getAdminPort(domainXml));
    }
    
    static boolean pingDAS(String domainsDir, String domainName) throws CommandValidationException {
        return pingDAS(getDomainXml(domainsDir, domainName));
    }
    
    static boolean pingDAS(int port) {
        try {
            CLILogger.getInstance().pushAndLockLevel(Level.WARNING);
            return RemoteCommand.pingDAS(port);
        }
        finally {
           CLILogger.getInstance().popAndUnlockLevel(); 
        }
    }
   /**
     * It either throws an Exception or returns a valid directory
     * @param parent
     * @return
     * @throws com.sun.enterprise.cli.framework.CommandValidationException
     */
    static File getTheOneAndOnlyDomain(File parent) throws CommandValidationException {
        // look for subdirs in the parent dir -- there must be one and only one

        File[] files = parent.listFiles(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        if (files == null || files.length == 0) {
            throw new CommandValidationException(
                    strings.get("noDomainDirs", parent));
        }

        if (files.length > 1) {
            throw new CommandValidationException(
                    strings.get("CLIUtils.tooManyDomainDirs", parent));
        }

        return files[0];
    }

     
    private RemoteUtils() {
    }
    
    private final static LocalStringsImpl strings = new LocalStringsImpl(RemoteUtils.class);
}
