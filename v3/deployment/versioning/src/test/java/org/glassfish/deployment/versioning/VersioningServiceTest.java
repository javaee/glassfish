/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.deployment.versioning;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.api.deployment.DeployCommandParameters;
import com.sun.enterprise.config.serverbeans.*;

/**
 *
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */

public class VersioningServiceTest {

    private static final String APPLICATION_NAME = "foo";

    /**
     * Test of {@link org.glassfish.deployment.versioning.VersioningService.getUntaggedName}
     *
     * Check the extraction of untagged names from different application names
     * as version identifier, version expression or untagged application name.
     *
     * @throws VersioningSyntaxException if the given application name had some
     *  critical patterns.
     */
    @Test
    public void testGetUntaggedName() throws VersioningSyntaxException {

        // test an application name that contains a version expression
        // application name : foo:RC-*
        String expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-"
                + VersioningService.EXPRESSION_WILDCARD;

        String result = VersioningService.getUntaggedName(expression);
        assertEquals(APPLICATION_NAME, result);

        // test an application name that contains a version identifier
        // application name : foo:RC-1.0.0
        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.0";

        result = VersioningService.getUntaggedName(expression);
        assertEquals(APPLICATION_NAME, result);

        // test an application name that is an untagged version name
        // application name : foo
        expression = APPLICATION_NAME;

        result = VersioningService.getUntaggedName(expression);
        assertEquals(APPLICATION_NAME, result);

        // test an application name containing a critical pattern
        // application name : foo:
        expression = APPLICATION_NAME + VersioningService.EXPRESSION_SEPARATOR;

        try {
            result = VersioningService.getUntaggedName(expression);
            fail("the getUntagged method did not throw a VersioningSyntaxException");
        }
        catch(VersioningSyntaxException e){}
    }

    /**
     * Test of {@link org.glassfish.deployment.versioning.VersioningService.getExpression}
     *
     * Check the extraction of version expression / identifier from different
     * application names.
     *
     * @throws VersioningSyntaxException if the given application name had some
     *  critical patterns.
     */
    @Test
    public void testGetExpression() throws VersioningSyntaxException {

        // test an application name containing a critical pattern
        // application name : foo:
        String expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR;

        try {
            String result = VersioningService.getExpression(expression);
            fail("the getExpression method did not throw a VersioningSyntaxException");
        } catch (VersioningSyntaxException e) {}

        // test an application name containing a critical pattern
        // application name : foo:RC-1;0.0
        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + "RC-1"
                + VersioningService.EXPRESSION_SEPARATOR
                + "0.0";

        try {
            String result = VersioningService.getExpression(expression);
            //fail("the getExpression method did not throw a VersioningSyntaxException");
        } catch (VersioningSyntaxException e) {}
    }

    /**
     * Test of {@link org.glassfish.deployment.versioning.VersioningService.getVersions}
     *
     * Check the extraction of a set of version(s) from a set of applications.
     */
    @Test
    public void testGetVersions() throws VersioningException {
        // the set of applications
        List<Application> listApplications = new ArrayList<Application>();
        listApplications.add(new ApplicationTest(APPLICATION_NAME));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                VersioningService.EXPRESSION_SEPARATOR+"BETA-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                VersioningService.EXPRESSION_SEPARATOR+"RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                "_RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                ";RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                ".RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                "-RC-1.0.0"));
        listApplications.add(new ApplicationTest(APPLICATION_NAME+
                APPLICATION_NAME));

        // the expected set of versions
        List<String> expResult = new ArrayList<String>();
        expResult.add(APPLICATION_NAME);
        expResult.add(APPLICATION_NAME+
                VersioningService.EXPRESSION_SEPARATOR+"BETA-1.0.0");
        expResult.add(APPLICATION_NAME+
                VersioningService.EXPRESSION_SEPARATOR+"RC-1.0.0");

        List result = VersioningService.getVersions(APPLICATION_NAME, listApplications);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of {@link org.glassfish.deployment.versioning.VersioningService.matchExpression}
     *
     * Check the matching of version expression over a set of version
     * 
     * @throws VersioningException for registration issues, or if the given
     *  application name had some
     *  critical patterns.
     */
    @Test
    public void testMatchExpression() throws VersioningException {
        // the set of all foo versions
        List<String> listVersion = new ArrayList<String>();
        // ALPHA versions
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.0.0");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.0.1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.0.2");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.1.0");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.1.1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.1.2");
        // BETA versions
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.0.0");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.0.1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.0.2");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.1.0");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.1.1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.1.2");
        // RC versions
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.0");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.2");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.1.0");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.1.1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.1.2");

        // **************************************************
        // TEST TYPE 1 : expression matching all the versions
        // **************************************************

        // the expected set of matched version is all the versions
        List expResult = new ArrayList<String>(listVersion);

        // ------------------------
        // application name foo:*
        // ------------------------

        String expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD;

        List result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // -----------------------------
        // application name foo:******
        // -----------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD;

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // *****************************************************
        // TEST TYPE 2 : expression matching all the RC versions
        // *****************************************************
        expResult.clear();
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.0");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.1");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.2");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.1.0");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.1.1");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.1.2");

        // --------------------------
        // application name foo:RC*
        // --------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC"
                + VersioningService.EXPRESSION_WILDCARD;

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // --------------------------
        // application name foo:*RC*
        // --------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD + "RC"
                + VersioningService.EXPRESSION_WILDCARD;

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // -------------------------------
        // application name foo:***RC***
        // -------------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD + "RC"
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD;

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // ********************************************************
        // TEST TYPE 3 : expression matching all the 1.0.2 versions
        // ********************************************************
        expResult.clear();
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.0.2");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "BETA-1.0.2");
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "RC-1.0.2");

        // ------------------------------
        // application name foo:*-1.0.2
        // ------------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD + "-1.0.2";

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // ----------------------------------
        // application name foo:***1.0.2***
        // ----------------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD + "-1.0.2"
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD;

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // ----------------------------------
        // application name foo:***1*0*2***
        // ----------------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD + "1"
                + VersioningService.EXPRESSION_WILDCARD + "0"
                + VersioningService.EXPRESSION_WILDCARD + "2"
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD
                + VersioningService.EXPRESSION_WILDCARD;

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // **************************************
        // TEST TYPE 4 : identifier as expression
        // **************************************
        expResult.clear();
        expResult.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.0.2");

        // ----------------------------------
        // application name foo:ALPHA-1.0.2
        // ----------------------------------

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "ALPHA-1.0.2";

        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(expResult, result);

        // *****************************************
        // check for pattern matching like issue 12132
        // *****************************************

        listVersion.clear();
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "abc-1");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "abc-2");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "abc-3");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "bac-4");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "cab-5");
        listVersion.add(APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "cba-6");
        
        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "a*";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 3);

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "*a";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 0);

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "a****1";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 1);

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "*-*";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 6);

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "*-4";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 1);

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "b*";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 1);

        expression = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR + "b*";
        result = VersioningService.matchExpression(listVersion, expression);
        assertEquals(result.size(), 1);
    }

    /**
     * Test of getIdentifier method, of class VersioningService.
     * @throws VersioningException
     */
    @Test
    public void testGetIdentifier() throws VersioningException {
        // *****************************************
        // check for getIdentifier with and without '*'
        // *****************************************
        String versionIdentifier = "BETA-1";
        String appName = "foo" + VersioningService.EXPRESSION_SEPARATOR + versionIdentifier;
        try{
            VersioningService.checkIdentifier(appName);
        } catch (VersioningSyntaxException e){
            fail(e.getMessage());
        }

        String versionExpression = "BETA-*";
        appName = "foo" + VersioningService.EXPRESSION_SEPARATOR + versionExpression;
        try {
            VersioningService.checkIdentifier(appName);
            fail("the getIdentifier method should not accept version with '*' in it.");
        } catch (VersioningException e) {}
     }
    /**
     * Test of getRepositoryName method, of class VersioningService.
     * @throws VersioningSyntaxException
     */
    @Test
    public void testGetRepositoryName() throws VersioningSyntaxException {
        String versionIdentifier = "RC-1.0.0";

        // application name foo:RC-1.0.0
        String appName = APPLICATION_NAME
                + VersioningService.EXPRESSION_SEPARATOR
                + versionIdentifier;

        String expectedResult = APPLICATION_NAME
                + VersioningService.REPOSITORY_DASH
                + versionIdentifier;

        String result = VersioningService.getRepositoryName(appName);
        assertEquals(expectedResult, result);
    }

    // this class is used to fake the List<Application> 
    // so we can call the VersioningService.matchExpression
    // with an home made set of applications.
    private class ApplicationTest implements Application {
        private String name;

        public ApplicationTest(String value){
            this.name = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String value) throws PropertyVetoException{
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContextRoot() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setContextRoot(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLocation(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getObjectType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setObjectType(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setEnabled(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLibraries() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLibraries(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getAvailabilityEnabled() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAvailabilityEnabled(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDirectoryDeployed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDirectoryDeployed(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDescription(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Module> getModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Engine> getEngine() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<WebServiceEndpoint> getWebServiceEndpoint() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Module getModule(String moduleName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Properties getDeployProperties() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public DeployCommandParameters getDeployParameters(ApplicationRef appRef) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, Properties> getModulePropertiesMap() {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public boolean isStandaloneModule() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsSnifferType(String snifferType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void recordFileLocations(File app, File plan) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File application() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File deploymentPlan() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Property> getProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property getProperty(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPropertyValue(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConfigBeanProxy getParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ConfigBeanProxy> T getParent(Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void injectedInto(Object target) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


        @Override
        public <T extends ConfigBeanProxy> T createChild(Class<T> type)
               throws TransactionFailure {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConfigBeanProxy deepCopy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
