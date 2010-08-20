/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.deployment.versioning;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Application;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.deployment.common.VersioningDeploymentException;
import org.glassfish.deployment.common.VersioningDeploymentSyntaxException;
import org.glassfish.deployment.common.VersioningDeploymentUtil;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 * This service provides methods to handle application names
 * in the versioning context
 *
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */
@I18n("versioning.service")
@Service
@Scoped(PerLookup.class)
public class VersioningService {

    @Inject
    private CommandRunner commandRunner;
    @Inject
    private Domain domain;

    /**
     * Extract the set of version(s) of the given application represented as
     * an untagged version name
     *
     * @param untaggedName the application name as an untagged version : an
     * application name without version identifier
     * @param target the target where we want to get all the versions
     * @return all the version(s) of the given application
     */
    public final List<String> getAllversions(String untaggedName, String target) {
        List<Application> allApplications = null;
        if (target != null) {
            allApplications = domain.getApplicationsInTarget(target);
        } else {
            allApplications = domain.getApplications().getApplications();
        }
        return VersioningDeploymentUtil.getVersions(untaggedName, allApplications);
    }

    /**
     * Search for the enabled version of the given application.
     *
     * @param name the application name
     * @param target an option supply from admin command, it's retained for
     * compatibility with other releases
     * @return the enabled version of the application, if exists
     * @throws VersioningSyntaxException if getUntaggedName throws an exception
     */
    public final String getEnabledVersion(String name, String target)
            throws VersioningDeploymentSyntaxException {

        String untaggedName = VersioningDeploymentUtil.getUntaggedName(name);
        List<String> allVersions = getAllversions(untaggedName, target);

        if (allVersions != null) {
            Iterator it = allVersions.iterator();

            while (it.hasNext()) {
                String app = (String) it.next();

                // if a version of the app is enabled
                if (domain.isAppEnabledInTarget(app, target)) {
                    return app;
                }
            }
        }
        // no enabled version found
        return null;
    }
    
    /**
     * Process the expression matching operation of the given application name.
     *
     * @param name the application name containing the version expression
     * @param target the target we are looking for the verisons 
     * @return a List of all expression matched versions, return empty list
     * if no version is registered on this target
     * or if getUntaggedName throws an exception
     */
    public final List<String> getMatchedVersions(String name, String target)
            throws VersioningDeploymentSyntaxException, VersioningDeploymentException {

        String untagged = VersioningDeploymentUtil.getUntaggedName(name);
        List<String> allVersions = getAllversions(untagged, target);

        if (allVersions.size() == 0) {
            // if versionned
            if(!name.equals(untagged)){
                throw new VersioningDeploymentException(
                        VersioningDeploymentUtil.LOCALSTRINGS.getLocalString("versioning.deployment.application.noversion",
                        "Application {0} has no version registered",
                        untagged));  
            }
            return Collections.EMPTY_LIST;
        }

        return VersioningDeploymentUtil.matchExpression(allVersions, name);
    }

    /**
     *  Disable the enabled version of the application if it exists. This method
     *  is used in versioning context.
     *
     *  @param appName application's name
     *  @param target an option supply from admin command, it's retained for
     * compatibility with other releases
     *  @param report ActionReport, report object to send back to client.
     */
    public void handleDisable(final String appName, final String target,
            final ActionReport report) throws VersioningDeploymentSyntaxException {

        // retrieve the currently enabled version of the application
        String enabledVersion = getEnabledVersion(appName, target);

        // invoke disable if the currently enabled version is not itself
        if (enabledVersion != null
                && !enabledVersion.equals(appName)) {
            final ParameterMap parameters = new ParameterMap();
            parameters.add("DEFAULT", enabledVersion);
            parameters.add("target", target);

            ActionReport subReport = report.addSubActionsReport();

            CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("disable", subReport);
            inv.parameters(parameters).execute();
        }
    }

    /**
    * @param directory
    * @return the name of the version currently using the directory, else null
    * @throws VersioningDeploymentSyntaxException
    */
    public String getVersionFromSameDir(File dir)
            throws VersioningDeploymentSyntaxException{

        try {
            Iterator it = domain.getApplications().getApplications().iterator();
            Application app = null;

            // check if directory deployment exist
            while ( it.hasNext() ) {
                app = (Application) it.next();
                if (app.getLocation().equals(dir.toURI().toString())) {
                    if(!VersioningDeploymentUtil.getUntaggedName(app.getName()).equals(app.getName())){
                        return app.getName();
                    }
                }
            }
        } catch (VersioningDeploymentSyntaxException ex) {
            // return null if an exception is thrown
        }
        return null;
    }
}
