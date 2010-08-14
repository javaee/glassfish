/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General public final  License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public final /CDDL+GPL.html
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
package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.PropertiesDecoder;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.ServerDirs;
import java.io.*;
import java.util.*;
import java.util.Map;
import static com.sun.enterprise.admin.servermgmt.services.Constants.*;

/**
 * The original implementation of Services had serious design problems.  The Service interface
 * is ENORMOUSLY fat and non OO in the sense that outside callers had to set things
 * to make things work.  The interface is not generic -- it is very SMF specific
 * It is extremely difficult to implement the interface because it has SO MANY methods
 * that non-SMF don't need.
 * This "Adapter" makes it easier to implement the interface.  Eventually we should
 * have one adapter for all services but the SMF code is difficult and time-consuming
 * to change.
 * Meantime I'm adding new functionality (August 2010, bnevins) for instances.  I'm
 * moving implementations of the new interface methods to "ServiceAdapter" which ALL
 * services extend.
 * 
 * @author bnevins
 */
public abstract class NonSMFServiceAdapter extends ServiceAdapter {

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public final AppserverServiceType getType() {
        return type;
    }

    @Override
    public final void setType(AppserverServiceType type) {
        this.type = type;
    }

    @Override
    public final String getDate() {
        return date;
    }

    @Override
    public final void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return the domain directory
     */
    @Override
    public final String getLocation() {
        return location;
    }

    /**
     * @param location the domain directoory
     */
    @Override
    public final void setLocation(String location) {
        this.location = location;
    }

    @Override
    public final String getFQSN() {
        throw new UnsupportedOperationException("getFQSN not supported for this Platform Service");
    }

    @Override
    public final void setFQSN() {
        // NOOP
    }

    @Override
    public final String getAsadminPath() {
        return asadminPath;
    }

    @Override
    public final void setAsadminPath(String path) {
        asadminPath = path;
    }

    @Override
    public final String getPasswordFilePath() {
        return passwordFilePath;
    }

    /**
     * Do all the fun activities associated with getting the username and verifying
     * the contents of the file.
     * @param path
     */
    @Override
    public final void setPasswordFilePath(String path) {
        setAsadminCredentials(path);
    }

    @Override
    public final int getTimeoutSeconds() {
        throw new UnsupportedOperationException("getTimeoutSeconds() is not supported on this platform");
    }

    @Override
    public final void setTimeoutSeconds(int number) {
        throw new UnsupportedOperationException("setTimeoutSeconds() is not supported on this platform");
    }

    @Override
    public final String getOSUser() {
        return user;
    }

    @Override
    public final void setOSUser() {
        // it has been done already...
    }

    @Override
    public final String getServiceProperties() {
        return flattenedServicePropertes;
    }

    @Override
    public final void setServiceProperties(String cds) {
        flattenedServicePropertes = cds;
    }

    @Override
    public final Map<String, String> tokensAndValues() {
        return PropertiesDecoder.unflatten(flattenedServicePropertes);
    }

    @Override
    public final String getManifestFilePath() {
        UnsupportedOperationException ex = new UnsupportedOperationException("getManifestFilePath() is not supported in this platform.");

        ex.printStackTrace();
        throw ex;
    }

    @Override
    public final String getManifestFileTemplatePath() {
        throw new UnsupportedOperationException("getManifestFileTemplatePath() is not supported in this platform.");
    }

    @Override
    public final void setTrace(boolean trace) {
        this.trace = trace;
    }

    @Override
    public final boolean isTrace() {
        return trace;
    }

    @Override
    public final void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    public final boolean isDryRun() {
        return dryRun;
    }

    @Override
    public final boolean isForce() {
        return force;
    }

    @Override
    public final void setForce(boolean b) {
        force = b;
    }

    final String getAppserverUser() {
        return appserverUser;
    }

    @Override
    public final String getStartCommand() {
        return type.startCommand();
    }

    @Override
    public final String getStopCommand() {
        return type.stopCommand();
    }


    /**
     * If the user has specified a password file than get the info
     * and convert into a String[] that CLI can use.
     * e.g. { "--user", "harry", "--passwordfile", "/xyz" }
     * authentication artifacts. Parameter may not be null.
     */
    private void setAsadminCredentials(String path) {
        appserverUser = null;
        passwordFilePath = null;

        // it is allowed to have no passwordfile specified in V3
        if (!ok(path))
            return;

        // But if they DID specify it -- it must be kosher...
        File f = SmartFile.sanitize(new File(path));

        if (!f.isFile())
            throw new IllegalArgumentException(Strings.get("windows.services.passwordFileNotA", f));

        if (!f.canRead())
            throw new IllegalArgumentException(Strings.get("windows.services.passwordFileNotReadable", f));

        Properties p = getProperties(f);

        // IT 10255
        // the password file may just have master password or just user or just user password
        //

        appserverUser = p.getProperty("AS_ADMIN_USER");

        // we need a user for "--user" arg to start-domain

        if (!ok(appserverUser))
            appserverUser = null;

        passwordFilePath = f.getPath().replace('\\', '/'); // already sanitized
    }

    /**
     * This method is here to get rid of painful boilerplating...
     * @return
     */
    private String validateProperty(File f, Properties p, String key) {
        String value = (String) p.get(key);

        if (!ok(value))
            throw new IllegalArgumentException(Strings.get("missingParamsInFile", f, key));

        return value;
    }

    private Properties getProperties(File f) {
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            final Properties p = new Properties();
            p.load(bis);
            return p;
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (bis != null) {
                try {
                    bis.close();
                }
                catch (Exception ee) {
                    // ignore
                }
            }
        }
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
    private final String user = System.getProperty("user.name");
    private String date = new Date().toString();    // default date string
    private String location;
    private String name;
    private String asadminPath;
    private String passwordFilePath;
    private String flattenedServicePropertes;
    private String appserverUser;
    private boolean trace;
    private boolean dryRun;
    private boolean force;
    private AppserverServiceType type = AppserverServiceType.Domain;
}
