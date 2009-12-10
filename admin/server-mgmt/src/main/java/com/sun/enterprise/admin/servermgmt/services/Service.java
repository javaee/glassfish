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

package com.sun.enterprise.admin.servermgmt.services;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Properties;

/** Represents an abstract Service. This interface defines sufficient methods
 *  for any platform integration of application server with various service
 *  control mechanisms on various platforms. An example is SMF for Solaris.
 * @since SJSAS 9.1
 * @see #isConfigValid
 * @see ServiceHandler
 */
public interface Service {
    
    /** Returns the <code> name </code> of the Service.
     */
    public String getName();
    
    /** Sets the name of the service. Parameter may not be null, 
     * an IllegalArgumentException results otherwise.
     */
    public void setName(final String name);
    
    /** Returns the <code> type </code> of service as an enum AppserverServiceType, from the given String.
     * @throws IllegalArgumentException if the enum value in the internal data structure is
     * not valid.
     * @see AppserverServiceType
     */
    public AppserverServiceType getType();
    /** Sets the type of the service to the given value in the enum.
     * @see AppserverServiceType
     */
    public void setType(final AppserverServiceType type);
    
    /** Returns the date the service is created.
     * @return A String Representation of Date.
     * @see java.util.Date
     */
    public String getDate();
    
    /** Sets the date as the date when this service is created.
     * @param date String representation of the date
     * @throws IllegalArgumentException if the parameter is null
     */
    public void setDate(final String date);
    
    /** Returns the location where configuration of the service is stored
     * on the disk.
     */
    public String getLocation();
    /** Sets the location to the parent of given location.
     * The location is treated as absolute and hence caller
     * must ensure that it passes the absolute location.
     */
    public void setLocation(final String cfgLocation);
    /** Returns the so-called <b> Fully Qualified Service Name </b> for this service.
     * It is a function of name and location where the configuration of the
     * service is stored. Might not return the intended value if the name and/or
     * location is not set prior to this call.
     * @return String representing the place where the manifest is stored
     */
    public String getFQSN();
    /** Sets the so-called <b> Fully Qualified Service Name </b> for this service.
     * Note that there is no parameter accepted by this method. This is because the
     * <b> Fully Qualified Service Name </b> is a function of name and location. The
     * callers are expected to call this method once name and location is set on
     * this service.
     */
    public void setFQSN();
    
    /** Returns the absolute path to the asadmin script.
     */
    public String getAsadminPath();
    /** Sets the absolute path to the asadmin script. May not be null.
     */
    public void setAsadminPath(final String path);
    /** Returns the absolute path of the password file that contains asadmin
     * authentication artifacts.
     */
    public String getPasswordFilePath();
    /** Sets the absolute path of the password file that contains asadmin
     * authentication artifacts. Parameter may not be null.
     */
    public void setPasswordFilePath(final String path);
    /** Returns timeout in seconds before the master boot restarter should
     * give up starting this service.
     */
    public int getTimeoutSeconds();
    /** Sets timeout in seconds before the master boot restarter should
     * give up starting this service.
     * @param number a non-negative integer representing timeout. A value of zero implies infinite timeout.
     */
    public void setTimeoutSeconds(final int number);
    /** Returns the OS-level user-id who should start and own the processes started
     * by this service.
     */
    public String getOSUser();
    /** Sets the OS-level user-id who should start and own the processes started
     * by this service. This user is the same as the value returned by
     * System.getProperty("user.name"). The idea is that the method is
     * called by the user who actually wants to own the service. 
     * @throws IllegalArgumentException if the user can not modify MANIFEST_HOME
     * @throws IllegalArgumentException if solaris.smf.modify Authorization is not implied by the authorizations available for the user.
     */
    public void setOSUser();
    
    /** Returns the additional properties of the Service.
     * @return String representing addtional properties of the service. May return default properties as well.
     */
    public String getServiceProperties();
    
    /** Sets the additional service properties that are specific to it.
     * @param must be a colon separated String, if not null. No effect, if null is passed.
     */
    public void setServiceProperties(final String cds);
    
    /** Determines if the configuration of the method is valid. When this class
     * is constructed, appropriate defaults are used. But before attempting to create
     * the service in the Solaris platform, it is important that the necessary
     * configuration is done by the users via various mutator methods of this class.
     * This method must be called to guard against some abnormal failures before
     * creating the service. It makes sure that the caller has set all the necessary
     * parameters reasonably. Note that it does not validate the actual values.
     * @throws RuntimeException if the configuration is not valid
     * @return true if the configuration is valid, an exception is thrown otherwise
     */
    public boolean isConfigValid();
    
    /** Returns the tokens and values of the service as a map.
     *  This method converts a service into corresponding tokens and their values.
     * @return tokens and values as a Map<String, String>.
     */
    public Map<String, String> tokensAndValues();
    /** Returns the absolute location of the manifest file as service understands it.
     * It takes into account the name, type and configuration location of the 
     * service. It is expected that these are set before calling this method.
     * If the <b> Fully Qualified Service Name </b> is invalid, a RuntimeException results.
     */
    public String getManifestFilePath();
    /** Returns the absolute location of the template for the given service.
     * The type of the service must be set before calling this method, otherwise
     * a runtime exception results.
     */
    public String getManifestFileTemplatePath();
    /** Creates an arbitrary service, specified by certain parameters. The implementations
     * should dictate the mappings in the parameters received. The creation of service is
     * either successful or not. In other words, the implementations must retain the original
     * state of the operating platform if the service creation is not successful completely.
     * @param params a Map between Strings that represents the name value pairs required to create the service
     * @throws RuntimeException if there is any error is creation of service
     */
    public void createService(final Map<String, String> params) throws RuntimeException;
    
    /** Sets the trace flag. The ServiceHandler is expected to provide trace output if the flag is set.
     */
    public void setTrace(final boolean trace);    
    /** For safety -- this is similar to the subversion dry-run command.
     * It does everything except create the service.
     */
    public void setDryRun(final boolean dryRun);

    /**
     * @return the trace flag
     */
    public boolean isTrace();

    /**
     * @return the dry-run flag
     */
    public boolean isDryRun();

    public String getSuccessMessage();

}
