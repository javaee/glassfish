/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
/**
 * TestEnv -- static methods for figuring out directories, files, etc.
 *
 *
 * @author Byron Nevins
 */
package admin;

import java.io.File;

public final class TestEnv {
    public static boolean isHadas() {
        return isHadas;
    }
    public static boolean isV3Layout() {
        return !isHadas();
    }
    public static boolean isV4Layout() {
        return isHadas();
    }
    public static File getGlassFishHome() {
        return gf_home;
    }
    public static File getDomainsHome() {
        return domains_home;
    }
    public static File getDomainHome(String domainName) {
        return new File(getDomainsHome(), domainName);
    }
    public static File getDomainServerHome(String domainName) {
        if(isHadas())
            return new File(getDomainHome(domainName), "server");
        else
            return getDomainHome(domainName);
    }
    public static File getDomainDocRoot(String domainName) {
        return new File(getDomainServerHome(domainName), DOCROOT);
    }
    public static File getDomainConfigDir(String domainName) {
        return new File(getDomainServerHome(domainName), CONFIG);
    }
    public static File getInfoDir(String domainName) {
        return new File(getDomainServerHome(domainName), INFO_DIRECTORY);
    }
    public static File getDomainXml(String domainName) {
        return new File(getDomainConfigDir(domainName), DOMAIN_XML);
    }
    public static File getConfigSpecificConfigDir(String domainName, String instanceName) {
        return new File(getDomainConfigDir(domainName), instanceName + "-config");
    }
    public static File getConfigSpecificDocRoot(String domainName, String instanceName) {
        return new File(getConfigSpecificConfigDir(domainName, instanceName), DOCROOT);
    }
    public static File getNodesHome() {
        if(isHadas())
            return getDomainsHome();
        else
            return new File(getGlassFishHome(), "nodes");
    }
    public static File getDasPropertiesFile(String nodeName) {
        // it goes in one of these (both are the DEFAULT locations for illustration)
        if (isHadas())
            return new File(TestEnv.getDomainHome(), DAS_PROPS_PATH);
        else
            return new File(TestEnv.getInstancesHome(nodeName), DAS_PROPS_PATH);
    }
    
    public static File getInstancesHome(String domainName, String nodeName) {
        if(isHadas())
            return getDomainHome(domainName);
        else
            return new File(getNodesHome(), nodeName);
    }
    public static File getInstanceDir(String domainName, String nodeName, String instanceName) {
        return new File(getInstancesHome(domainName, nodeName), instanceName);
    }
    public static File getInstanceConfigDir(String domainName, String nodeName, String instanceName) {
        return new File(getInstanceDir(domainName, nodeName, instanceName), CONFIG);
    }
    public static File getInstanceDomainXml(String domainName, String nodeName, String instanceName) {
        return new File(getInstanceConfigDir(domainName, nodeName, instanceName), DOMAIN_XML);
    }
    public static File getDomainLog(String domainName) {
        return new File(getDomainServerHome(domainName), SERVER_LOG);
    }
    public static File getDomainInfoXml(String domainName) {
        return new File(getInfoDir(domainName), DOMAIN_INFO_XML);
    }
    // ***************************
    // convenience methods that plug-in "domain1"
    // ****************************/

    public static File getDomainHome() {
        return getDomainHome(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainServerHome() {
        return getDomainServerHome(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainDocRoot() {
        return getDomainDocRoot(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainConfigDir() {
        return getDomainConfigDir(DEFAULT_DOMAIN_NAME);
    }
    public static File getDomainXml() {
        return getDomainXml(DEFAULT_DOMAIN_NAME);
    }
    public static File getConfigSpecificConfigDir(String instanceName) {
        return getConfigSpecificConfigDir(DEFAULT_DOMAIN_NAME, instanceName);
    }
    public static File getConfigSpecificDocRoot(String instanceName) {
        return getConfigSpecificDocRoot(DEFAULT_DOMAIN_NAME, instanceName);
    }
    public static File getInstancesHome(String nodeName) {
        return getInstancesHome(DEFAULT_DOMAIN_NAME, nodeName);
    }
    public static File getInstanceDir(String nodeName, String instanceName) {
        return getInstanceDir(DEFAULT_DOMAIN_NAME, nodeName, instanceName);
    }
    public static File getInstanceConfigDir(String nodeName, String instanceName) {
        return getInstanceConfigDir(DEFAULT_DOMAIN_NAME, nodeName, instanceName);
    }
    public static File getInstanceDomainXml(String nodeName, String instanceName) {
        return getInstanceDomainXml(DEFAULT_DOMAIN_NAME, nodeName, instanceName);
    }
    public static File getDomainLog() {
        return new File(getDomainServerHome(), SERVER_LOG);
    }
    public static File getDefaultTemplateDir() {
        return new File(gf_home, DEFUALT_TEMPLATE_RELATIVE_PATH);
    }

    ///////////////////////////////////////////////////////////////////////
    //  internal stuff below
    //////////////////////////////////////////////////////////////////////
    private TestEnv() {
        // no instances allowed!
    }

    private static final boolean isHadas;
    private static final File gf_home;
    private static final File domains_home;
    private static final String DEFAULT_DOMAIN_NAME = "domain1";
    private static final String DOCROOT = "docroot";
    private static final String CONFIG = "config";
    private static String DOMAIN_XML = "domain.xml";
    private static final String DAS_PROPS_PATH = "agent/config/das.properties";
    private final static String SERVER_LOG = "logs/server.log";

    /** Name of directory stores the domain information. */
    private static final String INFO_DIRECTORY = "init-info";
    /** The file name stores the basic domain information. */
    private static final String DOMAIN_INFO_XML = "domain-info.xml";
    private final static String DEFUALT_TEMPLATE_RELATIVE_PATH = "common" + File.separator + "templates" + File.separator + "gf";

    static {
        isHadas = Boolean.getBoolean("HADAS")
                || Boolean.getBoolean("hadas")
                || Boolean.parseBoolean(System.getenv("hadas"))
                || Boolean.parseBoolean(System.getenv("HADAS"));

        File gf_homeNotFinal = null;

        try {
            String home = System.getenv("S1AS_HOME");

            if (home == null) {
                gf_homeNotFinal = null;
                throw new IllegalStateException("No S1AS_HOME set!");
            }

            gf_homeNotFinal = new File(home);

            try {
                gf_homeNotFinal = gf_homeNotFinal.getCanonicalFile();
            }
            catch (Exception e) {
                gf_homeNotFinal = gf_homeNotFinal.getAbsoluteFile();
            }

            if (!gf_homeNotFinal.isDirectory()) {
                gf_homeNotFinal = null;
                throw new IllegalStateException("S1AS_HOME is not pointing at a real directory!");
            }
        }
        catch(IllegalStateException e) {
            // what's the point of struggling on?
            System.out.println("#####  CATASTROPHIC ERROR -- You must set S1AS_HOME to point to the GlassFish installation directory");
            System.exit(2);
        }
        finally {
            gf_home = gf_homeNotFinal;
            domains_home = new File(gf_home, "domains");
        }
    }
}
