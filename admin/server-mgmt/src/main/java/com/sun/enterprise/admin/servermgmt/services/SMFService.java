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

package com.sun.enterprise.admin.servermgmt.services;
import com.sun.enterprise.admin.util.LineTokenReplacer;
import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.admin.util.TokenValueSet;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.ProcessExecutor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.universal.io.SmartFile;

import java.io.*;
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

/** Represents the SMF Service.
 * Holds the tokens and their values that are consumed by the SMF templates. The recommended
 * way to use this class (or its instances) is to initialize it with default constructor
 * and then apply various mutators to configure the service. Finally, callers should
 * make sure that the configuration is valid, before attempting to create the service in
 * the Solaris platform.
 * @since SJSAS 9.0
 * @see #isConfigValid
 * @see SMFServiceHandler
 */
public final class SMFService implements Service {
    
    public static final String DATE_CREATED_TN              = "DATE_CREATED";
    public static final String AS_ADMIN_PATH_TN             = "AS_ADMIN_PATH";
    public static final String CREDENTIALS_TN               = "CREDENTIALS";
    public static final String SERVICE_NAME_TN              = "NAME";
    public static final String SERVICE_TYPE_TN              = "TYPE";
    public static final String CFG_LOCATION_TN              = "LOCATION";
    public static final String ENTITY_NAME_TN               = "ENTITY_NAME";
    public static final String FQSN_TN                      = "FQSN";
    public static final String AS_ADMIN_USER_TN             = "AS_ADMIN_USER";
    public static final String AS_ADMIN_PASSWORD_TN         = "AS_ADMIN_PASSWORD";
    public static final String AS_ADMIN_MASTERPASSWORD_TN   = "AS_ADMIN_MASTERPASSWORD";
    //public static final String PASSWORD_FILE_PATH_TN        = "PASSWORD_FILE_PATH";
    public static final String TIMEOUT_SECONDS_TN           = "TIMEOUT_SECONDS";
    public static final String OS_USER_TN                   = "OS_USER";
    public static final String PRIVILEGES_TN                = "PRIVILEGES";
    
    public static final String TIMEOUT_SECONDS_DV           = "0";
    public static final String AS_ADMIN_USER_DEF_VAL        = "admin";
    public static final String SP_DELIMITER                 = ":";
    public static final String PRIVILEGES_DEFAULT_VAL       = "basic";
    public static final String NETADDR_PRIV_VAL             = "net_privaddr";
    public static final String BASIC_NETADDR_PRIV_VAL       = PRIVILEGES_DEFAULT_VAL + "," + NETADDR_PRIV_VAL;
    public static final String START_INSTANCES_TN           = "START_INSTANCES";
    public static final String START_INSTANCES_DEFAULT_VAL  = Boolean.TRUE.toString();
    public static final String NO_START_INSTANCES_PROPERTY  = "startinstances=false";
    public static final String SVCCFG                       = "/usr/sbin/svccfg";
    public static final String SVCADM                       = "/usr/sbin/svcadm";
    public static final String MANIFEST_HOME                = "/var/svc/manifest/application/GlassFish/";

    private static final String NULL_VALUE                  = "null";
    private static final StringManager sm                   = StringManager.getManager(SMFService.class);
    private static final String nullArgMsg                  = sm.getString("null_arg");
    private static final String MANIFEST_FILE_SUFFIX        = "-service-smf.xml";
    private static final String MANIFEST_FILE_TEMPL_SUFFIX  = MANIFEST_FILE_SUFFIX + ".template";

    private static final String SERVICE_NAME_PREFIX         = "application/GlassFish/";
    private static final String REL_PATH_TEMPLATES          = "lib/install/templates";

    private final Map<String, String> pairs;
    private boolean trace = true;
    private boolean dryRun;
    private String  shortName;

    /**
     * Creates SMFService instance. All the tokens are initialized to default values. 
     * Callers must verify that the tokens are properly token-replaced before
     * using this instance.
     */
    SMFService() {
        if(!apropos()) {
            throw new IllegalArgumentException("Internal Error: SMFService constructor called but SMF is not available.");
        }
        pairs = new HashMap<String, String> ();
        init();
    }

 /**
     * Creates SMFService instance with tokens initialized from given map. Given
     * Map may not be null. Callers must verify that the tokens are properly token-replaced before
     * using this instance.
     * @param tv a Map of <String, String> that contains mappings between tokens and their values
     * @throws IllegalArgumentException in case the parameter is null
     */
    private SMFService(final Map<String, String> tv) {
        if (tv == null)
            throw new IllegalArgumentException(nullArgMsg);

        pairs = new HashMap<String, String> (tv);
    }



    static boolean apropos() {
        // suggested by smf-discuss forum on OpenSolaris
        return OS.isSun() && new File(SVCADM).isFile();
    }

    /** Returns the <code> name </code> of the SMF Service.
     */
    public String getName() {
        return ( pairs.get(SERVICE_NAME_TN) );
    }
    
    /** Sets the name of the service. Parameter may not be null, 
     * an IllegalArgumentException results otherwise.
     */
    public void setName(final String name) {
        if (name == null)
            throw new IllegalArgumentException(nullArgMsg);
        shortName = name;
        final String fullName = SERVICE_NAME_PREFIX + name;
        if (serviceNameExists(fullName)) {
            final String msg = sm.getString("serviceNameExists", fullName);
            throw new IllegalArgumentException(msg);
        }
        pairs.put(SERVICE_NAME_TN, fullName);
    }
    /** Returns the <code> type </code> of service as an enum AppserverServiceType, from the given String.
     * @throws IllegalArgumentException if the enum value in the internal data structure is
     * not valid.
     * @see AppserverServiceType
     */
    public AppserverServiceType getType() {
        return ( AppserverServiceType.valueOf(pairs.get(SERVICE_TYPE_TN)) );
    }
    /** Sets the type of the service to the given value in the enum.
     * @see AppserverServiceType
     */
    public void setType(final AppserverServiceType type) {
        pairs.put(SERVICE_TYPE_TN, type.toString());
    }
    
    /** Returns the date the service is created.
     * @return A String Representation of Date.
     * @see java.util.Date
     */
    public String getDate() {
        return ( pairs.get(DATE_CREATED_TN) );
    }
    /** Sets the date as the date when this service is created.
     * @param date String representation of the date
     * @throws IllegalArgumentException if the parameter is null
     */
    public void setDate(final String date) {
        if (date == null)
            throw new IllegalArgumentException(nullArgMsg);
        pairs.put(DATE_CREATED_TN, date);
    }
    
    /** Returns the location where configuration of the service is stored
     * on the disk.
     */
    public String getLocation() {
        return ( pairs.get(CFG_LOCATION_TN) );
    }
    /** Sets the location to the parent of given location.
     * The location is treated as absolute and hence caller
     * must ensure that it passes the absolute location.
     */
    public void setLocation(final String cfgLocation) {
        if (cfgLocation == null)
            throw new IllegalArgumentException(nullArgMsg);
        final File cf = FileUtils.safeGetCanonicalFile(new File(cfgLocation));
        pairs.put(CFG_LOCATION_TN, cf.getParent());
        pairs.put(ENTITY_NAME_TN, cf.getName());
    }
    /** Returns the so-called <b> Fully Qualified Service Name </b> for this service.
     * It is a function of name and location where the configuration of the
     * service is stored. Might not return the intended value if the name and/or
     * location is not set prior to this call.
     * @return String representing the place where the manifest is stored
     */
    public String getFQSN() {
        return ( pairs.get(FQSN_TN) );
    }
    /** Sets the so-called <b> Fully Qualified Service Name </b> for this service.
     * Note that there is no parameter accepted by this method. This is because the
     * <b> Fully Qualified Service Name </b> is a function of name and location. The
     * callers are expected to call this method once name and location is set on
     * this service.
     */
    public void setFQSN() {
        //note that this is function of name and location
        //note that it is a programming error to call this method b4 setName()
        assert !NULL_VALUE.equals(pairs.get(SERVICE_NAME_TN)):"Internal: Caller tried to call this method before setName()";
        final String underscored = pairs.get(ENTITY_NAME_TN) + pairs.get(CFG_LOCATION_TN).replace('/', '_');
        pairs.put(FQSN_TN, underscored);
    }
    
    /** Returns the absolute path to the asadmin script.
     */
    public String getAsadminPath() {
        return (pairs.get(AS_ADMIN_PATH_TN) );
    }
    /** Sets the absolute path to the asadmin script. May not be null.
     */
    public void setAsadminPath(final String path) {
        if (path == null)
            throw new IllegalArgumentException(nullArgMsg);
        if (! new File(path).exists()) {
            final String msg = sm.getString("doesNotExist", path);
            throw new IllegalArgumentException(msg);
        }
        pairs.put(AS_ADMIN_PATH_TN, path);
    }

    /** Returns the absolute path of the password file that contains asadmin
     * authentication artifacts.
     */
    public String getPasswordFilePath() {
        throw new UnsupportedOperationException("Not supported any longer.");
    }

    /** Sets the absolute path of the password file that contains asadmin
     * authentication artifacts. Parameter may not be null.
     */
    public void setPasswordFilePath(final String path) {
        if (path == null)
            throw new IllegalArgumentException(nullArgMsg);
        String msg = null;
        if (!new File(path).exists()) {
            msg = sm.getString("doesNotExist", path);
            throw new IllegalArgumentException(msg);
        }
        final String cp = FileUtils.safeGetCanonicalPath(new File(path));
        final Map<String, String> tv = new HashMap<String, String> ();
        if (!fileContainsToken(cp, AS_ADMIN_USER_TN, tv)) {
            msg = sm.getString("missingParamsInFile", cp, AS_ADMIN_USER_TN);
            throw new IllegalArgumentException(msg);
        }
        if (!fileContainsToken(cp, AS_ADMIN_PASSWORD_TN, tv)) {
            msg = sm.getString("missingParamsInFile", cp, AS_ADMIN_PASSWORD_TN);
            throw new IllegalArgumentException(msg);
        }
        if (!fileContainsToken(path, AS_ADMIN_MASTERPASSWORD_TN, tv)) {
            msg = sm.getString("missingParamsInFile", cp, AS_ADMIN_MASTERPASSWORD_TN);
            throw new IllegalArgumentException(msg);
        }
        //pairs.put(AS_ADMIN_USER_TN, tv.get(AS_ADMIN_USER_TN));
        //pairs.put(PASSWORD_FILE_PATH_TN, cp);
        pairs.put(CREDENTIALS_TN, " --user " + tv.get(AS_ADMIN_USER_TN) + " --passwordfile " + cp + " ");
    }
    /** Returns timeout in seconds before the master boot restarter should
     * give up starting this service.
     */
    public int getTimeoutSeconds() {
        final int to = Integer.parseInt(pairs.get(TIMEOUT_SECONDS_TN));
        return ( to );
    }
    /** Sets timeout in seconds before the master boot restarter should
     * give up starting this service.
     * @param number a non-negative integer representing timeout. A value of zero implies infinite timeout.
     */
    public void setTimeoutSeconds(final int number) {
        Integer to = Integer.valueOf(number);
        if (to < 0) {
            final String msg = sm.getString("invalidTO", number);
            throw new IllegalArgumentException(msg);
        }
        pairs.put(TIMEOUT_SECONDS_TN, to.toString() );
    }
    /** Returns the OS-level user-id who should start and own the processes started
     * by this service.
     */
    public String getOSUser() {
        return (pairs.get(OS_USER_TN) );
    }
    /** Sets the OS-level user-id who should start and own the processes started
     * by this service. This user is the same as the value returned by
     * System.getProperty("user.name"). The idea is that the method is
     * called by the user who actually wants to own the service. 
     * @throws IllegalArgumentException if the user can not modify MANIFEST_HOME
     * @throws IllegalArgumentException if solaris.smf.modify Authorization is not implied by the authorizations available for the user.
     */
    public void setOSUser() {
        final String user = System.getProperty("user.name");
        String msg;
        if (!canCreateManifest()) {
            msg = sm.getString("noPermissionToCreateManifest", user, MANIFEST_HOME);
            throw new IllegalArgumentException(msg);
        }
        final StringBuilder auths =  new StringBuilder();
        if (!isUserSmfAuthorized(user, auths)) {
            msg = sm.getString("noSmfAuth", user, auths);
            throw new IllegalArgumentException(msg);
        }
        pairs.put(OS_USER_TN, user);
    }
    
    /** Returns the additional properties of the Service.
     * @return String representing addtional properties of the service. May return default properties as well.
     */
    public String getServiceProperties() {
        return ( pairs.get(PRIVILEGES_TN) );
    }
    
    /** Sets the additional service properties that are specific to it.
     * @param must be a colon separated String, if not null. No effect, if null is passed.
     */
    public void setServiceProperties(final String cds) {
        /* For now, we have to take care of only net_privaddr privilege property.
         * Additional properties will result in additional tokens being replaced.
         * A null value for parameter results in setting the basic privilege property.
         */
        if (cds != null) {
            final Set<String> props = ps2Pairs(cds);
            if (props.contains(NETADDR_PRIV_VAL)) {
                pairs.put(PRIVILEGES_TN, BASIC_NETADDR_PRIV_VAL); // you get both basic, netaddr_priv
            }
            if (props.contains(NO_START_INSTANCES_PROPERTY)) {
               pairs.put(START_INSTANCES_TN, Boolean.FALSE.toString());
            }
        }
    }
    
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
    public boolean isConfigValid() {
        final Set<String> keys = pairs.keySet();
        for (final String k : keys) {
            final boolean aNullValue = NULL_VALUE.equals(pairs.get(k));
            if (aNullValue) {
                final String msg = sm.getString("smfTokenNeeded", k, pairs.get(k));
                throw new RuntimeException(msg);
            }
        }
        final File mf = new File(getManifestFileTemplatePath());
        if (!mf.exists()) {
            final String msg = sm.getString("serviceTemplateNotFound", getManifestFileTemplatePath());
            throw new RuntimeException(msg);
        }

        // bnevins May 27, 2009
        // passwordfile is now optional for start-domain
        // BEFORE:  --user %%%AS_ADMIN_USER%%% --passwordfile %%%PASSWORD_FILE_PATH%%%
        // AFTER:   %%%CREDENTIALS%%%
        

        return ( true );
    }
    
    /** Returns the tokens and values of the service as a map.
     * Note that a copy is returned.
     * @return a copy of tokens and values
     */
    public Map<String, String> tokensAndValues() {
        return ( new HashMap<String, String> (pairs) ); //send only copy
    }
    /** Returns the absolute location of the manifest file as SMF understands it.
     * It takes into account the name, type and configuration location of the 
     * service. It is expected that these are set before calling this method.
     * If the <b> Fully Qualified Service Name </b> is invalid, a RuntimeException results.
     */
    public String getManifestFilePath() {
        final String fqsn           = getFQSN();
        if (NULL_VALUE.equals(fqsn)) {
            final String msg = sm.getString("serviceNameInvalid", fqsn);
            throw new RuntimeException(msg);
        }
        //now we are sure that this is called after proper configuration
        final String fn = new StringBuilder().append(MANIFEST_HOME).append(fqsn).append("/").append(this.getType().toString()).append(MANIFEST_FILE_SUFFIX).toString();
        return ( fn ) ;
    }
    /** Returns the absolute location of the template for the given service.
     * If the file can not be found at its required location then the file will be
     * copied from inside this jar file to the file system.
     * The type of the service must be set before calling this method, otherwise
     * a runtime exception results.
     */
    public String getManifestFileTemplatePath() {
        if (NULL_VALUE.equals(getType().toString())) {
            final String msg = sm.getString("serviceTypeNotSet");
            throw new RuntimeException(msg);
        }

        String ir = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        if(!ok(ir))
            throw new RuntimeException("Internal Error - System Property not set: "
                    +  SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        File rootDir = SmartFile.sanitize(new File(ir));
        if(!rootDir.isDirectory())
            throw new RuntimeException("Internal Error - Not a directory: " + rootDir);

        File templatesDir = new File(rootDir, REL_PATH_TEMPLATES);
        String filename = getType().toString() + MANIFEST_FILE_TEMPL_SUFFIX;

        File f = new File(templatesDir, filename);
        return f.getPath();
    }

    private static boolean ok(String s) {
        return s!=null && s.length() > 0;
    }

    /** Creates the service on the given platform.
     */
    
    public void createService(final Map<String, String> params) throws RuntimeException {
        final SMFService smf = new SMFService(params);
        boolean success = false;
        boolean previousManifestExists = new File(smf.getManifestFilePath()).exists();
        try {
            smf.isConfigValid(); //safe, throws exception if not valid
            if (trace)
                printOut(smf.toString());
            validateManifest(smf.getManifestFilePath());
            previousManifestExists = false;
            ServicesUtils.tokenReplaceTemplateAtDestination(
                    smf.tokensAndValues(),
                    smf.getManifestFileTemplatePath(),
                    smf.getManifestFilePath());
            validateService(smf);
            success = importService(smf);
        } catch(final Exception e) {
            if (!success && !previousManifestExists) {
                cleanupManifest(smf);
            }
            throw new RuntimeException(e);
        }
    }
    /** Sets the trace information for debuggging purposes.
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }    
    /** Returns a String representation of the SMFService. It contains a new-line
        separated "name=value" String that contains the name and value of each of
        of the tokens that were set in the service.
        @return a String according to above description, never returns null
    */
    public String toString() {
        /* toString method useful for debugging */
        final StringBuilder sb = new StringBuilder();
        final String[] ka = new String[pairs.size()];
        Arrays.sort(pairs.keySet().toArray(ka));
        for (final String n : ka) {
            sb.append(n).append("=").append(pairs.get(n)).append(System.getProperty("line.separator"));
        }
        return ( sb.toString() );
    }
    /** For safety -- this is similar to the subversion dry-run command.
     * It does everything except create the service.
     */
    public void setDryRun(final boolean aDryRun) {
        dryRun = aDryRun;
}

    public String getSuccessMessage() {
        String msg = Strings.get("SMFServiceCreated", getName(), getType().toString(),
                getLocation(), getManifestFilePath(), shortName);

        if(dryRun) {
            msg += Strings.get("dryrun");
        }

        return msg;
    }

    ////////////////////// PRIVATE METHODS ////////////////////
    private void init() {
        pairs.put(DATE_CREATED_TN, new Date().toString());
        pairs.put(SERVICE_NAME_TN, NULL_VALUE);
        pairs.put(SERVICE_TYPE_TN, NULL_VALUE);
        pairs.put(CFG_LOCATION_TN, NULL_VALUE);
        pairs.put(ENTITY_NAME_TN, NULL_VALUE);
        pairs.put(FQSN_TN, NULL_VALUE);
        pairs.put(START_INSTANCES_TN, START_INSTANCES_DEFAULT_VAL);
        pairs.put(AS_ADMIN_PATH_TN, NULL_VALUE);
        pairs.put(AS_ADMIN_USER_TN, AS_ADMIN_USER_DEF_VAL);
        //pairs.put(PASSWORD_FILE_PATH_TN, NULL_VALUE);
        pairs.put(TIMEOUT_SECONDS_TN, TIMEOUT_SECONDS_DV);
        pairs.put(OS_USER_TN, NULL_VALUE);
        pairs.put(PRIVILEGES_TN, BASIC_NETADDR_PRIV_VAL);
        pairs.put(CREDENTIALS_TN, " ");
    }
    
    private Set<String> ps2Pairs(final String cds) {
        final StringTokenizer p = new StringTokenizer(cds, SP_DELIMITER);
        final Set<String> tokens = new HashSet<String>();
        while (p.hasMoreTokens()) {
            tokens.add(p.nextToken());
        }
        return ( tokens );
    }

    private boolean canCreateManifest() {
        final File mh = new File(MANIFEST_HOME);
        boolean ok = true;
        if (!mh.exists()) {
            ok = mh.mkdirs();
        }
        if (ok) {
            if (!mh.canWrite()) {
                ok = false;
            }
        }
        return ( ok );
    }
    private boolean isUserSmfAuthorized(final String user, final StringBuilder auths) {
          boolean authorized = false;
          String path2Auths = "auths";
          String at = ",";
          final String AUTH1 = "solaris.*";
          final String AUTH2 = "solaris.smf.*";
          final String AUTH3 = "solaris.smf.modify";
          if (System.getProperty("PATH_2_AUTHS") != null)
              path2Auths = System.getProperty("PATH_2_AUTHS");
          if (System.getProperty("AUTH_TOKEN") != null)
              at = System.getProperty("AUTH_TOKEN");
          try {
              final String[] cmd = new String[]{path2Auths, user};
              ProcessExecutor pe = new ProcessExecutor(cmd);
              pe.setExecutionRetentionFlag(true);
              pe.execute();
              auths.append(pe.getLastExecutionOutput());
              final StringTokenizer st = new StringTokenizer(pe.getLastExecutionOutput(), at);
              while (st.hasMoreTokens()) {
                  String t = st.nextToken();
                  if (t != null)
                      t = t.trim();
                  if (AUTH1.equals(t) || AUTH2.equals(t) || AUTH3.equals(t)) {
                      authorized = true;
                      break;
                  }
              }
              return ( authorized );
         } catch(Exception e) {
             throw new RuntimeException(e);
         }
    }
    private boolean fileContainsToken(final String path, final String t, final Map<String, String> tv) throws RuntimeException {
        BufferedInputStream bis = null;
        try {
            boolean present = false;
            bis = new BufferedInputStream(new FileInputStream(path));
            final Properties p = new Properties();
            p.load(bis);
            if (p.containsKey(t)) {
                tv.put(t, (String)p.get(t));
                present = true;
            }
            return ( present );
        }
        catch(final Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch(Exception ee) {
                    IGNORE_EXCEPTION(ee);
                }
            }
        }
    }
    
    
    private static void IGNORE_EXCEPTION(final Exception e ) {
        // ignore
    }
    
    private boolean serviceNameExists(final String sn) {
        boolean exists = false;
        try {
            final String[] cmd = new String[] {"/usr/bin/svcs", sn};
            ProcessExecutor pe = new ProcessExecutor(cmd);
            pe.setExecutionRetentionFlag(true);
            pe.execute();
            exists = true;
        } catch(final Exception e) {
            //returns a non-zero status -- the service does not exist, status is already set
        }
        return ( exists );
    }

    private void validateManifest(final String manifestPath) throws Exception {
        final File manifest = new File(manifestPath);
        final File manifestParent = manifest.getParentFile();
        
        if (manifestParent != null && manifestParent.isDirectory()) {
            final String msg = sm.getString("smfLeftoverFiles", manifest.getParentFile().getAbsolutePath());
            throw new IllegalArgumentException(msg);
        }
        manifest.getParentFile().mkdirs();
        if (trace)
            printOut("Manifest validated: " + manifestPath);
    }
    private void validateService(final SMFService smf) throws Exception {
        final String[] cmda = new String[]{SMFService.SVCCFG, "validate", smf.getManifestFilePath()};
        final ProcessExecutor pe = new ProcessExecutor(cmda);
        pe.execute();
        if (trace)
            printOut("Validated the SMF Service: " + smf.getFQSN() + " using: " + SMFService.SVCCFG);
    }
    private boolean importService(final SMFService smf) throws Exception {
        final String[] cmda = new String[]{SMFService.SVCCFG, "import", smf.getManifestFilePath()};
        final ProcessExecutor pe = new ProcessExecutor(cmda);
    
        if(dryRun)
            cleanupManifest(smf);
        else
            pe.execute(); //throws ExecException in case of an error

        if (trace)
            printOut("Imported the SMF Service: " + smf.getFQSN());
        return ( true );
    }
    private void cleanupManifest(final SMFService smf) throws RuntimeException {
        final File manifest = new File(smf.getManifestFilePath());
        if (manifest.exists()) {
            manifest.delete();
            manifest.deleteOnExit();
            if(trace)
                printOut("Attempted deleting failed service manifest: " + manifest.getAbsolutePath());
        }
        final File failedServiceNode = manifest.getParentFile();
        if (failedServiceNode.exists()) {
            failedServiceNode.delete();
            failedServiceNode.deleteOnExit();
            if(trace)
                printOut("Attempted deleting failed service folder: " + failedServiceNode.getAbsolutePath());
        }
    }
    private void printOut(final String s) {
        System.out.println(s);
    }

    public boolean isTrace() {
        return trace;
    }

    public boolean isDryRun() {
        return dryRun;
    }
}
